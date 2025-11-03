package com.taskhero.domain.hero.usecase

import com.taskhero.domain.hero.model.Hero
import com.taskhero.domain.hero.repository.HeroRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case to add XP to the hero and handle leveling up.
 *
 * This use case:
 * 1. Adds XP to hero's current and total XP
 * 2. Checks if level up occurs
 * 3. If level up, calculates new stats based on WoW-style polynomial curve
 * 4. Updates hero in repository
 * 5. Returns updated hero
 */
class AddXpToHeroUseCase @Inject constructor(
    private val repository: HeroRepository,
    private val levelUpHeroUseCase: LevelUpHeroUseCase
) {
    suspend operator fun invoke(xpAmount: Long): Result<Hero> {
        return try {
            // Get current hero
            val currentHero = repository.getHero().first()
                ?: return Result.failure(Exception("Hero not found"))

            // Add XP
            val newTotalXp = currentHero.totalXpEarned + xpAmount
            var newCurrentXp = currentHero.currentXp + xpAmount
            var updatedHero = currentHero.copy(
                totalXpEarned = newTotalXp,
                currentXp = newCurrentXp,
                tasksCompleted = currentHero.tasksCompleted + 1
            )

            // Check for level ups
            var xpForNextLevel = levelUpHeroUseCase.calculateXpForLevel(updatedHero.level + 1)
            while (newCurrentXp >= xpForNextLevel) {
                // Level up!
                newCurrentXp -= xpForNextLevel

                val levelUpInfo = levelUpHeroUseCase(updatedHero)

                updatedHero = updatedHero.copy(
                    level = levelUpInfo.newLevel,
                    currentXp = newCurrentXp,
                    strength = updatedHero.strength + levelUpInfo.statIncreases.strength,
                    dexterity = updatedHero.dexterity + levelUpInfo.statIncreases.dexterity,
                    constitution = updatedHero.constitution + levelUpInfo.statIncreases.constitution,
                    intelligence = updatedHero.intelligence + levelUpInfo.statIncreases.intelligence,
                    wisdom = updatedHero.wisdom + levelUpInfo.statIncreases.wisdom,
                    charisma = updatedHero.charisma + levelUpInfo.statIncreases.charisma,
                    currentTitle = levelUpInfo.newTitle ?: updatedHero.currentTitle
                )

                // Calculate XP needed for next level
                xpForNextLevel = levelUpHeroUseCase.calculateXpForLevel(updatedHero.level + 1)
            }

            // Update hero in repository
            repository.updateHero(updatedHero)

            Result.success(updatedHero)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
