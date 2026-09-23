# Phase 5 — Deadline notification escalation

## Verification status

Source implementation complete; Android compilation, unit-test execution, installation and notification delivery are NOT verified here.

- `./gradlew testDebugUnitTest`: exit 1 before compilation. Wrapper download of Gradle 9.5.0 fails with `java.net.SocketException: Network is unreachable`.
- `./gradlew assembleDebug`: exit 1 at the same wrapper download, before compilation.
- Direct connection diagnosis: `curl -I --connect-timeout 5 --max-time 10 https://services.gradle.org/distributions/gradle-9.5.0-bin.zip` exited 28, Proxy CONNECT timeout.
- Host Java is 17; the existing Gradle daemon configuration requests Java 25. No Android SDK/compiler was located. Existing toolchain settings were preserved.
- XML and TOML parsing, unchanged existing dependency versions, unchanged Room schema/risk engine, absence of prohibited permissions, and `git diff --check` passed. These checks do not substitute for compilation or unit tests.
- Full command output is in PHASE5_TEST_LOG.txt and PHASE5_BUILD_LOG.txt.

## Git preservation

Before implementation HEAD was `ed608d4` (Phase 3), with Phase 4 source uncommitted. There was no verified Phase 4 commit. Preserved those exact changes as `b1e0ac5`, “Phase 4: preserve risk engine source checkpoint (build unverified)”. Phase 5 is a separate commit named “Phase 5: implement deadline notification escalation”. No amend/squash or remote push. The ZIP includes a Git bundle for the local commit history.

## Files created

Under app/src/main/java/com/example/panic_app:

- notification/ReminderPolicy.kt — pure severity, preference and cooldown decisions.
- notification/ReminderStore.kt — persistent settings and per-task/global notification ledger.
- notification/NotificationChannels.kt — two stable channels.
- notification/PanicNotificationManager.kt — status, posting, cancellation and tap intent.
- notification/NotificationScheduler.kt — unique WorkManager requests.
- notification/DeadlineReminderController.kt — shared risk/notification pipeline.
- notification/DeadlineCheckWorker.kt — background entry point with bounded IO/SQLite retries.
- data/repository/TaskMutationGate.kt — shared task-write/notification lock.
- ui/screens/settings/ReminderSettingsRoute.kt — permission, lifecycle status and settings actions.

Also:

- app/src/main/res/drawable/ic_notification_deadline.xml
- app/src/test/java/com/example/panic_app/notification/ReminderPolicyTest.kt
- PHASE5_REPORT.md, PHASE5_TEST_LOG.txt, PHASE5_BUILD_LOG.txt

## Files modified

- gradle/libs.versions.toml and app/build.gradle.kts
- app/src/main/AndroidManifest.xml
- MainActivity.kt and PanicApplication.kt
- data/repository/TaskRepository.kt
- ui/PanicApp.kt and ui/screens/settings/SettingsScreen.kt

## Dependency and manifest

Added only `androidx.work:work-runtime:2.11.0` (includes CoroutineWorker). All existing version pins are unchanged. WorkManager 2.11 requires API 23+, compatible with this project's minSdk 26.

Added POST_NOTIFICATIONS. No custom receiver/service, exact alarm permission, foreground service implementation, overlay or full-screen intent. WorkManager supplies its standard merged-manifest infrastructure, including scheduling/reboot components and wake-lock/boot permissions; the merged manifest cannot be inspected until a build succeeds. No custom boot receiver.

## Channels and scheduling

- Deadline Reminders: default importance; WARNING alerts.
- Panic Alerts: high importance; HIGH, CRITICAL and OVERDUE alerts.

Channel IDs are stable and creation is idempotent. Android/user importance and Do Not Disturb settings are respected. High importance does not guarantee a heads-up display.

One unique periodic WorkManager request, 15-minute interval, KEEP policy. Task mutations and enabled preference changes enqueue a unique replaceable one-time check. Disabled settings cancel both request names. Worker always re-reads current Room rows and uses the existing RiskCalculator; scheduled work carries no stale task snapshot.

Separate per-task 24h/6h/2h alarms were deliberately omitted: the existing risk engine already includes deadline bands, and periodic checks observe their transitions under the same throttling policy. Delivery is deferrable and may occur well after a threshold. There is no exact timing guarantee.

## Escalation and anti-spam

SAFE and completed tasks never notify. WARNING uses the normal channel. HIGH and CRITICAL use the urgent channel. OVERDUE is a distinct escalation derived from RiskResult.isOverdue. At the exact deadline, RiskCalculator reports CRITICAL; overdue begins after the deadline.

Same-level (or downgraded) reminder cooldowns:

| Intensity | WARNING | HIGH | CRITICAL | OVERDUE |
| --- | --- | --- | --- | --- |
| Gentle | skipped | 12h | 6h | 24h |
| Balanced | 12h | 6h | 3h | 12h |
| Aggressive | 6h | 3h | 1h | 6h |

Higher severity can notify after a 30-minute minimum per task. A five-minute app-wide minimum and maximum one notification per check protect against bursts across many tasks. Eligible candidates follow overdue, score, deadline and ID order. Turning Panic Alerts off blocks HIGH/CRITICAL/OVERDUE, while WARNING may still notify under Balanced/Aggressive. Turning all notifications off blocks every severity.

Persisted ledger stores task ID, last severity and last timestamp, plus last global timestamp. Edits, completion/restore and preference toggles do not reset cooldowns. Deleted-task entries are pruned on the next check. Backward clock changes conservatively wait until stored times catch up.

Notification identity is a full Long task ID in the tag `deadline:<id>` with a constant integer slot, preventing Long-to-Int collisions and updating the same task's notification.

## Persistence and race handling

Small SharedPreferences file; no Room schema/entity changes or migration. Settings and delivery writes use synchronous commits on Dispatchers.IO, under the shared process-wide mutation mutex. Preferences are exposed through StateFlow. Dark mode retains its prior session-only behavior and is labelled accordingly.

Task writes and notification evaluation/posting use the same gate. Completion, edits and deletion immediately cancel the task's visible notification and enqueue a fresh check when enabled. Pending rows are recalculated. Restored tasks resume subject to the retained cooldown. Every check also removes active notifications for rows no longer pending, covering an interrupted previous cleanup.

The durable reservation occurs before the OS post. A process crash or permission revocation in the narrow reservation/post interval can skip one reminder until its cooldown, rather than duplicate alerts. This is an explicit at-most-once preference, not transactional delivery across Android and local preferences.

## Permission and tap UX

Permission requests occur only when the user taps “Allow notifications” in Settings on API 33+. No launch-time or repeated automatic permission dialogs. Explanatory text comes before the action. A system notification settings button remains available after denial; status refreshes when returning from settings and after the permission result. The UI says “Reminders requested”, never promises delivery just because the preference is on.

Immutable PendingIntent opens PANIC Mode. MainActivity handles both cold launches and onNewIntent using CLEAR_TOP/SINGLE_TOP. The action is consumed after navigation. It opens the ranked urgent-task list, not a potentially deleted task deep link.

Debuggable builds show “Run deadline check now”. This directly calls the same controller as the worker, does not fake notifications or bypass permissions/throttles, and displays the outcome. Release builds hide it using ApplicationInfo.FLAG_DEBUGGABLE.

## Tests added

20 fixed-time JUnit tests: SAFE, initial WARNING/HIGH/CRITICAL, OVERDUE severity, completed task, duplicate CRITICAL, WARNING-to-HIGH, HIGH-to-CRITICAL, minimum escalation gap, all notifications disabled, urgent alerts disabled, Gentle behavior, Aggressive cooldown, exact cooldown boundary, CRITICAL-to-OVERDUE, global throttle, clock rollback, downgrade, and integration with the real RiskCalculator.

These tests are written but NOT executed due to the wrapper download blocker. No claim of unit-test success.

## Required local validation

1. Open this existing panic_app project in Android Studio with its configured toolchain and SDK available. Keep package/SDK/dependency versions intact.
2. Run `./gradlew testDebugUnitTest` then `./gradlew assembleDebug` (Windows: `gradlew.bat testDebugUnitTest` and `gradlew.bat assembleDebug`). Investigate any actual compiler errors; none could be obtained here.
3. Install debug on an API 33+ emulator/device. Deny permission: the app must remain usable; Settings must show the block. Allow it in system settings and return.
4. Add one real task due in one hour with a two-hour estimate. In Settings, run the real deadline check. A mutation-triggered worker may already have posted it; a subsequent debug check should report cooldown rather than send another.
5. Tap the notification from a closed app and an already open app: both should open PANIC Mode.
6. Complete/delete a notified task: its notification should disappear. Edit its deadline into the future: stale notification should disappear. Restore a completed task: recalculation resumes without resetting anti-spam history.
7. Toggle Notifications and Panic Alerts separately; verify active cancellation and future suppression. Block each Android channel and verify status after returning.
8. Relaunch/reboot: preferences and cooldowns must persist. WorkManager resumes subject to Android scheduling restrictions. Force-stop is not equivalent to normal process death; scheduled execution may wait for relaunch.
9. Verify a release build has no debug check button. Check notification content/privacy, theme contrast, back stack and accessibility on-device.

## Remaining limits

No APK or successful build/test run is available from this environment. Runtime permission, navigation and actual notification delivery need device validation. WorkManager is not an alarm clock: Doze, battery policies, force-stop, notification cooldowns and user/DND settings can delay or suppress presentation. Large backlogs are intentionally delivered slowly, one eligible task per check. A wall-clock rollback can defer reminders. No Phase 6 features were introduced.

Official references consulted:
- https://developer.android.com/jetpack/androidx/releases/work
- https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started/define-work
- https://developer.android.com/develop/background-work/background-tasks/persistent/how-to/manage-work
- https://developer.android.com/develop/ui/compose/notifications/notification-permission
