package com.example.fitfurs.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitfurs.UserData
import com.example.fitfurs.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    private val repo = UserRepository()

    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    fun loadUser(userId: String) {
        viewModelScope.launch {
            _loading.value = true
            _userData.value = repo.getUserData(userId)
            _loading.value = false
        }
    }

    fun updateField(userId: String, field: String, value: String) {
        viewModelScope.launch {
            repo.updateField(userId, field, value)
            loadUser(userId)
        }
    }
}
