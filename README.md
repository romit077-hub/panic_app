
# 🚨 PANIC

## Deadline Enforcer

### **Your deadline doesn't care about your to-do list. PANIC does.**

**An offline-first Android productivity system that measures deadline
risk, detects impossible workloads, and turns your available time into a
realistic study plan.**

`Kotlin` · `Jetpack Compose` · `Material 3` · `Room` ·
`Coroutines / Flow` · `Offline-First`

![Version](https://img.shields.io/badge/version-v1.1.0-blue)
![Platform](https://img.shields.io/badge/platform-Android-brightgreen)
![Kotlin](https://img.shields.io/badge/Kotlin-100%25-purple)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-blue)
![Database](https://img.shields.io/badge/database-Room-orange)
![Status](https://img.shields.io/badge/status-Active-success)
![AI](https://img.shields.io/badge/fake%20AI-none-important)

**[Download APK](#-install-panic) · [How it works](#-how-panic-thinks) ·
[Architecture](#-architecture) · [Run locally](#-run-locally)**
:::

------------------------------------------------------------------------

## `23:47` --- The problem PANIC was built for

You have:

-   a DBMS assignment due tomorrow,
-   a CN quiz in two days,
-   ML notes you still haven't finished,
-   four hours of estimated work,
-   and two free hours tonight.

A normal task manager says:

> **4 tasks remaining.**

PANIC asks the more useful question:

> **Can those 4 tasks actually fit into the time you have left?**

If the answer is **no**, PANIC does not create a pretty impossible
timetable and pretend everything is fine.

It tells you.

That is the idea behind **PANIC --- Deadline Enforcer**.

------------------------------------------------------------------------

# ⚡ More than a to-do list

PANIC connects several systems into one deadline-management loop:

``` text
                    YOU ADD A TASK
                          │
                          ▼
                 ┌─────────────────┐
                 │  DEADLINE RISK  │
                 │     ENGINE      │
                 └────────┬────────┘
                          │
               How dangerous is it?
                          │
                          ▼
           SAFE ─ WARNING ─ HIGH ─ CRITICAL
                          │
                          ▼
                 ┌─────────────────┐
                 │  SMART PLANNER  │
                 └────────┬────────┘
                          │
            Where can the work fit?
                          │
              ┌───────────┴───────────┐
              ▼                       ▼
       SCHEDULED WORK           WORKLOAD AT RISK
              │                       │
              ▼                       ▼
       Study timeline          "This won't fit."
              │
              └───────────┬───────────┘
                          ▼
           Reminders • Calendar • Analytics
```

The goal is not to store more tasks.

The goal is to make the workload **actionable**.

------------------------------------------------------------------------

# 🧠 How PANIC thinks

PANIC deliberately separates two questions.

### 01 --- Risk Engine

**What deserves attention first?**

The deterministic risk system evaluates unfinished tasks using signals
such as:

-   deadline proximity,
-   remaining estimated workload,
-   priority,
-   overdue state.

Tasks are classified consistently as:

        State       Interpretation
  ----------------- -----------------------------------
     🟢 **SAFE**    Comfortable deadline headroom
   🟡 **WARNING**   Pressure is beginning to increase
     🟠 **HIGH**    Near-term attention is needed
   🔴 **CRITICAL**  Immediate attention is required

The same risk model is reused throughout PANIC instead of every screen
inventing its own meaning of urgency.

### 02 --- Smart Planner

**Where can the work realistically fit?**

The planner combines the task queue with:

``` text
Deadline
+ Risk
+ Priority
+ Estimated workload
+ Study availability
+ Session length
+ Break duration
+ Daily study limit
+ Planning horizon
──────────────────────────
= REALISTIC STUDY PLAN
```

No LLM is required.

No API call decides your timetable.

No random response changes the result.

The scheduler is **deterministic, offline and testable**.

------------------------------------------------------------------------

# ⏳ A planner that understands constraints

Suppose an assignment needs **120 minutes**.

Your preferred study session is **45 minutes** with a **10-minute
break**.

PANIC can produce:

``` text
18:00 ━━━━━━━━━ 18:45
       DBMS Assignment
       CRITICAL · 45 min

18:45 ───────── 18:55
       Break · 10 min

18:55 ━━━━━━━━━ 19:40
       DBMS Assignment
       CRITICAL · 45 min

19:40 ───────── 19:50
       Break · 10 min

19:50 ━━━━━ 20:20
       DBMS Assignment
       CRITICAL · 30 min
```

That last 30 minutes matters.

PANIC does not discard it simply because it does not fill a complete
45-minute block.

The scheduler is designed around edge cases including:

-   completed tasks,
-   overdue tasks,
-   small tasks,
-   split workloads,
-   multiple availability windows,
-   overlapping availability,
-   daily study limits,
-   breaks,
-   deadline boundaries,
-   sessions that would otherwise fall in the past,
-   insufficient available time.

------------------------------------------------------------------------

# ⚠️ The feature that matters most: admitting when the plan is impossible

Consider:

``` text
Work required before deadline     5h 00m
Actual available study time       2h 00m
                                  ───────
Work that cannot fit              3h 00m
```

A schedule generator can hide that problem.

PANIC surfaces it.

``` text
┌─────────────────────────────────────┐
│  ⚠ WORKLOAD AT RISK                │
│                                     │
│  Computer Networks Assignment       │
│  90 min could not be scheduled      │
│                                     │
│  Reason                             │
│  Not enough study time before       │
│  the deadline.                      │
└─────────────────────────────────────┘
```

Possible causes include:

`NO AVAILABILITY` · `INSUFFICIENT TIME` · `DAILY LIMIT` ·
`DEADLINE PASSED`

Internally these remain structured planner outcomes; the UI translates
them into human-readable explanations.

------------------------------------------------------------------------

# 📱 Inside PANIC

### 🏠 Dashboard

The command center.

See workload due today, highest task risk, your next action, and the
current Smart Plan without digging through menus.

### ✅ Tasks

The source of truth.

Create, edit, complete and delete tasks while keeping deadline,
workload, priority and risk visible. Filters surface **Today**,
**Upcoming**, **Critical** and completed work.

### 🧠 Smart Planner

The scheduling layer.

Generate a study plan from your real task database and configured
availability. Switch between **Today** and **Upcoming**, regenerate
after changes, and inspect work that could not fit.

### 🚨 PANIC Mode

The urgency layer.

High-risk work gets a focused view with risk visualization, explanations
and recommended actions. The goal is to answer:

> **What should I care about right now?**

### 📅 Calendar

The workload map.

Dates communicate task density and risk, while the selected day exposes
its estimated workload and deadlines.

### 📊 Analytics

The feedback loop.

Completion and productivity information is derived from existing task
data rather than decorative fake metrics.

### ⚙️ Settings

Appearance, reminders and planning configuration --- including
persistent Smart Planner preferences.

------------------------------------------------------------------------

# 📸 Product Tour

> **Screenshots from the final v1.1.0 build are coming here before
> portfolio publication.**

::: {align="center"}
  -------------------------------------------------------------------------------------------------------------
              Dashboard                          PANIC Mode                          Smart Planner
  ---------------------------------- ----------------------------------- --------------------------------------
   `docs/screenshots/dashboard.png`   `docs/screenshots/panic-mode.png`   `docs/screenshots/smart-planner.png`

        **Know what matters**             **Understand the danger**              **Know when to work**
  -------------------------------------------------------------------------------------------------------------

  ---------------------------------------------------------------------------------------------------
              Tasks                          Calendar                          Analytics
  ------------------------------ --------------------------------- ----------------------------------
   `docs/screenshots/tasks.png`   `docs/screenshots/calendar.png`   `docs/screenshots/analytics.png`

       **Manage the queue**            **See the workload**              **Review the pattern**
  ---------------------------------------------------------------------------------------------------
:::

------------------------------------------------------------------------

# 🏗 Architecture

PANIC keeps UI, state, domain logic and persistence separate.

``` text
┌────────────────────────────────────────────────────────┐
│                    JETPACK COMPOSE                     │
│                                                        │
│ Dashboard · Tasks · Plan · Calendar · PANIC · Analytics│
└─────────────────────────┬──────────────────────────────┘
                          │
                          ▼
┌────────────────────────────────────────────────────────┐
│                 VIEWMODELS / UI STATE                  │
│        reactive state • actions • presentation         │
└──────────────────────┬───────────────┬─────────────────┘
                       │               │
              ┌────────▼───────┐ ┌────▼──────────────┐
              │  DOMAIN LOGIC  │ │    REPOSITORY     │
              │                │ │                   │
              │ RiskCalculator │ │ Task data / Flow  │
              │ SmartPlanner   │ └────────┬──────────┘
              └────────┬───────┘          │
                       │                  ▼
                       │          ┌───────────────┐
                       │          │     ROOM      │
                       │          │ Local Storage │
                       │          └───────────────┘
                       │
                       ▼
              ┌─────────────────┐
              │   STUDY PLAN    │
              │ sessions        │
              │ summaries       │
              │ unscheduled work│
              └─────────────────┘
```

Generated plans are derived from current tasks, planner configuration
and time rather than requiring a second copy of task data.

Planner preferences remain local and survive application restarts.

------------------------------------------------------------------------

# 🧩 Tech stack

  Layer               Technology
  ------------------- ---------------------------------------
  **Language**        Kotlin
  **UI**              Jetpack Compose
  **Design system**   Material 3
  **Persistence**     Room
  **Reactive data**   Kotlin Coroutines + Flow
  **State**           ViewModel-based UI state
  **Risk analysis**   Custom deterministic `RiskCalculator`
  **Scheduling**      Custom deterministic `SmartPlanner`
  **Reminders**       Android notification APIs
  **Preferences**     Local settings persistence
  **Build**           Gradle
  **Testing**         JVM unit tests

### Intentionally absent

`Firebase` · `Accounts` · `Cloud dependency` · `Paid API` ·
`LLM dependency`

PANIC's core experience works locally.

------------------------------------------------------------------------

# 🔬 Smart Planner guarantees

For the same tasks, configuration and current timestamp, the planner is
designed to produce the same plan.

Core invariants include:

``` text
✓ Completed tasks never receive sessions
✓ Sessions never overlap
✓ Sessions remain inside valid availability
✓ Sessions are not scheduled in the past
✓ Daily study limits are respected
✓ Breaks consume clock time, not study workload
✓ Work is not silently dropped
✓ Deadline-passed work is handled explicitly
✓ Partial final sessions are preserved
✓ Generated plans do not mutate Room tasks
```

That makes the planner easier to test, debug and explain than an opaque
scheduling response.

------------------------------------------------------------------------

# 🧪 Tested where correctness matters

The project contains JVM tests for deterministic logic, including
scenarios around:

-   task completion exclusion,
-   critical-vs-safe ordering,
-   deadline tie-breaking,
-   workload splitting,
-   small tasks,
-   session collision prevention,
-   availability boundaries,
-   breaks,
-   daily limits,
-   past-time prevention,
-   insufficient availability,
-   overdue work,
-   zero estimated workload,
-   multiple and overlapping availability windows,
-   deadline enforcement,
-   deterministic generation,
-   workload consistency,
-   task filtering.

Run the test suite:

``` bash
./gradlew testDebugUnitTest
```

Windows:

``` powershell
.\gradlew.bat testDebugUnitTest
```

Build the debug APK:

``` powershell
.\gradlew.bat assembleDebug
```

------------------------------------------------------------------------

# 🚀 Install PANIC

## GitHub Release

PANIC v1.1.0 is intended to be distributed as a portfolio/demo Android
APK.

Once the release is published:

1.  Open **Releases**.
2.  Select **PANIC v1.1.0**.
3.  Download `PANIC-v1.1.0.apk`.
4.  Open it on your Android phone.
5.  Allow installation from that source if Android requests it.
6.  Grant notification permission for deadline reminders.

> Android may warn about applications installed outside Google Play.
> Install only the APK published from this repository.

------------------------------------------------------------------------

# 💻 Run locally

Clone:

``` bash
git clone https://github.com/romit077-hub/panic_app.git
cd panic_app
```

Open the project in Android Studio and allow Gradle sync to finish.

Then run the `app` configuration on an emulator or Android device.

### Local verification

``` powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

Machine-specific and sensitive files such as `local.properties`, signing
keys and secret configuration should remain outside version control.

------------------------------------------------------------------------

# 🔐 Offline by design

PANIC's core workflow does not need an account or internet connection.

``` text
Task creation      → local
Room database      → local
Risk calculation   → local
Smart scheduling   → local
Analytics          → local
Preferences        → local
```

This gives the project a simple privacy model and keeps scheduling
available even when the device is offline.

------------------------------------------------------------------------

# 🤖 Why isn't the Smart Planner called "AI"?

Because adding the word **AI** does not make scheduling better.

PANIC's core scheduling problem has hard constraints:

-   this deadline cannot move,
-   this study window ends at 20:00,
-   this task needs 120 minutes,
-   this student only wants 240 study minutes today.

Those constraints benefit from deterministic behavior.

An LLM could eventually be useful for optional natural-language
interactions or explanations, but the core scheduler does not need one
to do its job.

**Explainability beats an unnecessary AI sticker.**

------------------------------------------------------------------------

# 🎨 Product philosophy

PANIC follows three rules:

### 1. Urgency should be visible --- not annoying

Risk needs hierarchy, not a screen permanently flashing red.

### 2. A plan should be honest

If the workload does not fit, say so.

### 3. Motion should explain state

Animations communicate completion, risk, transitions and generated plans
--- not decorate every pixel.

------------------------------------------------------------------------

# 📍 Current status

## `PANIC v1.1.0`

``` text
[x] Task CRUD
[x] Offline Room persistence
[x] Deterministic deadline risk engine
[x] SAFE / WARNING / HIGH / CRITICAL classifications
[x] PANIC Mode
[x] Escalating deadline reminders
[x] Smart Planner engine
[x] Today + Upcoming plans
[x] Weekly study availability
[x] Configurable sessions and breaks
[x] Daily study limits
[x] Workload-at-risk detection
[x] Workload Calendar
[x] Productivity Analytics
[x] Light / Dark / System appearance
[x] UI interaction + animation polish
[x] JVM tests for core deterministic behavior
```

------------------------------------------------------------------------

# 🛣 What's next?

PANIC v1.1 intentionally focuses on a strong offline core rather than
feature count.

Potential future exploration:

-   adaptive rescheduling after missed sessions,
-   richer planner explanations,
-   optional focus-session workflow,
-   import/export,
-   deeper productivity trends,
-   accessibility refinement,
-   optional natural-language task entry,
-   broader device testing.

These are roadmap ideas, not claims about the current build.

------------------------------------------------------------------------

# 🎓 Built as an engineering project

PANIC began as a student Android project and evolved into an exploration
of a more interesting problem:

> **How do you turn a list of deadlines into a schedule that admits when
> there isn't enough time?**

The project combines Android application development with deterministic
risk analysis, constraint-aware scheduling, persistent state,
notifications, reactive UI and testable domain logic.

It is a portfolio project --- but the goal was to build something that
behaves like a product rather than a collection of disconnected demo
screens.

------------------------------------------------------------------------

::: {align="center"}
## 🚨 Don't just track the deadline.

# **Know when you're in PANIC.**

Built by **Romit Trivedi**

[GitHub](https://github.com/romit077-hub) ·
[LinkedIn](https://www.linkedin.com/in/romit-trivedi-56a57032a/)

**PANIC v1.1.0 · Android · Kotlin · Offline-First**
:::
