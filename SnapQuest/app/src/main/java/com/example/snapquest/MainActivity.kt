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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.snapquest.navigation.Screens
import com.example.snapquest.screens.DailyQuestScreen
import com.example.snapquest.screens.HomeScreen
import com.example.snapquest.screens.NotificationsScreen
import com.example.snapquest.screens.QuestsScreen
import com.example.snapquest.screens.SettingsScreen
import com.example.snapquest.screens.SignInScreen
import com.example.snapquest.signin.GoogleAuthUiClient
import com.example.snapquest.signin.UserData
import com.example.snapquest.ui.theme.SnapQuestTheme
import com.example.snapquest.viewModels.HomeViewModel
import com.example.snapquest.viewModels.HomeViewModelFactory
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.example.snapquest.viewModels.SignInViewModel
import com.example.snapquest.viewModels.SignInViewModelFactory
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
class MainActivity : ComponentActivity() {
    private val userRepository by lazy {
        (application as SnapQuestApp).userRepository
    }

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext),
            userRepository = userRepository
        )
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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
                            val viewModel = viewModel<SignInViewModel>(
                                factory = SignInViewModelFactory(userRepository)
                            )
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
                            val homeViewModel = viewModel<HomeViewModel>(
                                factory = HomeViewModelFactory(userRepository)
                            )
                            SignInScreen(
                                state = state,
                                viewModel = homeViewModel,
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
                        composable(Screens.Home.route) {
                            val homeViewModel = viewModel<HomeViewModel>(
                                factory = HomeViewModelFactory(userRepository)
                            )
                            val locationPermissions = rememberMultiplePermissionsState(
                                permissions = listOf(
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.POST_NOTIFICATIONS,
                                    android.Manifest.permission.ACCESS_NOTIFICATION_POLICY,
                                )
                            )

                            // Request location permissions when the app get launched
                            LaunchedEffect(true) {
                                locationPermissions.launchMultiplePermissionRequest()
                            }

                            HomeScreen(
                                modifier = Modifier,
                                navController = navController,
                                viewModel = homeViewModel
                            )
                        }
                        composable(Screens.Quests.route) {
                            QuestsScreen(
                                modifier = Modifier,
                                navController = navController,
                            )
                        }
                        composable(Screens.DailyQuest.route) {
                            DailyQuestScreen(
                                modifier = Modifier,
                                navController = navController,
                            )
                        }
                        composable(Screens.Scan.route) {

                        }
                        composable(Screens.Notifications.route) {
                            NotificationsScreen(
                                modifier = Modifier,
                                navController = navController,
                            )
                        }
                        composable(Screens.Settings.route) {
                            val localContext = LocalContext.current

                            SettingsScreen(
                                modifier = Modifier,
                                navController = navController,
                                onSignOut = {
                                    lifecycleScope.launch {
                                        googleAuthUiClient.signOut()

                                        Toast.makeText(
                                            localContext,
                                            "Signed out",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        navController.navigate(Screens.SignIn.route)
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
