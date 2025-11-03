package com.taskhero.domain.hero.usecase

import com.taskhero.domain.hero.model.Title
import javax.inject.Inject

/**
 * Use case to get unlocked titles for the hero.
 * Returns a list of titles that the hero has unlocked based on their level.
 */
class GetUnlockedTitlesUseCase @Inject constructor() {
    operator fun invoke(heroLevel: Int): List<Title> {
        // Predefined list of titles
        val allTitles = listOf(
            Title(
                id = "novice",
                name = "Novice",
                description = "Just starting your journey",
                requiredLevel = 1,
                isUnlocked = heroLevel >= 1
            ),
            Title(
                id = "apprentice",
                name = "Apprentice",
                description = "Learning the ropes",
                requiredLevel = 3,
                isUnlocked = heroLevel >= 3
            ),
            Title(
                id = "adept",
                name = "Adept",
                description = "Skilled and capable",
                requiredLevel = 5,
                isUnlocked = heroLevel >= 5
            ),
            Title(
                id = "expert",
                name = "Expert",
                description = "Master of your craft",
                requiredLevel = 10,
                isUnlocked = heroLevel >= 10
            ),
            Title(
                id = "champion",
                name = "Champion",
                description = "A true hero",
                requiredLevel = 15,
                isUnlocked = heroLevel >= 15
            ),
            Title(
                id = "legend",
                name = "Legend",
                description = "Stories are told of your deeds",
                requiredLevel = 20,
                isUnlocked = heroLevel >= 20
            ),
            Title(
                id = "mythic",
                name = "Mythic",
                description = "Your legend transcends time",
                requiredLevel = 30,
                isUnlocked = heroLevel >= 30
            )
        )

        return allTitles.filter { it.isUnlocked }
    }
}
