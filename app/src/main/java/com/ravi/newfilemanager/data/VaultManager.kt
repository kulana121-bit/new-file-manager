package com.ravi.newfilemanager.data

import android.content.Context
import android.net.Uri
import java.io.File
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Properties
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Secured Storage vault: AES-256-GCM file encryption.
 * Key derived from the 4-digit PIN via PBKDF2-HMAC-SHA256 (120k iterations).
 * Index kept in a plain Properties file (no reflection-heavy JSON libs -> R8 safe).
 */
class VaultManager(private val ctx: Context) {

    private val prefs = Prefs(ctx)
    private val vaultDir: File get() = File(ctx.filesDir, "vault").also { it.mkdirs() }
    private val indexFile: File get() = File(vaultDir, "index.properties")

    data class Entry(
        val id: String,
        val name: String,
        val mime: String,
        val category: String, // photos | videos | audio | docs
        val size: Long,
        val addedAt: Long
    )

    // ---------- PIN ----------
    fun hasPin(): Boolean = prefs.hasPin

    fun setPin(pin: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        prefs.pinSalt = salt.joinToString("") { "%02x".format(it) }
        prefs.pinHash = sha256(prefs.pinSalt!! + pin)
    }

    fun verifyPin(pin: String): Boolean {
        val salt = prefs.pinSalt ?: return false
        return sha256(salt + pin) == prefs.pinHash
    }

    private fun sha256(s: String): String {
        val d = MessageDigest.getInstance("SHA-256").digest(s.toByteArray())
        return d.joinToString("") { "%02x".format(it) }
    }

    private fun keyFromPin(pin: String): SecretKeySpec {
        val saltHex = prefs.pinSalt ?: throw IllegalStateException("no pin")
        val salt = saltHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        val spec = PBEKeySpec(pin.toCharArray(), salt, 120_000, 256)
        val key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        spec.clearPassword()
        return SecretKeySpec(key, "AES")
    }

    // ---------- index ----------
    private fun loadIndex(): Properties =
        Properties().also { if (indexFile.exists()) indexFile.inputStream().use(it::load) }

    private fun saveIndex(p: Properties) =
        indexFile.outputStream().use { p.store(it, null) }

    fun list(category: String? = null): List<Entry> {
        val p = loadIndex()
        val ids = p.getProperty("ids", "").split(",").filter { it.isNotBlank() }
        return ids.mapNotNull { id ->
            val name = p.getProperty("$id.name") ?: return@mapNotNull null
            Entry(
                id = id,
                name = name,
                mime = p.getProperty("$id.mime", "application/octet-stream"),
                category = p.getProperty("$id.category", "docs"),
                size = p.getProperty("$id.size", "0").toLongOrNull() ?: 0L,
                addedAt = p.getProperty("$id.added", "0").toLongOrNull() ?: 0L
            )
        }.filter { category == null || it.category == category }
            .sortedByDescending { it.addedAt }
    }

    // ---------- crypto ----------
    @Throws(Exception::class)
    fun lockStream(input: java.io.InputStream, name: String, mime: String, category: String, pin: String): Entry {
        val key = keyFromPin(pin)
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        val id = System.currentTimeMillis().toString(36) + SecureRandom().nextInt(9999).toString()
        val outFile = File(vaultDir, "$id.enc")
        val plain = input.readBytes()
        outFile.outputStream().use {
            it.write(iv)
            it.write(cipher.doFinal(plain))
        }
        val entry = Entry(id, name, mime, category, plain.size.toLong(), System.currentTimeMillis())
        val p = loadIndex()
        val ids = (p.getProperty("ids", "").split(",").filter { it.isNotBlank() } + id).joinToString(",")
        p.setProperty("ids", ids)
        p.setProperty("$id.name", name)
        p.setProperty("$id.mime", mime)
        p.setProperty("$id.category", category)
        p.setProperty("$id.size", plain.size.toString())
        p.setProperty("$id.added", entry.addedAt.toString())
        saveIndex(p)
        return entry
    }

    @Throws(Exception::class)
    fun lockUri(uri: Uri, name: String, mime: String, category: String, pin: String): Entry {
        ctx.contentResolver.openInputStream(uri)?.use { ins ->
            return lockStream(ins, name, mime, category, pin)
        } ?: throw IllegalStateException("cannot open input")
    }

    /** Decrypt entry into a temp cache file and return it. */
    @Throws(Exception::class)
    fun unlockToTemp(entry: Entry, pin: String): File {
        val key = keyFromPin(pin)
        val enc = File(vaultDir, "${entry.id}.enc").readBytes()
        val iv = enc.copyOfRange(0, 12)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        val plain = cipher.doFinal(enc.copyOfRange(12, enc.size))
        val tmp = File(ctx.cacheDir, "vault_${entry.id}_${entry.name}")
        tmp.writeBytes(plain)
        return tmp
    }

    fun delete(entry: Entry) {
        File(vaultDir, "${entry.id}.enc").delete()
        val p = loadIndex()
        val ids = p.getProperty("ids", "").split(",").filter { it.isNotBlank() && it != entry.id }
        p.setProperty("ids", ids.joinToString(","))
        listOf("name", "mime", "category", "size", "added").forEach { p.remove("${entry.id}.$it") }
        saveIndex(p)
    }

    /** Session that derives the key ONCE and reuses it (PBKDF2 is expensive). */
    inner class Session(pin: String) {
        private val key: SecretKeySpec = run {
            val saltHex = prefs.pinSalt ?: throw IllegalStateException("no pin")
            val salt = saltHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
            val spec = PBEKeySpec(pin.toCharArray(), salt, 120_000, 256)
            val k = SecretKeySpec(
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded,
                "AES"
            )
            spec.clearPassword()
            k
        }

        @Throws(Exception::class)
        fun decrypt(entry: Entry): ByteArray {
            val enc = File(vaultDir, "${entry.id}.enc").readBytes()
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, enc.copyOfRange(0, 12)))
            return cipher.doFinal(enc.copyOfRange(12, enc.size))
        }

        @Throws(Exception::class)
        fun decryptToTemp(entry: Entry): File {
            val tmp = File(ctx.cacheDir, "vault_${entry.id}_${entry.name}")
            tmp.writeBytes(decrypt(entry))
            return tmp
        }
    }

    fun session(pin: String): Session = Session(pin)
}
