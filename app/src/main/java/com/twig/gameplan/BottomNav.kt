package com.twig.gameplan

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNav(navController: NavController) {
    // 3. Observe the current back stack to determine the selected screen
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination

    NavigationBar {
        NavigationBarItem(
            label = { Text("Home") },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            // 4. Check if the current route matches the screen's route
            selected = currentDestination?.route == Routes.Plans::class.qualifiedName,
            // 5. Navigate when the item is clicked
            onClick = {
                navController.navigate(Routes.Plans) {
                    // Pop up to the start destination of the graph to
                    // avoid building up a large stack of destinations
                    // on the back stack as users select items
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    // Avoid multiple copies of the same destination when
                    // re-selecting the same item
                    launchSingleTop = true
                    // Restore state when re-selecting a previously selected item
                    restoreState = true
                }
            }
        )
        NavigationBarItem(
            label = { Text("To Do") },
            icon = { Icon(Icons.Default.Checklist, contentDescription = "To Do") },
            // 4. Check if the current route matches the screen's route
            selected = currentDestination?.route == Routes.ToDo::class.qualifiedName,
            // 5. Navigate when the item is clicked
            onClick = {
                navController.navigate(Routes.ToDo) {
                    // Pop up to the start destination of the graph to
                    // avoid building up a large stack of destinations
                    // on the back stack as users select items
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    // Avoid multiple copies of the same destination when
                    // re-selecting the same item
                    launchSingleTop = true
                    // Restore state when re-selecting a previously selected item
                    restoreState = true
                }
            }
        )
        NavigationBarItem(
            label = { Text("Groups") },
            icon = { Icon(Icons.Default.Group, contentDescription = "Groups") },
            // 4. Check if the current route matches the screen's route
            selected = currentDestination?.route == Routes.Group::class.qualifiedName,
            // 5. Navigate when the item is clicked
            onClick = {
                navController.navigate(Routes.Group) {
                    // Pop up to the start destination of the graph to
                    // avoid building up a large stack of destinations
                    // on the back stack as users select items
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    // Avoid multiple copies of the same destination when
                    // re-selecting the same item
                    launchSingleTop = true
                    // Restore state when re-selecting a previously selected item
                    restoreState = true
                }
            }
        )
    }
}
