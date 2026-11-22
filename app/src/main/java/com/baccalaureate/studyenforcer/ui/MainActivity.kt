package com.baccalaureate.studyenforcer.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.baccalaureate.studyenforcer.BaccEnforcerApp
import com.baccalaureate.studyenforcer.data.repository.StudyRepository
import com.baccalaureate.studyenforcer.service.StudyEnforcementService
import com.baccalaureate.studyenforcer.ui.navigation.BottomNavBar
import com.baccalaureate.studyenforcer.ui.navigation.BottomNavItems
import com.baccalaureate.studyenforcer.ui.navigation.Screen
import com.baccalaureate.studyenforcer.ui.screens.games.ErrorHunterScreen
import com.baccalaureate.studyenforcer.ui.screens.games.ErrorHunterViewModel
import com.baccalaureate.studyenforcer.ui.screens.games.GamesScreen
import com.baccalaureate.studyenforcer.ui.screens.games.GamesViewModel
import com.baccalaureate.studyenforcer.ui.screens.games.SpeedGrammarMatchScreen
import com.baccalaureate.studyenforcer.ui.screens.games.SpeedGrammarViewModel
import com.baccalaureate.studyenforcer.ui.screens.games.SyntaxFillScreen
import com.baccalaureate.studyenforcer.ui.screens.games.SyntaxFillViewModel
import com.baccalaureate.studyenforcer.ui.screens.games.TrueFalseScreen
import com.baccalaureate.studyenforcer.ui.screens.games.TrueFalseViewModel
import com.baccalaureate.studyenforcer.ui.screens.lessons.LessonReaderScreen
import com.baccalaureate.studyenforcer.ui.screens.lessons.LessonReaderViewModel
import com.baccalaureate.studyenforcer.ui.screens.locker.FocusLockerScreen
import com.baccalaureate.studyenforcer.ui.screens.locker.FocusLockerViewModel
import com.baccalaureate.studyenforcer.ui.screens.onboarding.OnboardingScreen
import com.baccalaureate.studyenforcer.ui.screens.onboarding.PermissionsScreen
import com.baccalaureate.studyenforcer.ui.screens.policy.PolicyScreen
import com.baccalaureate.studyenforcer.ui.screens.quiz.ActiveQuizScreen
import com.baccalaureate.studyenforcer.ui.screens.settings.SettingsScreen
import com.baccalaureate.studyenforcer.ui.screens.settings.SettingsViewModel
import com.baccalaureate.studyenforcer.ui.screens.subjects.SubjectsScreen
import com.baccalaureate.studyenforcer.ui.screens.subjects.SubjectsViewModel
import com.baccalaureate.studyenforcer.ui.screens.today.TodayScreen
import com.baccalaureate.studyenforcer.ui.screens.today.TodayViewModel
import com.baccalaureate.studyenforcer.ui.theme.AccentEmerald
import com.baccalaureate.studyenforcer.ui.theme.CanvasAlabaster
import com.baccalaureate.studyenforcer.ui.theme.StudyEnforcerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var studyRepository: StudyRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ensure background enforcement engine is running
        (application as? BaccEnforcerApp)?.startEnforcementService()

        setContent {
            StudyEnforcerTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val userStats by studyRepository.getUserStatsFlow().collectAsState(initial = null)

                    if (userStats == null) {
                        // Smooth minimalist loader
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(CanvasAlabaster),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AccentEmerald)
                        }
                    } else {
                        val navController = rememberNavController()
                        val startDestination = if (userStats?.isOnboardingCompleted == true) {
                            Screen.Today.route
                        } else {
                            Screen.Onboarding.route
                        }

                        // Register In-App Quiz Broadcast Receiver
                        InAppQuizBroadcastHandler(
                            context = this@MainActivity,
                            navController = navController
                        )

                        MainScaffold(
                            navController = navController,
                            startDestination = startDestination,
                            studyRepository = studyRepository
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InAppQuizBroadcastHandler(
    context: Context,
    navController: NavHostController
) {
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (intent?.action == StudyEnforcementService.ACTION_TRIGGER_IN_APP_QUIZ) {
                    navController.navigate(Screen.ActiveQuiz.createRoute(-1L))
                }
            }
        }
        val filter = IntentFilter(StudyEnforcementService.ACTION_TRIGGER_IN_APP_QUIZ)
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {
                // Ignore if unregistered
            }
        }
    }
}

@Composable
fun MainScaffold(
    navController: NavHostController,
    startDestination: String,
    studyRepository: StudyRepository
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: startDestination

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
                startDestination = startDestination
            ) {
                // Onboarding First-Run Flow
                composable(Screen.Onboarding.route) {
                    OnboardingScreen(
                        studyRepository = studyRepository,
                        onComplete = {
                            navController.navigate(Screen.Today.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    )
                }

                // 4 Main Bottom Navigation Tabs
                composable(Screen.Today.route) {
                    val viewModel: TodayViewModel = hiltViewModel()
                    TodayScreen(
                        viewModel = viewModel,
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                        onNavigateToSubjects = { navController.navigate(Screen.Subjects.route) },
                        onStartInAppQuiz = { navController.navigate(Screen.ActiveQuiz.createRoute(-1L)) }
                    )
                }

                composable(Screen.Subjects.route) {
                    val viewModel: SubjectsViewModel = hiltViewModel()
                    SubjectsScreen(
                        viewModel = viewModel,
                        onSolveQuestion = { questionId ->
                            navController.navigate(Screen.ActiveQuiz.createRoute(questionId))
                        },
                        onOpenLesson = { lessonId ->
                            navController.navigate(Screen.LessonReader.createRoute(lessonId))
                        }
                    )
                }

                composable(Screen.FocusLocker.route) {
                    val viewModel: FocusLockerViewModel = hiltViewModel()
                    FocusLockerScreen(
                        viewModel = viewModel,
                        onNavigateToPolicy = { navController.navigate(Screen.Policy.route) }
                    )
                }

                composable(Screen.Games.route) {
                    val viewModel: GamesViewModel = hiltViewModel()
                    GamesScreen(
                        viewModel = viewModel,
                        onNavigateToSpeedGrammar = { navController.navigate(Screen.SpeedGrammar.route) },
                        onNavigateToErrorHunter = { navController.navigate(Screen.ErrorHunter.route) },
                        onNavigateToTrueFalse = { subject, topic ->
                            navController.navigate(Screen.TrueFalse.createRoute(subject, topic))
                        },
                        onNavigateToSyntaxFill = { subject, topic ->
                            navController.navigate(Screen.SyntaxFill.createRoute(subject, topic))
                        }
                    )
                }

                // Lesson Reader Screen
                composable(
                    route = Screen.LessonReader.route,
                    arguments = listOf(
                        navArgument("lessonId") {
                            type = NavType.StringType
                            defaultValue = "101"
                        }
                    )
                ) {
                    val viewModel: LessonReaderViewModel = hiltViewModel()
                    LessonReaderScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onStartLessonQuiz = {
                            navController.navigate(Screen.ActiveQuiz.createRoute(-1L))
                        }
                    )
                }

                // Sub-screens & Tools
                composable(Screen.Settings.route) {
                    val viewModel: SettingsViewModel = hiltViewModel()
                    SettingsScreen(
                        viewModel = viewModel,
                        onBackPressed = { navController.popBackStack() },
                        onNavigateToPermissions = { navController.navigate(Screen.Permissions.route) },
                        onNavigateToPolicy = { navController.navigate(Screen.Policy.route) }
                    )
                }

                composable(Screen.Policy.route) {
                    PolicyScreen(onBackPressed = { navController.popBackStack() })
                }

                composable(Screen.Permissions.route) {
                    PermissionsScreen(onBackPressed = { navController.popBackStack() })
                }

                composable(
                    route = Screen.ActiveQuiz.route,
                    arguments = listOf(
                        navArgument("questionId") {
                            type = NavType.LongType
                            defaultValue = -1L
                        }
                    )
                ) { backStackEntry ->
                    val qId = backStackEntry.arguments?.getLong("questionId")?.takeIf { it > 0 }
                    ActiveQuizScreen(
                        studyRepository = studyRepository,
                        targetQuestionId = qId,
                        onFinished = { navController.popBackStack() }
                    )
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

                composable(
                    route = Screen.TrueFalse.route,
                    arguments = listOf(
                        navArgument("subject") {
                            type = NavType.StringType
                            defaultValue = "ALL"
                        },
                        navArgument("topic") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val subject = backStackEntry.arguments?.getString("subject") ?: "ALL"
                    val topic = backStackEntry.arguments?.getString("topic")
                    val viewModel: TrueFalseViewModel = hiltViewModel()
                    TrueFalseScreen(
                        viewModel = viewModel,
                        subject = subject,
                        topic = topic,
                        onBackPressed = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.SyntaxFill.route,
                    arguments = listOf(
                        navArgument("subject") {
                            type = NavType.StringType
                            defaultValue = "ALL"
                        },
                        navArgument("topic") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val subject = backStackEntry.arguments?.getString("subject") ?: "ALL"
                    val topic = backStackEntry.arguments?.getString("topic")
                    val viewModel: SyntaxFillViewModel = hiltViewModel()
                    SyntaxFillScreen(
                        viewModel = viewModel,
                        subject = subject,
                        topic = topic,
                        onBackPressed = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
