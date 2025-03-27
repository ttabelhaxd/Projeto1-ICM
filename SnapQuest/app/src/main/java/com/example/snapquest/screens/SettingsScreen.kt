package com.example.snapquest.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.snapquest.ui.components.Navbar
import com.example.snapquest.ui.components.TitleMessage

@Composable
fun SettingsScreen(
    modifier: Modifier,
    navController: NavHostController,
    onSignOut: () -> Unit,
) {
    Scaffold(
        bottomBar =  {
            Navbar(modifier = Modifier, navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ){
            TitleMessage(modifier = modifier, text1 = "", text2 = "Settings")
            Button(
                onClick = onSignOut,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Row {
                    Text(text = "Logout")
                    Icons.AutoMirrored.Filled.Logout
                }
            }
        }
    }
}