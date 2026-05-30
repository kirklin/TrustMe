# ===== TrustMe ProGuard Rules =====

# Keep the entire module package — all classes run in hooked app processes
# via Xposed cross-process class loading, which R8 cannot trace.
-keep class hk.kirk.trustme.** { *; }

# Keep all Xposed interface implementations
-keep class * implements de.robv.android.xposed.IXposedHookLoadPackage { *; }
-keep class * implements de.robv.android.xposed.IXposedHookZygoteInit { *; }

# Keep all Xposed hook callbacks
-keep class * extends de.robv.android.xposed.XC_MethodHook { *; }
-keep class * extends de.robv.android.xposed.XC_MethodReplacement { *; }

# Suppress Xposed API warnings
-dontwarn de.robv.android.xposed.**
