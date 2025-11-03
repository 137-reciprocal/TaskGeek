# TaskHero - Gamified Task Management for Android

<div align="center">

**Transform Productivity into an Epic Adventure**

[![Android](https://img.shields.io/badge/Android-16+-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Compose-BOM%202025.01-brightgreen)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material%203-Dynamic%20Colors-purple)](https://m3.material.io)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2B%20MVI-orange)](https://developer.android.com/topic/architecture)

</div>

---

## 📖 Overview

TaskHero is a native Android application that combines the powerful task management features of **Taskwarrior 3.x** with **RPG-style gamification**. Complete tasks to earn XP, level up your hero, and unlock achievements while maintaining a robust, feature-complete task management system.

### Key Features

🎯 **Complete Taskwarrior 3.x Implementation**
- Full task CRUD with UUID support
- Hierarchical projects (dot notation)
- Tags with many-to-many relationships
- Task dependencies (blocking/blocked)
- Priority levels (H, M, L)
- Annotations (timestamped notes)
- User-Defined Attributes (UDAs)
- 14-coefficient urgency calculation
- Advanced filtering and querying
- Date expression parser (today, +3d, eom, etc.)
- Recurring tasks (ISO-8601)

🎮 **RPG Gamification Layer**
- Hero character with customizable avatar
- Level progression (1-100) with polynomial XP curve
- D&D-style stat system (STR, DEX, CON, INT, WIS, CHA)
- Title/achievement system
- XP rewards based on task urgency and completion
- Streak tracking
- XP history log

📊 **Reports & Analytics**
- Burndown charts (Vico)
- Calendar view with task visualization
- Comprehensive statistics
- Completion rate tracking

🔔 **Smart Notifications**
- Due date reminders
- Daily task summaries
- Level-up celebrations
- WorkManager integration

⚙️ **Customization**
- Material 3 with dynamic colors (Material You)
- Dark/Light/System theme modes
- Configurable urgency coefficients
- Adaptive layouts (phone/tablet/foldable)

---

## 🏗️ Architecture

TaskHero follows **Clean Architecture** principles with **MVI (Model-View-Intent)** pattern for presentation layer.

```
┌─────────────────────────────────────────────────────┐
│                  Presentation Layer                 │
│  (Jetpack Compose + Material 3 + MVI ViewModels)   │
└────────────────────┬────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────┐
│                   Domain Layer                      │
│     (Pure Kotlin: Models, Use Cases, Interfaces)   │
└────────────────────┬────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────┐
│                   Data Layer                        │
│  (Room Database + DataStore + Repository Impls)    │
└─────────────────────────────────────────────────────┘
```

### Multi-Module Structure

```
app/                          # Main application entry point
├── feature/                  # Feature modules (UI)
│   ├── tasklist/            # Task list with filtering/sorting
│   ├── taskdetail/          # Task editor with all fields
│   ├── hero/                # Hero profile and stats
│   ├── reports/             # Burndown charts & analytics
│   └── settings/            # App configuration
├── domain/                   # Business logic
│   ├── task/                # Task domain models & use cases
│   ├── hero/                # Hero domain models & use cases
│   └── report/              # Report domain models & use cases
├── data/                     # Data layer implementations
│   ├── task/                # Task repository implementation
│   ├── hero/                # Hero repository implementation
│   └── preferences/         # Settings repository (DataStore)
├── core/                     # Shared infrastructure
│   ├── database/            # Room database & DAOs
│   ├── datastore/           # DataStore configuration
│   ├── ui/                  # Theme, components, styles
│   ├── common/              # Utilities, extensions
│   ├── parser/              # Date/filter/recurrence parsers
│   └── notifications/       # Notification system
└── widget/                   # Glance widgets
```

---

## 🛠️ Tech Stack

| Category | Technology |
|----------|-----------|
| **Language** | Kotlin 2.0.21 |
| **UI Framework** | Jetpack Compose (BOM 2025.01) |
| **Design System** | Material Design 3 (Dynamic Colors) |
| **Architecture** | Clean Architecture + MVI |
| **DI** | Hilt 2.52 |
| **Database** | Room 2.7.0-alpha12 |
| **Preferences** | DataStore 1.1.1 |
| **Async** | Kotlin Coroutines + Flow |
| **Navigation** | Navigation Compose 2.8.5 |
| **Charts** | Vico 2.0.0-alpha.28 |
| **Background Tasks** | WorkManager 2.10.0 |
| **Widgets** | Glance 1.1.1 |
| **Serialization** | kotlinx.serialization 1.8.0 |
| **Date/Time** | kotlinx.datetime 0.6.1 |
| **Build Tool** | Gradle 8.11.1 + AGP 8.7.3 |

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Ladybug or later (2024.2+)
- **JDK**: 17 or higher
- **Android SDK**: API 36 (Android 16)
- **Gradle**: 8.11.1 (auto-downloaded via wrapper)

### Building the Project

```bash
# Clone the repository
git clone https://github.com/yourusername/TaskHero.git
cd TaskHero

# Build the project
./gradlew build

# Run on connected device/emulator
./gradlew installDebug

# Run tests
./gradlew test
./gradlew connectedAndroidTest
```

### Project Configuration

The project uses Gradle version catalogs (`gradle/libs.versions.toml`) for dependency management.

---

## 📱 Features in Detail

### Task Management

- **Quick Add**: FAB for rapid task creation
- **Detailed Editor**: Full-featured task editor with all Taskwarrior fields
- **Filtering**: Complex query builder with AND/OR logic
- **Sorting**: Multiple sort options (urgency, due date, project, etc.)
- **Search**: Real-time search across all task fields
- **Tags**: Visual tag chips with easy add/remove
- **Dependencies**: Visual dependency graph (tasks that block others)
- **Annotations**: Timestamped notes on tasks
- **Recurring Tasks**: ISO-8601 recurrence patterns

### Hero System

- **Character Profile**: Avatar, name, class, title
- **Leveling**: XP-based progression with polynomial curve
- **Stats**: D&D-style attributes with modifiers
- **Titles**: Unlock titles at milestone levels
- **Achievements**: Track tasks completed and streaks
- **XP History**: See XP earned from each completed task

### Reports

- **Burndown Chart**: Visualize task completion over time
- **Calendar View**: See tasks by due date
- **Statistics**: Completion rate, overdue count, average urgency
- **Date Ranges**: View data for custom time periods

### Settings

- **Appearance**: Theme (light/dark/system), dynamic colors
- **Notifications**: Enable/disable reminders
- **Urgency Config**: Customize all 14 urgency coefficients
- **Defaults**: Set default project, recurrence limit
- **Data Management**: Import/export (Taskwarrior JSON format)

---

## 🗄️ Database Schema

### Core Tables

**tasks**
- Primary Key: `uuid` (String)
- Indexed: `status`, `project`, `due`, `modified`, `[status, due]`
- Relations: tags (M:M), annotations (1:M), dependencies (M:M)

**hero** (Singleton)
- Primary Key: `id` (always 1)
- Fields: profile, level, XP, 6 stats, streaks

**Supporting Tables**
- `tags`, `task_tags` (junction)
- `annotations`
- `task_dependencies` (junction, self-referencing)
- `unlocked_titles`
- `xp_history`

---

## 🧪 Testing

The project includes comprehensive testing:

- **Unit Tests**: ViewModels, use cases, parsers
- **Integration Tests**: Room DAOs, repositories
- **UI Tests**: Compose tests for screens
- **Target**: 70% code coverage

```bash
# Run all tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Generate coverage report
./gradlew jacocoTestReport
```

---

## 🎨 Design System

TaskHero uses **Material Design 3** with full support for:

- **Dynamic Colors**: Material You theming on Android 12+
- **Dark Theme**: Automatic dark mode support
- **Typography**: Complete Material 3 type scale
- **Components**: Cards, Chips, FABs, Dialogs, Sheets
- **Adaptive Layouts**: Different layouts for phones, tablets, foldables

---

## 📊 Performance

- **Startup**: Optimized with baseline profiles
- **Database**: Strategic indices for common queries
- **UI**: ImmutableList for preventing recomposition
- **Memory**: Efficient Flow usage, no memory leaks
- **APK Size**: ProGuard/R8 optimization

---

## 🔒 Permissions

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

---

## 🤝 Contributing

Contributions are welcome! Please read our [Contributing Guidelines](CONTRIBUTING.md) first.

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- **Taskwarrior**: Inspiration for task management features
- **D&D**: Stat system design
- **World of Warcraft**: XP progression curve
- **Material Design**: UI/UX guidelines
- **Jetpack Compose**: Modern Android UI toolkit

---

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/yourusername/TaskHero/issues)
- **Discussions**: [GitHub Discussions](https://github.com/yourusername/TaskHero/discussions)
- **Email**: support@taskhero.app

---

## 🗺️ Roadmap

### Version 1.0 (Current)
- ✅ Core task management
- ✅ Hero system with XP
- ✅ Reports and analytics
- ✅ Notifications
- ✅ Settings

### Version 1.1 (Planned)
- ⏳ Glance widgets
- ⏳ Recurring tasks generation
- ⏳ Task templates
- ⏳ Taskwarrior sync

### Version 1.2 (Future)
- ⏳ Timewarrior integration
- ⏳ Google Drive backup
- ⏳ Custom themes
- ⏳ Wear OS app

---

<div align="center">

**Made with ❤️ using Jetpack Compose**

[⬆ Back to Top](#taskhero---gamified-task-management-for-android)

</div>
