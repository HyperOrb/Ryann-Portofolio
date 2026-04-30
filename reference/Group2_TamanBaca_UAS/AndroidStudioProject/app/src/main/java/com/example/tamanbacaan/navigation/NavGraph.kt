package com.example.tamanbacaan.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tamanbacaan.components.DrawerContent
import com.example.tamanbacaan.ui.AdminLoginScreen
import com.example.tamanbacaan.ui.AdminScreen
import com.example.tamanbacaan.ui.BookDetailScreen
import com.example.tamanbacaan.ui.CatalogScreen
import com.example.tamanbacaan.ui.HomeScreen
import com.example.tamanbacaan.ui.LoansScreen
import com.example.tamanbacaan.ui.LoginScreen
import com.example.tamanbacaan.ui.OnboardingScreen
import com.example.tamanbacaan.ui.ProfileScreen
import com.example.tamanbacaan.ui.RegisterScreen
import com.example.tamanbacaan.ui.ReviewsScreen
import com.example.tamanbacaan.ui.SuperAdminScreen
import com.example.tamanbacaan.ui.admin.AdminAnalyticsScreen
import com.example.tamanbacaan.ui.books.AddBookScreen
import com.example.tamanbacaan.ui.books.BookListScreen
import com.example.tamanbacaan.ui.carousel.CarouselManagerScreen
import com.example.tamanbacaan.ui.users.UsersScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.tamanbacaan.util.SentimentAnalyzer
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookLendApp() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    // Auth state + role
    var isLoggedIn by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser != null) }
    var isAdmin by remember { mutableStateOf(false) }
    var isSuperAdmin by remember { mutableStateOf(false) }
    val firestore = remember { FirebaseFirestore.getInstance() }

    // Listen auth changes → fetch role
    DisposableEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val listener = FirebaseAuth.AuthStateListener { fa ->
            val user = fa.currentUser
            isLoggedIn = user != null
            if (user != null) {
                firestore.collection("users").document(user.uid).get()
                    .addOnSuccessListener { doc ->
                        val role = doc?.getString("role")
                        isAdmin = role == "admin" || role == "superadmin"
                        isSuperAdmin = role == "superadmin"
                    }
                    .addOnFailureListener {
                        isAdmin = false; isSuperAdmin = false
                    }
            } else {
                isAdmin = false; isSuperAdmin = false
            }
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    // Drawer items
    val drawerItems = remember(isAdmin, isSuperAdmin) {
        buildList {
            add(DrawerItem("home", "Home", Icons.Filled.Home))
            add(DrawerItem("catalog", "Catalog", Icons.Filled.Book))
            add(DrawerItem("loans", "Loans", Icons.Filled.History))
            if (isAdmin) add(DrawerItem(ROUTE_ADMIN, "Admin", Icons.Filled.AdminPanelSettings))
            if (isAdmin) add(DrawerItem(ROUTE_ANALYTICS, "Sentimen Analytics", Icons.Filled.BarChart))
            if (isSuperAdmin) add(DrawerItem("superadmin", "Super Admin", Icons.Filled.SupervisorAccount))
            add(DrawerItem("login", "Login", Icons.Filled.Person))
        }
    }

    val startDest = if (isLoggedIn) "home" else "onboarding"

    // Active route
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute: String = backStackEntry?.destination?.route ?: startDest

    // Which routes show drawer + bottom nav
    val routesWithDrawer = remember {
        setOf(
            "home", "catalog", "loans", "profile",
            ROUTE_ADMIN, "superadmin", ROUTE_ANALYTICS,
            ROUTE_BOOKS, ROUTE_ADD_BOOK, ROUTE_CAROUSEL, ROUTE_USERS, ROUTE_USER_DETAIL
        )
    }
    val routesWithBottomNav = setOf("home", "catalog", "loans", "profile")
    val showDrawer = currentRoute in routesWithDrawer
    val showBottomNav = currentRoute in routesWithBottomNav && isLoggedIn

    val scope = rememberCoroutineScope()

    if (showDrawer) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                DrawerContent(
                    items = drawerItems,
                    navController = navController,
                    drawerState = drawerState
                )
            }
        ) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text(titleForRoute(currentRoute)) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Menu")
                            }
                        }
                    )
                },
                bottomBar = {
                    if (showBottomNav) {
                        BottomNavigationBar(navController = navController, currentRoute = currentRoute)
                    }
                }
            ) { inner ->
                AppNavHost(
                    navController = navController,
                    startDest = startDest,
                    modifier = Modifier.padding(inner)
                )
            }
        }
    } else {
        // Onboarding / Login / Register / BookDetail
        Scaffold(
            topBar = {
                if (currentRoute != "onboarding" && !currentRoute.startsWith("book/")) {
                    CenterAlignedTopAppBar(title = { Text(titleForRoute(currentRoute)) })
                }
            }
        ) { inner ->
            AppNavHost(
                navController = navController,
                startDest = startDest,
                modifier = Modifier.padding(inner)
            )
        }
    }
}

@Composable
private fun BottomNavigationBar(navController: NavHostController, currentRoute: String) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(if (currentRoute == "home") Icons.Filled.Home else Icons.Outlined.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentRoute == "home",
            onClick = {
                navController.navigate("home") {
                    popUpTo("home") { inclusive = false }
                    launchSingleTop = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(if (currentRoute == "catalog") Icons.Filled.Book else Icons.Outlined.Book, contentDescription = "Catalog") },
            label = { Text("Catalog") },
            selected = currentRoute == "catalog",
            onClick = {
                navController.navigate("catalog") {
                    launchSingleTop = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(if (currentRoute == "loans") Icons.Filled.History else Icons.Outlined.History, contentDescription = "Loans") },
            label = { Text("Loans") },
            selected = currentRoute == "loans",
            onClick = {
                navController.navigate("loans") {
                    launchSingleTop = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(if (currentRoute == "profile") Icons.Filled.Person else Icons.Outlined.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = currentRoute == "profile",
            onClick = {
                navController.navigate("profile") {
                    launchSingleTop = true
                }
            }
        )
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    startDest: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDest,
        modifier = modifier
    ) {
        // ONBOARDING
        composable("onboarding") {
            OnboardingScreen(
                onNext = {
                    navController.navigate("login") {
                        singleTop()
                        popUpTo("onboarding") { inclusive = true }
                    }
                },
                onRegister = {
                    navController.navigate("register") {
                        singleTop()
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        // AUTH
        composable("login") {
            LoginScreen(
                onSubmit = {
                    navController.navigate("home") {
                        singleTop()
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate("register") { singleTop() } }
            )
        }
        composable("register") {
            RegisterScreen(
                onSubmit = {
                    navController.navigate("login") {
                        singleTop()
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        // USER ROUTES
        composable("home") {
            HomeScreen(
                onBookClick = { bookId ->
                    navController.navigate("book/$bookId")
                },
                onProfileClick = {
                    navController.navigate("profile") {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable("catalog") { CatalogScreen() }
        composable("loans") { LoansScreen() }

        // BOOK DETAIL
        composable(ROUTE_BOOK_DETAIL) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            BookDetailScreen(
                bookId = bookId,
                onBack = { navController.popBackStack() },
                onReviewsClick = { bId, bTitle ->
                    navController.navigate("book/$bId/reviews")
                }
            )
        }

        // REVIEWS & SENTIMENT ANALYSIS
        composable(ROUTE_REVIEWS) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            val context = LocalContext.current
            val sentimentAnalyzer = remember {
                SentimentAnalyzer(context)
            }
            ReviewsScreen(
                bookId = bookId,
                onBack = { navController.popBackStack() },
                sentimentAnalyzer = sentimentAnalyzer
            )
        }

        // ADMIN HUB
        composable(ROUTE_ADMIN) {
            AdminScreen(onNavigate = { route -> navController.navigate(route) { singleTop() } })
        }

        // ADMIN SUB-ROUTES
        composable(ROUTE_ADD_BOOK) {
            AddBookScreen(onDone = { navController.popBackStack() })
        }
        composable(ROUTE_BOOKS) {
            BookListScreen()
        }
        composable(ROUTE_CAROUSEL) {
            CarouselManagerScreen(onBack = { navController.popBackStack() })
        }
        composable(ROUTE_USERS) { UsersScreen() }

        // DETAIL USER (argumen uid) — sementara panggil UsersScreen() tanpa parameter
        composable(route = ROUTE_USER_DETAIL) { backStack ->
            // val uid = backStack.arguments?.getString("uid").orEmpty() // siap dipakai nanti
            UsersScreen()
        }

        // ADMIN ANALYTICS - Sentimen Analysis Results (admin only)
        composable(ROUTE_ANALYTICS) {
            AdminAnalyticsScreen(onBack = { navController.popBackStack() })
        }

        // SUPER ADMIN
        composable("superadmin") { SuperAdminScreen() }

        // ADMIN LOGIN (opsional)
        composable("adminLogin") { AdminLoginScreen() }

        // PROFILE
        composable("profile") {
            ProfileScreen(
                onLogout = {
                    navController.navigate("onboarding") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

/* ---------- Helpers ---------- */

data class DrawerItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun currentRoute(navController: NavHostController): String {
    val backStackEntry by navController.currentBackStackEntryAsState()
    return backStackEntry?.destination?.route ?: "home"
}

// Title untuk TopBar
fun titleForRoute(route: String): String = when {
    route == "onboarding" -> "Pojok Bacaan"
    route == "login"      -> "Login"
    route == "register"   -> "Register"
    route == "home"       -> "Home"
    route == "catalog"    -> "Catalog"
    route == "loans"      -> "Loans"
    route == "profile"    -> "Profile"
    route == "superadmin" -> "Super Admin"
    route == ROUTE_ADMIN  -> "Admin"
    route == ROUTE_ANALYTICS -> "Sentimen Analytics"
    route == ROUTE_BOOKS || route == ROUTE_ADD_BOOK || route.startsWith("admin/books") -> "Kelola Buku"
    route == ROUTE_CAROUSEL -> "Kelola Carousel"
    route == ROUTE_USERS || route == ROUTE_USER_DETAIL || route.startsWith("admin/users") -> "Kelola Pengguna"
    route.startsWith("book/") && route.contains("reviews") -> "Reviews"
    route.startsWith("book/") -> "Detail Buku"
    else -> "Taman Bacaan"
}

// Small nav option helper
private fun NavOptionsBuilder.singleTop() {
    launchSingleTop = true
}
