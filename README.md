# Al-Bacc Enforcer (منظومة المتفوق للثانوية العامة)

> **High-Discipline Automated Study Enforcement & Micro-Learning System**  
> Tailored for the **Egyptian Baccalaureate (2nd Year Secondary — Engineering & CS Track / علمي رياضة وحاسبات)**  
> Covering: **Arabic Grammar (النحو), English, History (تاريخ مصر والشرق الأوسط), Programming & AI**.

---

## 1. Visual Identity & Apple-Inspired Minimalism
The application strictly rejects generic AI design tropes (no navy blues, no neon gradients, no 3D renders, no visual clutter). Built upon a strict **Swiss-Grid / Apple-Style Alabaster Aesthetic**:
- **Primary Canvas / Background:** `#FAF8F5` (Warm Alabaster / Ivory)
- **Elevated Cards / Surfaces:** `#FFFFFF` (Pure Crisp White) with subtle `1dp` `#E5E5EA` hairline borders
- **Primary Typography:** `#1A1A1A` (Matte Titanium)
- **Secondary Typography:** `#6E6E73` (Apple Space Gray)
- **Primary Accent / Action:** `#10B981` (Crisp Mint Emerald)
- **Secondary Luxury Accent:** `#C5A059` (Champagne Gold)
- **Destructive / Penalty Alert:** `#EF4444` (Clean Crimson)
- **Typography & RTL:** Seamless bidirectional support (`RTL` for Arabic Nahw/History, `LTR` for English/Code) with concise label copy and generous line height.

---

## 2. Core Architecture & Tech Stack
- **Language & UI:** 100% Native **Kotlin**, **Jetpack Compose** (Material 3 customized to Swiss minimalism), Kotlin Coroutines & Flow.
- **Architecture:** Clean Architecture + MVVM + Hilt (Dagger) for Dependency Injection.
- **Local Database (0ms Latency Offline-First):** **Room Database (SQLite)** storing:
  - 50 pre-bundled high-yield MCQs in `seed_questions.json` auto-seeding Room on first launch.
  - User study statistics, streak records, rolling strike counter, and emergency snooze tokens.
  - Active locker state, countdown epochs, and penalty logs.
  - Monitored & blocked distracting applications.
- **Cloud Integration (Supabase):**
  - Remote PostgREST question bank synchronization via lightweight OkHttp client.
  - In-app Settings screen to configure `Supabase URL` and `Anon Key`.
  - Comprehensive `supabase_schema.sql` included in the root directory for remote database deployment.

---

## 3. System-Level Enforcement Engine

### A. System Alert Overlay (`SYSTEM_ALERT_WINDOW`)
- `StudyEnforcementService` runs as a persistent Foreground Service with a Live Notification showing current streak, countdown timer, and locker status.
- Periodically triggers a non-dismissible `WindowManager` Compose overlay (`TYPE_APPLICATION_OVERLAY`) presenting a curriculum MCQ.
  - **Correct Answer:** Displays a concise explanation card with a button to dismiss and reset the timer.
  - **Incorrect Answer:** Highlights the mistake, increments the **Strike Counter**, and serves an immediate follow-up question.
  - **Escape Hatch:** 2 daily "Snooze 5 mins" emergency tokens.

### B. 3-Strikes Penalty System & App Blocker (`AccessibilityService`)
- Dedicated `AppMonitorAccessibilityService` monitors `TYPE_WINDOW_STATE_CHANGED`.
- **3-Strikes Rule:** 3 incorrect answers trigger a **60-minute Penalty Mode**.
- Intercepts launches of blocked apps (Instagram, Facebook, TikTok, YouTube, X, Reddit, etc.) -> executes `performGlobalAction(GLOBAL_ACTION_HOME)` and launches a full-screen lockout screen.
- **Anti-Cheat Self-Protection:** While Penalty or Locker is active, any attempt to access Android Settings / Force Stop for this app is intercepted and redirected Home.
- **HyperOS / MIUI Onboarding Checklist:** Deep links for Overlay, Accessibility, Notification Access, Autostart, and Battery Optimization bypass.

### C. Distraction Notification Silencer (`NotificationListenerService`)
- Extends `NotificationListenerService`. During Penalty or Lockdown, incoming notifications from blocked apps are automatically canceled via `cancelNotification(sbn.key)`.

### D. Hardcore Deep Study Locker
- Choose 30m, 1h, 2h, or 4h durations.
- Whitelists only essential tools (Phone dialer, Calculator, and this Study App).
- Persists across device reboots via `BootReceiver` (`RECEIVE_BOOT_COMPLETED`).

---

## 4. Micro-Games & Gamification
1. **طابق الإعراب (Speed Grammar Match):** 60-second rapid-fire matching pairing words with their exact grammatical cases (e.g. الأسماء الخمسة، الأفعال الخمسة، إعراب المضارع).
2. **صائد الأخطاء (Error Hunter):** 10-second timer to detect the grammatically broken sentence segment with instant rule explanation.
3. **Concentric Activity Rings:** Apple-Fitness style rings tracking Daily Streak (Emerald), Focus Time (Gold), and Accuracy (Titanium).

---

## 5. Build & CI/CD Pipeline
The project includes a production-ready GitHub Actions workflow (`.github/workflows/android-build.yml`):
- Sets up JDK 17 & Android SDK.
- Caches Gradle dependencies.
- Compiles both **Debug** and **Release** APKs (`./gradlew assembleDebug assembleRelease`).
- Uploads installable APK artifacts directly to the workflow run.
