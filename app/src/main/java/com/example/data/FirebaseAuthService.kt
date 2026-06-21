package com.example.data

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

sealed interface AuthResult {
    data class Success(val user: FirebaseUser?) : AuthResult
    data class Error(val message: String) : AuthResult
}

class FirebaseAuthService(private val context: Context) {
    private val TAG = "FirebaseAuthService"

    // Gracefully handle if Firebase App is not initialized (e.g., missing google-services.json)
    val isFirebaseInitialized: Boolean by lazy {
        try {
            val apps = FirebaseApp.getApps(context)
            apps.isNotEmpty()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseApp is not initialized yet. Please follow configuration steps to connect to your project.", e)
            false
        }
    }

    private val firebaseAuth: FirebaseAuth? by lazy {
        if (isFirebaseInitialized) {
            try {
                FirebaseAuth.getInstance()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get FirebaseAuth instance", e)
                null
            }
        } else {
            null
        }
    }

    private val _currentUserFlow = MutableStateFlow<FirebaseUser?>(null)
    val currentUserFlow: StateFlow<FirebaseUser?> = _currentUserFlow

    init {
        // Observe auth state changes gracefully if initialized
        try {
            firebaseAuth?.addAuthStateListener { auth ->
                _currentUserFlow.value = auth.currentUser
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register AuthStateListener", e)
        }
    }

    val currentUser: FirebaseUser?
        get() = firebaseAuth?.currentUser

    /**
     * Firebase Sign Up with Email and Password
     */
    suspend fun signUpWithEmail(email: String, password: String): AuthResult {
        if (!isFirebaseInitialized || firebaseAuth == null) {
            return AuthResult.Error("Firebase is not initialized. Please add google-services.json.")
        }
        return try {
            val result = firebaseAuth!!.createUserWithEmailAndPassword(email, password).await()
            AuthResult.Success(result.user)
        } catch (e: Exception) {
            Log.e(TAG, "Sign up error: ${e.localizedMessage}", e)
            AuthResult.Error(e.localizedMessage ?: "Invalid registration email or password.")
        }
    }

    /**
     * Firebase Sign In with Email and Password
     */
    suspend fun signInWithEmail(email: String, password: String): AuthResult {
        if (!isFirebaseInitialized || firebaseAuth == null) {
            return AuthResult.Error("Firebase is not initialized. Please add google-services.json.")
        }
        return try {
            val result = firebaseAuth!!.signInWithEmailAndPassword(email, password).await()
            AuthResult.Success(result.user)
        } catch (e: Exception) {
            Log.e(TAG, "Sign in error: ${e.localizedMessage}", e)
            AuthResult.Error(e.localizedMessage ?: "Invalid credentials pool. Check email or password.")
        }
    }

    /**
     * Firebase Google Authentication using ID Token
     */
    suspend fun signInWithGoogleIdToken(idToken: String): AuthResult {
        if (!isFirebaseInitialized || firebaseAuth == null) {
            return AuthResult.Error("Firebase is not initialized. Please add google-services.json.")
        }
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth!!.signInWithCredential(credential).await()
            AuthResult.Success(result.user)
        } catch (e: Exception) {
            Log.e(TAG, "Google auth credential sign in error: ${e.localizedMessage}", e)
            AuthResult.Error(e.localizedMessage ?: "Google Credentials integration failed.")
        }
    }

    /**
     * Update user Profile Display Name
     */
    suspend fun updateDisplayName(name: String): Boolean {
        val user = currentUser ?: return false
        return try {
            val updates = com.google.firebase.auth.userProfileChangeRequest {
                displayName = name
            }
            user.updateProfile(updates).await()
            _currentUserFlow.value = firebaseAuth?.currentUser
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating profile template name", e)
            false
        }
    }

    /**
     * Sign Out
     */
    fun signOut() {
        try {
            firebaseAuth?.signOut()
            // Sign out google client if available to renew account picker prompt
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            val googleSignInClient = GoogleSignIn.getClient(context, gso)
            googleSignInClient.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "Error during sign out chain sequence", e)
        }
    }
}
