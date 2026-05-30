# ===== TrustMe ProGuard Rules =====

# 保留 Xposed 入口类
-keep class hk.kirk.trustme.MainHook { *; }

# 保留 SettingsActivity.isModuleActive — MainHook 通过 Xposed 反射按名称访问
-keep class hk.kirk.trustme.ui.SettingsActivity {
    static boolean isModuleActive;
}

# 保留所有 Xposed 接口实现
-keep class * implements de.robv.android.xposed.IXposedHookLoadPackage { *; }
-keep class * implements de.robv.android.xposed.IXposedHookZygoteInit { *; }

# 保留所有 Xposed Hook 回调
-keep class * extends de.robv.android.xposed.XC_MethodHook { *; }
-keep class * extends de.robv.android.xposed.XC_MethodReplacement { *; }

# 保留 Trust 辅助类（运行时动态实例化）
-keep class hk.kirk.trustme.trust.** { *; }

# Xposed API 警告抑制
-dontwarn de.robv.android.xposed.**

