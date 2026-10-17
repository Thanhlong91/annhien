package com.annhien.womenshealth.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.annhien.womenshealth.core.backup.BackupManager
import com.annhien.womenshealth.core.preferences.AppPreferences
import com.annhien.womenshealth.domain.model.LifeStage
import com.annhien.womenshealth.feature.calendar.CalendarScreen
import com.annhien.womenshealth.feature.calendar.CalendarViewModel
import com.annhien.womenshealth.feature.conception.ConceptionModuleScreen
import com.annhien.womenshealth.feature.cycle.CycleModuleScreen
import com.annhien.womenshealth.feature.dailyhealth.DailyHealthModuleScreen
import com.annhien.womenshealth.feature.home.HomeScreen
import com.annhien.womenshealth.feature.home.HomeViewModel
import com.annhien.womenshealth.feature.journal.JournalEditScreen
import com.annhien.womenshealth.feature.journal.JournalScreen
import com.annhien.womenshealth.feature.journal.JournalViewModel
import com.annhien.womenshealth.feature.knowledge.KnowledgeScreen
import com.annhien.womenshealth.feature.medicalrecords.MedicalRecordsScreen
import com.annhien.womenshealth.feature.onboarding.LifeStageSelectionScreen
import com.annhien.womenshealth.feature.onboarding.WelcomeScreen
import com.annhien.womenshealth.feature.perimenopause.PerimenopauseModuleScreen
import com.annhien.womenshealth.feature.postpartum.PostpartumModuleScreen
import com.annhien.womenshealth.feature.pregnancy.PregnancyModuleScreen
import com.annhien.womenshealth.feature.relaxation.RelaxationModuleScreen
import com.annhien.womenshealth.feature.reports.ReportsScreen
import com.annhien.womenshealth.feature.reports.ReportsViewModel
import com.annhien.womenshealth.feature.breasthealth.BreastSelfExamScreen
import com.annhien.womenshealth.feature.familyplanning.PillPackScreen
import com.annhien.womenshealth.feature.postpartum.EpdsScreeningScreen
import com.annhien.womenshealth.feature.postpartum.KegelScreen
import com.annhien.womenshealth.feature.reminders.ReminderCenterScreen
import com.annhien.womenshealth.feature.settings.BackupRestoreScreen
import com.annhien.womenshealth.feature.settings.SettingsScreen
import com.annhien.womenshealth.feature.utilities.UtilitiesScreen
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun MainNavGraph(
    navController: NavHostController,
    startDestination: String,
    appPreferences: AppPreferences,
    backupManager: BackupManager,
    onLockApp: (disguise: Boolean) -> Unit = {},
    onToggleTheme: () -> Unit = {},
    isDarkTheme: Boolean = false
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedStage by remember { mutableStateOf(LifeStage.CYCLE) }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ─── Onboarding ───────────────────────────────────────────────────────
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onContinue = { navController.navigate(Screen.LifeStageSelection.route) }
            )
        }

        composable(Screen.LifeStageSelection.route) {
            LifeStageSelectionScreen(
                selectedStage = selectedStage,
                onStageSelected = { selectedStage = it },
                onContinue = {
                    coroutineScope.launch {
                        appPreferences.setOnboardingCompleted(true)
                        appPreferences.setCurrentLifeStage(selectedStage.name)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        // ─── Main Scaffold Container ──────────────────────────────────────────
        composable(Screen.Home.route) {
            MainScaffold(
                navController = navController,
                appPreferences = appPreferences,
                backupManager = backupManager,
                onLockApp = onLockApp,
                onToggleTheme = onToggleTheme,
                isDarkTheme = isDarkTheme
            )
        }

        // ─── Direct Sub-Routes ────────────────────────────────────────────────
        composable(Screen.JournalEdit.route) {
            val journalViewModel: JournalViewModel = hiltViewModel()
            JournalEditScreen(
                date = LocalDate.now(),
                viewModel = journalViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CycleModule.route) {
            CycleModuleScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.ConceptionModule.route) {
            ConceptionModuleScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.PregnancyModule.route) {
            PregnancyModuleScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.PostpartumModule.route) {
            PostpartumModuleScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.PerimenopauseModule.route) {
            PerimenopauseModuleScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.DailyHealthModule.route) {
            DailyHealthModuleScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.RelaxationModule.route) {
            RelaxationModuleScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.MedicalRecordsModule.route) {
            MedicalRecordsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Knowledge.route) {
            KnowledgeScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                appPreferences = appPreferences,
                onNavigateToBackup = { navController.navigate(Screen.BackupRestore.route) },
                onNavigateBack = { navController.popBackStack() },
                onLockApp = onLockApp
            )
        }

        composable(Screen.BackupRestore.route) {
            val context = androidx.compose.ui.platform.LocalContext.current
            BackupRestoreScreen(
                backupManager = backupManager,
                context = context,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ReminderCenter.route) {
            ReminderCenterScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.PillPackModule.route) {
            PillPackScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.BreastSelfExamModule.route) {
            BreastSelfExamScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.EpdsScreeningModule.route) {
            EpdsScreeningScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.KegelModule.route) {
            KegelScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}

@Composable
fun MainScaffold(
    navController: NavHostController,
    appPreferences: AppPreferences,
    backupManager: BackupManager,
    onLockApp: (disguise: Boolean) -> Unit = {},
    onToggleTheme: () -> Unit = {},
    isDarkTheme: Boolean = false
) {
    var currentTab by remember { mutableStateOf(0) }

    val homeViewModel: HomeViewModel = hiltViewModel()
    val calendarViewModel: CalendarViewModel = hiltViewModel()
    val journalViewModel: JournalViewModel = hiltViewModel()
    val reportsViewModel: ReportsViewModel = hiltViewModel()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                val itemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.outline,
                    unselectedTextColor = MaterialTheme.colorScheme.outline
                )

                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.Spa, contentDescription = "Hôm nay") },
                    label = { Text("Hôm nay", fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = itemColors
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Lịch") },
                    label = { Text("Lịch", fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = itemColors
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.EditNote, contentDescription = "Nhật ký") },
                    label = { Text("Nhật ký", fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = itemColors
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Báo cáo") },
                    label = { Text("Báo cáo", fontWeight = if (currentTab == 3) FontWeight.Bold else FontWeight.Normal) },
                    colors = itemColors
                )
                NavigationBarItem(
                    selected = currentTab == 4,
                    onClick = { currentTab = 4 },
                    icon = { Icon(Icons.Default.GridView, contentDescription = "Tiện ích") },
                    label = { Text("Tiện ích", fontWeight = if (currentTab == 4) FontWeight.Bold else FontWeight.Normal) },
                    colors = itemColors
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentTab) {
                0 -> HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToJournal = { currentTab = 2 },
                    onNavigateToCalendar = { currentTab = 1 },
                    onNavigateToModule = { route -> navController.navigate(route) }
                )
                1 -> CalendarScreen(
                    viewModel = calendarViewModel,
                    onNavigateToJournalEdit = { date ->
                        navController.navigate(Screen.JournalEdit.route)
                    }
                )
                2 -> JournalScreen(
                    viewModel = journalViewModel,
                    onNavigateToEdit = { date ->
                        navController.navigate(Screen.JournalEdit.route)
                    }
                )
                3 -> ReportsScreen(
                    viewModel = reportsViewModel
                )
                4 -> UtilitiesScreen(
                    onNavigateToRoute = { route -> navController.navigate(route) },
                    onQuickDisguise = { onLockApp(true) },
                    onToggleTheme = onToggleTheme,
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}
