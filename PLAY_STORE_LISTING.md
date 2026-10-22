# 🃏 29 Card Game (Twenty-Nine) - Google Play Store Listing & Pre-Release Spec

---

## 📋 1. Google Play Store Listing Metadata

### App Title (Max 30 characters)
`29 Card Game`

### Short Description (Max 80 characters)
`Play classic 29 (Twenty-Nine) card game with smart AI bots, mentor coach & SFX!`

### Full Description (Formatted for Play Store)
```markdown
Experience the classic South Asian card game 29 (Twenty-Nine) right on your Android phone or tablet!

Enjoy high-stakes strategic trick-taking, clever trump reveals, and luxury card table visuals against intelligent bot personalities or with friends.

🌟 KEY GAME FEATURES:
• Authentic 29 Rules: 28-point trick deck (J=3, 9=2, A=1, 10=1, K=0, Q=0, 8=0, 7=0) + last trick point = 29 total points.
• Dynamic Bidding Engine: Bid from 16 to 28, or declare a Single Hand / Royal Slam.
• Royal Pair Bonus ("Royals"): Claim 40 points in trump suit or 20 points in side suits with King + Queen marriage declarations.
• Seventh Card Rule: Can't pick a trump suit? Choose the 7th card blind trump rule!
• Smart AI Opponents & Partner: Adaptive bots with distinct personalities (Arjun the Tactician, Meera the Master, Dev the Blitz Prodigy).
• Interactive AI Coach: Learn advanced strategies, evaluate your bids, and get real-time recommendations.
• Multiple Game Modes:
   - Classic Single Player Match
   - Fast Blitz Mode (instant bid & fast turns)
   - Campaign Adventure (conquer challenging table levels)
   - Pass & Play Local Multiplayer
• Rich Visuals & Themes: Multiple card back designs, custom avatars, and high-fidelity card animations.
• Detailed Statistics & Leaderboards: Track win rate, highest bids, trump contracts won, and quest progression.
• Offline Ready: Play anytime, anywhere without an active internet connection.

Monetization & Fair Play:
- Completely free to play.
- Safe, non-intrusive rewarded ads to unlock cosmetic cards, avatars, and bonus shields.
```

---

## 🔒 2. Privacy & Data Safety Declarations

| Category | Declared Usage | Details |
|---|---|---|
| **Personal Info** | Optional / Anonymous | Guest ID / Firebase Auth session tokens for cloud save (if signed in) |
| **App Activity** | Game Progress & Stats | Stored locally via Room Database & SharedPreferences |
| **Ads & Analytics** | Ad Delivery | Google Mobile Ads SDK (AdMob) standard ad-serving metrics |
| **Network** | Online Features | `INTERNET` permission for cloud sync, leaderboard, and AdMob ads |
| **Vibration** | Haptic Feedback | `VIBRATE` permission for card play tactile feedback |

---

## 🛠️ 3. Pre-Release Technical Specifications

- **Application ID:** `com.universeinvedors.twentynine`
- **Namespace:** `com.example`
- **Version Code:** `2`
- **Version Name:** `1.1`
- **Minimum SDK:** `API 24` (Android 7.0 Nougat)
- **Target SDK:** `API 36` (Android 16 / Latest Google Play compliant)
- **Compile SDK:** `API 36`
- **Orientation:** Landscape (`sensorLandscape`)
- **ProGuard / R8:** Enabled (`isMinifyEnabled = true`, `isShrinkResources = true`)
- **Release Bundle:** `release.aab` (Production signed, optimized APK split packaging)
- **Play Store Feature Graphic:** `feature_graphic_1024x500.png` (1024x500 px)
- **High-Res App Icon:** `app_icon.png` / `app_icon_1024.jpg` (1024x1024 px)
