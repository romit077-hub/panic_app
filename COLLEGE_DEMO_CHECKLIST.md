# PANIC v1 — College Demo Checklist

## Build and install on your Windows laptop

Phase 6 passed tests and assembleDebug locally, as reported by you. Run both again after applying Phase 7:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

Expected output after a successful build:

`app/build/outputs/apk/debug/app-debug.apk`

Use Android Studio Run to install on your connected phone/emulator, or install that debug APK normally. No release keystore, signing password, API key or public-release setup is required. Keep the existing project/package and normal debug signing. Do not uninstall or clear app data merely for the presentation; existing tasks/settings should remain.

## Before presenting

- Confirm both commands say BUILD SUCCESSFUL for Phase 7.
- Launch the installed app and confirm its launcher name is PANIC.
- Check Dashboard, Tasks, Calendar, PANIC Mode, Analytics and Settings.
- Check a long title/subject/description and a narrow screen or increased font size. Priority/filter chips should scroll horizontally; description editing should scroll within its field after six lines.
- Try Light, Dark and System. Open date/time pickers too. Restart and confirm the selected theme and existing tasks persist.
- If testing notifications, check Android permission, both channel settings and Do Not Disturb. High importance does not guarantee a heads-up banner.

## Recommended live demonstration (real data only)

1. Open Dashboard. Explain that PANIC stores tasks offline and uses a deterministic urgency score.
2. For a predictable notification segment, temporarily turn Notifications off in Settings while creating the task. Leave the anti-spam ledger intact.
3. Add a task titled “CN Assignment — Live Demo”, subject “Computer Networks”, with a short description.
4. Set its deadline approximately one hour from now, estimate 120 minutes and select High priority. Save once.
5. Show the saved task in Tasks and Dashboard. It should be CRITICAL: the estimated work exceeds the available time. Counts should increase relative to the existing database; do not assume the database was empty.
6. Open PANIC Mode. Show the score, remaining time, explanation and deterministic recommendation. If other overdue tasks exist, they correctly rank ahead of this new task.
7. Open Analytics. Show the matching pending/critical counts, workload and completion rate. Explain that upcoming windows are cumulative and estimates are not measured study time.
8. Edit the task to a deadline five days away, 60 estimated minutes and Low priority. Save. It should become SAFE, leave the urgency list, and update Analytics automatically.
9. Edit it back to one hour away, 120 minutes, High priority. Show CRITICAL returning consistently across screens.
10. Optional notification segment: enable Notifications and Panic Alerts, select Balanced, and allow Android permission. Enabling may start the real background check before you tap the debug button. Tap “Run deadline check now” if needed. If a reminder was already posted, a cooldown result is correct; do not reset or bypass throttling.
11. Tap the notification: PANIC Mode should open. Also verify this once with the app already open on another screen.
12. Mark the task complete from Tasks. Its visible notification should disappear; it should leave active PANIC urgency and pending workload. Completion statistics should update.
13. Use the Completed filter and restore it. Risk becomes active again; previously delivered reminders remain subject to cooldown.
14. Delete only this demonstration task using the confirmation dialog, if desired. Existing tasks and preferences should remain.
15. Close and reopen the app. Show that saved tasks and settings persist.

## Optional manual examples for the four risk bands

Use real task creation and set deadlines relative to presentation time:

| Target level | Time remaining | Estimate | Priority |
| --- | --- | --- | --- |
| SAFE | About 5 days | 60 min | Low |
| WARNING | About 20 hours | 60 min | Low |
| HIGH | About 5 hours | 60 min | Medium |
| CRITICAL | About 1 hour | 120 min | High |
| OVERDUE | 1 hour in the past | 60 min | Low |

Risk evolves with time. These examples intentionally avoid exact category boundaries. Do not auto-insert them or hardcode their scores.

## Focused final regression checks

- Empty database / only completed tasks: clean empty states; no NaN, fake statistics or invented streak.
- Save validation: blank title/subject, zero or invalid estimate must show errors. A valid past deadline is permitted and should become overdue.
- Complete/delete: removes active urgency and cancels the task's notification. Restore resumes eligibility without resetting cooldowns.
- Calendar: earlier-today overdue tasks may appear in today's selected date list, but must not appear in Upcoming deadlines. A deadline exactly now remains upcoming until it passes.
- Calendar after midnight/resume: selected date remains within the visible seven-day strip.
- Tasks filters / editor priority controls: all options remain reachable at large font sizes.
- Navigation: repeated tab taps do not accumulate screens. Saving returns once. Analytics -> PANIC -> Dashboard should not unexpectedly restore Analytics. Notification taps must open the PANIC list rather than a saved editor/detail screen.
- Notification during save: finish the task write before handling the navigation request.
- Permission denied or a channel blocked: app stays usable and Settings reports the block. No repeated automatic permission prompt.
- Theme changes: persist without triggering notification checks or changing notification preferences.

## What to explain during viva

“Room is the source of task data. Kotlin Flow updates the shared screen state. The same deterministic RiskCalculator powers the risk indicators, PANIC ranking, analytics and background notification eligibility. WorkManager performs approximate checks, and a persistent cooldown ledger prevents repeated alerts. No machine learning or cloud service is used.”

WorkManager is not an exact alarm. Android battery policies, force-stop, user settings and Do Not Disturb can delay or suppress notifications. Show the real debug check rather than promising a precisely timed background alert.
