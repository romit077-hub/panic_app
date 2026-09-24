# PANIC v1.1 Pass C — visual system and interactions

## 1–3. Git baseline

Branch: feature/smart-planner-ui. Initial tree clean. Starting commit:
56c109ef93ed6c39aab5a4f7e858e722ac1720f4 (Pass B), preceded by Pass A
6a957d7cfd88a010a8ec0547cdb13439e73a0992. The user reports both local tests/build
and launch succeeded for that baseline; that is user-provided verification, not
new verification performed here. Ending commit is recorded in delivery COMMIT.txt.
One commit: `v1.1: polish PANIC UI and interactions`.
Main remains at the supplied 7df6f5f3c0937e38bd27f1e6796b4d414aa0daad snapshot.
No merge into main.

## 4. Files created

Under app/src/main/java/com/example/panic_app:
- ui/theme/PanicDesign.kt
- ui/components/PanicMotion.kt
- ui/screens/tasks/TaskFilters.kt

Test: app/src/test/java/com/example/panic_app/ui/screens/tasks/TaskFiltersTest.kt.
Documentation: this report, PASS_C_TEST_LOG.txt and PASS_C_BUILD_LOG.txt.

## 5. Existing files modified

Under the same production package:
- ui/PanicApp.kt
- ui/navigation/PanicDestination.kt
- ui/theme/Type.kt
- ui/components/PanicComponents.kt
- ui/components/RealTaskComponents.kt
- ui/components/RiskSummary.kt
- ui/screens/dashboard/DashboardScreen.kt
- ui/screens/tasks/TasksScreen.kt
- ui/screens/addedittask/TaskEditorScreen.kt
- ui/screens/panic/PanicScreen.kt
- ui/screens/planner/PlannerScreen.kt
- ui/screens/planner/PlannerSettingsScreen.kt
- ui/screens/calendar/CalendarScreen.kt
- ui/screens/analytics/AnalyticsScreen.kt
- ui/screens/settings/SettingsScreen.kt
- ui/screens/settings/ReminderSettingsRoute.kt

## 6. Design system

Reused Material 3, established brand colors and light/dark risk palettes.
Centralized screen padding (20dp), section gaps (16dp), card padding (18dp),
content gap (12dp), card radius (20dp) and shared motion duration (240ms).
Refined headline-small typography. Existing Panel, StatCard, SectionHeader and
RiskBadge remain the common visual foundation. New progress/gauge components
handle animated display values only. Empty task states gain a small check icon.
No images, gradients, external assets or new dependencies.

## 7. Dashboard

Due-today hero shows pending task count and remaining estimated work, plus
critical/overdue counts. Highest-task-risk gauge is explicitly labelled as one
task's risk, never an invented aggregate. Next Action uses the existing ranked
pending list and opens the existing editor. Pending/completed metrics animate
when their displayed values change. Nearest deadline and Panic Mode remain
accessible. Add task, Analytics and Settings retain actions.

Smart Plan shares the existing PlannerViewModel at application scope. It shows
actual scheduled minutes today, current/next session and View plan. Before a plan
exists, Build my plan opens Plan, where Generate remains explicit. Date/zone
changes prompt a refresh. A generated plan stays available across tab navigation;
it is still not persisted as a timetable database. No additional planner instance
or scheduling formula was introduced.

## 8. Tasks

Existing Pending, Completed and All filters remain; Today, Upcoming and Critical
are added as lightweight presentation filters. Today includes earlier-today overdue
work but excludes completed tasks; Upcoming means dates after today, not later
hours today. Critical uses existing RiskResult levels. Pending task order is
unchanged. Lists use stable task keys and native animateItem for rearrangement
and removal. Notes expand on request; cards resize in 240ms. Existing Material
buttons/chips retain ripple and selected-state feedback. Completion persistence
is immediate, not delayed by animation. Edit/delete and confirmation are retained.

## 9. Add/Edit Task

Explicit Task details and Workload grouping, remaining-work label, more compact
notes input and consistent Save task copy. Existing deadline/priority groups,
validation, input state, system pickers, past-deadline support, save protections
and cancellation behavior are preserved.

## 10. PANIC Mode

Highest real task score appears in a restrained 320ms circular gauge with risk
label and accessible score text. Immediate attention and overdue counts stay
visible. Ranked task cards offer expandable plain-language reasons and recommended
actions from RiskResult. No new scoring, probabilities, timer or focus feature.
No infinite pulse/flashing. The empty state explicitly says there is no immediate
deadline danger.

## 11. Smart Planner

Today/Upcoming and generation semantics are preserved. Generate changes its
label immediately using AnimatedContent; no artificial wait was added. Summary
shows whole-plan scheduled minutes, today's minutes, covered tasks and unscheduled
work with explicit scope labels. Keyed summary/session/warning items use native
list motion. Session details are rounded surfaces beside the time rail. Break
inference remains Pass B logic. Unscheduled capacity warnings use neutral readable
text, separate from application errors. Session durations use the common display
formatter. Planner time pickers now match the selected app light/dark theme.

## 12. Calendar

Each date chip shows due count and highest existing risk label for that date.
The selected day shows pending task count and estimated workload. Date-summary
changes use AnimatedContent; keyed task items animate placement. Existing seven-day
selection bounds and future-only Upcoming deadlines logic remain unchanged.
This remains a deadline view, not another study timetable.

## 13. Analytics

Existing metric cards now animate value changes through the shared component;
completion/distribution bars animate their displayed fraction over 240ms.
Calculations, periods, percentages and risk counts are untouched. No fabricated
streak, on-time completion rate or history metric. Copy is shorter while retaining
scope notes that prevent confusing cumulative periods with independent totals.

## 14–15. Settings and navigation

Added Planning section linking to the same PlannerSettingsScreen through central
planner/settings destination. Theme selection uses labelled, scrollable filter
chips with explicit selection. About text describes user benefits rather than
Room internals. Notification permission, theme persistence, reminder controls
and debug check actions are unchanged. Five primary tabs remain. NavHost adds
180ms enter / 150ms exit fades while preserving back-stack options, insets, IME
padding, editor guards and notification deep-link routing.

## 16. Motion

- 150/180ms navigation fades.
- 240ms shared progress interpolation and card size changes.
- 320ms highest-task-risk gauge entrance/data change.
- Native AnimatedContent for metric values, generation feedback and selected date.
- Native AnimatedVisibility for notes/risk explanations.
- Native keyed animateItem for task placement/removal and planner content.
- Existing Material ripple/selection feedback.

No animation callbacks trigger writes, navigation or generation. No fake generation
delay, infinite decorative animation, particles, blur or third-party motion system.
Native Compose motion follows the platform animation-duration mechanism.

## 17–19. Accessibility, themes and copy

Risk always has labels/scores; gauge semantics announce score and level together.
Buttons retain Material touch targets; full-width primary actions, wrapping titles,
scrollable screens/chips and weighted content support phone widths. No new raw
risk colors: existing contrasting light/dark pairs and Material colors are reused.
Time picker theme follows app selection. Reused human-readable work duration and
deadline formatters. Expanded risk explanation no longer exposes scoring arithmetic;
the original plain-language reason remains available. Existing source reasoning
is still available in domain code for viva.

## 20. Business logic preserved

Source comparison against baseline confirms no changes under data/, domain/,
notification/, util/, model/, or any existing ViewModel. No Gradle/catalog/manifest
changes. Room entity/schema and CRUD semantics remain intact. SmartPlanner,
RiskCalculator, analytics calculations and planner preference persistence are
unchanged. Only planner ViewModel ownership moves from Plan destination to the
application Compose owner so Dashboard and settings share its actual state.

Source callback review covers create/edit/delete/complete, Dashboard and Calendar
Room snapshots, PANIC risks, Generate/Regenerate, settings saves, Today/Upcoming,
Analytics, theme selection and notifications. This is source review, not runtime
verification.

## 21–23. Tests/build

Six new fixed-time TaskFiltersTest cases cover Today with overdue/completed tasks,
future dates, Critical using existing ranking, All uniqueness/Completed selection,
device-local midnight boundaries and empty data. Existing tests were not modified.

./gradlew testDebugUnitTest — exit 1 before compilation.
./gradlew assembleDebug — exit 1 before compilation.
Both fail downloading Gradle 9.5.0 with java.net.SocketException: Network is
unreachable. Exact logs included. No new successful compilation, test execution,
APK or launch is claimed. Build versions/dependencies were not altered.
Whitespace/source checks passed; these do not replace compilation.

## 24–25. Push and remote verification

PUSH NOT PERFORMED. Origin in this checkout is the uploaded local Git bundle,
not an authenticated GitHub remote. No GitHub branch was verified. The supplied
bundle preserves original ancestry plus Pass A, Pass B and this focused Pass C
commit. User-local Git configuration is preserved by fetching into the existing repo.

## 26–27. Limitations and local visual/functional checklist

No emulator/rendered screenshots were available for this pass. Review was from
source/layout constraints only; no pixel-perfect or performance claim is made.
Shared planner observation now lives for the application ViewModel lifetime.
The scheduler remains explicit/stateless and generated plans remain in memory.
Animations are restrained native defaults where a custom duration is not supplied.

On your existing local project, import and run tests/build, then verify:
1. Light/dark/system theme, including all task and planner time/date dialogs.
2. 360dp and 430dp widths, long titles, 1.3x/2x text, portrait/landscape.
3. Dashboard today totals, highest score label, next action and live Smart Plan.
4. All six task filters; complete/reopen, edit and delete without duplicated events.
5. Add/Edit validation, keyboard/insets, date/time/priority, saving Back protection.
6. PANIC gauge and expandable reasons; empty/no-critical case.
7. Planner Generate/Regenerate, rapid task edits, settings changes, all-disabled days,
   Today/Upcoming, midnight/zone change, breaks, unscheduled warnings and task links.
8. Calendar date indicators, selected-day totals, empty days and upcoming deadlines.
9. Analytics values unchanged; bars/counters settle without continuous motion.
10. General Settings -> Planner settings -> Back; persisted theme/reminders/windows.
11. All tabs, back stack, notification entry into PANIC and bottom/IME padding.
12. Fast scrolling with hundreds of tasks, repeated navigation and Android animation
    scale disabled. Confirm no clipped controls, overlapping FABs or unreadable badges.

## Import into the existing Windows project

Extract ZIP separately, ensure your existing repository has no uncommitted source
changes, then use Android Studio's terminal:

```powershell
git switch feature/smart-planner-ui
git fetch "C:\actual\extracted\path\panic-pass-c.bundle" refs/heads/feature/smart-planner-ui
git merge --ff-only FETCH_HEAD
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

Replace the sample path with the actual bundle location. If fast-forward refuses,
stop and inspect the divergent commits; do not reset or force. Do not replace .git.
After successful checks, use your configured authenticated remote:
`git push origin feature/smart-planner-ui`. Confirm the ending commit on that branch.
