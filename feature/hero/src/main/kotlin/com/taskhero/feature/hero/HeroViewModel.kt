package com.taskhero.feature.hero

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskhero.domain.hero.model.Hero
import com.taskhero.domain.hero.usecase.GetHeroUseCase
import com.taskhero.domain.hero.usecase.GetUnlockedTitlesUseCase
import com.taskhero.domain.hero.usecase.GetXpHistoryUseCase
import com.taskhero.domain.hero.usecase.UpdateHeroUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Hero profile screen using MVI architecture.
 */
@HiltViewModel
class HeroViewModel @Inject constructor(
    private val getHeroUseCase: GetHeroUseCase,
    private val updateHeroUseCase: UpdateHeroUseCase,
    private val getUnlockedTitlesUseCase: GetUnlockedTitlesUseCase,
    private val getXpHistoryUseCase: GetXpHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HeroUiState>(HeroUiState.Loading)
    val uiState: StateFlow<HeroUiState> = _uiState.asStateFlow()

    private val _effect = Channel<HeroEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        handleIntent(HeroIntent.LoadHero)
    }

    /**
     * Handle user intents
     */
    fun handleIntent(intent: HeroIntent) {
        when (intent) {
            is HeroIntent.LoadHero -> loadHero()
            is HeroIntent.UpdateDisplayName -> updateDisplayName(intent.name)
            is HeroIntent.UpdateAvatar -> updateAvatar(intent.uri)
            is HeroIntent.UpdateClass -> updateClass(intent.classType)
            is HeroIntent.SelectTitle -> selectTitle(intent.titleId)
        }
    }

    /**
     * Load hero data and related information
     */
    private fun loadHero() {
        viewModelScope.launch {
            _uiState.value = HeroUiState.Loading

            combine(
                getHeroUseCase(),
                getXpHistoryUseCase(limit = 10)
            ) { hero, xpHistory ->
                hero to xpHistory
            }
                .catch { exception ->
                    _uiState.value = HeroUiState.Error(
                        message = exception.message ?: "Failed to load hero data"
                    )
                }
                .collect { (hero, xpHistory) ->
                    if (hero != null) {
                        val titles = getUnlockedTitlesUseCase(hero.level)
                        _uiState.value = HeroUiState.Success(
                            hero = hero,
                            titles = titles,
                            recentXpHistory = xpHistory
                        )
                    } else {
                        _uiState.value = HeroUiState.Error(
                            message = "Hero not found. Please create a hero first."
                        )
                    }
                }
        }
    }

    /**
     * Update hero's display name
     */
    private fun updateDisplayName(name: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is HeroUiState.Success) {
                val updatedHero = currentState.hero.copy(displayName = name)
                updateHero(updatedHero, "Display name updated successfully")
            }
        }
    }

    /**
     * Update hero's avatar
     */
    private fun updateAvatar(uri: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is HeroUiState.Success) {
                val updatedHero = currentState.hero.copy(avatarUri = uri)
                updateHero(updatedHero, "Avatar updated successfully")
            }
        }
    }

    /**
     * Update hero's class type
     */
    private fun updateClass(classType: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is HeroUiState.Success) {
                val updatedHero = currentState.hero.copy(classType = classType)
                updateHero(updatedHero, "Class updated successfully")
            }
        }
    }

    /**
     * Select a title for the hero
     */
    private fun selectTitle(titleId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is HeroUiState.Success) {
                val selectedTitle = currentState.titles.find { it.id == titleId }
                if (selectedTitle != null && selectedTitle.isUnlocked) {
                    val updatedHero = currentState.hero.copy(currentTitle = selectedTitle.name)
                    updateHero(updatedHero, "Title '${selectedTitle.name}' equipped")
                } else {
                    _effect.send(HeroEffect.ShowSnackbar("This title is not unlocked yet"))
                }
            }
        }
    }

    /**
     * Update hero in repository and show feedback
     */
    private suspend fun updateHero(hero: Hero, successMessage: String) {
        updateHeroUseCase(hero)
            .onSuccess {
                _effect.send(HeroEffect.ShowSnackbar(successMessage))
                // Hero data will be automatically updated through the Flow
            }
            .onFailure { exception ->
                _effect.send(
                    HeroEffect.ShowSnackbar(
                        exception.message ?: "Failed to update hero"
                    )
                )
            }
    }
}
