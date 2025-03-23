package com.example.snapquest

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.snapquest.navigation.Screens
import com.example.snapquest.screens.SignInScreen
import com.example.snapquest.signin.GoogleAuthUiClient
import com.example.snapquest.signin.UserData
import com.example.snapquest.ui.theme.SnapQuestTheme
import com.example.snapquest.viewModels.HomeViewModel
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.example.snapquest.viewModels.SignInViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set up fusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            SnapQuestTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = Screens.SignIn.route
                    ) {
                        composable(Screens.SignIn.route) {
                            val viewModel = viewModel<SignInViewModel>()
                            val state by viewModel.state.collectAsStateWithLifecycle()

                            LaunchedEffect(true) {
                                viewModel.loginUser(
                                    UserData(
                                    userId = "",
                                    username = "",
                                    profilePictureURL = ""
                                )
                                )
                                viewModel.resetState()

                            }

                            LaunchedEffect(key1 = Unit) {
                                viewModel.resetState()
                                val signedInUser = googleAuthUiClient.getSignedInUser()

                                if (signedInUser != null && signedInUser.userId?.isNotBlank() == true) {
                                    navController.navigate("home")
                                    Log.d(
                                        "MainActivity",
                                        "User already signed in with Google -> ${signedInUser.username}"
                                    )
                                    viewModel.loginUser(signedInUser)
                                } else {
                                    Log.d("MainActivity", "User not signed in with Google")
                                }
                            }

                            val launcher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartIntentSenderForResult(),
                                onResult = { result ->
                                    if (result.resultCode == RESULT_OK) {
                                        lifecycleScope.launch {
                                            val signInResult = googleAuthUiClient.signInWithIntent(
                                                intent = result.data ?: return@launch
                                            )
                                            viewModel.onSignInResult(signInResult)
                                            viewModel.loginUser(signInResult.data ?: return@launch)
                                        }
                                    }
                                }
                            )

                            LaunchedEffect(key1 = state.isSignInSuccessful) {
                                if (state.isSignInSuccessful) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Sign in successful",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    navController.navigate("home")
                                    viewModel.resetState()
                                }
                            }
                            val homeviewModel = viewModel<HomeViewModel>()
                            SignInScreen(
                                state = state,
                                viewModel = homeviewModel,
                                onSignInClick = {
                                    lifecycleScope.launch {
                                        val signInIntentSender = googleAuthUiClient.signIn()
                                        launcher.launch(
                                            IntentSenderRequest.Builder(
                                                signInIntentSender ?: return@launch
                                            ).build()
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
