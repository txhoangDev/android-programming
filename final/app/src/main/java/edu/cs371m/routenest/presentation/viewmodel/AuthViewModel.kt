package edu.cs371m.routenest.presentation.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.cs371m.routenest.data.repository.AuthRepository
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
): ViewModel() {
    private val _user = mutableStateOf<FirebaseUser?>(null)
    val user: State<FirebaseUser?> = _user
    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    private val _errors = Channel<String>()
    val errors = _errors.receiveAsFlow()

    private val _success = Channel<Boolean>()
    val success = _success.receiveAsFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _user.value = user
            }
        }
    }

    fun login(email: String, password: String) {
        Log.d("AuthViewModel", "Logging in with email: $email")
        viewModelScope.launch {
            _loading.value = true
            try {
                authRepository.login(email, password)
            } catch(e: Exception) {
                Log.d("AuthViewModel", "Login failed: $e")
                _errors.send(e.message ?: "Unknown error")
                _loading.value = false
                _success.send(false)
                return@launch
            }
            _loading.value = false
            _success.send(true)
        }
    }

    fun logout() {
        Log.d("AuthViewModel", "Logging out user")
        authRepository.logout()
    }

    fun createAccount(name: String, email: String, password: String) {
        Log.d("AuthViewModel", "Creating account with email: $email")
        viewModelScope.launch {
            _loading.value = true
            try {
                authRepository.signUp(name, email, password)
            } catch(e: Exception) {
                Log.d("AuthViewModel", "Login failed: $e")
                _errors.send(e.message ?: "Unknown error")
                _loading.value = false
                _success.send(false)
                return@launch
            }
            _loading.value = false
            _success.send(true)
        }
    }
}