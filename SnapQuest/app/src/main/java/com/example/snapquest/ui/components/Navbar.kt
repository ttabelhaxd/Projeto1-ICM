package com.example.snapquest.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationSearching
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import com.example.snapquest.navigation.Screens

data class NavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNews: Boolean,
    val badgeCounter: Int? = null,
    val route: String
)


@Composable
fun Navbar(
    modifier: Modifier,
    navController: NavController
) {
    val items = listOf(
        NavItem(
            label = "Home",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            hasNews = false,
            route = Screens.Home.route
        ),
        NavItem(
            label = "Quests",
            selectedIcon = Icons.Filled.LocationSearching,
            unselectedIcon = Icons.Outlined.LocationSearching,
            hasNews = false,
            route = Screens.Quests.route
        ),

        NavItem(
            label = "Alerts",
            selectedIcon = Icons.Filled.Notifications,
            unselectedIcon = Icons.Outlined.Notifications,
            hasNews = false,
            route = Screens.Notifications.route
        ),

        NavItem(
            label = "Settings",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings,
            hasNews = false,
            route = Screens.Settings.route
        )


    )


    NavigationBar(modifier = Modifier) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = navController.currentDestination?.route == item.route,
                onClick = {
                    navController.navigate(item.route)
                },
                icon = {
                    BadgedBox(
                        badge =
                        {
                            if (item.badgeCounter != null) {
                                Badge {
                                    Text(text = item.badgeCounter.toString())
                                }
                            } else if (item.hasNews) {
                                Badge()
                            }

                        }) {
                        Icon(
                            imageVector =
                            if (navController.currentDestination?.route == item.route) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label
                        )
                    }
                },
                label = {
                    Text(text = item.label)
                },
            )
        }
    }
}