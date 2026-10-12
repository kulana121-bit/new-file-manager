# R8 rules for New File Manager v1.0
# Proven recipe from the old Files app (release-81-1, 2026-10-08):
# R8 shrinking ON, but keep every class; no obfuscation, no aggressive
# optimization. Zero reflection-heavy third-party libs in this app
# (no Gson/Moshi/Retrofit/Drive), so this is safe and reliable.
-dontoptimize
-dontobfuscate

# Keep every program class (proven to work; size optimization deferred)
-keep class ** { *; }

-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# --- DontWarns (prevent build failures from absent optional deps) ---
-dontwarn androidx.compose.**
-dontwarn kotlinx.coroutines.**
-dontwarn androidx.**
-dontwarn kotlin.**
-dontwarn java.lang.invoke.**
-dontwarn org.w3c.dom.**
-dontwarn org.xml.sax.**
