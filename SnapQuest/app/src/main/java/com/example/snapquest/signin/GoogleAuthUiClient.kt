package com.example.snapquest.signin

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import com.example.snapquest.R
import com.example.snapquest.repositories.FirestoreUserRepository
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class GoogleAuthUiClient(
    private val context: Context,
    private val oneTapClient: SignInClient,
    private val firestoreUserRepository: FirestoreUserRepository
) {
    private val auth = Firebase.auth

    suspend fun signIn(): IntentSender? {
        return try {
            val result = oneTapClient.beginSignIn(buildSignInRequest()).await()
            result?.pendingIntent?.intentSender
        } catch (e: Exception) {
            Log.e("GoogleAuthUiClient", "Sign-in failed", e)
            if (e is CancellationException) throw e
            null
        }
    }

    suspend fun signInWithIntent(intent: Intent): SignInResult {
        return try {
            val credential = oneTapClient.getSignInCredentialFromIntent(intent)
            val googleIdToken = credential.googleIdToken
            val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
            val user = auth.signInWithCredential(googleCredentials).await().user
            user?.let { firebaseUser ->
                val userData = UserData(
                    userId = firebaseUser.uid,
                    username = firebaseUser.displayName ?: "",
                    profilePictureURL = firebaseUser.photoUrl?.toString(),
                    email = firebaseUser.email
                )

                SignInResult(
                    data = userData,
                    errorMessage = null
                )
            } ?: SignInResult(
                data = null,
                errorMessage = "Usuário não encontrado"
            )
        } catch (e: Exception) {
            Log.e("GoogleAuthUiClient", "Sign-in with intent failed", e)
            if (e is CancellationException) throw e
            SignInResult(
                data = null,
                errorMessage = e.message
            )
        }
    }

    suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
            auth.signOut()
            firestoreUserRepository.resetCurrentUser()
        } catch (e: Exception) {
            Log.e("GoogleAuthUiClient", "Sign-out failed", e)
            if (e is CancellationException) throw e
        }
    }

    fun getSignedInUser(): UserData? = auth.currentUser?.run {
        UserData(
            userId = uid,
            username = displayName,
            profilePictureURL = photoUrl?.toString(),
            email = email
        )
    }

    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.web_client_id))
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }
}