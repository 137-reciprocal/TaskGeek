# Brain Dump Feature - Usage Examples

## Quick Start Examples

### Example 1: Simple Comma-Separated Tasks
**Input:**
```
Buy milk, Call dentist, Review code
```

**Result:**
- ✓ Task 1: "Buy milk"
- ✓ Task 2: "Call dentist"
- ✓ Task 3: "Review code"

**Use Case:** Quick capture of simple tasks without metadata.

---

### Example 2: Newline-Separated with Metadata
**Input:**
```
Buy groceries tomorrow #personal @shopping
Call dentist p1
Review pull requests #work @review p2
```

**Result:**
- ✓ Task 1: "Buy groceries"
  - Due: Tomorrow
  - Project: personal
  - Tags: shopping

- ✓ Task 2: "Call dentist"
  - Priority: HIGH

- ✓ Task 3: "Review pull requests"
  - Project: work
  - Tags: review
  - Priority: MEDIUM

**Use Case:** Organized tasks with priorities and categorization.

---

### Example 3: Mixed Format (Commas + Newlines)
**Input:**
```
Buy milk tomorrow, Call dentist p1
Review code #work, Write tests
Fix bug #urgent p1
```

**Result:**
- ✓ Task 1: "Buy milk" (Due: Tomorrow)
- ✓ Task 2: "Call dentist" (Priority: HIGH)
- ✓ Task 3: "Review code" (Project: work)
- ✓ Task 4: "Write tests"
- ✓ Task 5: "Fix bug" (Project: urgent, Priority: HIGH)

**Use Case:** Rapid brain dump with flexible formatting.

---

## Real-World Scenarios

### Scenario 1: Morning Routine Planning
**Input:**
```
Morning coffee and breakfast today #personal
Check emails p2 #work
Team standup 10am #work @meeting
Review yesterday's code changes #work p1
```

**Result:** 4 tasks organized by priority and time
- Morning personal task
- Work tasks with priorities
- Meeting scheduled

**Use Case:** Planning your day first thing in the morning.

---

### Scenario 2: Project Kickoff
**Input:**
```
Set up project repository #newproject
Create project documentation next monday #newproject
Schedule team kickoff meeting #newproject @meeting
Define project milestones next friday #newproject p1
Set up CI/CD pipeline #newproject @devops p2
```

**Result:** 5 project-related tasks with due dates and priorities
- All tagged with same project
- Different priorities based on importance
- Clear timeline

**Use Case:** Quickly capturing all tasks for a new project.

---

### Scenario 3: Weekly Review
**Input:**
```
Review last week's completed tasks
Plan next week's priorities tomorrow
Update project status report next friday #work p1
Schedule 1-on-1s with team #work @meetings
Review Q4 goals and progress #work p1
```

**Result:** 5 review and planning tasks
- Mix of immediate and future tasks
- Work-related with appropriate priorities

**Use Case:** Weekly planning and review session.

---

### Scenario 4: Personal Errands
**Input:**
```
Grocery shopping tomorrow @shopping #personal
Pick up dry cleaning @errands #personal
Call mom p1 @family
Schedule dentist appointment +7d @health
Buy birthday gift for Sarah next monday @shopping #personal
```

**Result:** 5 personal tasks with various categories
- Shopping tasks grouped
- Family and health priorities
- Specific deadlines

**Use Case:** Weekend errands and personal to-dos.

---

### Scenario 5: Bug Triage
**Input:**
```
Fix login bug p1 #bugfix @urgent
Investigate memory leak #bugfix p2
Update error messages #bugfix p3
Add logging to API calls #bugfix
Review bug reports from users next monday #bugfix
```

**Result:** 5 bug-related tasks with priorities
- Critical bugs marked HIGH priority
- Less urgent bugs with lower priorities
- Review scheduled for next week

**Use Case:** Triaging bugs after a bug bash or user reports.

---

### Scenario 6: Content Creation
**Input:**
```
Write blog post about TaskHero next friday #blog @writing
Create social media posts #marketing
Record demo video tomorrow #marketing @video
Edit and publish video next monday #marketing @video p1
Respond to comments #blog
```

**Result:** 5 content creation tasks
- Writing and marketing tasks separated
- Video production workflow
- Clear timeline

**Use Case:** Planning content calendar for a week.

---

### Scenario 7: Study Plan
**Input:**
```
Read chapter 5 tomorrow #study @reading
Complete practice problems #study @homework p1
Review lecture notes #study
Prepare for quiz next friday #study p1 @exam
Join study group thursday #study @meeting
```

**Result:** 5 study-related tasks
- Reading and homework separated
- Exam prep prioritized
- Study group scheduled

**Use Case:** Student planning weekly study schedule.

---

### Scenario 8: Home Maintenance
**Input:**
```
Fix leaky faucet p1 #home @maintenance
Schedule HVAC maintenance next month #home @maintenance
Clean garage tomorrow #home @cleaning
Plant garden next weekend #home @outdoor
Order replacement filters #home @shopping
```

**Result:** 5 home tasks organized by type
- Urgent maintenance prioritized
- Scheduled tasks with deadlines
- Shopping list items

**Use Case:** Home maintenance task list.

---

### Scenario 9: Fitness Goals
**Input:**
```
Gym workout today #fitness @exercise
Meal prep tomorrow #fitness @cooking
Track calories #fitness
Schedule trainer session next week #fitness @training p1
Buy protein powder #fitness @shopping
```

**Result:** 5 fitness-related tasks
- Daily workout scheduled
- Meal planning included
- Training session prioritized

**Use Case:** Weekly fitness planning.

---

### Scenario 10: Event Planning
**Input:**
```
Book venue next monday #party @planning p1
Send invitations next friday #party @planning
Order catering +14d #party @planning p1
Create playlist #party @entertainment
Buy decorations #party @shopping
Set up day before #party p1
```

**Result:** 6 event planning tasks with timeline
- Critical tasks prioritized
- Clear sequence of tasks
- All related to same project

**Use Case:** Planning a party or event.

---

## Advanced Examples

### Example A: Complex Natural Language
**Input:**
```
Review quarterly report next friday at 2pm #work @review p1
Schedule dentist appointment +30d @health #personal
Buy groceries tomorrow evening @shopping #personal
Call mom this weekend @family #personal p2
Fix production bug ASAP #urgent p1 @devops
```

**Result:** 5 tasks with complex metadata
- Specific times (parsed as due dates)
- Multiple tags and projects
- Various priorities
- Different urgency levels

**Use Case:** Mixed personal and work tasks with detailed requirements.

---

### Example B: Recurring-Style Planning
**Input:**
```
Weekly team sync monday #work @meeting
Daily standup every morning #work @standup
Code review every afternoon #work @review
Weekly report every friday #work @report p1
Monthly 1-on-1s first monday of month #work @meeting p2
```

**Result:** 5 recurring-style tasks
- Regular schedule captured
- All work-related with meetings
- Priorities assigned

**Note:** These create single tasks; true recurrence requires separate feature.

**Use Case:** Capturing regular commitments.

---

### Example C: Project Breakdown
**Input:**
```
Design database schema #website p1 @backend
Create API endpoints #website p2 @backend
Build frontend components #website p2 @frontend
Write unit tests #website p3 @testing
Deploy to staging #website p1 @devops
Conduct user testing #website @qa
```

**Result:** 6 project tasks organized by component
- Backend, frontend, testing, devops separated
- Priorities reflect dependencies
- All under same project

**Use Case:** Breaking down a project into tasks.

---

## Tips and Best Practices

### Tip 1: Use Consistent Naming
```
Good:
Buy groceries #personal
Call dentist #personal
Gym workout #personal

Better:
Buy groceries #personal @errands
Call dentist #personal @health
Gym workout #personal @fitness
```
**Why:** More specific tags make filtering easier.

---

### Tip 2: Prioritize Wisely
```
Avoid:
Everything is p1

Better:
Fix production bug p1
Update documentation p2
Refactor code p3
```
**Why:** Proper prioritization helps focus on what matters.

---

### Tip 3: Use Due Dates Effectively
```
Good:
Buy birthday gift next monday
Schedule meeting tomorrow
Review code this week

Better:
Buy birthday gift next monday #personal @shopping
Schedule meeting tomorrow at 10am #work @meeting
Review code +2d #work @review p1
```
**Why:** Specific dates and times provide clarity.

---

### Tip 4: Group Related Tasks
```
Good:
Task 1 #project1
Task 2 #project1
Task 3 #project1

Better:
Task 1 #project1 @feature-a p1
Task 2 #project1 @feature-a p2
Task 3 #project1 @feature-b p1
```
**Why:** Sub-categorization with tags helps organization.

---

### Tip 5: Keep Descriptions Clear
```
Avoid:
Do the thing
Fix it
Call

Better:
Implement user authentication
Fix login redirect bug
Call dentist to schedule appointment
```
**Why:** Clear descriptions prevent confusion later.

---

## Common Patterns

### Pattern 1: Daily Planning
```
Check emails #work
Morning standup 10am #work @meeting
Deep work session 11am-1pm #work
Lunch break 1pm #personal
Afternoon code review #work @review
End of day wrap-up 5pm #work
```

### Pattern 2: Weekly Planning
```
Monday: Team sync #work @meeting
Tuesday: Project planning #work
Wednesday: Code review day #work @review
Thursday: 1-on-1s #work @meeting
Friday: Weekly report #work @report
```

### Pattern 3: Project Phases
```
Phase 1: Research and planning #project @research
Phase 2: Design and architecture #project @design
Phase 3: Implementation #project @dev
Phase 4: Testing and QA #project @qa
Phase 5: Deployment #project @deploy
```

### Pattern 4: Priority Levels
```
Critical (p1): Fix production outage #urgent
High (p1): Deploy hotfix #urgent
Medium (p2): Update documentation #work
Low (p3): Refactor old code #tech-debt
```

---

## Edge Cases Handled

### Edge Case 1: Empty Lines
**Input:**
```
Task 1

Task 2


Task 3
```
**Result:** 3 tasks (empty lines ignored)

---

### Edge Case 2: Trailing Commas
**Input:**
```
Task 1, Task 2, Task 3,
```
**Result:** 3 tasks (trailing comma ignored)

---

### Edge Case 3: Mixed Separators
**Input:**
```
Task 1, Task 2
Task 3
Task 4, Task 5, Task 6
```
**Result:** 6 tasks (all separators handled)

---

### Edge Case 4: Only Metadata
**Input:**
```
#project @tag p1 tomorrow
```
**Result:** 0 tasks (no description = filtered out)

---

### Edge Case 5: Duplicate Projects/Tags
**Input:**
```
Task 1 #work #work @tag @tag
```
**Result:** 1 task with project "work" and tag "tag" (duplicates handled by parser)

---

## Conclusion

The Brain Dump feature handles a wide variety of input formats and use cases, making it easy to quickly capture tasks in any situation. The natural language parsing makes it intuitive, while the preview ensures you always know what will be created.

**Key Takeaways:**
- Use the format that feels natural to you
- Combine commas and newlines freely
- Add metadata as needed
- Preview before creating
- Edit or delete unwanted tasks
- Enjoy the speed and efficiency!
