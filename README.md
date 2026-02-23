# Punjabi Urdu English Dictionary (Android)

A simple Android app built with Kotlin + Jetpack Compose that:

- translates Punjabi words/phrases to Urdu and English using a local dictionary
- supports speech-to-text input for Punjabi (`pa-PK`)

## Features

- Punjabi input text field
- **Translate** button for local dictionary lookup
- **Speech to Text** button using Android `RecognizerIntent`
- Displays translated Urdu and English output

## Notes

- Current translations are powered by a small offline in-app dictionary for demo purposes.
- You can extend the `dictionary` list in `MainActivity.kt` to add more words.
