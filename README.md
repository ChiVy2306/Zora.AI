Zora.AI
A fully customizable native Android AI companion app. Bring your own API key, define your own persona, own your experience - no subscription, no ads, no data collection.

How It Works
Clone the repo, drop your master prompt into local_master_prompt.txt, put your API key in the app, and start chatting. That's it.
git clone https://github.com/ChiVy2306/Zora.AI.git

Setup

Clone the repository
Create a local_master_prompt.txt file in the project root with your desired base system prompt
Build and install the APK via Android Studio
Launch the app, enter your Gemini API key on the user side
Start chatting


Get a free Gemini API key at aistudio.google.com


Features

Multi-vendor API support - works with Gemini, OpenAI, Grok, Claude, and Mixtral. Swap models per session, your key your choice
Image support - send up to 6 images per message, vision model handles the rest
Fully customizable sessions - each session has its own avatar, background, and instruction prompt
Multi-session - run multiple AI personas simultaneously, switch between them freely
Custom instruction prompt - layer your own prompt on top of each session for any use case
2 app icons - because why not
Animations - a bit fancy
Privacy first - API key stored locally, chat history stored locally, everything is local except the API call itself. Zero data sent to anyone except Gemini's (or wahtever API vendor server you using) servers during inference.


Requirements

Android 8.0+
Gemini API key (free tier works fine) or ANY api key that the app support
A local_master_prompt.txt before building


License
MIT - do whatever you want, just keep the credit.
