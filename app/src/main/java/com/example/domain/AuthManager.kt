package com.example.domain

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class RoleMode {
    CASHIER, OWNER
}

class AuthManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("omah_auth_prefs", Context.MODE_PRIVATE)

    private val _currentMode = MutableStateFlow(RoleMode.CASHIER)
    val currentMode: StateFlow<RoleMode> = _currentMode.asStateFlow()

    private val _activeCashierName = MutableStateFlow("Kasir 1")
    val activeCashierName: StateFlow<String> = _activeCashierName.asStateFlow()

    init {
        // Initialize default owner PIN if not set
        if (!prefs.contains(KEY_OWNER_PIN)) {
            prefs.edit().putString(KEY_OWNER_PIN, DEFAULT_PIN).apply()
        }
    }

    fun setCashierName(name: String) {
        _activeCashierName.value = name
    }

    fun verifyAndUnlockOwner(enteredPin: String): Boolean {
        val savedPin = prefs.getString(KEY_OWNER_PIN, DEFAULT_PIN) ?: DEFAULT_PIN
        return if (enteredPin == savedPin) {
            _currentMode.value = RoleMode.OWNER
            true
        } else {
            false
        }
    }

    fun lockToCashier() {
        _currentMode.value = RoleMode.CASHIER
    }

    fun changeOwnerPin(oldPin: String, newPin: String): Boolean {
        val savedPin = prefs.getString(KEY_OWNER_PIN, DEFAULT_PIN) ?: DEFAULT_PIN
        return if (oldPin == savedPin && newPin.length >= 6) {
            prefs.edit().putString(KEY_OWNER_PIN, newPin).apply()
            true
        } else {
            false
        }
    }

    companion object {
        private const val KEY_OWNER_PIN = "owner_pin"
        private const val DEFAULT_PIN = "123456"
    }
}
