package com.deltasoft.pharmatracker.screens.login


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val repository = LoginRepository(this)

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState = _loginState.asStateFlow()

    fun login(phoneNumber: String) {
        _loginState.value = LoginState.Loading
        try {
            repository.generateOtp(phoneNumber)
        } catch (e: Exception) {
            _loginState.value = LoginState.Error("Login failed: ${e.message}")
        }
    }

    fun updateLoginState(code: Int, errorMessage: String){
        when(code){
            200->{
                _loginState.value = LoginState.Success
            }
            400->{
                _loginState.value = LoginState.Error(errorMessage)
            }
            500->{
                _loginState.value = LoginState.Error(errorMessage)
            }
            else->{
                _loginState.value = LoginState.Error(errorMessage)
            }
        }
    }

    fun clearLoginState() {
        _loginState.value = LoginState.Idle
    }
}

