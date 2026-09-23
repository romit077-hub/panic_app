# Phase 6 — Real analytics and persistent theme

## Status and Git

Source implementation completed. Unit tests, Kotlin compilation, installation and runtime behavior are NOT verified in this environment.

Before Phase 6: clean working tree, HEAD `513b4b7` (Phase 5). Preserved that commit unchanged. It was an unverified source checkpoint, not a proven successful build. Phase 6 has its own commit: “Phase 6: implement analytics and productivity insights”. No remote push and no Phase 7 work.

## Files created

Within app/src/main/java/com/example/panic_app:
- domain/analytics/AnalyticsSummary.kt
- domain/analytics/TaskAnalytics.kt
- domain/analytics/ProductivityInsight.kt
- model/ThemePreference.kt

Tests:
- app/src/test/java/com/example/panic_app/domain/analytics/TaskAnalyticsTest.kt
- app/src/test/java/com/example/panic_app/model/ThemePreferenceTest.kt

Reports: PHASE6_REPORT.md, PHASE6_TEST_LOG.txt, PHASE6_BUILD_LOG.txt.

## Files modified

Within app/src/main/java/com/example/panic_app:
- notification/ReminderPolicy.kt (settings data class gains theme; escalation logic unchanged)
- notification/ReminderStore.kt
- notification/DeadlineReminderController.kt (theme save method; notification pipeline unchanged)
- ui/PanicApp.kt
- ui/screens/analytics/AnalyticsScreen.kt
- ui/screens/dashboard/DashboardScreen.kt
- ui/screens/settings/ReminderSettingsRoute.kt
- ui/screens/settings/SettingsScreen.kt
- ui/screens/tasks/TaskRiskState.kt
- ui/screens/tasks/TasksViewModel.kt

Also app/src/test/java/com/example/panic_app/ui/screens/tasks/TaskRiskStateTest.kt.

## Architecture

Room task Flow -> existing TasksViewModel -> pure TaskAnalytics.calculate(tasks, nowMillis, zone) -> one TaskAnalysis containing summary, risk results and ranked IDs -> atomic TasksUiState shared by Dashboard, Analytics and PANIC Mode.

Room entities map to pure AnalyticsTask/RiskTask inputs at the presentation boundary. RiskCalculator is called once per task in this UI snapshot, and RiskRanking is reused. No second database or risk formula. No analytics calculations are performed inside Compose rendering loops. The existing 30-second foreground tick and foreground resume refresh time-dependent state. Room changes automatically recalculate after add/edit/complete/restore/delete.

## Statistics and definitions

- Total, pending and completed tasks.
- Overdue: incomplete and dueDateMillis strictly less than the supplied current timestamp, using the existing risk result. At the exact deadline a task is critical but not yet overdue.
- Completion: completed / total * 100, rounded to the nearest whole percent, bounded 0..100. An unrounded fraction drives progress. Empty data has a null percentage shown as an em dash; no invented 0% claim or NaN.
- HIGH and CRITICAL counts, and SAFE/WARNING/HIGH/CRITICAL distributions for pending tasks only.
- LOW/MEDIUM/HIGH priority distributions for pending tasks only.
- Nearest pending deadline: earliest timestamp, then ID; may already be overdue, labelled as such. Dashboard uses the same selected task.
- Pending estimated workload uses Long sums. Nonpositive estimates contribute zero workload; RiskCalculator's existing fallback for urgency remains unchanged. Completed tasks contribute no pending workload.

Workload formatting supports 0 min, 45 min, 2h 30m, 8h, etc., without converting large sums back to Int.

## Upcoming workload windows

Total pending workload includes overdue work. All upcoming windows exclude overdue and completed tasks:

- Remaining today: from now inclusive to the next local midnight exclusive. Local calendar boundaries respect daylight-saving changes.
- Within 24 hours: now through now + 24 elapsed hours, inclusive.
- Within 3 days: now through now + 72 elapsed hours, inclusive.
- Within 7 days: now through now + 168 elapsed hours, inclusive.

The UI explicitly labels these as cumulative windows: longer windows include shorter ones and must not be added together. Each displays a count and estimated workload. These are workload estimates, not tracked hours or predictions of actual free time.

## Insights

Deterministic priority: overdue -> non-overdue critical tasks -> workload within 24 hours -> recorded completion progress -> all caught up. At most four messages. Overdue tasks already count as CRITICAL, so the critical insight excludes them to avoid repeating the same problem. Empty data gets an explicit first-task prompt. Quiet future workloads get a factual planning summary; no random quotes or fabricated trends/streaks. One message is shown when only one useful observation exists.

## UI

Removed all sample analytics, the fake 72% progress and example streak. Added real completion progress, six overview counts, workload totals/windows, nearest deadline, native progress bars for both distributions, and rule-based insights. Existing cards, typography and risk badges are reused. Loading and read-error states use the existing components; counts are not shown as current during an error. Dashboard's “Analytics preview” label is now “Analytics”.

## Settings

Theme now offers System / Light / Dark. It is persisted by the same SharedPreferences store introduced in Phase 5, exposed via the existing StateFlow and applied app-wide. Missing or unknown stored values default to System. System mode follows Android appearance. The existing notification settings, ledger and cooldowns remain intact.

Theme writes run on IO under the existing shared gate and preserve the latest notification fields. Theme changes neither enqueue notification checks nor cancel alerts. Notification toggles and intensity changes preserve the selected theme. No new persistence framework, dependency, permission, database schema, manifest component or toolchain version change.

## Optional utilities

Clear All Tasks and Load Demo Tasks were omitted: neither is necessary for real analytics or settings completion. No automatic sample insertion or destructive reset was added. The Phase 5 debug-only real deadline-check button remains.

## Tests added

28 new test cases:

- 23 pure analytics tests: empty list, rounded completion, strict overdue timestamp, old completed tasks, pending workload, 24h and 7-day inclusive boundaries, real RiskCalculator distribution, priority distribution, critical/overdue/caught-up insights, bounded percentages, all-pending zero percent, workload above Int.MAX_VALUE, zero/negative estimates, far-future tasks, all-overdue tasks, local midnight, DST day, nearest-task tie-breaks, cumulative windows and workload formatting.
- Four theme tests: missing/unknown stored values, enum round-trips, System behavior and explicit overrides.
- One shared-state test: pending/completed/overdue/critical counts agree through completion, restoration, edits and deletion.

Tests use fixed timestamps. The theme tests verify pure mapping/selection, not actual on-device disk persistence. No tests have run successfully here.

## Verification

Final commands:

`./gradlew testDebugUnitTest`

Result: exit 1 before compilation while downloading https://services.gradle.org/distributions/gradle-9.5.0-bin.zip. Error: `java.net.SocketException: Network is unreachable`. No test results were produced.

`./gradlew assembleDebug`

Result: exit 1 at the same wrapper download. No Kotlin compiler diagnostics or APK were produced.

Both commands were retried after source updates, with the same environment blocker. The prior direct network diagnosis also timed out. Changing project versions would not fix this network restriction and was not attempted.

Source checks passed: XML/TOML parsing; unchanged Gradle/dependency/SDK/manifest/Room/risk-engine/scheduler sources; sample analytics removal; `git diff --check`. These are not substitutes for compilation, unit-test execution or emulator testing.

## Remaining validation and limitations

1. On the configured Android Studio machine, run `gradlew.bat testDebugUnitTest` then `gradlew.bat assembleDebug` (or ./gradlew on macOS/Linux). This environment previously had JDK 17 while the existing daemon configuration requests JDK 25, and no Android SDK was located; versions were preserved.
2. Verify empty, mixed, all-completed and all-overdue datasets in the running app. Compare counts with Dashboard and PANIC Mode after each task mutation.
3. Keep Analytics open across a deadline and local midnight, then background/resume it. Time-based values should refresh on resume and at the existing foreground interval.
4. Select each theme, navigate, rotate and force-close/relaunch. System mode should track Android appearance; explicit modes should persist. Verify notification preferences and cooldown behavior survive theme changes.
5. Runtime layout, accessibility, theme persistence and notification regression checks still need a device. No APK or successful test result is claimed.

Analytics describe currently stored tasks. Deleting tasks changes the denominator. There is no completion-event history, so no historical trends or streaks are inferred. Upcoming windows measure due workload, not an achievable study timetable. Phase 7 was not started.
