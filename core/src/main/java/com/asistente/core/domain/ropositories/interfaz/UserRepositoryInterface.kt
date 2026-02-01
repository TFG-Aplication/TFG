package com.asistente.core.domain.ropositories.interfaz

import com.asistente.core.domain.models.User

interface UserRepositoryInterface {
    suspend fun getUserById(id: String): User?
    suspend fun getUserByEmail(email: String): User?

    suspend fun getUserByCodeQR(email: String): User?


}