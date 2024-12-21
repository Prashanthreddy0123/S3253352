package uk.ac.tees.mad.estore.repository

import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<FirebaseUser>
    suspend fun signUp(email: String, password: String): Result<FirebaseUser>
    fun getCurrentUser(): FirebaseUser?
    fun signOut()
}