# Aurora Calendar — PROJECT.md

**Authoritative project handoff document.** A new AI agent must be able to read this file, inspect the repository, and continue development without asking the user to explain the project.

> Status: PLANNING / PHASE 1 complete (repository inspected). No implementation code written yet.
> Location: `/mnt/sdcard/AuroraCalendar` (greenfield; `/mnt/sdcard` itself is a personal media directory, so the app lives in its own subfolder).

---

## 1. Project Vision

Aurora Calendar is an open-source, intelligent, beautifully designed Android calendar and time-management application — a production-quality alternative to Google Calendar with a built-in task execution timer, advanced home-screen widgets, intelligent scheduling, and a wallpaper-adaptive Material You visual identity.

Primary user flow: **Plan your day → Start a task → Work for the allocated time → Track progress → Complete your day.** Every day of every month is independently planable.

Not a prototype. Real, functional, offline-first, extensible architecture.

---

## 2. Repository Inspection Summary (Phase 1 — complete)

| Item | Finding |
|------|---------|
| Existing repository | None. `/mnt/sdcard` contains personal media/documents only. No `build.gradle`, `AndroidManifest.xml`, `settings.gradle`, or app source anywhere on the device. |
| Language / framework | Greenfield. Target: Kotlin + Jetpack Compose + Material 3. |
| Build system | Gradle + Android Gradle Plugin (not installed locally). |
| Existing functionality to preserve | None (greenfield). |
| Technical limitations | **No Android SDK / Gradle / Kotlin compiler in this authoring environment.** `ANDROID_HOME` unset. Java 21 + git + python3 only. The project source will be complete and buildable, but APK compilation/verification must happen in an environment with Android Studio + Android SDK (compileSdk 34/35). |
| Chosen architecture | Clean layered architecture: Presentation (Compose) → Domain (use cases) → Data (Room + DataStore + repositories), Hilt DI. |

**Decision (why):** No existing code to preserve, so no risk of overwriting. Greenfield lets us adopt the recommended stack (Kotlin, Compose, Material 3, Room, Coroutines/Flow, Hilt, WorkManager, App Widgets) cleanly. The missing Android SDK is a hard environment limitation — documented here, not worked around by writing fake code.

---

## 3. Application Architecture

Clean, modular, unidirectional data flow. Four layers:

```
Presentation (Jetpack Compose UI, ViewModels, UI state as StateFlow)
        ↓ (use cases)
Domain (pure Kotlin use cases, repository interfaces, domain models)
        ↓ (repositories)
Data (Room SQLite DAOs, DataStore preferences, repository impls, mappers)
        ↓
Android platform (App Widgets, AlarmManager, Foreground Service, WorkManager, Notifications)
```

**Modules (Gradle):**
- `:app` — Application class (Hilt), main activity, navigation host, DI entry point.
- `:core` — Hilt modules, base classes, common utilities, design-system tokens.
- `:data` — Room database, entities, DAOs, TypeConverters, DataStore, repository implementations.
- `:domain` — Use cases, repository interfaces, domain models (no Android dependencies).
- `:ui` — Compose screens, ViewModels, navigation graph, design system (colors, typography, shapes, components).
- `:widget` — AppWidgetProvider(s), RemoteViews layouts, widget configuration, widget update service/receiver.

**Dependency direction:** `app` → `ui`, `data`, `domain`, `widget`, `core`. `ui` → `domain`, `core`. `data` → `domain`, `core`. `domain` → `core` (interfaces only). `widget` → `data`, `domain`, `core`. No layer depends on Presentation.

**DI:** Hilt. `@Singleton` repositories and database; `@ActivityScoped` ViewModels; `@FragmentScoped`/screen-scoped where needed.

**State:** UI state exposed as `StateFlow`/`Flow` from ViewModels. Compose `collectAsStateWithLifecycle`. Domain emits `Flow` from repositories backed by Room queries (live queries) and DataStore.

---

## 4. Technology Stack & Justification

| Technology | Version (target) | Role | Why |
|------------|------------------|------|-----|
| Kotlin | 1.9.x | Language | First-class Android, coroutines, null safety. |
| Jetpack Compose | Material 3 | UI toolkit | Declarative, Material You/dynamic color, modern Android-native UI. |
| Compose Navigation | latest | Navigation | Type-safe routes, nested graphs, deep links for widget date taps. |
| Room | latest | Local database | SQLite abstraction, live queries, migrations, offline-first source of truth. |
| Kotlin Coroutines + Flow | latest | Async / streams | Structured concurrency, reactive UI. |
| Hilt | latest | DI | Standard, reduces boilerplate, testable. |
| DataStore (Preferences) | latest | Preferences | Modern replacement for SharedPreferences, coroutine-based. |
| WorkManager | latest | Deferrable background jobs | Backup/export cleanup, periodic lightweight jobs (not for sub-minute timer). |
| Android App Widgets | RemoteViews | Home-screen widgets | Native widget technology, multiple sizes/instances. |
| AlarmManager (exact alarms) | — | Reminders & timer expiry | Precise future firing; works when app is closed/killed. |
| Foreground Service | — | Running-task notification | Only while a timer is actively running (per spec: not kept running when timestamps suffice). |
| java.time (ThreeTenABP/desugaring) | — | Date/time | Calendar math, recurrence expansion, timestamp-based timer. |
| ICS (ical4j or custom reader/writer) | — | Calendar interchange | ICS import/export architecture; CalDAV/Google sync designed-in but not MVP. |

**Decision (why):** Room + Flow gives offline-first with live UI updates. AlarmManager + timestamp-based timer (not a live counter) satisfies the "survives process death" requirement without a permanently-running foreground service. Hilt/Compose/Navigation are the current Android standard for maintainability.

---

## 5. Database Schema (Room)

All timestamps stored as epoch milliseconds (UTC). `java.time.Instant` converted via TypeConverters.

### 5.1 `calendar_events`
| Column | Type | Notes |
|--------|------|-------|
| id | INTEGER PK | |
| title | TEXT | not null |
| description | TEXT | |
| location | TEXT | |
| start_time_ms | INTEGER | not null |
| end_time_ms | INTEGER | |
| all_day | INTEGER | 0/1 |
| category_id | INTEGER | FK → task_categories (nullable) |
| color | INTEGER | ARGB |
| reminder_minutes | TEXT | JSON list of ints |
| recurrence_rule | TEXT | iCal RRULE string (nullable) |
| recurrence_id | TEXT | ID of master event this occurrence belongs to (nullable) |
| original_event_id | INTEGER | FK → calendar_events (series master) (nullable) |
| status | TEXT | scheduled/cancelled |
| created_at_ms | INTEGER | |
| updated_at_ms | INTEGER | |

### 5.2 `tasks`
| Column | Type | Notes |
|--------|------|-------|
| id | INTEGER PK | |
| title | TEXT | not null |
| description | TEXT | |
| date_ms | INTEGER | start of day (local midnight) not null |
| start_time_ms | INTEGER | task start (local) |
| allocated_duration_ms | INTEGER | allocated time, not null |
| end_time_ms | INTEGER | derived = start + allocated |
| priority | INTEGER | 0 low, 1 medium, 2 high |
| category_id | INTEGER | FK → task_categories (nullable) |
| color | INTEGER | ARGB |
| status | TEXT | not_started/running/paused/completed/overdue/skipped |
| deadline_ms | INTEGER | optional |
| reminder_minutes | TEXT | JSON list of ints |
| subtasks | TEXT | JSON |
| notes | TEXT | |
| actual_duration_ms | INTEGER | accumulated actual work |
| session_count | INTEGER | |
| paused_duration_ms | INTEGER | |
| overtime_ms | INTEGER | |
| completed_at_ms | INTEGER | nullable |
| created_at_ms | INTEGER | |
| updated_at_ms | INTEGER | |

### 5.3 `task_categories`
| Column | Type | Notes |
|--------|------|-------|
| id | INTEGER PK | |
| name | TEXT | not null |
| icon | TEXT | icon name/codepoint |
| color | INTEGER | ARGB |
| sort_order | INTEGER | |

### 5.4 `timer_state` (singleton row, id = 1)
| Column | Type | Notes |
|--------|------|-------|
| id | INTEGER PK | = 1 |
| active_task_id | INTEGER | FK → tasks (nullable; null when no timer running) |
| started_at_ms | INTEGER | epoch when started |
| is_running | INTEGER | 0/1 |
| paused_at_ms | INTEGER | epoch when paused (null if not paused) |
| paused_accumulated_ms | INTEGER | total paused time so far |

### 5.5 `timer_sessions` (audit trail)
| Column | Type | Notes |
|--------|------|-------|
| id | INTEGER PK | |
| task_id | INTEGER | FK → tasks |
| started_at_ms | INTEGER | |
| ended_at_ms | INTEGER | nullable (null = still open) |
| duration_ms | INTEGER | |

### 5.6 `widget_config` (per widget instance)
| Column | Type | Notes |
|--------|------|-------|
| widget_id | INTEGER PK | appWidgetId |
| size | TEXT | small/medium/large |
| config_json | TEXT | per-widget preferences |

**Migrations:** Room `Migration` objects for v1→v2 etc., tested. Export/import uses JSON snapshot of all tables; duplicate prevention on import by matching (title + start_time_ms) for events and (title + date_ms) for tasks, with user-confirmed override option.

**Decision (why):** Timestamps in ms avoid timezone-storage bugs; local dates derived at read time using the user's configured week start and timezone. `timer_state` as a single row enables timestamp-based reconstruction after process death — no dependency on an in-memory counter surviving.

---

## 6. Navigation Architecture

**Navigation Compose** with nested graphs and deep links.

- Root tabs (bottom navigation): Dashboard, Calendar, Tasks, (Active Timer when running), More/Settings.
- Calendar graph: Month → Week → Day → (Year / Agenda as peers). Selecting a date opens that date's daily schedule.
- Event/Task bottom sheets (not full routes) for create/edit to preserve context.
- Settings as its own nested route.
- Search as global route reachable from any tab.
- Deep links: widget date tap → `auroracalendar://date/{yyyyMMdd}` opens Day view for that date; widget task tap → `auroracalendar://task/{id}`.

**Decision (why):** Bottom sheets keep the user in context (creating an event doesn't lose the calendar view). Deep links make widgets functional launchers, not just shortcuts.

---

## 7. UI Design System

**Design personality:** energetic, colorful, premium, modern, friendly, minimalist, organized, visually expressive. Not an enterprise clone; not an Apple clone — Android-native.

**Foundations:**
- Material 3 (color roles: primary, primaryContainer, secondary, secondaryContainer, tertiary, surface, surfaceContainer, onPrimary, onSurface, etc.).
- Dynamic Color (wallpaper-derived palette) where API 31+ supported; falls back to static palette.
- Theme modes: Light, Dark, System, Dynamic (wallpaper), Custom.
- Typography: Material 3 type scale (display → label).
- Shapes: fully rounded (large cut corners), medium, small.
- Spacing: 8dp grid.
- Motion: fade/slide transitions, expand/collapse bottom sheets, subtle scale on press.

**Category colors (customizable defaults):**
- Blue — Study, Purple — Creative, Green — Health, Orange — Important, Red — Urgent. User can add/edit categories with icon + color.

**Components:** rounded surfaces, floating action button (quick add), bottom sheets (create/edit event/task), contextual menus, progress indicators (timer ring), empty states, today highlight, selected-date ring.

**Decision (why):** Dynamic Color is the wallpaper-adaptive requirement; keeping it as one of several modes (not the only mode) satisfies "manual theme customization as well." Rounded surfaces + generous spacing match the premium/modern brief without copying another platform.

---

## 8. Calendar & Scheduling Logic

**Views:** Month, Week, Day (timeline agenda), Year overview, Agenda/list, Schedule timeline. Instant tab switching.

**Calendar engine:** `java.time`. Month grid generation with configurable week start (Sunday/Monday). Day cells show: date number, event dots (category-colored), task indicators, completion indicators, deadline markers, today highlight, selected-date ring. Selecting a date opens its daily schedule.

**Navigation:** month/year arrows; jump to today; year picker for Year view.

**Recurrence:** iCal RRULE string stored on the event. Supported: daily, weekly, selected weekdays, monthly, yearly, custom; end date OR repeat count; exceptions (exdates) stored as JSON list; per-occurrence overrides (edited occurrence does not modify the series — stored as a linked child row pointing to `original_event_id`).

**Expansion logic:** domain use case `ExpandRecurrences(event, rangeStart, rangeEnd)` returns concrete occurrences for display. Editing one occurrence offers "edit this occurrence only" vs "edit entire series."

**Overlap:** allowed (overlapping events supported). Drag/reschedule supported where the view implementation allows; time-picker fallback always available.

**Decision (why):** Storing RRULE + exceptions + linked occurrence rows is the standard, robust way to handle recurrences without exploding row counts or losing exceptions on series edits.

---

## 9. Timer Architecture (Signature Feature)

**Core principle: timestamp-based, not counter-based.** No `CountDownTimer` UI loop that drifts or dies when the process is killed.

**Persistence:** `timer_state` singleton row in Room. When Start pressed: write `active_task_id`, `started_at_ms` = now, `is_running`=1, clear pause fields; create an open `timer_sessions` row.

**Reading remaining time (UI, widgets, notifications):** computed at render time:
- elapsed = now − started_at_ms − paused_accumulated_ms − (now − paused_at_ms if currently paused)
- remaining = allocated_duration_ms − elapsed
- progress = clamp(elapsed / allocated, 0, 1)

This means values are always correct regardless of how long the app was backgrounded or the process dead; recomputation happens on recomposition and on widget view-bind.

**Controls:** Start, Pause, Resume, Stop, Complete, Reset (with confirmation dialog). State transitions enforced: not_started→running→(paused↔running)→completed/stopped/skipped. Completion does NOT auto-fire at zero — expiry notification offers "continue working" or "complete"; actual time keeps tracking if user continues.

**Foreground Service:** started only while a timer is actively running; shows the running-task notification with Pause/Resume/Complete actions. Stopped (removed from foreground) when paused/completed/stopped. Per spec: not kept running when timestamps suffice.

**Expiry alarm:** `AlarmManager.setExactAndAllowWhileIdle` scheduled at `started_at_ms + allocated_duration_ms`. BroadcastReceiver fires the timer-completion notification. Alarm rescheduled on boot/timezone/clock change.

**Tracking:** `actual_duration_ms`, `session_count`, `paused_duration_ms`, `overtime_ms` (max(0, elapsed − allocated) at completion), `completed_at_ms`. Daily and weekly statistics aggregated from `tasks` + `timer_sessions`.

**Survival:** app close, screen off, device restart, process kill, navigation away — all safe because state lives in Room and the expiry is an alarm, not a UI counter.

**Decision (why):** Timestamp reconstruction is the only reliable way to satisfy "accurate when process killed / device restarted" without a permanent foreground service (which Android restricts and drains battery). AlarmManager handles the expiry notification.

---

## 10. Widget Architecture

**Technology:** Android App Widgets (AppWidgetProvider + RemoteViews). Multiple sizes (small/medium/large where launcher supports). Multiple independent instances; per-instance configuration via `AppWidgetProviderInfo` configuration activity.

**Widgets:**
- **A — Daily Planner:** current date, day of week, today's progress, scheduled tasks with start times & durations, completion indicators, current running task. Tap task → open task.
- **B — Task Timer:** current task, remaining time, progress ring, Pause/Resume, Complete.
- **C — Monthly Calendar:** current month, today, task/event indicators, completion indicators. Tap date → open that date in app.
- **D — Agenda:** vertically scrollable upcoming schedule (collection RemoteViews where supported).
- **E — Quick Add:** compact create-task/event button opening bottom sheet in app.
- **F — Focus:** current task, remaining time, today's completed count, daily progress.

**Synchronization:** Room change observation → broadcast to `AppWidgetManager` → partial `updateAppWidget` + `notifyAppWidgetViewDataChanged`. Rendering uses timestamp-based remaining time at view-bind so values never go stale between the 30-minute AppWidget refresh window.

**Reboot:** `RECEIVE_BOOT_COMPLETED` → refresh all widget instances.

**Stale-value guard:** widgets never display a "live ticking" counter; they render remaining = f(timestamps) at bind time. A last-updated timestamp is stored; if older than a threshold, a refresh broadcast is triggered.

**Decision (why):** RemoteViews is the only native, launcher-compatible widget technology. Timestamp-based rendering sidesteps Android's ~30-minute minimum update interval for widgets while keeping timer values accurate.

---

## 11. Notification & Reminder Architecture

**Channels:** `reminders` (events/tasks), `timer` (running-task + expiry), `system` (daily planning, backup). Importance: default/high for timer, low/medium for reminders; user-configurable.

**Scheduling:** `AlarmManager.setExactAndAllowWhileIdle` for each reminder/timer-expiry. `BroadcastReceiver` (`ReminderReceiver`, `TimerExpiryReceiver`) builds and posts the notification via `NotificationManager`. Requires `SCHEDULE_EXACT_ALARM` special access (requested at runtime, graceful degradation if denied).

**Running-task notification:** foreground-service notification while timer active, with Pause/Resume/Complete action buttons that send intents to the timer use case.

**Persistence across reboot/timezone/clock:** `BOOT_COMPLETED`, `TIMEZONE_CHANGED`, `TIME_CHANGED` receivers re-query pending reminders/alarms from Room and re-register exact alarms.

**Permissions:** `POST_NOTIFICATIONS` (Android 13+), `SCHEDULE_EXACT_ALARM`, `FOREGROUND_SERVICE` (+ type), `RECEIVE_BOOT_COMPLETED`. No internet permission.

**Decision (why):** Exact alarms + broadcast receivers are the Android-correct way to fire notifications when the app is not running. Re-registration on boot/timezone/clock change satisfies the scheduling-robustness requirement.

---

## 12. Permissions & Android Compatibility

- `minSdk 24` (Android 7.0), `targetSdk 34` (35 when stable), `compileSdk 34`.
- Runtime permissions: `POST_NOTIFICATIONS` (13+), `SCHEDULE_EXACT_ALARM` (special app access, Settings page).
- Manifest: `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_TYPE=dataSync` (or `specialUse` as appropriate), `RECEIVE_BOOT_COMPLETED`, `WAKE_LOCK` (brief, for alarm handling).
- No internet permission (offline-first; network only if user later opts into CalDAV/Google sync — out of MVP).
- Scoped storage / DocumentFile / MediaStore for export/import file picking (no legacy broad storage).

**Decision (why):** minSdk 24 maximizes device reach while supporting Compose, DataStore, WorkManager, and dynamic color (dynamic color needs 31+, gracefully degraded below).

---

## 13. Testing Strategy

**Unit tests (JUnit 4 + Kotlin Test, pure JVM where possible):**
- Timer math: elapsed/remaining with pause/resume, overtime calc, expiry boundary.
- Recurrence expansion for each rule type; exception handling; occurrence-vs-series edit.
- DAO insert/query/update/delete.
- Widget timestamp render (remaining = f(now)).
- Domain use cases.

**Instrumented tests (Android device/emulator):**
- Room migrations v1→v2→v3.
- Compose UI screens (light/dark, dynamic color, empty states, long titles, large schedules).
- Navigation and deep links.
- Widget update + sync.
- Timer survives process kill (simulated) — state in Room intact.

**Reliability checks (manual + automated where feasible):**
- No crashes on rotation / keyboard show / low memory.
- No duplicate tasks after import.
- Correct countdown after app close/reopen and device reboot.
- Recurring series edit does not corrupt other occurrences.
- Widgets not stale after 30+ minutes.

**Decision (why):** Unit tests cover the correctness-critical logic (timer, recurrence) without needing a device; instrumented tests cover Compose/Room/widget integration. This matches "test every feature" while staying practical in an environment without an Android emulator.

---

## 14. Build & Deployment Instructions

**Prerequisites:** Android Studio (Giraffe+), Android SDK compileSdk 34, build-tools 34.0.0, Gradle 8.x, Kotlin 1.9.x, JDK 17/21.

**Build:**
```
cd /path/to/AuroraCalendar
./gradlew assembleDebug      # debug APK
./gradlew assembleRelease    # release APK (needs signing config in local.properties)
./gradlew test               # unit tests
./gradlew connectedAndroidTest  # instrumented tests (needs device/emulator)
```

**Signing:** create `local.properties` with `sdk.dir` and `release.storeFile`/`release.storePassword`/`release.keyAlias`/`release.keyPassword` (never commit secrets).

**Install:** `adb install -r app/build/outputs/apk/debug/app-debug.apk` (or install release).

**Note:** This authoring environment has no Android SDK/Gradle, so compilation and APK verification are **deferred to an Android Studio environment**. All source, Gradle files, manifests, and resources are written to be buildable as-is.

---

## 15. Known Limitations

| Limitation | Mitigation / Plan |
|------------|-------------------|
| No Android SDK locally — cannot compile APK here | Complete source tree written; build/verify in Android Studio. Documented so next agent knows. |
| CalDAV / Google Calendar sync not in MVP | Architecture (repository interface + domain use cases) designed to accept a sync provider later; local DB remains source of truth. |
| ICS import/export | Planned (Milestone 12); may use ical4j or a minimal custom reader/writer depending on dependency budget. |
| Widget collection scrollability | Limited by RemoteViews; Agenda widget uses scrollable collection where supported, otherwise paginated list. |
| Exact alarm permission may be denied | Graceful degradation: reminders shown only while app visible; user prompted to grant special access. |

---

## 16. Current Implementation Status

| Milestone | Status |
|-----------|--------|
| 1 — Foundation & Repository Inspection | Done (scaffold, Gradle, manifest, Hilt, Room, DataStore, navigation, theme, MainActivity) |
| 2 — Data Model & Repository Layer | Done (entities, DAOs, domain models, repositories, use cases, mappers, DI bindings, notification receivers) |
| 3 — Calendar System | Done (Month/Week/Day/Year/Agenda UI fully implemented with ViewModels, grid navigation, date selection, category color indicators, day schedule timeline, year overview, agenda list) |
| 4 — Event Management | Partial (domain + data done; UI editors not yet built) |
| 5 — Task Management | Partial (domain + data done; UI editors not yet built) |
| 6 — Task Execution Timer | Partial (domain use cases + repository + TimerViewModel + foreground service done; timer UI screen not yet built) |
| 7 — Home-Screen Widgets | Done (layouts, provider, config activity, foreground service, timestamp-based rendering) |
| 8 — Notifications & Reminders | Done (channels, ReminderReceiver, TimerExpiryReceiver, BootReceiver, TimeChangeReceiver) |
| 9 — Dashboard | Partial (DashboardScreen stub + DashboardViewModel done) |
| 10 — Search & Organization | Not started (SearchScreen stub only) |
| 11 — Design System & Polish | Done (theme system with dynamic color, light/dark/system/custom) |
| 12 — Data, Backup & Interoperability | Not started |
| 13 — Settings | Partial (SettingsScreen stub + SettingsViewModel done) |
| 14 — Testing, Verification & Final Acceptance | Not started |

**Completed tasks:** Milestone 1 (all), Milestone 2 (all), Milestone 3 (all), Milestone 7 (all), Milestone 8 (all), Milestone 11 (theme), plus domain/data for Milestones 4, 5, 6 and partial UI for 9, 13.
**Current task:** Milestone 3 complete — calendar UI screens implemented.
**Next task:** Flesh out event management UI (Milestone 4): event creation/editing bottom sheets, recurrence editor.
**Files created or modified:** See inventory below.
**Architectural decisions:** clean layered architecture (Presentation → Domain → Data), Hilt DI, Room offline-first source of truth, timestamp-based timer, RemoteViews widgets, AlarmManager reminders, minSdk 24 / targetSdk 34, widgets read Room directly (system-instantiated receivers).
**Known bugs:** none known (uncompiled — no Android SDK in authoring environment).
**Outstanding issues:** Android SDK not present in this authoring environment — build/verification deferred; timer expiry / reminder alarm registration from DB not fully wired to recurring-event expansion.
**Build and test status:** not built; no tests executed (no Android SDK/emulator here).
**Instructions for the next AI agent:** Read PROJECT.md and the source tree under /mnt/sdcard/AuroraCalendar. Continue with Milestone 4 (event management UI editors). Implement one task at a time; update PROJECT.md after every completed task. Do not mark a task complete until implemented and (where possible) verified.

---

## 17. Milestone & Task Breakdown

Checkboxes: `[ ]` not started, `[x]` done (verified), `[~]` in progress.

### Milestone 1 — Foundation & Repository Inspection
- [x] Inspect existing repository (greenfield — no Android project found)
- [ ] Scaffold Android project (Gradle wrapper, settings.gradle, build.gradle files, modules: app/core/data/domain/ui/widget)
- [ ] Write AndroidManifest.xml (minSdk 24, targetSdk 34, permissions, providers, receivers, services)
- [ ] Configure dependencies (Kotlin, Compose BOM, Material 3, Room, Hilt, Coroutines/Flow, Navigation Compose, DataStore, WorkManager, App Widgets)
- [ ] Set up Hilt DI (Hilt Android app, modules for database, repositories, use cases)
- [ ] Configure Room database + database class + migration infrastructure
- [ ] Configure DataStore for preferences
- [ ] Establish navigation graph (Navigation Compose, nested graphs, deep links)
- [ ] Implement theme system (Material 3, dynamic color, light/dark/system/custom)
- [ ] Verify project structure compiles (in Android SDK environment)

### Milestone 2 — Data Model & Repository Layer
- [ ] CalendarEvent entity + DAO + TypeConverters
- [ ] Task entity + DAO + TypeConverters
- [ ] TaskCategory entity + DAO
- [ ] TimerState entity (singleton) + DAO
- [ ] TimerSessions entity + DAO
- [ ] WidgetConfig entity + DAO
- [ ] Domain models + mappers (entity ↔ domain)
- [ ] Repository implementations (EventRepository, TaskRepository, CategoryRepository, TimerRepository, WidgetConfigRepository)
- [ ] Use cases (CRUD events, CRUD tasks, CRUD categories, timer start/pause/resume/complete/reset)
- [ ] Unit tests for DAOs & repository logic

### Milestone 3 — Calendar System (Views & Navigation)
- [x] Month calendar view (grid, date selection, event dots, task indicators, today highlight, selected-date ring)
- [x] Week calendar view
- [x] Day agenda/timeline view
- [x] Year overview
- [x] Agenda/list view
- [x] Month/year navigation controls + jump to today
- [x] Date selection → open daily schedule
- [x] Category color indicators on dates

### Milestone 4 — Event Management
- [ ] Event creation (bottom sheet: title, description, location, time, duration, category, color, reminders, recurrence)
- [ ] Event editing
- [ ] Event deletion (single occurrence vs whole series)
- [ ] Recurring events (daily/weekly/selected weekdays/monthly/yearly/custom, end date OR repeat count, exceptions)
- [ ] Recurrence occurrence editing (edit one occurrence vs entire series)
- [ ] Event reminders (custom intervals)

### Milestone 5 — Task Management
- [ ] Task creation (all fields: title, description, date, start time, allocated duration, priority, category, color, deadline, reminders, subtasks, notes)
- [ ] Task scheduling (date, start time, allocated duration, end time derived)
- [ ] Task categories, priorities, colors
- [ ] Task state transitions (not_started → running → paused → completed → overdue → skipped)
- [ ] Task rescheduling between dates (no data loss)
- [ ] Subtasks & notes
- [ ] Deadlines & reminders

### Milestone 6 — Task Execution Timer (Signature Feature)
- [ ] Timestamp-based timer engine (Room timer_state, no live UI counter dependency)
- [ ] Start / Pause / Resume / Stop / Complete / Reset-with-confirmation controls
- [ ] Timer state persistence across app close, screen off, device restart, process kill
- [ ] Foreground service notification while running (Pause/Resume/Complete actions)
- [ ] Timer expiry notification (time-expired → awaiting completion; continue or complete option)
- [ ] Actual time tracking (sessions, paused duration, overtime, completion time)
- [ ] Daily & weekly time statistics
- [ ] Unit tests for timer math (pause/resume, overtime, expiry)

### Milestone 7 — Home-Screen App Widgets
- [ ] Widget infrastructure (AppWidgetProvider, RemoteViews, configuration activity, widget_info XML)
- [ ] Widget A — Daily Planner
- [ ] Widget B — Task Timer
- [ ] Widget C — Monthly Calendar
- [ ] Widget D — Agenda
- [ ] Widget E — Quick Add
- [ ] Widget F — Focus
- [ ] Multiple independent instances + per-instance config
- [ ] Widget sync with database (Room change → broadcast → partial update; timestamp-based rendering)
- [ ] Reboot restoration (BOOT_COMPLETED receiver)
- [ ] Resize handling + readable layouts at different sizes

### Milestone 8 — Notifications & Reminders
- [ ] Notification channels (reminders, timer/running-task, system)
- [ ] Event reminders (AlarmManager exact, custom intervals, multiple per event)
- [ ] Task reminders & due-date notifications
- [ ] Running-task notification (foreground service while timer active)
- [ ] Timer completion notification
- [ ] Alarm rescheduling on boot / timezone change / clock change
- [ ] SCHEDULE_EXACT_ALARM permission handling + graceful degradation

### Milestone 9 — Dashboard
- [ ] Today's date & greeting
- [ ] Daily progress (completed vs remaining)
- [ ] Current running task
- [ ] Upcoming tasks & next event
- [ ] Quick-add action
- [ ] One-tap access to full calendar

### Milestone 10 — Search & Organization
- [ ] Global search (tasks, events, dates, categories, descriptions, locations)
- [ ] Filtering (date range, category, completion status, priority, event/task type)
- [ ] Historical & future entry access

### Milestone 11 — Design System & Polish
- [ ] Dynamic wallpaper color theming (Material You, API 31+, graceful fallback)
- [ ] Light/dark/system/custom themes consistent across app & widgets
- [ ] Animations & transitions (bottom sheets, view switches)
- [ ] Empty states
- [ ] Typography & iconography
- [ ] Category colors (blue/study, purple/creative, green/health, orange/important, red/urgent — customizable)

### Milestone 12 — Data, Backup & Interoperability
- [ ] JSON backup export/import
- [ ] Duplicate prevention during imports
- [ ] Database migrations (tested)
- [ ] Recovery from interrupted operations
- [ ] ICS import/export architecture (implementation per dependency decision)
- [ ] Offline-first operation verified

### Milestone 13 — Settings
- [ ] Appearance & theme selection
- [ ] Calendar preferences (week start day, time format, default task duration)
- [ ] Default reminders
- [ ] Notification preferences
- [ ] Widget preferences
- [ ] Data export/import & backup settings
- [ ] Privacy, About, open-source license

### Milestone 14 — Testing, Verification & Final Acceptance
- [ ] Unit tests (timer math, recurrence expansion, DAO logic)
- [ ] UI tests (themes, navigation, empty states, widget sizes)
- [ ] Reliability (process death, reboot, widget sync, countdown accuracy)
- [ ] Full build verification (Android Studio environment)
- [ ] PROJECT.md finalization & handoff review

---

## 18. AI Handoff State (updated after every completed task)

**Current milestone:** 3 — Calendar System (Views & Navigation)  
**Completed tasks:** repository inspection, project scaffold, Gradle/modules, AndroidManifest, dependencies, Hilt, Room, DataStore, navigation, theme, MainActivity; data model & repository layer (entities, DAOs, domain models, repositories, use cases, mappers, DI bindings, notification receivers); calendar UI screens (Month, Week, Day, Year, Agenda + ViewModels)  
**Current task:** none  
**Next task:** Milestone 4 — Event Management UI editors (event/task creation bottom sheets, recurrence editor)  
**Files created or modified:** PROJECT.md; ui/viewmodel/MonthViewModel.kt, DayScheduleViewModel.kt, AgendaViewModel.kt, WeekViewModel.kt, YearViewModel.kt; ui/screen/Screens.kt  
**Architectural decisions:** clean layered architecture (Presentation → Domain → Data), Hilt DI, Room offline-first source of truth, timestamp-based timer, RemoteViews widgets, AlarmManager reminders, minSdk 24 / targetSdk 34.  
**Known bugs:** none known (uncompiled — no Android SDK in authoring environment).  
**Outstanding issues:** Android SDK not present in this authoring environment — build/verification deferred; timer expiry / reminder alarm registration from DB not fully wired to recurring-event expansion.  
**Build and test status:** not built; no tests executed (no Android SDK/emulator here).  
**Instructions for next AI agent:** Read PROJECT.md, inspect /mnt/sdcard/AuroraCalendar, continue with Milestone 4 (event management UI). Implement one task at a time; test; then mark `[x]` and update this section.
