package com.example.tripmanager.data.session

object UserSession {
    private var currentUser: String? = null
    
    fun setCurrentUser(username: String) {
        currentUser = username
    }
    
    fun getCurrentUser(): String? = currentUser
    
    fun clearSession() {
        currentUser = null
    }
}
