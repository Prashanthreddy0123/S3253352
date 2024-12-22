package uk.ac.tees.mad.estore.ui.screens.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.ac.tees.mad.estore.repository.AuthRepository
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState = _authState.asStateFlow()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword) }
    }

    fun signIn() {
        if (!validateSignInForm()) return

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.signIn(
                _uiState.value.email,
                _uiState.value.password
            )
            _authState.value = when {
                result.isSuccess -> AuthState.Success(result.getOrNull()!!)
                result.isFailure -> AuthState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
                else -> AuthState.Error("Unknown error")
            }
        }
    }

    fun signUp() {
        if (!validateSignUpForm()) return

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.signUp(
                _uiState.value.email,
                _uiState.value.password
            )
            _authState.value = when {
                result.isSuccess -> AuthState.Success(result.getOrNull()!!)
                result.isFailure -> AuthState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
                else -> AuthState.Error("Unknown error")
            }
        }
    }

    private fun validateSignInForm(): Boolean {
        val emailError = if (_uiState.value.email.isEmpty()) "Email is required"
        else if (!Patterns.EMAIL_ADDRESS.matcher(_uiState.value.email).matches()) "Invalid email"
        else null

        val passwordError = if (_uiState.value.password.isEmpty()) "Password is required"
        else if (_uiState.value.password.length < 6) "Password must be at least 6 characters"
        else null

        _uiState.update {
            it.copy(emailError = emailError, passwordError = passwordError)
        }

        return emailError == null && passwordError == null
    }

    private fun validateSignUpForm(): Boolean {
        val basicValidation = validateSignInForm()
        val confirmPasswordError = if (_uiState.value.password != _uiState.value.confirmPassword) {
            "Passwords don't match"
        } else null

        _uiState.update {
            it.copy(confirmPasswordError = confirmPasswordError)
        }

        return basicValidation && confirmPasswordError == null
    }
}

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isSignUp: Boolean = false
)

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}