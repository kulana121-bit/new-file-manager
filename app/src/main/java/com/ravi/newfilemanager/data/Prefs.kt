package com.ravi.newfilemanager.data

import android.content.Context
import androidx.core.content.edit

class Prefs(ctx: Context) {
    private val sp = ctx.getSharedPreferences("fm_prefs", Context.MODE_PRIVATE)

    var darkMode: Boolean
        get() = sp.getBoolean("dark_mode", false)
        set(v) = sp.edit { putBoolean("dark_mode", v) }

    var autoLockMinutes: Int
        get() = sp.getInt("auto_lock_min", 1)
        set(v) = sp.edit { putInt("auto_lock_min", v) }

    // vault
    var pinHash: String?
        get() = sp.getString("vault_pin_hash", null)
        set(v) = sp.edit { putString("vault_pin_hash", v) }
    var pinSalt: String?
        get() = sp.getString("vault_pin_salt", null)
        set(v) = sp.edit { putString("vault_pin_salt", v) }
    val hasPin: Boolean get() = pinHash != null

    // SAF persisted tree uris, keyed by folder label
    fun safUri(label: String): String? = sp.getString("saf_$label", null)
    fun setSafUri(label: String, uri: String) = sp.edit { putString("saf_$label", uri) }
}
