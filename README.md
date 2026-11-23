# Morsik

Aplikacja na Androida, która nadaje wpisany tekst alfabetem Morse'a: latarką, wibracjami i dźwiękiem.

Projekt zespołu **DN** na przedmiot *Programowanie Interfejsów Mobilnych* (Politechnika Wrocławska, semestr zimowy 2026/27).

## Technologie

- Kotlin + Jetpack Compose (Material 3)
- Android Studio `2026.2.1 Rabbit 1`
- Android 8.0 (API 26) lub nowszy
- Latarka: `CameraManager`, wibracje: `Vibrator`, dźwięk: `ToneGenerator`, mowa na tekst: `SpeechRecognizer`

## Struktura

- `composeApp` - współdzielony kod: ekrany Compose i logika alfabetu Morse'a
- `androidApp` - aplikacja Android: `MainActivity` i implementacje funkcji sprzętowych

## Uruchomienie

```
./gradlew :androidApp:installDebug
```

## Licencja

MIT, zobacz [LICENSE](LICENSE).
