package com.taskhero.domain.hero.usecase

import com.taskhero.domain.hero.model.Hero
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * Test suite for LevelUpHeroUseCase.
 *
 * Tests:
 * - XP threshold calculation using polynomial curve (BASE_XP * level^3)
 * - Stat increases on level up (1 point to each stat)
 * - Title unlocks at milestone levels (5, 10, 15, 20, 25, 30, 40, 50)
 * - Level progression
 * - Edge cases
 */
class LevelUpHeroUseCaseTest {

    private lateinit var levelUpHeroUseCase: LevelUpHeroUseCase
    private val currentTime = System.currentTimeMillis()

    @Before
    fun setup() {
        levelUpHeroUseCase = LevelUpHeroUseCase()
    }

    private fun createHero(
        level: Int = 1,
        strength: Int = 10,
        dexterity: Int = 10,
        constitution: Int = 10,
        intelligence: Int = 10,
        wisdom: Int = 10,
        charisma: Int = 10
    ): Hero {
        return Hero(
            displayName = "Test Hero",
            classType = "Warrior",
            currentTitle = "Novice",
            level = level,
            strength = strength,
            dexterity = dexterity,
            constitution = constitution,
            intelligence = intelligence,
            wisdom = wisdom,
            charisma = charisma,
            createdAt = currentTime
        )
    }

    @Test
    fun `test calculateXpForLevel uses polynomial curve`() {
        // Given & When
        val level1Xp = levelUpHeroUseCase.calculateXpForLevel(1)
        val level2Xp = levelUpHeroUseCase.calculateXpForLevel(2)
        val level3Xp = levelUpHeroUseCase.calculateXpForLevel(3)
        val level5Xp = levelUpHeroUseCase.calculateXpForLevel(5)
        val level10Xp = levelUpHeroUseCase.calculateXpForLevel(10)

        // Then - Formula: 100 * level^3
        assertEquals("Level 1 should need 100 XP", 100L, level1Xp)
        assertEquals("Level 2 should need 800 XP", 800L, level2Xp)
        assertEquals("Level 3 should need 2700 XP", 2700L, level3Xp)
        assertEquals("Level 5 should need 12500 XP", 12500L, level5Xp)
        assertEquals("Level 10 should need 100000 XP", 100000L, level10Xp)
    }

    @Test
    fun `test level up increases all stats by 1`() {
        // Given
        val hero = createHero(level = 1)

        // When
        val levelUpInfo = levelUpHeroUseCase(hero)

        // Then
        assertEquals("Strength should increase by 1", 1, levelUpInfo.statIncreases.strength)
        assertEquals("Dexterity should increase by 1", 1, levelUpInfo.statIncreases.dexterity)
        assertEquals("Constitution should increase by 1", 1, levelUpInfo.statIncreases.constitution)
        assertEquals("Intelligence should increase by 1", 1, levelUpInfo.statIncreases.intelligence)
        assertEquals("Wisdom should increase by 1", 1, levelUpInfo.statIncreases.wisdom)
        assertEquals("Charisma should increase by 1", 1, levelUpInfo.statIncreases.charisma)
    }

    @Test
    fun `test level up from 1 to 2`() {
        // Given
        val hero = createHero(level = 1)

        // When
        val levelUpInfo = levelUpHeroUseCase(hero)

        // Then
        assertEquals("Old level should be 1", 1, levelUpInfo.oldLevel)
        assertEquals("New level should be 2", 2, levelUpInfo.newLevel)
        assertEquals("XP for next level (3) should be 2700", 2700L, levelUpInfo.xpForNextLevel)
    }

    @Test
    fun `test level up from 5 to 6`() {
        // Given
        val hero = createHero(level = 5)

        // When
        val levelUpInfo = levelUpHeroUseCase(hero)

        // Then
        assertEquals("Old level should be 5", 5, levelUpInfo.oldLevel)
        assertEquals("New level should be 6", 6, levelUpInfo.newLevel)
        // XP for level 7 = 100 * 7^3 = 34300
        assertEquals("XP for next level (7) should be 34300", 34300L, levelUpInfo.xpForNextLevel)
    }

    @Test
    fun `test title unlock at level 5`() {
        // Given
        val hero = createHero(level = 4)

        // When
        val levelUpInfo = levelUpHeroUseCase(hero)

        // Then
        assertEquals("Should level up to 5", 5, levelUpInfo.newLevel)
        assertNotNull("Should unlock title at level 5", levelUpInfo.newTitle)
        assertEquals("Title should be Apprentice", "Apprentice", levelUpInfo.newTitle)
    }

    @Test
    fun `test title unlock at level 10`() {
        // Given
        val hero = createHero(level = 9)

        // When
        val levelUpInfo = levelUpHeroUseCase(hero)

        // Then
        assertEquals("Should level up to 10", 10, levelUpInfo.newLevel)
        assertNotNull("Should unlock title at level 10", levelUpInfo.newTitle)
        assertEquals("Title should be Adept", "Adept", levelUpInfo.newTitle)
    }

    @Test
    fun `test title unlock at level 15`() {
        // Given
        val hero = createHero(level = 14)

        // When
        val levelUpInfo = levelUpHeroUseCase(hero)

        // Then
        assertEquals("Should level up to 15", 15, levelUpInfo.newLevel)
        assertNotNull("Should unlock title at level 15", levelUpInfo.newTitle)
        assertEquals("Title should be Expert", "Expert", levelUpInfo.newTitle)
    }

    @Test
    fun `test title unlock at level 20`() {
        // Given
        val hero = createHero(level = 19)

        // When
        val levelUpInfo = levelUpHeroUseCase(hero)

        // Then
        assertEquals("Should level up to 20", 20, levelUpInfo.newLevel)
        assertNotNull("Should unlock title at level 20", levelUpInfo.newTitle)
        assertEquals("Title should be Master", "Master", levelUpInfo.newTitle)
    }

    @Test
    fun `test title unlock at level 25`() {
        // Given
        val hero = createHero(level = 24)

        // When
        val levelUpInfo = levelUpHeroUseCase(hero)

        // Then
        assertEquals("Should level up to 25", 25, levelUpInfo.newLevel)
        assertNotNull("Should unlock title at level 25", levelUpInfo.newTitle)
        assertEquals("Title should be Grandmaster", "Grandmaster", levelUpInfo.newTitle)
    }

    @Test
    fun `test title unlock at level 30`() {
        // Given
        val hero = createHero(level = 29)

        // When
        val levelUpInfo = levelUpHeroUseCase(hero)

        // Then
        assertEquals("Should level up to 30", 30, levelUpInfo.newLevel)
        assertNotNull("Should unlock title at level 30", levelUpInfo.newTitle)
        assertEquals("Title should be Legend", "Legend", levelUpInfo.newTitle)
    }

    @Test
    fun `test title unlock at level 40`() {
        // Given
        val hero = createHero(level = 39)

        // When
        val levelUpInfo = levelUpHeroUseCase(hero)

        // Then
        assertEquals("Should level up to 40", 40, levelUpInfo.newLevel)
        assertNotNull("Should unlock title at level 40", levelUpInfo.newTitle)
        assertEquals("Title should be Mythic", "Mythic", levelUpInfo.newTitle)
    }

    @Test
    fun `test title unlock at level 50`() {
        // Given
        val hero = createHero(level = 49)

        // When
        val levelUpInfo = levelUpHeroUseCase(hero)

        // Then
        assertEquals("Should level up to 50", 50, levelUpInfo.newLevel)
        assertNotNull("Should unlock title at level 50", levelUpInfo.newTitle)
        assertEquals("Title should be Immortal", "Immortal", levelUpInfo.newTitle)
    }

    @Test
    fun `test no title unlock at non-milestone level`() {
        // Given
        val hero = createHero(level = 6)

        // When
        val levelUpInfo = levelUpHeroUseCase(hero)

        // Then
        assertEquals("Should level up to 7", 7, levelUpInfo.newLevel)
        assertNull("Should not unlock title at level 7", levelUpInfo.newTitle)
    }

    @Test
    fun `test no title at level 3`() {
        // Given
        val hero = createHero(level = 2)

        // When
        val levelUpInfo = levelUpHeroUseCase(hero)

        // Then
        assertEquals("Should level up to 3", 3, levelUpInfo.newLevel)
        assertNull("Should not have title at level 3", levelUpInfo.newTitle)
    }

    @Test
    fun `test XP requirement grows polynomially`() {
        // Given & When
        val xp5 = levelUpHeroUseCase.calculateXpForLevel(5)
        val xp10 = levelUpHeroUseCase.calculateXpForLevel(10)
        val xp15 = levelUpHeroUseCase.calculateXpForLevel(15)

        // Then
        // Level 5: 100 * 5^3 = 12,500
        // Level 10: 100 * 10^3 = 100,000 (8x level 5)
        // Level 15: 100 * 15^3 = 337,500 (27x level 5)
        assertEquals("Level 5 XP", 12500L, xp5)
        assertEquals("Level 10 XP", 100000L, xp10)
        assertEquals("Level 15 XP", 337500L, xp15)
    }

    @Test
    fun `test level 1 hero leveling up`() {
        // Given
        val hero = createHero(level = 1, strength = 10, dexterity = 10)

        // When
        val levelUpInfo = levelUpHeroUseCase(hero)

        // Then
        assertEquals("Should go from level 1 to 2", 2, levelUpInfo.newLevel)
        assertEquals("Each stat increases by 1", 6, levelUpInfo.statIncreases.getTotalStats())
    }

    @Test
    fun `test high level hero leveling up`() {
        // Given
        val hero = createHero(level = 49, strength = 58, dexterity = 58)

        // When
        val levelUpInfo = levelUpHeroUseCase(hero)

        // Then
        assertEquals("Should level up to 50", 50, levelUpInfo.newLevel)
        assertEquals("Should unlock Immortal title", "Immortal", levelUpInfo.newTitle)
        assertEquals("Stat increases should be 6 total", 6, levelUpInfo.statIncreases.getTotalStats())
    }

    @Test
    fun `test stats are consistent across levels`() {
        // Given
        val hero1 = createHero(level = 1)
        val hero2 = createHero(level = 25)
        val hero3 = createHero(level = 50)

        // When
        val info1 = levelUpHeroUseCase(hero1)
        val info2 = levelUpHeroUseCase(hero2)
        val info3 = levelUpHeroUseCase(hero3)

        // Then
        assertEquals("All levels get same stat increase",
            info1.statIncreases.getTotalStats(),
            info2.statIncreases.getTotalStats()
        )
        assertEquals("All levels get same stat increase",
            info2.statIncreases.getTotalStats(),
            info3.statIncreases.getTotalStats()
        )
    }

    @Test
    fun `test XP threshold for level 2`() {
        // Given & When
        val xp = levelUpHeroUseCase.calculateXpForLevel(2)

        // Then
        // 100 * 2^3 = 800
        assertEquals("Level 2 requires 800 XP", 800L, xp)
    }

    @Test
    fun `test XP threshold for level 100`() {
        // Given & When
        val xp = levelUpHeroUseCase.calculateXpForLevel(100)

        // Then
        // 100 * 100^3 = 100,000,000
        assertEquals("Level 100 requires 100M XP", 100000000L, xp)
    }

    @Test
    fun `test all milestone titles are unique`() {
        // Given
        val titles = mutableSetOf<String>()

        // When
        listOf(5, 10, 15, 20, 25, 30, 40, 50).forEach { level ->
            val hero = createHero(level = level - 1)
            val info = levelUpHeroUseCase(hero)
            info.newTitle?.let { titles.add(it) }
        }

        // Then
        assertEquals("All 8 titles should be unique", 8, titles.size)
    }

    @Test
    fun `test total stat increase per level is 6`() {
        // Given
        val hero = createHero(level = 10)

        // When
        val levelUpInfo = levelUpHeroUseCase(hero)

        // Then
        val totalIncrease = levelUpInfo.statIncreases.strength +
                levelUpInfo.statIncreases.dexterity +
                levelUpInfo.statIncreases.constitution +
                levelUpInfo.statIncreases.intelligence +
                levelUpInfo.statIncreases.wisdom +
                levelUpInfo.statIncreases.charisma
        assertEquals("Total stat increase should be 6", 6, totalIncrease)
    }

    @Test
    fun `test level 0 to 1 XP requirement`() {
        // Given & When
        val xp = levelUpHeroUseCase.calculateXpForLevel(1)

        // Then
        assertEquals("Level 1 should require 100 XP", 100L, xp)
    }

    @Test
    fun `test xpForNextLevel is calculated for newLevel plus 1`() {
        // Given
        val hero = createHero(level = 5)

        // When
        val levelUpInfo = levelUpHeroUseCase(hero)

        // Then
        // Hero goes from 5 to 6, so xpForNextLevel should be for level 7
        val expectedXp = levelUpHeroUseCase.calculateXpForLevel(7)
        assertEquals("XP for next level should be for level 7", expectedXp, levelUpInfo.xpForNextLevel)
    }

    @Test
    fun `test sequential level ups maintain consistency`() {
        // Given
        var hero = createHero(level = 1)

        // When & Then
        for (expectedLevel in 2..10) {
            val levelUpInfo = levelUpHeroUseCase(hero)
            assertEquals("Should level up to $expectedLevel", expectedLevel, levelUpInfo.newLevel)
            assertEquals("Old level should be ${expectedLevel - 1}", expectedLevel - 1, levelUpInfo.oldLevel)

            // Simulate applying the level up
            hero = hero.copy(level = expectedLevel)
        }
    }

    @Test
    fun `test polynomial growth is exponential`() {
        // Given & When
        val xp1 = levelUpHeroUseCase.calculateXpForLevel(1)
        val xp2 = levelUpHeroUseCase.calculateXpForLevel(2)
        val xp4 = levelUpHeroUseCase.calculateXpForLevel(4)

        // Then
        // Level 2 is 8x level 1 (2^3 = 8)
        assertEquals("Level 2 should be 8x level 1", xp1 * 8, xp2)
        // Level 4 is 64x level 1 (4^3 = 64)
        assertEquals("Level 4 should be 64x level 1", xp1 * 64, xp4)
    }

    @Test
    fun `test hero with existing stats still gets 1 point per stat`() {
        // Given
        val hero = createHero(
            level = 10,
            strength = 20,
            dexterity = 18,
            constitution = 15,
            intelligence = 12,
            wisdom = 14,
            charisma = 16
        )

        // When
        val levelUpInfo = levelUpHeroUseCase(hero)

        // Then
        assertEquals("Strength increase should be 1", 1, levelUpInfo.statIncreases.strength)
        assertEquals("Dexterity increase should be 1", 1, levelUpInfo.statIncreases.dexterity)
        assertEquals("Constitution increase should be 1", 1, levelUpInfo.statIncreases.constitution)
        assertEquals("Intelligence increase should be 1", 1, levelUpInfo.statIncreases.intelligence)
        assertEquals("Wisdom increase should be 1", 1, levelUpInfo.statIncreases.wisdom)
        assertEquals("Charisma increase should be 1", 1, levelUpInfo.statIncreases.charisma)
    }

    @Test
    fun `test level 51 has no title unlock`() {
        // Given
        val hero = createHero(level = 50)

        // When
        val levelUpInfo = levelUpHeroUseCase(hero)

        // Then
        assertEquals("Should level up to 51", 51, levelUpInfo.newLevel)
        assertNull("No title at level 51", levelUpInfo.newTitle)
    }
}
