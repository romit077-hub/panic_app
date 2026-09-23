# Phase 7 — Final college demo QA and focused fixes

## Baseline

Started with a clean tree at `bb1f6c2`, Phase 6. Phase 5 (`513b4b7`) and earlier history were preserved. The user explicitly reported BUILD SUCCESSFUL for Windows `gradlew.bat testDebugUnitTest` and `gradlew.bat assembleDebug` on Phase 6. That is the accepted local baseline; this report does not invalidate it because the cloud environment lacks download access.

Inspected current source for Dashboard, Tasks, Add/Edit Task, Calendar, PANIC Mode, Analytics, Settings, navigation, shared components, dialogs, validation, Room/DAO/repository, risk scoring/ranking/time text, notification channels/manager/controller/worker/scheduler/throttling, preference storage and themes. This was a source audit, not a running-device visual inspection.

## Demonstrated issues fixed

1. Launcher label still used the project identifier `panic_app`. Updated the user-facing string to PANIC; project name, package and application ID unchanged.
2. Calendar's Upcoming filter compared calendar dates, so an earlier-today overdue task appeared as upcoming. It now compares actual timestamps, excludes completed tasks and sorts by deadline/ID. The selected-date list intentionally still shows overdue tasks belonging to that date.
3. Calendar's saved selection could move outside the visible seven-day strip after midnight/resume. It is now clamped to the visible range, and the date is derived from the shared refreshed snapshot.
4. Multi-day time formatting dropped remaining minutes (1,470 estimated minutes displayed as just “1 day”). Formatting now includes days, hours and remaining minutes in task explanations and notifications. The risk score formula is unchanged.
5. Tab navigation could archive secondary screens as part of a tab's restored stack. It now saves stack state only when switching from a primary destination; tapping the already-selected tab is a no-op.
6. Notification entry reused tab restoration, which could restore a detail/editor destination instead of PANIC Mode. It now explicitly opens the PANIC list without restored detail state and waits while an editor save is in progress.
7. Native date/time dialogs inherited the manifest's light platform theme even when PANIC's persisted theme was Dark. Their explicit platform dialog theme now follows the active Compose theme.

## Conservative polish and cleanup

- Task filters and editor priority chips scroll horizontally to remain reachable on narrow screens / with larger text.
- The description editor displays up to six lines and scrolls internally, avoiding an excessively tall field when editing long content.
- Existing scrollable cards retain full task content rather than arbitrarily truncating it.
- Removed the unreferenced DemoNotice preview composable.
- Removed a 2,842-byte obsolete template-source ZIP embedded under app/src/main/java. Its original contents remain in previous Git commits; it was not compiled application source.
- Retained the deliberately labelled debug deadline-check control, which uses the real notification pipeline.
- Retained the template backup-policy TODO because it is not user-visible and changing backup behavior is outside this focused demo phase.

## Consistency findings

CRUD already validates required title/subject, lengths, valid estimate range and deadline year; past deadlines are intentionally allowed. Editing preserves completion/creation fields, save suppresses duplicate submissions, and deletion requires confirmation.

Dashboard, Tasks, PANIC and Analytics consume a single atomic task/risk/analytics snapshot. Counts and risk classes share definitions. The notification worker independently uses the same RiskCalculator with a fresh execution timestamp. UI and worker values can legitimately differ if time advances between checks; there is no separate formula.

Completed tasks are excluded from active urgency and overdue counts. Nearest pending deadline intentionally includes overdue tasks. Analytics uses Long workload sums and explicitly labelled cumulative upcoming windows; no fake statistics or historical streaks were found. Empty completion rate is an em dash.

Room remains the task source of truth. The existing mutex protects task writes against stale concurrent notification posts. Completion/edit/delete cancel associated notifications. Persistent cooldowns, unique WorkManager jobs, deterministic notification tags, permission checks and both channels were retained. No scheduling or notification policy changes were made.

Notification preferences and System/Light/Dark theme use the existing SharedPreferences/StateFlow architecture. System bars follow the resolved theme; risk badges use centralized light/dark colors. Dialog theming was the only identified theme inconsistency fixed. Actual color rendering, font-scale layout and system-bar appearance still require device checks.

## Tests

Added four focused regressions:
- One RiskTime test for multi-day remaining-minute preservation, including overdue text.
- Three CalendarSchedule tests for timestamp eligibility/completed exclusion, stable ordering, and selection clamping after midnight.

Existing tests were preserved. No test-count expansion unrelated to discovered bugs. Navigation and native-dialog changes require the specific manual checks in COLLEGE_DEMO_CHECKLIST.md; they are not claimed as emulator-tested.

## Exact verification results

Baseline attempts:
- `./gradlew testDebugUnitTest`: exit 1 before compilation, Gradle 9.5.0 wrapper download failed with `java.net.SocketException: Network is unreachable`.
- `./gradlew assembleDebug`: exit 1 at the same download step.

Final attempts after all fixes:
- `./gradlew testDebugUnitTest`: exit 1 before compilation, same network error.
- `./gradlew assembleDebug`: exit 1 before compilation, same network error.

Logs: PHASE7_BASELINE_TEST_LOG.txt, PHASE7_BASELINE_BUILD_LOG.txt, PHASE7_TEST_LOG.txt, PHASE7_BUILD_LOG.txt. No Kotlin compiler diagnostics, executed unit-test results or APK were produced here. No version changes were made in response to this environmental failure.

Source checks passed: XML/TOML parsing, expected app label, dead-preview removal, `git diff --check`, and unchanged Gradle/dependency/SDK/manifest/data/notification/analytics/risk-calculation sources. These checks do not replace the final local Gradle run or device QA.

## Git and deliverable

Phase 7 is a separate commit named “Phase 7: finalize PANIC college demo”; no earlier commit was amended/squashed. The final response gives the resulting hash. No automatic push. Archive includes source, reports, demo checklist, command logs and Git history bundle.

No major feature, dependency, migration, networking, release signing, secrets, public-store configuration or production infrastructure was added. No task database was modified or populated for presentation.

## Remaining known limits and APK

Phase 7 compilation, regression-test execution, navigation, dialog appearance, small-screen behavior and notification delivery await final local/device verification. Phase 6's successful local results do not prove Phase 7's changes have passed.

After successful local `gradlew.bat assembleDebug`, the normal debug APK should be at:

`app/build/outputs/apk/debug/app-debug.apk`

This is an expected local output path, not an APK generated or inspected here. Normal debug signing is sufficient.

Existing platform limits remain: WorkManager checks are inexact, foreground risk refresh is every 30 seconds/resume, system permissions/channels/DND/battery rules can suppress presentation, and cooldowns intentionally prevent instant repeat alerts. Calendar remains a seven-day strip plus an upcoming list; no calendar feature expansion was attempted. Analytics represents current stored tasks, not historical completion events.

Follow COLLEGE_DEMO_CHECKLIST.md for the exact live demo and targeted regression checks. No further development phase was started.
