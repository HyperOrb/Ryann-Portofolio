package com.example.tamanbacaan.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.example.tamanbacaan.navigation.DrawerItem
import com.example.tamanbacaan.navigation.currentRoute
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.example.tamanbacaan.data.AuthRepo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerContent(
    items: List<DrawerItem>,
    navController: NavHostController,
    drawerState: DrawerState
) {
    val scope = rememberCoroutineScope()
    val current = currentRoute(navController)

    var isLoggedIn by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser != null) }
    var userName by remember { mutableStateOf<String?>(null) }

    // Dengarkan perubahan auth
    DisposableEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val listener = FirebaseAuth.AuthStateListener { fa ->
            val user = fa.currentUser
            isLoggedIn = user != null
            if (user != null) {
                AuthRepo.loadMyProfile { p, _ -> userName = p?.name }
            } else {
                userName = null
            }
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    // Sidebar pakai krem dari theme
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.background
    ) {
        // Header sapaan
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (!userName.isNullOrBlank()) {
                Text("Hai, $userName", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
            }
        }
        HorizontalDivider()

        // Menu utama (tanpa login/logout)
        items.filterNot { it.route == "login" }.forEach { item ->
            NavigationDrawerItem(
                label = { Text(item.label) },
                selected = current == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                    scope.launch { drawerState.close() }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f)) // dorong ke bawah

        // Login/Logout di bawah
        val isLogout = isLoggedIn
        val labelText = if (isLogout) "Logout" else "Login"
        val actionColor = if (isLogout) Color(0xFFB71C1C) else MaterialTheme.colorScheme.onSurface

        NavigationDrawerItem(
            label = { Text(labelText, color = actionColor) },
            selected = false,
            onClick = {
                scope.launch { drawerState.close() }
                if (isLogout) {
                    AuthRepo.logout()
                    navController.navigate("login") {
                        launchSingleTop = true
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    }
                } else {
                    navController.navigate("login") {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    }
                }
            },
            icon = {
                Icon(
                    imageVector = items.first { it.route == "login" }.icon,
                    contentDescription = labelText,
                    tint = actionColor
                )
            },
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = Color.Transparent,
                selectedContainerColor = Color.Transparent
            )
        )
        Spacer(Modifier.height(12.dp))
    }
}