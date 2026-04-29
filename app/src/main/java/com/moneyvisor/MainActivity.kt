package com.moneyvisor

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moneyvisor.core.designsystem.theme.MoneyVisorTheme
import com.moneyvisor.feature.dashboard.DashboardScreen
import com.moneyvisor.feature.dashboard.OnboardingScreen
import com.moneyvisor.feature.dashboard.TimelineScreen
import com.moneyvisor.feature.dashboard.StatusScreen
import com.moneyvisor.feature.dashboard.SettingsScreen
import com.moneyvisor.feature.dashboard.components.BottomNavigationBar
import com.moneyvisor.feature.transaction.TransactionScreen
import com.moneyvisor.data.repository.UserPreferencesRepository
import com.moneyvisor.core.designsystem.icons.MoneyVisorIcons
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    
    @Inject
    lateinit var userPrefs: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by userPrefs.themeMode.collectAsState(initial = "SYSTEM")

            MoneyVisorTheme(themeMode = themeMode) {
                val navController = rememberNavController()
                val isFirstLaunch by userPrefs.isFirstLaunch.collectAsState(initial = null)
                val isBiometricEnabled by userPrefs.isBiometricEnabled.collectAsState(initial = null)
                val isFabEnabled by userPrefs.isFabEnabled.collectAsState(initial = true)
                val coroutineScope = rememberCoroutineScope()
                
                var isAuthenticated by remember { mutableStateOf(false) }

                LaunchedEffect(isBiometricEnabled) {
                    if (isBiometricEnabled == true && !isAuthenticated) {
                        showBiometricPrompt { authenticated ->
                            isAuthenticated = authenticated
                        }
                    } else if (isBiometricEnabled == false) {
                        isAuthenticated = true
                    }
                }

                if (isFirstLaunch != null && (isAuthenticated || isFirstLaunch == true)) {
                    NavHost(
                        navController = navController,
                        startDestination = if (isFirstLaunch == true) "onboarding" else "main",
                        enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = tween(400)
                            ) + fadeIn(animationSpec = tween(400))
                        },
                        exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { -it },
                                animationSpec = tween(400)
                            ) + fadeOut(animationSpec = tween(400))
                        },
                        popEnterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { -it },
                                animationSpec = tween(400)
                            ) + fadeIn(animationSpec = tween(400))
                        },
                        popExitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = tween(400)
                            ) + fadeOut(animationSpec = tween(400))
                        }
                    ) {
                        composable("onboarding") {
                            OnboardingScreen(
                                onGetStartedClick = {
                                    coroutineScope.launch {
                                        userPrefs.setFirstLaunchCompleted()
                                        navController.navigate("main") {
                                            popUpTo("onboarding") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }
                        composable("main") {
                            MainScreen(
                                onNavigateToAddTransaction = {
                                    navController.navigate("add_transaction")
                                },
                                onNavigateToEditTransaction = { id ->
                                    navController.navigate("edit_transaction/$id")
                                },
                                isFabEnabled = isFabEnabled
                            )
                        }
                        composable("add_transaction") {
                            TransactionScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("edit_transaction/{transactionId}") { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("transactionId") ?: ""
                            TransactionScreen(
                                transactionId = id,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                } else if (isBiometricEnabled == true && !isAuthenticated) {
                    // Show a blank screen while waiting for biometric
                    Box(Modifier.fillMaxSize())
                }
            }
        }
    }

    private fun showBiometricPrompt(onResult: (Boolean) -> Unit) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    finish()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onResult(true)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("MoneyVisor Lock")
            .setSubtitle("Authenticate to open the app")
            .setNegativeButtonText("Exit")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}

@Composable
fun MainScreen(
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToEditTransaction: (String) -> Unit,
    isFabEnabled: Boolean = true
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

    var settledPage by remember { mutableIntStateOf(0) }
    
    // More robust tab settling logic
    LaunchedEffect(pagerState.settledPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            android.util.Log.d("MoneyVisor", "Strict Settled Page: ${pagerState.settledPage}")
            settledPage = pagerState.settledPage
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedIndex = pagerState.currentPage,
                onTabSelected = { index ->
                    coroutineScope.launch {
                        pagerState.scrollToPage(index)
                    }
                }
            )
        },
        floatingActionButton = {
            if (isFabEnabled) {
                FloatingActionButton(
                    onClick = onNavigateToAddTransaction,
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier
                        .padding(bottom = 16.dp, end = 8.dp)
                        .size(56.dp)
                ) {
                    Icon(
                        imageVector = MoneyVisorIcons.Add,
                        contentDescription = "Add Money",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            userScrollEnabled = true,
            beyondViewportPageCount = 3 // Pre-render all tabs to fix stickiness
        ) { page ->
            when (page) {
                0 -> DashboardScreen(
                    isVisible = settledPage == 0,
                    onNavigateToAddTransaction = onNavigateToAddTransaction,
                    onNavigateToEditTransaction = onNavigateToEditTransaction,
                    onNavigateToTimeline = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    }
                )
                1 -> TimelineScreen(
                    isVisible = settledPage == 1
                )
                2 -> StatusScreen(
                    isVisible = settledPage == 2
                )
                3 -> SettingsScreen(
                    isVisible = settledPage == 3
                )
            }
        }
    }
}
