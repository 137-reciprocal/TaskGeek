# Brain Dump Feature - Implementation Documentation

## Overview

The Brain Dump feature allows users to quickly add multiple tasks at once using natural language syntax. This feature is designed for users who want to "empty their mind" by rapidly capturing multiple tasks without the friction of creating them one by one.

## Features

### 1. Quick Entry Syntax Support

The feature supports multiple input formats:

- **Comma-separated**: `"task1, task2, task3"`
- **Newline-separated**: Multi-line input with one task per line
- **Mixed format**: Combination of commas and newlines
- **Natural language per task**: Each task can include metadata like dates, priorities, projects, and tags

### 2. Smart Separator Detection

The parser automatically detects the best separator mode based on input:
- If input contains 2+ commas: Treats as comma-separated
- If input contains newlines but few commas: Treats as newline-separated
- If input contains both: Treats as mixed mode

### 3. Real-time Parsing

- Shows live count of tasks as user types
- Preview list displays parsed tasks with metadata
- Instant feedback on what will be created

## User Interface

### Brain Dump Button

Located in the TaskList TopAppBar:
- Icon: `Psychology` (brain icon)
- Action: Opens full-screen brain dump dialog

### Brain Dump Dialog

Full-screen dialog with:

**Input Section**:
- Large multi-line TextField (8-10 lines visible)
- Placeholder: "Empty your mind... (one task per line or comma-separated)"
- Helper text with examples
- Real-time task count display

**Preview Section**:
- Numbered list (1, 2, 3...)
- Each task shows:
  - Task description
  - Parsed metadata (date, priority, project, tags)
  - Edit button (for inline edits)
  - Delete button (to remove before adding)

**Action Buttons**:
- "Cancel": Dismisses dialog without adding tasks
- "Add All Tasks": Creates all parsed tasks

**Keyboard Shortcuts**:
- `Ctrl/Cmd+Enter`: Add all tasks and close dialog

## File Structure

### Core Parser Module

**`core/parser/src/main/kotlin/com/taskhero/core/parser/BrainDumpParser.kt`**
- Parses brain dump input into list of `ParsedTaskData`
- Handles separator detection (comma, newline, or mixed)
- Filters empty lines and trailing separators
- Integrates with `NaturalLanguageParser` for metadata extraction

**`core/parser/src/test/kotlin/com/taskhero/core/parser/BrainDumpParserTest.kt`**
- Comprehensive unit tests for all input formats
- Tests separator detection logic
- Tests natural language parsing integration

### Feature TaskList Module

**`feature/tasklist/src/main/kotlin/com/taskhero/feature/tasklist/components/BrainDumpDialog.kt`**
- Full-screen dialog component
- Manages input state and real-time parsing
- Handles keyboard shortcuts
- Integrates with preview component

**`feature/tasklist/src/main/kotlin/com/taskhero/feature/tasklist/components/BrainDumpPreview.kt`**
- Preview list showing parsed tasks
- Numbered task cards with metadata chips
- Edit and delete functionality per task
- Color-coded metadata (date=red, priority=orange, project=blue, tags=purple)

**`feature/tasklist/src/main/kotlin/com/taskhero/feature/tasklist/TaskListIntent.kt`**
- `OpenBrainDump`: Opens the brain dump dialog
- `CreateMultipleTasks(tasks: List<ParsedTaskData>)`: Batch creates tasks

**`feature/tasklist/src/main/kotlin/com/taskhero/feature/tasklist/TaskListEffect.kt`**
- `ShowBrainDumpDialog`: Side effect to show dialog

**`feature/tasklist/src/main/kotlin/com/taskhero/feature/tasklist/TaskListViewModel.kt`**
- `openBrainDump()`: Emits effect to show dialog
- `createMultipleTasks(tasks)`: Batch creates tasks with progress tracking
- Handles partial failures gracefully
- Shows appropriate snackbar messages

**`feature/tasklist/src/main/kotlin/com/taskhero/feature/tasklist/TaskListScreen.kt`**
- Brain dump icon button in TopAppBar
- Dialog state management
- Integration with ViewModel intents

## Usage Examples

### Example 1: Comma-Separated Simple Tasks
```
Buy milk, Call dentist, Review code
```
Creates 3 tasks with descriptions only.

### Example 2: Newline-Separated with Metadata
```
Buy groceries tomorrow #personal @shopping
Call dentist p1
Review pull requests #work @review p2
```
Creates 3 tasks with:
- Task 1: Due date (tomorrow), project (personal), tag (shopping)
- Task 2: Priority (HIGH)
- Task 3: Project (work), tag (review), priority (MEDIUM)

### Example 3: Mixed Format
```
Buy milk tomorrow, Call dentist p1
Review code #work, Write tests
Fix bug #urgent p1
```
Creates 5 tasks with appropriate metadata.

## Natural Language Syntax

Each task in the brain dump can use the full natural language syntax:

- **Due dates**: `tomorrow`, `today`, `next monday`, `+3d`, `2025-12-31`
- **Priority**:
  - `p1` or `!1` = HIGH
  - `p2` or `!2` = MEDIUM
  - `p3` or `!3` = LOW
- **Project**: `#projectname`
- **Tags**: `@tagname`

## Technical Implementation Details

### Parser Logic

1. **Input Validation**: Check for blank/empty input
2. **Separator Detection**: Count commas and newlines to determine mode
3. **Splitting**: Split by detected separator(s)
4. **Trimming**: Remove whitespace from each task
5. **Filtering**: Remove empty tasks
6. **Natural Language Parsing**: Parse each task for metadata
7. **Result**: Return `List<ParsedTaskData>`

### Dialog State Management

The dialog manages its own input state locally using `remember { mutableStateOf() }`:
- Input text
- Parsed tasks list
- Focus management

### Batch Task Creation

The ViewModel processes tasks sequentially:
1. Iterate through parsed tasks
2. Create `Task` object for each with metadata
3. Call `AddTaskUseCase` for each
4. Track success/failure counts
5. Show appropriate snackbar message

### Error Handling

- Empty input: Shows "No tasks yet" in preview
- Parsing failures: Tasks with blank descriptions are filtered out
- Creation failures: Tracked and reported in snackbar
- Partial failures: Shows count of successes and failures

## Accessibility

- Brain dump button has semantic description
- Dialog is dismissible with back button
- Keyboard shortcut for power users
- Screen reader friendly with proper labels
- High contrast metadata chips

## Performance Considerations

- Real-time parsing uses `LaunchedEffect` with input dependency
- Batch creation runs in coroutine scope
- UI updates only when parsing completes
- No blocking operations on main thread

## Future Enhancements

Potential improvements for future iterations:

1. **Drag to Reorder**: Allow users to reorder tasks before adding
2. **Bulk Edit**: Edit all tasks at once (e.g., add same project to all)
3. **Templates**: Save common brain dump templates
4. **Voice Input**: Dictate tasks using speech-to-text
5. **Import from Clipboard**: Auto-detect clipboard content
6. **Undo/Redo**: Allow undoing the batch creation
7. **Progress Indicator**: Show progress bar for large batches (10+ tasks)
8. **Task Validation**: Warn about duplicate tasks
9. **Smart Suggestions**: Suggest projects/tags based on history
10. **Export Preview**: Export parsed tasks to other formats

## Testing

Run the parser tests:
```bash
./gradlew :core:parser:test --tests BrainDumpParserTest
```

Expected test coverage:
- Blank input handling
- Comma-separated parsing
- Newline-separated parsing
- Mixed format parsing
- Natural language metadata extraction
- Empty line handling
- Trailing comma handling
- Task count accuracy
- Complex mixed format scenarios

## Integration Checklist

- [x] BrainDumpParser created with separator detection
- [x] BrainDumpDialog component implemented
- [x] BrainDumpPreview component implemented
- [x] TaskListIntent updated with brain dump intents
- [x] TaskListEffect updated with ShowBrainDumpDialog
- [x] TaskListViewModel updated with batch creation logic
- [x] TaskListScreen integrated with brain dump button and dialog
- [x] Comprehensive unit tests for parser
- [x] Documentation completed

## User Flow

1. User clicks brain dump button (psychology icon) in TopAppBar
2. Full-screen dialog opens with focus on text field
3. User types multiple tasks (comma or newline separated)
4. Real-time count updates: "5 tasks ready to add"
5. Preview shows parsed tasks with metadata chips
6. User can edit/delete individual tasks if needed
7. User clicks "Add All Tasks" or presses Ctrl/Cmd+Enter
8. Dialog closes with loading state
9. Snackbar shows: "Added 5 tasks!"
10. Tasks appear in list with appropriate metadata
11. XP rewards calculated for completed tasks

## Success Metrics

The feature is successful if:
- Users can add 5+ tasks in under 30 seconds
- Parser correctly identifies separator mode 95%+ of the time
- Batch creation succeeds for 99%+ of valid inputs
- User feedback is positive on reduced friction
- Task creation rate increases after feature launch
