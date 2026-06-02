# Zora.AI

Zora.AI is a native Android AI companion app built with Kotlin and Jetpack Compose. It is designed around bring-your-own-key usage: the app stores chats, personas, settings, and API keys locally on the device.

Created by Phan Chi Vy.

## Current Version

`2.1.0`

The app includes two build flavors:

- `Zora.AI`
- `SanLoVerse (SLV)`

The visible app name can also be switched from inside the app settings.

## Features

- Custom AI personas with avatar, background, instruction prompt, and beginner/advanced persona modes.
- Solo or group sessions with 1-4 AI personas.
- Local chat history using Room database.
- Local global memory block with per-session read toggle.
- Per-session export and import.
- Message actions: copy, retry, and edit user messages.
- Markdown and LaTeX rendering support.
- Image attachments, up to 6 images per message.
- Fullscreen image preview for sent or received images.
- Local quota display for daily usage tracking.
- Web search tool through Tavily as an optional RAG layer.
- Gemini grounding support when using Google-compatible search flow.
- Optional Rule34 image tool for adult image search.
- Optional ElevenLabs text-to-speech for AI responses, with local audio caching after generation.
- Multiple API vendor options, including Google, OpenAI, Claude, Grok, and Mistral-compatible flows.
- App icon presets and app name presets.

## Version 2.1.0

- Added a local global memory block in app settings.
- Added a per-session toggle so each chat can choose whether it reads global memory.
- Memory is injected as model context only when enabled for that session.
- Added Room migration support for the session memory toggle.
- Added ElevenLabs TTS support with local audio caching.

## Privacy Model

The repository does not include private API keys or a private master prompt.

Local-only data:

- API keys entered by the user.
- Chat sessions and messages.
- Persona settings.
- Global memory text and each session's memory toggle.
- Cached TTS audio generated from AI messages.
- App preferences.
- Local quota counters.

Network requests only happen when the user sends a message or uses an enabled tool. Requests go to the selected model provider and, when enabled, Tavily, Rule34, or ElevenLabs.

## Master Prompt

The app loads the private master prompt from:

```text
app/src/main/assets/local_master_prompt.txt
```

That file is intentionally ignored by git. To build your own APK with a private master prompt, create the file locally before building.

If the file is missing, the app falls back to the default prompt in code.

## API Keys

API keys are entered inside the app settings and stored locally with Android DataStore.

Useful links:

- Google AI Studio: https://aistudio.google.com/app/apikey
- OpenAI API keys: https://platform.openai.com/api-keys
- Anthropic Console: https://console.anthropic.com/settings/keys
- xAI Console: https://console.x.ai/
- Mistral Console: https://console.mistral.ai/api-keys/
- Tavily Dashboard: https://app.tavily.com/
- ElevenLabs API keys: https://elevenlabs.io/app/settings/api-keys
- Rule34 account options: https://rule34.xxx/index.php?page=account&s=options

Rule34 requires both the numeric user ID and API key from the account options page.

ElevenLabs TTS requires an API key and voice ID. Some ElevenLabs voices may return a `402 payment_required` error unless the account has the required paid plan or subscription for that voice/API usage.

## Build

Requirements:

- Android Studio or Android SDK command-line tools.
- JDK 17.
- Android 8.0+ target device.
- `app/src/main/assets/local_master_prompt.txt` if you want a private compiled-in master prompt.

Build Zora.AI:

```powershell
.\gradlew.bat assembleZoraRelease
```

Build SanLoVerse (SLV):

```powershell
.\gradlew.bat assembleSlvRelease
```

Debug builds:

```powershell
.\gradlew.bat assembleZoraDebug
.\gradlew.bat assembleSlvDebug
```

## Notes

This is a bring-your-own-key app. Model behavior, rate limits, safety behavior, and tool reliability depend on the user-selected provider and API account.

The Rule34 tool is adult-only and should only be used where legal and appropriate.

## License

MIT. Keep the credit.
