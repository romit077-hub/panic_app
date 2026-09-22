# PANIC — Phase 2 foundation

Source implementation is delivered. Build and launch verification are pending.

## Apply to the existing project

Back up your current panic_app folder. Extract this archive to a temporary location,
then copy the changed/new files below into your EXISTING panic_app folder using the
same relative paths. Keep your existing local.properties, .idea, and machine setup.
Do not create or rename an Android project. Sync Gradle and run the app.

Windows terminal from the project root:

```powershell
.\gradlew.bat assembleDebug
```

Linux/macOS:

```sh
./gradlew assembleDebug
```

After a successful build, the debug APK is app/build/outputs/apk/debug/app-debug.apk.
No APK is included because this environment could not complete the build.

## Preserved configuration

- Project: panic_app
- Namespace/application ID: com.example.panic_app
- minSdk 26; compileSdk/targetSdk 37
- AGP 9.3.3; Gradle wrapper 9.5.0 and original distribution checksum
- Kotlin Compose compiler plugin 2.2.10; Compose BOM 2026.02.01
- Java source/target 11; original Gradle daemon toolchain 25
- Existing manifest, resources, tests, repositories, and plugin configuration

## Dependencies added

- androidx.navigation:navigation-compose:2.9.5
- androidx.compose.material:material-icons-core:1.7.8

Existing version declarations are unchanged. These two dependencies are declared
through the existing version catalog. There is no full extended icon pack.

## Files modified

- app/build.gradle.kts
- gradle/libs.versions.toml
- app/src/main/java/com/example/panic_app/MainActivity.kt
- app/src/main/java/com/example/panic_app/ui/theme/Color.kt
- app/src/main/java/com/example/panic_app/ui/theme/Theme.kt
- app/src/main/java/com/example/panic_app/ui/theme/Type.kt

The Gradle wrapper script is marked executable for Linux/macOS; its contents are unchanged.

## Kotlin files created

All paths below are relative to app/src/main/java/com/example/panic_app/:

- model/DemoTask.kt
- ui/PanicApp.kt
- ui/navigation/PanicDestination.kt
- ui/components/PanicComponents.kt
- ui/screens/dashboard/DashboardScreen.kt
- ui/screens/tasks/TasksScreen.kt
- ui/screens/calendar/CalendarScreen.kt
- ui/screens/panic/PanicScreen.kt
- ui/screens/analytics/AnalyticsScreen.kt
- ui/screens/settings/SettingsScreen.kt

This README and PHASE2_BUILD_LOG.txt are also new.

## Behaviour

Dashboard starts the application. Dashboard, Tasks, Calendar, and Panic are primary
bottom tabs. Analytics opens from Dashboard. Settings opens from Dashboard or the
app header. Secondary screens have a Back action and hide the bottom bar.
Navigation uses central enum routes, singleTop, and primary destination save/restore.

Shared components: ScreenList, ScreenHeading, DemoNotice, Panel, SectionHeader,
RiskBadge, StatCard, TaskCard, DeadlineCard.

Light/dark indigo colour schemes, centralized semantic risk colours, rounded cards,
typography, scrollable screens, scaffold insets, and theme-aware system-bar icons.
The theme starts with the system preference; Dark mode overrides it for the current
saved UI state. Other settings are preview preferences only; nothing is scheduled.

Tasks can be filtered between pending, completed, and all fixture tasks. Calendar
has a selectable seven-day strip and an upcoming list. Add Task opens an explanatory
dialog with a link to sample tasks. No CRUD is presented as working functionality.

Counts are consistent across screens: 13 sample tasks, 5 pending, 8 complete,
2 critical, 1 overdue. Three sample tasks appear in Panic mode, ordered by their
supplied illustrative scores. These are NOT calculated risk predictions.
Fixture dates are anchored when the process loads. This is not a live countdown.
The dashboard focus list is a suggested demo study list, not a claim that every
item is due today. Analytics completion is 62% from 8/13 tasks.

No database, persistence service, ViewModel dependency, networking, notifications,
alarm service, risk engine, or new permissions were added. Settings use hoisted
rememberSaveable UI state, which is not durable user-preference storage.

## Verification performed and limitations

The baseline build and post-change assembleDebug attempts stopped while downloading
Gradle 9.5.0 with java.net.SocketException: Network is unreachable, BEFORE compiling.
The cache contains no usable Gradle distribution. This environment has JDK 17,
not the requested daemon JDK 25, and no detected Android SDK installation.
The project versions were not changed to work around the environment.
See PHASE2_BUILD_LOG.txt for the final command output.

XML/TOML parsing and preservation of every existing catalog version were checked.
Source and route wiring were reviewed. Compilation, emulator/device launch,
visual rendering and interaction checks have NOT been verified here.

## Device checks after a successful local build

1. Launch: Dashboard should appear with labelled sample content.
2. Tap all four tabs repeatedly; selected state should match the destination.
3. On Tasks choose Completed, change tabs, then return; filter should be restored.
4. Open Settings, switch Dark mode, go Back: theme and system-bar contrast should update.
5. Rotate the device: temporary settings and navigation should retain saved state.
6. Select Calendar dates: the chosen date and corresponding sample deadlines should update.
7. Open Analytics from Dashboard, then Back: Dashboard should return.
8. Tap Add Task: explanatory dialog opens; no task is added.
9. Check a small screen and larger font setting for text wrapping, scrolling, and bar overlap.

Resolve any actual local build/launch failure before starting Phase 3.
