package edu.cs371m.routenest.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import jakarta.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository
@Inject constructor(
    private val auth: FirebaseAuth
) {
    val currentUser: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun login(email: String, password: String) {
        Log.d("AuthRepository", "Logging in with email: $email")
        auth.signInWithEmailAndPassword(email, password).await()
        Log.d("AuthRepository", "Logged in successfully user id: ${auth.currentUser?.uid}")
    }

    fun logout() {
        Log.d("AuthRepository", "Logging out user: ${auth.currentUser?.uid}")
        auth.signOut()
    }

    suspend fun signUp(name: String, email: String, password: String) {
        Log.d("AuthRepository", "Signing up with email: $email and name: $name")
        auth.createUserWithEmailAndPassword(email, password).await()
        Log.d("AuthRepository", "Signed up successfully user id: ${auth.currentUser?.uid}")
        auth.currentUser?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build())?.await()
        Log.d("AuthRepository", "Updated user name successfully user id: ${auth.currentUser?.uid} and name: $name")
    }
}