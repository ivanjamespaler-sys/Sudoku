package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Carpenter
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Carpenter
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.PreferencesRepository
import com.example.data.local.SudokuDatabase
import com.example.data.local.SudokuRepository
import com.example.domain.model.Difficulty
import com.example.domain.model.GameMode
import com.example.ui.components.woodGrainBackground
import com.example.ui.screens.GameScreen
import com.example.ui.screens.GuideScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.WorkshopScreen
import com.example.ui.theme.LocalWoodPalette
import com.example.ui.theme.TimberSudokuTheme
import com.example.ui.viewmodel.SudokuViewModel
import com.example.ui.viewmodel.SudokuViewModelFactory

const val ROUTE_WORKSHOP = "workshop"
const val ROUTE_LEDGER = "ledger"
const val ROUTE_CRAFT = "craft"
const val ROUTE_BOARD = "board"

private data class NavTab(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val topTabs = listOf(
    NavTab(ROUTE_WORKSHOP, R.string.nav_workshop, Icons.Filled.GridOn, Icons.Outlined.GridOn),
    NavTab(ROUTE_LEDGER, R.string.nav_ledger, Icons.Filled.Assessment, Icons.Outlined.Assessment),
    NavTab(ROUTE_CRAFT, R.string.nav_craft, Icons.Filled.Carpenter, Icons.Outlined.Carpenter)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TimberSudokuApp()
        }
    }
}

@Composable
fun TimberSudokuApp() {
    val context = LocalContext.current.applicationContext
    val repository = remember(context) {
        val db = SudokuDatabase.getInstance(context)
        val prefs = PreferencesRepository(context)
        SudokuRepository(db.sudokuDao(), prefs)
    }
    val viewModel: SudokuViewModel = viewModel(factory = SudokuViewModelFactory(repository))

    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val selectedHomeMode by viewModel.selectedHomeMode.collectAsStateWithLifecycle()
    val resumePreview by viewModel.resumePreview.collectAsStateWithLifecycle()
    val dailySolvedToday by viewModel.dailySolvedToday.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: ROUTE_WORKSHOP
    val showNavigation = currentRoute != ROUTE_BOARD

    TimberSudokuTheme(woodTheme = preferences.woodTheme) {
        val palette = LocalWoodPalette.current

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .woodGrainBackground(palette)
        ) {
            val useRail = maxWidth >= 600.dp

            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets.safeDrawing,
                bottomBar = {
                    if (showNavigation && !useRail) {
                        NavigationBar(
                            containerColor = palette.cardSurface.copy(alpha = 0.96f),
                            tonalElevation = 4.dp
                        ) {
                            topTabs.forEach { tab ->
                                val selected = currentRoute == tab.route
                                val label = stringResource(tab.labelRes)
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        navController.navigate(tab.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = label
                                        )
                                    },
                                    label = { Text(label) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = palette.onAccentPrimary,
                                        selectedTextColor = palette.accentPrimary,
                                        indicatorColor = palette.accentPrimary,
                                        unselectedIconColor = palette.textSecondary,
                                        unselectedTextColor = palette.textSecondary
                                    )
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (showNavigation && useRail) {
                        NavigationRail(
                            containerColor = palette.cardSurface.copy(alpha = 0.94f),
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            topTabs.forEach { tab ->
                                val selected = currentRoute == tab.route
                                val label = stringResource(tab.labelRes)
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = {
                                        navController.navigate(tab.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = label
                                        )
                                    },
                                    label = { Text(label) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = palette.onAccentPrimary,
                                        selectedTextColor = palette.accentPrimary,
                                        indicatorColor = palette.accentPrimary,
                                        unselectedIconColor = palette.textSecondary,
                                        unselectedTextColor = palette.textSecondary
                                    )
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                        NavHost(
                            navController = navController,
                            startDestination = ROUTE_WORKSHOP
                        ) {
                            composable(ROUTE_WORKSHOP) {
                                WorkshopScreen(
                                    preferences = preferences,
                                    selectedMode = selectedHomeMode,
                                    resumePreview = resumePreview,
                                    dailySolvedToday = dailySolvedToday,
                                    dailyStreak = stats.currentDailyStreak,
                                    onSelectWoodTheme = viewModel::setWoodTheme,
                                    onSelectMode = viewModel::selectHomeMode,
                                    onToggleZenMode = viewModel::setZenMode,
                                    onResumeGame = { gameId ->
                                        viewModel.resumeSavedGame(gameId)
                                        navController.navigate(ROUTE_BOARD)
                                    },
                                    onStartDaily = {
                                        viewModel.startNewGame(
                                            mode = GameMode.CLASSIC_9X9,
                                            difficulty = Difficulty.ARTISAN,
                                            isDailyChallenge = true
                                        )
                                        navController.navigate(ROUTE_BOARD)
                                    },
                                    onStartNewGame = { mode, difficulty ->
                                        viewModel.startNewGame(
                                            mode = mode,
                                            difficulty = difficulty,
                                            isDailyChallenge = false
                                        )
                                        navController.navigate(ROUTE_BOARD)
                                    }
                                )
                            }

                            composable(ROUTE_LEDGER) {
                                StatsScreen(stats = stats)
                            }

                            composable(ROUTE_CRAFT) {
                                GuideScreen(
                                    preferences = preferences,
                                    onSelectWoodTheme = viewModel::setWoodTheme,
                                    onSetZenMode = viewModel::setZenMode,
                                    onSetAutoCleanNotes = viewModel::setAutoCleanNotes,
                                    onSetHighlightSameDigits = viewModel::setHighlightSameDigits,
                                    onSetHapticsEnabled = viewModel::setHapticsEnabled
                                )
                            }

                            composable(ROUTE_BOARD) {
                                BackHandler {
                                    viewModel.pauseGame()
                                    navController.popBackStack()
                                }
                                GameScreen(
                                    state = gameState,
                                    preferences = preferences,
                                    onBack = { navController.popBackStack() },
                                    onSelectCell = viewModel::selectCell,
                                    onDigitClick = viewModel::enterDigit,
                                    onUndo = viewModel::undo,
                                    onErase = viewModel::eraseSelectedCell,
                                    onToggleNotes = viewModel::toggleNotesMode,
                                    onFillCandidates = viewModel::fillAllCandidates,
                                    onHint = viewModel::requestSmartHint,
                                    onDismissHint = viewModel::dismissHint,
                                    onPause = viewModel::pauseGame,
                                    onResume = viewModel::resumeGameFromPause,
                                    onContinueZen = viewModel::continueInZenMode,
                                    onPlayAgain = {
                                        viewModel.startNewGame(
                                            mode = gameState.mode,
                                            difficulty = gameState.difficulty,
                                            isDailyChallenge = false
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
