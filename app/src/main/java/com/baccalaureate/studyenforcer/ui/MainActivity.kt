package com.baccalaureate.studyenforcer.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.baccalaureate.studyenforcer.BaccEnforcerApp
import com.baccalaureate.studyenforcer.ui.navigation.BottomNavBar
import com.baccalaureate.studyenforcer.ui.navigation.BottomNavItems
import com.baccalaureate.studyenforcer.ui.navigation.Screen
import com.baccalaureate.studyenforcer.ui.screens.games.ErrorHunterScreen
import com.baccalaureate.studyenforcer.ui.screens.games.ErrorHunterViewModel
import com.baccalaureate.studyenforcer.ui.screens.games.GamesScreen
import com.baccalaureate.studyenforcer.ui.screens.games.SpeedGrammarMatchScreen
import com.baccalaureate.studyenforcer.ui.screens.games.SpeedGrammarViewModel
import com.baccalaureate.studyenforcer.ui.screens.locker.FocusLockerScreen
import com.baccalaureate.studyenforcer.ui.screens.locker.FocusLockerViewModel
import com.baccalaureate.studyenforcer.ui.screens.onboarding.PermissionsScreen
import com.baccalaureate.studyenforcer.ui.screens.settings.SettingsScreen
import com.baccalaureate.studyenforcer.ui.screens.settings.SettingsViewModel
import com.baccalaureate.studyenforcer.ui.screens.subjects.SubjectsScreen
import com.baccalaureate.studyenforcer.ui.screens.subjects.SubjectsViewModel
import com.baccalaureate.studyenforcer.ui.screens.today.TodayScreen
import com.baccalaureate.studyenforcer.ui.screens.today.TodayViewModel
import com.baccalaureate.studyenforcer.ui.theme.StudyEnforcerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ensure background enforcement engine is running
        (application as? BaccEnforcerApp)?.startEnforcementService()

        setContent {
            StudyEnforcerTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val navController = rememberNavController()
                    MainScaffold(navController = navController)
                }
            }
        }
    }
}

@Composable
fun MainScaffold(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Today.route

    val isBottomBarVisible = BottomNavItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (isBottomBarVisible) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { screen ->
                        if (currentRoute != screen.route) {
                            navController.navigate(screen.route) {
                                popUpTo(Screen.Today.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Today.route
            ) {
                composable(Screen.Today.route) {
                    val viewModel: TodayViewModel = hiltViewModel()
                    TodayScreen(
                        viewModel = viewModel,
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                        onNavigateToLocker = { navController.navigate(Screen.FocusLocker.route) },
                        onNavigateToSubjects = { navController.navigate(Screen.Subjects.route) }
                    )
                }
                composable(Screen.Subjects.route) {
                    val viewModel: SubjectsViewModel = hiltViewModel()
                    SubjectsScreen(viewModel = viewModel)
                }
                composable(Screen.FocusLocker.route) {
                    val viewModel: FocusLockerViewModel = hiltViewModel()
                    FocusLockerScreen(viewModel = viewModel)
                }
                composable(Screen.Games.route) {
                    GamesScreen(
                        onNavigateToSpeedGrammar = { navController.navigate(Screen.SpeedGrammar.route) },
                        onNavigateToErrorHunter = { navController.navigate(Screen.ErrorHunter.route) }
                    )
                }

                // Sub-screens
                composable(Screen.Settings.route) {
                    val viewModel: SettingsViewModel = hiltViewModel()
                    SettingsScreen(
                        viewModel = viewModel,
                        onBackPressed = { navController.popBackStack() },
                        onNavigateToPermissions = { navController.navigate(Screen.Permissions.route) }
                    )
                }
                composable(Screen.Permissions.route) {
                    PermissionsScreen(onBackPressed = { navController.popBackStack() })
                }
                composable(Screen.SpeedGrammar.route) {
                    val viewModel: SpeedGrammarViewModel = hiltViewModel()
                    SpeedGrammarMatchScreen(
                        viewModel = viewModel,
                        onBackPressed = { navController.popBackStack() }
                    )
                }
                composable(Screen.ErrorHunter.route) {
                    val viewModel: ErrorHunterViewModel = hiltViewModel()
                    ErrorHunterScreen(
                        viewModel = viewModel,
                        onBackPressed = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
