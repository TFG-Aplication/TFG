package com.asistente.core.data.repository

import com.asistente.core.data.local.daos.UserDao
import com.asistente.core.data.remote.UserRemoteService
import com.asistente.core.domain.models.User
import com.asistente.core.domain.ropositories.`interface`.UserRepositoryInterface

class UserRepository (

    private val localUser: UserDao,
    private val remoteUser: UserRemoteService
) : UserRepositoryInterface {


    override suspend fun getUserById(id: String): User? {
        var user: User? = localUser.getUsersById(id)

        if (user == null) {
            user = remoteUser.getUserByIdRemote(id)
            if (user != null) localUser.insertUser(user)
        }

        return user
    }

    override suspend fun getUserByEmail(email: String): User? {
        var user = localUser.getUsersByEmail(email)

        if (user == null) {
            user = remoteUser.getUserByEmailRemote(email)
            if (user != null) localUser.insertUser(user)
        }

        return user    }

    override suspend fun getUserByCodeQR(code: String): User? {
        return remoteUser.getUserByCodeQRRemote(code)
    }

}