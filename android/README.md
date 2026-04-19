# CoParse Android

## Requirements

- Android Studio (recommended) or Android SDK + JDK 17
- Create `local.properties` in this folder with `sdk.dir` pointing at your Android SDK (see `local.properties.example`).

```properties
sdk.dir=C:\\Users\\YOU\\AppData\\Local\\Android\\Sdk
```

Optional API override (defaults to the Android emulator host for `localhost`):

```properties
coparse.apiBaseUrl=http://10.0.2.2:8000/
```

For a physical device on the same Wi‑Fi as your PC, use your computer’s LAN IP, e.g. `http://192.168.1.10:8000/`.

## Run

Open the `android/` directory in Android Studio, sync Gradle, run the `app` configuration.

## Stack

- Kotlin, Jetpack Compose, Material 3
- Navigation Compose, Retrofit + OkHttp + kotlinx.serialization
- Room (saved summaries), DataStore (disclaimer acceptance)
