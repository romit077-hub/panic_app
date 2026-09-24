# PANIC v1.1 — Smart Planner, Pass A

## Git baseline and scope

Imported the supplied Git bundle without replacing its history. Work started on clean branch `feature/smart-planner-ui`, at `7df6f5f3c0937e38bd27f1e6796b4d414aa0daad`. The bundle's main reference points to that same stable commit and remains unchanged. No existing source file or version pin was modified.

This pass adds an offline domain scheduler, a read-only Room snapshot adapter, and JVM tests. No UI, navigation, animation, database migration, persistence of plans, notification behavior, dependencies or APIs were added/changed. The planner is not machine learning and is not integrated into a screen yet.

## Created files

Under app/src/main/java/com/example/panic_app:

- domain/planner/PlannerConfig.kt: configuration plus weekly AvailabilityWindow.
- domain/planner/StudyPlan.kt: session/result/unscheduled models and derived totals.
- domain/planner/PlannerWindows.kt: local-date window expansion and overlap normalization.
- domain/planner/SmartPlanner.kt: stateless scheduling engine.
- data/repository/TaskPlanning.kt: read-only List<TaskEntity>.generateStudyPlan adapter.

Also app/src/test/java/com/example/panic_app/domain/planner/SmartPlannerTest.kt and this report with two Gradle logs. Existing files modified: none.

## API and data ownership

`SmartPlanner.generatePlan(tasks, config, nowMillis, zone, taskTitles)` consumes the existing pure RiskTask DTO. It does not introduce another stored task entity. Titles are supplied separately for presentation snapshots. The adapter maps current Room task rows to those inputs; callers can use a repository Flow emission later. Planning does not mutate task rows, completion, risk scores or notification state.

StudySession contains task ID/title, start/end epoch milliseconds, actual whole-minute duration and generation-time risk score/level. StudyPlan exposes sessions, generatedAt, horizonEnd, unscheduled records, scheduled/unscheduled Long totals, distinct covered tasks and distinct critical tasks receiving sessions.

## Algorithm

1. Validate configuration and require unique task IDs.
2. Expand configured weekly availability into the planning horizon; merge overlapping/adjacent windows per date, clip past time, and discard windows with less than one complete minute available.
3. Exclude completed tasks and calculate remaining tasks' risk once with the existing RiskCalculator and the supplied timestamp.
4. Rank by overdue first, risk score descending, deadline ascending, priority HIGH/MEDIUM/LOW, then task ID ascending. Existing critical risk scores naturally precede lower levels. Overdue tasks remain in attention results but are not eligible for future sessions.
5. Visit windows chronologically. At each available cursor, choose the highest-ranked task with remaining positive work and at least one full minute before its deadline. Schedule the minimum of preferred session length, remaining work, remaining daily study budget, available window time and time to deadline.
6. Advance the cursor, insert the required resting gap before the next session if any, and repeat. Nothing is reserved as a trailing break after the final session.
7. Report every remaining workload with a structured reason; do not hide failures to fit.

This is a deterministic greedy schedule, not an optimization solver. A different global assignment could fit more work. Risk ordering is a generation-time snapshot. Regeneration with current tasks is intentional; it neither assumes generated sessions were completed nor subtracts them from Room estimates.

## Availability, calendar and time semantics

AvailabilityWindow uses a weekday and minutes after local midnight. Start is 0..1439, end is 1..1440 and must be later than start. Example Monday 18:00–22:00 is (MONDAY, 1080, 1320). End 1440 means the next midnight. Overnight availability must be split into separate day-specific windows. Empty availability is the default: no study time is silently assumed.

The default horizon is seven local calendar dates, including the remainder of today; the end is exclusive midnight at the start of the eighth date. Configurable from 1 to 366 dates. It is not seven rolling 24-hour periods.

The caller supplies ZoneId and nowMillis. There are no system-clock reads in the planner. Configured local endpoints in a DST gap are skipped rather than silently shifted; ambiguous endpoints use the earlier offset, consistent with the existing task date-input convention. Work durations and breaks are measured in actual elapsed minutes. Availability bounds are resolved zoned intervals. Skipped windows may reduce capacity and produce unscheduled work.

## Sessions, breaks and daily limit

Defaults: 45-minute sessions, 10-minute breaks, 240 study minutes per local day. Sessions can be shorter: 120 minutes can split into 45+45+30; a 20-minute task stays 20 minutes. Sub-minute capacity cannot hold a whole work minute and is not rounded upward.

A minimum break separates all consecutive sessions, including different tasks. A natural gap between windows already counts as rest; if too short, only the missing gap delays the next start. Breaks do not count toward the daily study limit or scheduled workload. The daily limit is shared across every window that day and resets at local midnight. A limit of zero is supported and returns unscheduled work safely.

Sessions are emitted chronologically, never overlap, never start in the past and never end beyond their deadline or availability window. End exactly at deadline is allowed.

## Unscheduled reasons

Records expose required, scheduled and unscheduled minutes plus one primary reason:

- DEADLINE_ALREADY_PASSED: deadline is at or before generation time; it cannot be met by a future positive-duration session.
- INVALID_ESTIMATE: nonpositive remaining estimate; produces an attention record with zero work, never a zero-duration session. Overdue reason takes precedence if both apply.
- NO_AVAILABILITY: no usable future window exists in the horizon (including skipped DST windows or wholly past availability).
- DAILY_LIMIT_REACHED: an eligible task was blocked by an exhausted daily budget while window time remained. This is an observed constraint, not a claim that it is the only obstacle.
- PLANNING_HORIZON_REACHED: remaining work has a deadline beyond the horizon and was not already attributed to the daily limit.
- INSUFFICIENT_TIME_BEFORE_DEADLINE: remaining work cannot fit in the available capacity before its deadline, including competition with higher-ranked tasks and break overhead.

For positive pending estimates, scheduled + unscheduled equals requested workload. Completed tasks are excluded entirely. Totals use Long; individual requested estimates reuse the existing Int task field, preventing Int aggregate overflow. Input lists and configuration are not mutated.

## Tests

31 fixed-time JVM test cases cover the requested twenty behaviors plus next-day budget reset, horizon, close-window breaks, exact-now deadlines, zero daily budget, regeneration, priority/ID tie-breaking, titles/derived summaries, invalid config, duplicate IDs and DST gaps. Existing tests remain unchanged.

Exact final verification commands:

- `./gradlew testDebugUnitTest`: exit 1 before compilation. Gradle 9.5.0 download failed with `java.net.SocketException: Network is unreachable`.
- `./gradlew assembleDebug`: exit 1 before compilation at the same download.

Commands were retried; no successful Kotlin compilation, test execution or APK is claimed. Full logs accompany this report. Source review confirms pure planner imports, one existing RiskCalculator, no clock reads, no existing-source/dependency changes and no whitespace errors. Source checks are not compiler or test results.

## Commit, push and import

One focused commit is created as `v1.1: implement Smart Planner scheduling engine`. The final response supplies its hash. PUSH NOT PERFORMED: cloning a bundle sets origin to the local bundle path, not the original GitHub URL. Bundles do not carry repository remote configuration/authentication. No GitHub remote branch was queried or verified; imported branch refs only establish the supplied snapshot. No unrelated Git history was created and main was not modified.

To import the delivered smart-planner-pass-a.bundle into the same Windows repository:

1. Extract the delivery ZIP to a separate folder. Use your existing Android Studio project's terminal.
2. Run `git status --short` and ensure there are no uncommitted source changes. An untracked input .bundle file alone is not a source change; do not add it to the commit.
3. Run `git switch feature/smart-planner-ui`.
4. Run `git fetch "C:\path\to\smart-planner-pass-a.bundle" refs/heads/feature/smart-planner-ui` using the extracted file's actual path.
5. Run `git merge --ff-only FETCH_HEAD`. If this refuses due to divergent commits, stop and inspect; do not reset or force-push.
6. Run `.\gradlew.bat testDebugUnitTest`, then `.\gradlew.bat assembleDebug`.
7. After verification, `git push origin feature/smart-planner-ui` pushes through your existing configured GitHub remote. Confirm the commit on that GitHub branch.

The bundle carries the existing feature-branch ancestry plus the new commit; merging it preserves your project's Git configuration and main branch. Do not replace your project's .git directory. There are intentionally no visible app changes in Pass A. Smart Planner UI integration remains a later pass.
