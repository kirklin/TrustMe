# TrustMe

A comprehensive SSL/TLS certificate pinning bypass module for **LSPosed** / **Zygisk** on Android.

TrustMe provides a unified approach to SSL/TLS certificate pinning bypass with a modern Jetpack Compose settings UI and fine-grained per-hook toggle controls.

## Features

- **37+ hook targets** covering virtually all SSL pinning implementations on Android
- **Dual-strategy Conscrypt hooks** — hardcoded class matching + dynamic reflection
- **Settings UI** — global master switch + 12 independent per-hook toggles (Jetpack Compose)
- **Multi-dex support** — third-party library hooks via deferred `Application.attach()` loading
- **Optional hook logging** — per-app log files for debugging
- **Fault-isolated** — each hook runs independently; one failure won't break others

## Hook Coverage

| Target | Hook Module | Techniques |
|--------|-------------|------------|
| `SSLContext.init()` | SSLContext | Replace TrustManager array with trust-all implementation |
| `TrustManagerFactory` | TrustManager | Intercept `getTrustManagers()` return value |
| `X509TrustManagerExtensions` | TrustManager | Bypass `checkServerTrusted()` chain validation |
| Conscrypt `TrustManagerImpl` | Conscrypt | Hardcoded + dynamic reflection (Zygote-level) |
| Conscrypt Socket layer | ConscryptSocket | Disable `setHostname()` / peer verification |
| Conscrypt `PinManager` | PinManager | Force `isChainValid()` to return true |
| `HttpsURLConnection` | URLConnection | Replace `SSLSocketFactory` and `HostnameVerifier` |
| OkHttp 2.x / 3.x / 4.x+ | OkHttp | Bypass `CertificatePinner`, clear pins |
| `WebViewClient` SSL errors | WebView | Auto-proceed on `onReceivedSslError` |
| Apache HTTP Client | Apache | Replace `SSLSocketFactory` and scheme registry |
| Network Security Config | NSC | Override `isCleartextTrafficPermitted`, trust anchors |
| Cronet | Cronet | Disable public key pinning via `enablePublicKeyPinningBypassForLocalTrustAnchors` |
| xutils / httpclientandroidlib | ThirdParty | Custom `SSLSocketFactory` and `HostnameVerifier` |

## Architecture

```
MainHook (Entry Point)
├── initZygote()          → Zygote-level Conscrypt TrustManagerImpl hook
└── handleLoadPackage()   → Per-app hook dispatch
    ├── System hooks (direct ClassLoader)
    │   ├── SSLContextHook
    │   ├── TrustManagerHook
    │   ├── TrustManagerImplHook (app-level)
    │   ├── HttpsURLConnectionHook
    │   ├── WebViewHook
    │   ├── NetworkSecurityConfigHook
    │   ├── ConscryptSocketHook
    │   └── PinManagerHook
    └── Application.attach() deferred hooks
        ├── OkHttpHook
        ├── ApacheHttpHook
        ├── CronetHook
        └── ThirdPartyHook
```

## Requirements

- Android 8.0+ (API 26+)
- [LSPosed](https://github.com/LSPosed/LSPosed) or compatible Xposed framework (e.g. Vector)

## Usage

1. Install the TrustMe APK on a device with **LSPosed** enabled
2. Activate the module in LSPosed Manager
3. Select the target app(s) you want to bypass SSL pinning for
4. Force-stop and relaunch the target app
5. (Optional) Open TrustMe app to toggle individual hooks or enable logging

## Build

```bash
./gradlew assembleDebug
```

Output APK: `app/build/outputs/apk/debug/app-debug.apk`

For release builds:

```bash
./gradlew assembleRelease
```

## Disclaimer

This tool is provided strictly for **security research**, **authorized penetration testing**, and **legitimate debugging purposes** only. Users are solely responsible for ensuring their use of this tool complies with all applicable laws, regulations, and the terms of service of any target application. The author assumes no liability for any misuse, damage, or legal consequences arising from the use of this software. Unauthorized interception of network traffic may violate local, state, or federal laws.

**By using this tool, you acknowledge that you have obtained proper authorization to test the target application.**

## Author

Kirk Lin — [GitHub](https://github.com/kirklin)
