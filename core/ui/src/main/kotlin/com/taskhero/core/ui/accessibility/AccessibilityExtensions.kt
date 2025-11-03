package com.taskhero.core.ui.accessibility

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics

/**
 * Extension functions for improving accessibility in Compose UI.
 * These functions help make the app more accessible for users with disabilities,
 * particularly those using TalkBack and other screen readers.
 */

/**
 * Add a semantic description to a composable for screen readers.
 * This is read aloud by TalkBack when the user navigates to the element.
 *
 * @param description The content description to be read by screen readers
 * @return Modified Modifier with semantic description
 *
 * Example:
 * ```
 * Icon(
 *     imageVector = Icons.Default.Add,
 *     contentDescription = null,
 *     modifier = Modifier.semanticDescription("Add new task")
 * )
 * ```
 */
fun Modifier.semanticDescription(description: String): Modifier {
    return this.semantics(mergeDescendants = true) {
        contentDescription = description
    }
}

/**
 * Set the semantic role of a composable for accessibility services.
 * This helps screen readers understand the purpose and behavior of UI elements.
 *
 * @param role The semantic role (Button, Checkbox, Tab, etc.)
 * @return Modified Modifier with semantic role
 *
 * Example:
 * ```
 * Box(
 *     modifier = Modifier
 *         .clickable { /* ... */ }
 *         .semanticRole(Role.Button)
 * )
 * ```
 */
fun Modifier.semanticRole(role: Role): Modifier {
    return this.semantics {
        this.role = role
    }
}

/**
 * Mark a composable as a heading for screen reader navigation.
 * This allows users to navigate between sections using heading navigation.
 *
 * @return Modified Modifier marked as heading
 *
 * Example:
 * ```
 * Text(
 *     text = "Task Details",
 *     modifier = Modifier.semanticHeading()
 * )
 * ```
 */
fun Modifier.semanticHeading(): Modifier {
    return this.semantics {
        heading()
    }
}

/**
 * Combine semantic description and role for commonly used patterns.
 * This is a convenience function for elements that need both.
 *
 * @param description The content description to be read by screen readers
 * @param role The semantic role
 * @return Modified Modifier with both semantic description and role
 *
 * Example:
 * ```
 * Box(
 *     modifier = Modifier
 *         .clickable { /* ... */ }
 *         .semanticDescriptionWithRole("Delete task", Role.Button)
 * )
 * ```
 */
fun Modifier.semanticDescriptionWithRole(description: String, role: Role): Modifier {
    return this.semantics(mergeDescendants = true) {
        contentDescription = description
        this.role = role
    }
}
