package com.annhien.womenshealth.navigation

// Navigation route constants for the app
// All routes are sealed objects to prevent typos and enable IDE navigation

sealed class Screen(val route: String) {

    // ─── Onboarding ───────────────────────────────────────────────────────
    object Onboarding : Screen("onboarding_graph")
    object Welcome : Screen("onboarding/welcome")
    object GoalSelection : Screen("onboarding/goals")
    object LifeStageSelection : Screen("onboarding/life_stage")
    object ModuleSelection : Screen("onboarding/modules")
    object ProfileInput : Screen("onboarding/profile")
    object AppLockSetup : Screen("onboarding/lock")
    object ReminderSetup : Screen("onboarding/reminders")

    // ─── Main bottom nav ──────────────────────────────────────────────────
    object Home : Screen("home")
    object Calendar : Screen("calendar")
    object Journal : Screen("journal")
    object Reports : Screen("reports")
    object Utilities : Screen("utilities")

    // ─── Journal sub-screens ──────────────────────────────────────────────
    object JournalDetail : Screen("journal/detail/{entryId}") {
        fun createRoute(entryId: String) = "journal/detail/$entryId"
    }
    object JournalEdit : Screen("journal/edit?entryId={entryId}&date={date}") {
        fun createRoute(entryId: String? = null, date: String? = null): String {
            var route = "journal/edit?"
            if (entryId != null) route += "entryId=$entryId&"
            if (date != null) route += "date=$date"
            return route.trimEnd('&', '?')
        }
    }

    // ─── Reports sub-screens ──────────────────────────────────────────────
    object ReportPreview : Screen("reports/preview")

    // ─── Module screens ───────────────────────────────────────────────────
    object CycleModule : Screen("module/cycle")
    object ConceptionModule : Screen("module/conception")
    object PregnancyModule : Screen("module/pregnancy")
    object PregnancyDetail : Screen("module/pregnancy/{profileId}") {
        fun createRoute(profileId: String) = "module/pregnancy/$profileId"
    }
    object PostpartumModule : Screen("module/postpartum")
    object PerimenopauseModule : Screen("module/perimenopause")
    object DailyHealthModule : Screen("module/daily_health")
    object RelaxationModule : Screen("module/relaxation")
    object MedicalRecordsModule : Screen("module/medical_records")
    object PillPackModule : Screen("module/pill_pack")
    object BreastSelfExamModule : Screen("module/breast_exam")
    object EpdsScreeningModule : Screen("module/epds")
    object KegelModule : Screen("module/kegel")
    object MedicalVisitDetail : Screen("module/medical_records/{visitId}") {
        fun createRoute(visitId: String) = "module/medical_records/$visitId"
    }

    // ─── Knowledge ────────────────────────────────────────────────────────
    object Knowledge : Screen("knowledge")
    object ArticleDetail : Screen("knowledge/article/{articleId}") {
        fun createRoute(articleId: String) = "knowledge/article/$articleId"
    }

    // ─── Settings & Reminders ──────────────────────────────────────────
    object Settings : Screen("settings")
    object SecuritySettings : Screen("settings/security")
    object BackupRestore : Screen("settings/backup")
    object ModuleSettings : Screen("settings/modules")
    object PrivacyPolicy : Screen("settings/privacy")
    object ReminderCenter : Screen("settings/reminders_center")
    object OnlineUtilities : Screen("settings/online_utilities")

    // ─── Online utilities ──────────────────────────────────────────────
    object FoodSearch : Screen("online/food_search")
    object HealthTopics : Screen("online/health_topics")
    object AirQuality : Screen("online/air_quality?latitude={latitude}&longitude={longitude}") {
        fun createRoute(latitude: Double, longitude: Double) =
            "online/air_quality?latitude=$latitude&longitude=$longitude"
    }

    // ─── New online modules (Modules 1, 2, 3, 6, 7) ───────────────────
    object WeatherUv : Screen("online/weather_uv")
    object Workouts : Screen("online/workouts")
    object DailyQuotes : Screen("online/daily_quotes")
    object UsdaNutrition : Screen("online/usda_nutrition")
    object PollenAllergy : Screen("online/pollen_allergy")
}

// Bottom navigation items
data class BottomNavItem(
    val screen: Screen,
    val labelRes: Int,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val contentDescriptionRes: Int
)
