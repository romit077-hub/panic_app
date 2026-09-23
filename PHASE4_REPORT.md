# Phase 4 — deterministic deadline risk and PANIC Mode

Source implementation complete. Android compilation, unit execution and device verification are pending.
The scores are explainable urgency indicators, not probabilities, machine learning or an LLM feature.
No Phase 5 work was started.

## Git state and verification gate

Before changes the working tree was clean, with these local commits:

- ed608d4 — Phase 3: implement persistent task management with Room
- 3be7ddc — Phase 2: PANIC UI foundation and navigation

No remote is configured in this checkout. Nothing was pushed to GitHub.
Phase 3 history is untouched. Its earlier build was NOT verified; the Phase 4
baseline build reproduced the same external Gradle download failure.

Your Phase 4 instructions require successful verification before creating its commit.
That condition has not been met. NO Phase 4 commit was created. The ZIP contains
all source changes plus phase4.patch and the complete Phase 2/3 Git history bundle,
so the work and checkpoint remain recoverable without claiming verification.

## Apply to your EXISTING project

Back up local changes first. Keep your Android Studio folder, application ID,
local.properties, and any existing Git history.

Preferred method for a matching Phase 3 checkout, from its root:

```powershell
git status --short
git apply --check "C:\path\to\extracted\phase4.patch"
git apply "C:\path\to\extracted\phase4.patch"
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

If the patch check fails, stop and inspect differences; do not force or overwrite
local work. Alternatively copy only the new/modified source files listed below.
When copying manually, also remove the obsolete model/DemoTask.kt file. The source
folder in this ZIP is a complete updated copy of the same panic_app project.

After tests/build and an Android launch check succeed locally, create the requested
separate commit (review the diff and stage only Phase 4 changes):

```powershell
git diff --check
git add <reviewed-phase-4-files>
git commit -m "Phase 4: implement deadline risk engine and PANIC mode"
```

The history bundle contains only the preserved Phase 2/3 commits, not a fabricated
Phase 4 commit. Do not overwrite your local repository with it.

## Exact formula

For an incomplete task with a future deadline:

    availableMinutes = (deadlineMillis - nowMillis) / 60000.0
    effectiveEstimate = max(1, estimatedMinutes)
    workRatio = effectiveEstimate / availableMinutes
    baseScore = clamp(timePoints + workloadPoints + priorityPoints, 0, 100)
    score = max(baseScore, 90) if workRatio >= 1; otherwise baseScore

Timestamp subtraction saturates on Long overflow while preserving its sign.
Sub-minute differences stay fractional, preventing division by zero or premature
overdue classification. Nonpositive estimates use 1 minute and the explanation
explicitly asks the user to update the estimate. The form still rejects invalid estimates.

Time contribution:

| Remaining time | Points |
| --- | ---: |
| More than 7 days | 5 |
| More than 3 days, up to 7 days | 10 |
| More than 24 hours, up to 3 days | 20 |
| More than 12 hours, up to 24 hours | 35 |
| More than 6 hours, up to 12 hours | 45 |
| More than 2 hours, up to 6 hours | 60 |
| More than zero, up to 2 hours | 85 |

Workload contribution:

| Estimated work / available time | Points |
| --- | ---: |
| Up to 10% | 0 |
| More than 10%, up to 25% | 5 |
| More than 25%, up to 50% | 15 |
| More than 50%, up to 75% | 25 |
| More than 75%, less than 100% | 40 |
| 100% or more | 50, with a minimum final score of 90 |

Priority adds LOW = 0, MEDIUM = 5, HIGH = 10.

These weights are a documented design heuristic, not empirically calibrated.
Deadline proximity is the strongest signal near expiry: even low-priority work
within two hours is critical. Workload adds urgency as scheduling slack disappears.
The capacity floor prevents a long but impossible workload from looking safe merely
because its deadline is distant. Priority is capped at 10 points so it cannot make
an ordinary next-month task critical by itself.

Examples with LOW priority:

| Remaining time | Work estimate | Score | Classification |
| --- | --- | ---: | --- |
| 10 days | 1 hour | 5 | SAFE |
| 20 hours | 2 hours | 35 | WARNING |
| 10 hours | 9 hours | 85 | CRITICAL |
| 2 days | 3 days | 90 | CRITICAL (capacity floor) |
| 90 minutes | 1 minute | 85 | CRITICAL |

## Levels and overrides

Boundaries live in the domain RiskLevel definition:
SAFE 0–30; WARNING 31–60; HIGH 61–80; CRITICAL 81–100.

- Completed: score 0, SAFE, inactive, not overdue, no attention recommendation.
- Incomplete and deadline < now: score 100, CRITICAL, overdue true.
- Incomplete and deadline == now: score 100, CRITICAL, overdue false (due now).

Structured RiskResult includes score, level, isOverdue, isActive, remainingMillis,
reason, recommendedAction, component points and whether the capacity floor raised
the score. All explanations and recommendations are deterministic.

## Architecture and ordering

The domain/risk files import only standard Kotlin/JVM APIs and the existing pure
TaskPriority enum. They have no Compose, Android, Room, Activity or ViewModel dependency.
RiskCalculator takes explicit nowMillis. It never reads the real clock.

TaskRiskState maps the existing Room entity list into pure risk inputs. TasksViewModel
publishes one atomic snapshot of tasks, results, ordering and calculation time.
The risk map and sorted IDs are derived presentation data, not another mutable task store.
No risk columns, schema version change, migration or database reordering was added.

Pending tasks sort by:
1. Overdue first.
2. Higher score first.
3. Earlier deadline first.
4. Smaller task ID as a deterministic tie-break.

Completed tasks never enter the urgency ranking. The original input list is not mutated.

## Screen integration

- Tasks: pending cards show real badges, scores and time remaining, with existing CRUD
  actions preserved. Pending/All filters show pending work in urgency order. Completed
  cards retain completion styling and do not show urgency badges.
- Dashboard: real pending/completed counts, HIGH-only count, CRITICAL-only count,
  highest-risk attention card with explanation/action, nearest deadline, and real
  PANIC counts. Empty states do not invent tasks.
- PANIC Mode: shows WARNING/HIGH/CRITICAL pending tasks in ranked order. Each card
  shows rank, title, subject, deadline, remaining time, work estimate, score, level,
  explanation, contribution breakdown and recommendation. SAFE and completed tasks
  are excluded. An under-control state appears when no task needs attention.
- 'Need planning or action' counts WARNING + HIGH + CRITICAL. 'Need immediate attention'
  counts HIGH + CRITICAL. Overdue is a subset of CRITICAL, never an extra task count.
- Calendar: now reads the same real snapshot, removing its old simulated task risk badges.
  It remains a simple date-strip and read-only deadline view with an edit link.
- Analytics: remains a clearly labelled non-risk design preview. Its sample risk
  distribution was removed; no analytics engine was added.

The obsolete demo task model, scores and component overloads were removed. The shared
risk colours remain centralized and unchanged; only their enum import moved to domain.

## Live time updates

Risk is recomputed on every Room list emission after create, edit, completion or delete.
A lifecycle-scoped coroutine refreshes time immediately when the app becomes visible
and every 30 seconds while STARTED. It cancels when the app is stopped and restarts
on foregrounding. All relevant screens consume the same timestamped snapshot.

This is an in-app refresh, not background enforcement. No alarms, notifications,
WorkManager, networking, cloud services or new permissions/dependencies were added.
Clock changes are reflected on the next refresh, at most approximately 30 seconds
while visible (subject to normal scheduling delays). This is not a second-by-second clock.

Lifecycle reference: https://developer.android.com/topic/libraries/architecture/coroutines

## Tests created

31 new JUnit tests in four files:

- RiskCalculatorTest: 21 tests covering completed/overdue/due-now states, distant and
  imminent deadlines, workload pressure, bounded priority, score ranges, exact band
  boundaries, zero/negative/extreme estimates, timestamp overflow, sub-minute time,
  monotonicity as work/time pressure increases, and deterministic explanations.
- RiskRankingTest: 4 tests for overdue ordering, scores before proximity,
  deadline/ID tie-breaks, no input mutation and completed-task exclusion.
- RiskTimeTest: 3 tests for readable durations, overdue/due-now/sub-minute formatting
  and Long.MIN_VALUE magnitude handling.
- TaskRiskStateTest: 3 tests for completed/deleted task removal from attention,
  elapsed-time updates without database mutation, and edit-driven ranking/explanations.

Every test supplies fixed timestamps. No test depends on the real clock.
The existing Phase 3 tests were left unchanged.

## Commands and exact results

Baseline: ./gradlew assembleDebug — exit 1 before compilation.
Unit tests: ./gradlew testDebugUnitTest — exit 1 before test execution.
Final build: ./gradlew assembleDebug — exit 1 before compilation.

Each stops while downloading Gradle 9.5.0 with:

    java.net.SocketException: Network is unreachable

See PHASE4_TEST_LOG.txt and PHASE4_BUILD_LOG.txt. The distribution cache has no usable
Gradle installation, and no standalone Kotlin compiler was found in the inspected
system/tool caches. Existing JDK/Android SDK limitations remain. Dependency versions
were not changed to hide this external failure. No APK or generated schema is claimed.

Checks that DID run:
- Confirmed clean initial Git state and intact Phase 3 commit.
- Compared build files/catalog, manifest, task entity, DAO, database and repository
  against Phase 3: unchanged byte-for-byte.
- Parsed XML/TOML; checked internal source imports.
- Checked pure-domain dependency and clock boundaries.
- Confirmed no remaining DemoData/DemoTask source references.
- Ran a lightweight string-literal check and git diff --check successfully.

These checks do NOT replace Kotlin compilation or running the tests. No test pass,
Compose rendering, device launch or compiler-warning-free claim is made.

## Limitations for the viva and review

The score uses wall-clock time, not a student's actual free hours. It does not know
sleep, classes, parallel workload, progress or estimation uncertainty. Estimates are
treated as remaining work and should be revised manually. Stepwise bands create
intentional jumps at thresholds. A score of 85 does not mean an 85% chance of failure.
The metric is bounded, transparent and repeatable, but not validated against outcomes.

## Device checks after a successful local build

1. Create a LOW task due in ten days with a one-hour estimate: SAFE, score 5.
2. Change it to ten hours away with nine hours of work: CRITICAL, score 85.
3. Move it to the past: score 100 and explicit overdue reason.
4. Complete it: it disappears from PANIC and has no urgent card badge.
5. Restore it, then delete it: counts update immediately after each write.
6. Check equal scores sort by deadline and ID; check safe-only and empty databases.
7. Leave the app in the background past a deadline, return, and check risk refresh.
8. Inspect remaining-time labels after 30 seconds while visible.
9. Check dark mode, long explanations, small screens, large fonts and navigation.

## Files created

- PHASE4_BUILD_LOG.txt
- PHASE4_TEST_LOG.txt
- app/src/main/java/com/example/panic_app/domain/risk/RiskCalculator.kt
- app/src/main/java/com/example/panic_app/domain/risk/RiskLevel.kt
- app/src/main/java/com/example/panic_app/domain/risk/RiskRanking.kt
- app/src/main/java/com/example/panic_app/domain/risk/RiskResult.kt
- app/src/main/java/com/example/panic_app/domain/risk/RiskTime.kt
- app/src/main/java/com/example/panic_app/ui/components/RiskSummary.kt
- app/src/main/java/com/example/panic_app/ui/screens/tasks/TaskRiskState.kt
- app/src/test/java/com/example/panic_app/domain/risk/RiskCalculatorTest.kt
- app/src/test/java/com/example/panic_app/domain/risk/RiskRankingTest.kt
- app/src/test/java/com/example/panic_app/domain/risk/RiskTimeTest.kt
- app/src/test/java/com/example/panic_app/ui/screens/tasks/TaskRiskStateTest.kt
- PHASE4_REPORT.md

## Files modified

- app/src/main/java/com/example/panic_app/ui/PanicApp.kt
- app/src/main/java/com/example/panic_app/ui/components/PanicComponents.kt
- app/src/main/java/com/example/panic_app/ui/components/RealTaskComponents.kt
- app/src/main/java/com/example/panic_app/ui/screens/analytics/AnalyticsScreen.kt
- app/src/main/java/com/example/panic_app/ui/screens/calendar/CalendarScreen.kt
- app/src/main/java/com/example/panic_app/ui/screens/dashboard/DashboardScreen.kt
- app/src/main/java/com/example/panic_app/ui/screens/panic/PanicScreen.kt
- app/src/main/java/com/example/panic_app/ui/screens/tasks/TasksScreen.kt
- app/src/main/java/com/example/panic_app/ui/screens/tasks/TasksViewModel.kt
- app/src/main/java/com/example/panic_app/ui/theme/Color.kt

## Files removed

- app/src/main/java/com/example/panic_app/model/DemoTask.kt
