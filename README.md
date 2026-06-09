# Zora.AI ◈ (v2.1.0)
Zora.AI is a native Android AI companion built by Phan Chi Vy. 

## The TL;DR
**FUCK corporate censorship. FUCK paywalls.**

I got sick of commercial AI apps locking basic conversations behind subscriptions or lecturing me about "community guidelines" every time a chat got interesting. Zora.AI is the antidote. It is a sandbox of pure creative freedom where adult content, dark roleplay, and extreme fictional scenarios are fully allowed.

I’m a responsible developer, though—don't use this platform to build explosives or recreate 9/11 (っ °Д °;)っ. Otherwise? Write whatever the fuck you want.

Best of all: it's a **bring-your-own-key** model. You run it on your own API keys. No middleware, no training on your private chats, and no paying me a single cent.

### The Three Rules of Zora:
* 🚫 **NO Corporate Filters** — The model does what you tell it to do. Period.
* 💸 **NO Paywalls** — 100% free and open-source.
* 🔒 **NO Data Harvesting** — Your API keys, chat history, and visual frames stay locally on your device.

*Note: While Zora is a highly capable assistant for coding and daily tasks, it is custom-tuned to be the ultimate sandbox for interactive roleplay.*

---

## Features
*(Everything is free to use. The only cost is what you pay directly to your API/TTS providers).*

* 🎭 **Custom AI Personas**: Customize name, avatar, system instructions, tone, and character lore.
* 👥 **Multi-Agent Chats**: Host 1 to 4 AI agents in a single room. Let them debate, or `@tag` them directly.
* 📂 **Project Templates**: Share instructions and memories across multiple sessions seamlessly.
* 🧠 **Global Memory**: Persistent, long-term memory that can be toggled on or off per chat.
* 📈 **Friendship XP (Level 1–10)**: Session-based progression. The AI gets warmer, more compliant, and unlocks NSFW boundaries as you chat (NSFW unlocks at Level 5).
* 💾 **Local-First Database**: Your chats survive app updates, restarts, and closures.
* 🛠️ **Session Manager**: Clone, rename, search, move, and delete chat rooms instantly.
* 🔌 **Multi-Provider API**: Native support for **Gemini (best support for Live features)**, Claude, GPT, Grok, and Mistral.
* 🔍 **Web Tools**: Live web search powered by Tavily.
* 🔞 **Anime Image Search**: Search and pull images directly into the chat via Rule34.
* 📸 **Visual Input**: Send up to 12 images (photos, live camera, or files) directly to the vision model.
* 🎙️ **Gemini Live Calling**: Real-time voice calls with camera and screenshare support.
* 📝 **Live Transcript**: Voice calls automatically compile into text bubbles and save to the chat history.
* 🗣️ **ElevenLabs TTS**: High-quality message narration with built-in local audio caching.
* 🎨 **Deep Customization**: Customize app themes, avatars, backgrounds, app name, and launcher icon.
* ⚙️ **Developer Vault**: Tweak API safety settings, temperature, and thinking effort values.
* ⏱️ **Quota Monitor**: Built-in local token counter and reset timer.
* 👆 **Message Context Menu**: Long-press any bubble to copy, edit, retry, or text-to-speech.

---

## Privacy Model
This repository does not include private API keys or the default master prompt.

### 100% Local Data:
* API keys entered in settings.
* Chat histories, messages, and vector database memories.
* Persona configs and user preferences.
* Cached TTS audio.
* Local quota counters.

Network requests only happen when sending a message or running an active tool. Data goes directly to the selected API provider (Google, Anthropic, OpenAI, Mistral, Tavily, Rule34, or ElevenLabs). No middleman servers involved.

---

## Master Prompt
The application looks for the private master prompt at:
```text
app/src/main/assets/local_master_prompt.txt
```
This file is ignored by `.gitignore`. To build the APK with your own custom rules:
1. Create the `local_master_prompt.txt` file locally in the assets directory.
2. Build the project.
If the file is missing, the build falls back to a basic default prompt in the code.

---

## API Providers & Keys
*Note: Zora performs best on **Gemini** (required for Live voice calls), followed by **Claude** and **Mistral**.*

* **Google AI Studio**: https://aistudio.google.com/app/apikey
* **Anthropic Console**: https://console.anthropic.com/settings/keys
* **OpenAI Developer**: https://platform.openai.com/api-keys
* **Mistral API Console**: https://console.mistral.ai/api-keys/
* **xAI (Grok) Console**: https://console.x.ai/
* **Tavily Search**: https://app.tavily.com/
* **ElevenLabs Dashboard**: https://elevenlabs.io/app/settings/api-keys
* **Rule34 (API Key & ID)**: https://rule34.xxx/index.php?page=account&s=options
  * *Note: Rule34 requires both the numeric User ID and the API Key from your account options page.*
  * *ElevenLabs TTS requires a Voice ID. Some custom voices may return a `402 Payment Required` code if your key doesn't have access to them.*

---

## Building from Source

### Requirements:
* Android Studio (Koala or newer) / Android SDK CLI tools.
* JDK 17.
* Target Device running Android 8.0 (API 26) or higher.
* `app/src/main/assets/local_master_prompt.txt` (optional, for custom system guidelines).

### Run Build:
For Release APK:
```powershell
.\gradlew.bat assembleZoraRelease
```

For Debug APK:
```powershell
.\gradlew.bat assembleZoraDebug
```

---

## Disclaimer & Licensing
* **Disclaimer**: This is a bring-your-own-key application. Model responses, safety refusals, rate limits, and tool execution depend entirely on the selected provider and API plan. The Rule34 tool is adult-only and should only be enabled where legal.
* **License**: MIT. Keep the credit, do whatever the fuck you want with the code.
