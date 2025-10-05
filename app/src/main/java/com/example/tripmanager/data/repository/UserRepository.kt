package com.example.tripmanager.data.repository

import com.example.tripmanager.data.dao.UserDao
import com.example.tripmanager.data.model.User
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val userDao: UserDao
) {
    suspend fun login(username: String, password: String): User? {
        return userDao.getUser(username, password)
    }

    suspend fun register(user: User): Boolean {
        return try {
            userDao.insertUser(user)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun userExists(username: String): Boolean {
        return userDao.userExists(username) > 0
    }
}
