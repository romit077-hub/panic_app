# PANIC v1.1 Pass B — Smart Planner integration

## Git and scope

Branch before and after work: feature/smart-planner-ui. Started clean at
6a957d7cfd88a010a8ec0547cdb13439e73a0992, the Pass A engine commit. The supplied
main snapshot remains 7df6f5f3c0937e38bd27f1e6796b4d414aa0daad. One focused commit:
`v1.1: integrate Smart Planner UI`. See delivery COMMIT.txt for the ending hash.
No main commits, history replacement, dependency changes, Gradle changes,
Room migration, network feature or whole-app redesign.

## Files

New production files under app/src/main/java/com/example/panic_app:
- data/settings/PlannerPreferences.kt — versioned pure settings codec/defaults.
- data/settings/PlannerSettingsStore.kt — SharedPreferences persistence and Flow.
- ui/screens/planner/PlannerViewModel.kt — task observation, generation and saving state.
- ui/screens/planner/PlannerPresentation.kt — date/time, durations, reasons and break mapping.
- ui/screens/planner/PlannerScreen.kt — route, today/upcoming timeline and workload warnings.
- ui/screens/planner/PlannerSettingsScreen.kt — preferences and weekly availability editor.

New test: app/src/test/java/com/example/panic_app/ui/screens/planner/PlannerIntegrationTest.kt.
New documentation: this report and SMART_PLANNER_PASS_B_TEST_LOG.txt / SMART_PLANNER_PASS_B_BUILD_LOG.txt.

Existing files modified: MainActivity.kt, PanicApplication.kt, ui/PanicApp.kt,
ui/navigation/PanicDestination.kt, ui/screens/dashboard/DashboardScreen.kt.
All existing tests and Pass A engine files are unchanged.

## Navigation and Dashboard

Five primary tabs: Dashboard, Tasks, Plan, Calendar, Panic. Plan uses the central
smart_planner route and existing tab back-stack behavior. Settings and Analytics
retain their existing access. Dashboard has one small Smart Planner panel and
Open Smart Planner action. Existing notification routing remains unchanged.
A study session's Open task action and unscheduled Review task action use the
existing task editor. Availability/settings opens within the planner screen;
Done or Android Back returns to the plan.

## Screen, timeline and risks

Screen includes a settings entry, Generate/Regenerate, progress/error handling,
plan summary, Today/Upcoming selector, date headings, timeline and unscheduled
work. Timeline uses a narrow time column, vertical marker, task title, duration,
risk label and risk score. Material theme colors and existing RiskBadge support
light/dark themes; labels carry meaning independently of color. All content is
scrollable; task titles wrap. No global animations or new decorative assets.

Today groups by device-local date, refreshed while visible every 30 seconds.
Upcoming lists all future groups chronologically with session counts and minutes;
sessions are directly visible without an additional expand action. If today has
no sessions, the next future session is shown when present. Display uses shared
formatters and the existing deadline formatter. A date/time-zone change after
generation prompts regeneration. Past sessions may remain in today's snapshot;
they are not marked completed, and regeneration starts from the current time.

Breaks are inferred only for the exact configured gap, on the same date and within
one merged available interval. Larger gaps or separate availability windows are
not called breaks. The existing engine is not altered for presentation.

Unscheduled records show title, remaining minutes, deadline, readable reason and
Review task. All six engine reasons are mapped. Zero/invalid estimates show an
attention reason even when remaining minutes are zero. No availability and
insufficient capacity are normal plan outcomes. An all-work-fits message appears
only when the engine returns no unscheduled/attention records.

## Preferences and availability

Defaults: weekdays 18:00–22:00; weekends 10:00–18:00; sessions 45 minutes;
breaks 10 minutes; maximum 240 study minutes/day; seven local calendar dates.
Domain defaults are unchanged; these are application preference defaults.

Dropdown selectors provide sessions 25/30/45/60/90, breaks 5/10/15/20,
daily maxima 120/180/240/300/360 and horizons 3/7/14. A valid previously stored
custom value remains selectable. A weekday switch enables a default evening
window or removes that day's windows. Each day supports multiple Add study
window / Remove window actions. Start/end use platform time pickers respecting
the device's 12/24-hour setting. Choosing 00:00 for the end means end-of-day
midnight. Invalid end-before-start is rejected with a visible explanation.
Overnight study must be split by day, matching Pass A. Overlaps are merged by
the existing engine. Re-enabling a disabled day starts with its default evening
window, not its previously removed windows.

Settings save immediately to a dedicated local SharedPreferences file, using
the same mechanism as the existing reminder settings without mixing their keys.
Writes run on IO and publish state only after successful commit. Controls disable
while saving; a failed write displays an error. Missing, malformed, unsupported
version or invalid configuration data falls back safely to defaults. Intentionally
empty availability is preserved. No new persistence dependency is required.

## Real task and state integration

PlannerViewModel observes TaskRepository.observeAll() and persisted settings.
The existing List<TaskEntity>.generateStudyPlan adapter calls Pass A; no duplicate
task entity, database or risk formula. Generate uses the current observed task
snapshot, settings, device zone and supplied current clock. Dispatchers.Default
keeps planning off the UI thread. The screen never computes a plan on recomposition.

After the first Generate request, task or settings emissions replace the plan
with a fresh one. Cancellation prevents an older in-flight result replacing the
new request. Manual Regenerate samples the current clock and replaces sessions,
never appends. Loading, generation, saving, data errors and plan results are
explicit state fields. Empty pending tasks show You're clear; before generation,
users see a prompt to review availability and Generate. Defaults mean a mandatory
NeedsConfiguration state is unnecessary. Retry resubscribes and regenerates if
previously requested. The generated plan stays in ViewModel memory only; settings
survive process restart, while a new process can generate another current plan.

Estimates mean remaining work. Suggested sessions are not completion history.
Students should revise estimates/complete tasks as work progresses. Risk labels
and scores are generation-time values from the existing RiskCalculator.

## Tests and verification

13 new fixed-input JVM tests cover defaults, multiple-window and midnight codec
round-trip, intentionally disabled availability, corrupt/unknown formats,
invalid windows/weekdays/settings, zero daily budget, reason labels, exact breaks,
separate-window gaps, long gaps, merged overlaps, local-date boundaries and
large/remainder duration formatting. All 31 Pass A tests and earlier suites remain.
No new coroutine/UI testing dependency or fragile UI suite was introduced.

Final commands (also retried after source review):
- ./gradlew testDebugUnitTest — exit 1 BEFORE compilation.
- ./gradlew assembleDebug — exit 1 BEFORE compilation.

Both fail downloading https://services.gradle.org/distributions/gradle-9.5.0-bin.zip
with java.net.SocketException: Network is unreachable. Logs accompany delivery.
No successful compilation, executed unit tests, APK, emulator launch or visual
verification is claimed. Source/diff inspection is not a substitute for those.

## Push, import and local verification

PUSH NOT PERFORMED. The configured origin is the local uploaded Git bundle, not
an authenticated GitHub remote. No remote GitHub branch verification was possible.
The delivered bundle includes original ancestry, Pass A and Pass B. Import into
your existing repository; do not replace .git or reset main.

Extract the ZIP to a separate directory. In your existing Android Studio terminal,
check git status and commit/stash any unrelated source work before importing:

```powershell
git switch feature/smart-planner-ui
git fetch "C:\actual\extracted\path\smart-planner-pass-b.bundle" refs/heads/feature/smart-planner-ui
git merge --ff-only FETCH_HEAD
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

Replace the sample path with the actual extracted bundle path. A fast-forward
works from the supplied baseline or Pass A; if Git reports divergence, stop and
inspect rather than force/reset. After passing local checks, push using your own
configured authenticated remote: git push origin feature/smart-planner-ui.

Manual phone checklist:
1. Open Plan and Dashboard entry; switch tabs/back and open a session's task.
2. Add real pending tasks due during future available windows, then Generate.
3. Check Today and Upcoming; compare total minutes, breaks and deadlines.
4. Edit/complete/delete a task, return, and confirm the generated plan updates.
5. Change session/break/daily/horizon options; verify replacement, not duplication.
6. Add/remove windows, disable all days and check the no-availability warning.
7. Reject end-before-start; verify midnight end and separate-window gaps.
8. Restart the app and confirm settings persist; generate a fresh plan.
9. Check overdue/invalid-estimate warnings and an all-completed task list.
10. Verify light/dark mode, large font, rotation and 360–430 dp phone layouts.

Known limitations: local build/tests and UI checks still required; no persisted
plans, tracked study completion, availability exceptions for specific dates,
planner notifications or clock-driven continuous regeneration. Greedy scheduling
and DST conventions remain those documented in Pass A. No Pass C work started.
