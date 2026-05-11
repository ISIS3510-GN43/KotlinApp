package com.techsolutions.worqee.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techsolutions.worqee.models.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MainUiState {
    object Loading : MainUiState()
    object Authenticated : MainUiState()
    object Unauthenticated : MainUiState()
}

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun restoreSession() {
        viewModelScope.launch {
            val user = SessionRepository.restoreSession()

            _uiState.value =
                if (user != null) {
                    MainUiState.Authenticated
                } else {
                    MainUiState.Unauthenticated
                }
        }
    }
}