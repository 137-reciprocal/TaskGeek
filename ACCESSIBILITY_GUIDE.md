# Accessibility Implementation Guide

This guide provides instructions for implementing accessibility features in TaskHero screens that have not yet been fully updated.

## Overview

Accessibility has been partially implemented in TaskHero with:
- **Completed**: TaskListScreen, TaskCard, and core infrastructure
- **Pending**: HeroScreen, ReportsScreen, SettingsScreen, TaskDetailScreen (partial)

## Core Components

### 1. Accessibility Extensions
Location: `/core/ui/src/main/kotlin/com/taskhero/core/ui/accessibility/AccessibilityExtensions.kt`

Available modifier extensions:
```kotlin
// Add semantic description for screen readers
Modifier.semanticDescription(description: String)

// Set semantic role (Button, Checkbox, etc.)
Modifier.semanticRole(role: Role)

// Mark as heading for navigation
Modifier.semanticHeading()

// Combine description and role
Modifier.semanticDescriptionWithRole(description: String, role: Role)
```

### 2. Accessibility Constants
Location: `/core/ui/src/main/kotlin/com/taskhero/core/ui/accessibility/AccessibilityConstants.kt`

Provides pre-defined content descriptions for all screens:
- `AccessibilityConstants.TaskList.*`
- `AccessibilityConstants.TaskDetail.*`
- `AccessibilityConstants.Hero.*`
- `AccessibilityConstants.Reports.*`
- `AccessibilityConstants.Settings.*`
- `AccessibilityConstants.Navigation.*`
- `AccessibilityConstants.Common.*`

## Implementation Checklist

### For Each Screen

#### 1. Add Imports
```kotlin
import com.taskhero.core.ui.accessibility.AccessibilityConstants
import com.taskhero.core.ui.accessibility.semanticDescription
import com.taskhero.core.ui.accessibility.semanticHeading
import com.taskhero.core.ui.accessibility.semanticRole
import androidx.compose.ui.semantics.Role
```

#### 2. Screen Title
Mark the screen title as a heading:
```kotlin
TopAppBar(
    title = {
        Text(
            text = "Screen Title",
            modifier = Modifier.semanticHeading()
        )
    }
)
```

#### 3. Navigation Icons
Add descriptions to back buttons and navigation:
```kotlin
IconButton(
    onClick = onNavigateBack,
    modifier = Modifier.semanticDescription(AccessibilityConstants.BACK_BUTTON)
) {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = null // Handled by IconButton semantics
    )
}
```

#### 4. Form Fields
Add descriptions to all input fields:
```kotlin
OutlinedTextField(
    value = value,
    onValueChange = onChange,
    label = { Text("Field Label") },
    modifier = Modifier.semanticDescription(AccessibilityConstants.FIELD_NAME)
)
```

#### 5. Buttons
Add descriptions to all action buttons:
```kotlin
Button(
    onClick = onSave,
    modifier = Modifier.semanticDescription(AccessibilityConstants.SAVE_BUTTON)
) {
    Text("Save")
}
```

#### 6. Icons Without Text
Always provide descriptions for standalone icons:
```kotlin
IconButton(
    onClick = onDelete,
    modifier = Modifier.semanticDescription("Delete ${itemName}")
) {
    Icon(
        imageVector = Icons.Default.Delete,
        contentDescription = null // Handled by IconButton semantics
    )
}
```

#### 7. Loading States
Add descriptions to loading indicators:
```kotlin
CircularProgressIndicator(
    modifier = Modifier.semanticDescription(
        AccessibilityConstants.Common.loading("content")
    )
)
```

#### 8. Empty States
Add descriptions to empty state messages:
```kotlin
Box(
    modifier = Modifier.semanticDescription(
        AccessibilityConstants.EMPTY_LIST
    )
) {
    Text("No items found")
}
```

## Pending Screen Implementations

### HeroScreen

**Priority Elements:**
1. Avatar image: `AccessibilityConstants.Hero.AVATAR_IMAGE`
2. Level badge: Use `AccessibilityConstants.Hero.heroLevel(level)`
3. XP progress bar: `AccessibilityConstants.Hero.XP_PROGRESS_BAR` with dynamic description
4. Stat cards: Use `AccessibilityConstants.Hero.statCard(name, value)`
5. Section headings: Mark "Attributes", "Hero Statistics" as headings

**Example for XpProgressBar:**
```kotlin
@Composable
fun XpProgressBar(level: Int, currentXp: Int, xpToNextLevel: Int) {
    val percentage = (currentXp.toFloat() / xpToNextLevel * 100).toInt()

    LinearProgressIndicator(
        progress = currentXp.toFloat() / xpToNextLevel,
        modifier = Modifier
            .fillMaxWidth()
            .semanticDescription(
                AccessibilityConstants.Hero.xpProgress(
                    current = currentXp,
                    needed = xpToNextLevel,
                    percentage = percentage
                )
            )
    )
}
```

**Example for StatCard:**
```kotlin
@Composable
fun StatCard(statName: String, statValue: Int) {
    Card(
        modifier = Modifier.semanticDescription(
            AccessibilityConstants.Hero.statCard(statName, statValue.toString())
        )
    ) {
        // Card content
    }
}
```

### ReportsScreen

**Priority Elements:**
1. Tab selector: Use `AccessibilityConstants.Reports.tabSelected(tabName)`
2. Calendar view: `AccessibilityConstants.Reports.CALENDAR_VIEW`
3. Charts: `AccessibilityConstants.Reports.BURNDOWN_CHART`
4. Statistics cards: Use `AccessibilityConstants.Reports.statsCard(title, value)`
5. Section headings: Mark tab names and section titles as headings

**Example for Tab:**
```kotlin
Tab(
    selected = selected,
    onClick = onClick,
    modifier = Modifier.semanticDescription(
        if (selected) {
            AccessibilityConstants.Reports.tabSelected(tabName)
        } else {
            tabName
        }
    )
) {
    Text(tabName)
}
```

### SettingsScreen

**Priority Elements:**
1. Screen title heading
2. Category headers: Mark as headings
3. Setting items: Use `AccessibilityConstants.Settings.settingItem(title, value)`
4. Toggle switches: Use `AccessibilityConstants.Settings.toggleSetting(title, isEnabled)`
5. Action buttons: Export, Import, Clear data buttons

**Example for Toggle Setting:**
```kotlin
@Composable
fun SettingToggle(title: String, enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semanticDescription(
                AccessibilityConstants.Settings.toggleSetting(title, enabled)
            )
    ) {
        Text(title)
        Switch(
            checked = enabled,
            onCheckedChange = onToggle
        )
    }
}
```

### TaskDetailScreen (Completion)

**Remaining Elements:**
1. Status dropdown: Add description
2. Section headings: Mark "Tags", "Dependencies", "Annotations", "UDAs" as headings
3. Dependency items: Add descriptions for remove buttons
4. Tag chips: Use `AccessibilityConstants.TaskDetail.tagChip(tag)`

## Testing

### Manual Testing with TalkBack

1. **Enable TalkBack:**
   - Settings → Accessibility → TalkBack → Enable
   - Use TalkBack tutorial to learn gestures

2. **Test Each Screen:**
   - Navigate through all screens
   - Verify all elements are announced correctly
   - Check that descriptions are clear and helpful
   - Ensure navigation between elements is logical

3. **Test Interactions:**
   - Verify buttons announce their action
   - Check that form fields announce their purpose
   - Ensure state changes are announced (loading, errors, etc.)

### Automated Testing

Run the accessibility tests:
```bash
./gradlew connectedAndroidTest
```

The tests are located in:
`/app/src/androidTest/kotlin/com/taskhero/app/AccessibilityTest.kt`

## Best Practices

### 1. Content Description Guidelines

**DO:**
- Be concise but descriptive
- Describe the action, not the visual
- Use consistent terminology
- Include state information when relevant

**DON'T:**
- Say "button" or "image" (screen readers announce this)
- Be overly verbose
- Include visual-only information
- Repeat visible text unnecessarily

### 2. Examples

**Good:**
```kotlin
// Button
modifier = Modifier.semanticDescription("Add new task")

// Status indicator
modifier = Modifier.semanticDescription("Task completed")

// Progress
modifier = Modifier.semanticDescription("Loading tasks, please wait")
```

**Bad:**
```kotlin
// Too verbose
modifier = Modifier.semanticDescription("Click this button to add a new task to your list")

// Visual-only info
modifier = Modifier.semanticDescription("Blue circular button with plus icon")

// Missing action
modifier = Modifier.semanticDescription("Plus icon")
```

### 3. Dynamic Content

For dynamic content, build descriptions programmatically:
```kotlin
val description = buildString {
    append("Task: ${task.description}")
    if (task.priority != null) {
        append(", Priority: ${task.priority.name}")
    }
    if (task.dueDate != null) {
        append(", Due: ${formatDate(task.dueDate)}")
    }
}
modifier = Modifier.semanticDescription(description)
```

### 4. Headings

Mark all section headers as headings to enable heading navigation:
```kotlin
Text(
    text = "Section Title",
    style = MaterialTheme.typography.titleLarge,
    modifier = Modifier.semanticHeading()
)
```

### 5. Loading and Error States

Always provide context for loading and error states:
```kotlin
// Loading
CircularProgressIndicator(
    modifier = Modifier.semanticDescription(
        AccessibilityConstants.Common.loading("tasks")
    )
)

// Error
Text(
    text = errorMessage,
    modifier = Modifier.semanticDescription(
        AccessibilityConstants.Common.error(errorMessage)
    )
)
```

## Resources

- [Material Design Accessibility](https://m3.material.io/foundations/accessible-design/overview)
- [Jetpack Compose Accessibility](https://developer.android.com/jetpack/compose/accessibility)
- [TalkBack User Guide](https://support.google.com/accessibility/android/answer/6283677)
- [Android Accessibility Best Practices](https://developer.android.com/guide/topics/ui/accessibility/principles)

## Questions or Issues?

Refer to the implemented examples in:
- `/feature/tasklist/src/main/kotlin/com/taskhero/feature/tasklist/TaskListScreen.kt`
- `/feature/tasklist/src/main/kotlin/com/taskhero/feature/tasklist/components/TaskCard.kt`

These files demonstrate proper accessibility implementation.
