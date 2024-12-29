package uk.ac.tees.mad.estore.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {
    override suspend fun signIn(email: String, password: String): Result<FirebaseUser> =
        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun signUp(email: String, password: String): Result<FirebaseUser> =
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            firestore.collection("users").document(result.user!!.uid).set(
                hashMapOf(
                    "email" to email,
                    "password" to password
                )
            ).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override fun getCurrentUser(): FirebaseUser? = auth.currentUser

    override fun signOut() = auth.signOut()
}