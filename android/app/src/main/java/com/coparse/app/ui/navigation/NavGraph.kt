package com.coparse.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.coparse.app.MainViewModel
import com.coparse.app.ui.screens.ClauseDetailScreen
import com.coparse.app.ui.screens.ConfirmScreen
import com.coparse.app.ui.screens.DashboardScreen
import com.coparse.app.ui.screens.DisclaimerScreen
import com.coparse.app.ui.screens.HomeScreen
import com.coparse.app.ui.screens.IntakeScreen
import com.coparse.app.ui.screens.ProcessingScreen
import com.coparse.app.ui.screens.QuestionsScreen
import com.coparse.app.ui.screens.SavedScreen

object Routes {
    const val DISCLAIMER = "disclaimer"
    const val HOME = "home"
    const val INTAKE = "intake"
    const val PROCESSING = "processing/{documentId}/{jobId}"
    const val CONFIRM = "confirm/{documentId}"
    const val DASHBOARD = "dashboard/{documentId}"
    const val CLAUSE = "clause/{documentId}/{clauseId}"
    const val QUESTIONS = "questions/{documentId}"
    const val SAVED = "saved"
}

@Composable
fun CoparseNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    mainVm: MainViewModel,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.DISCLAIMER,
        modifier = modifier,
    ) {
        composable(Routes.DISCLAIMER) {
            DisclaimerScreen(
                onAccepted = { navController.navigate(Routes.HOME) { popUpTo(Routes.DISCLAIMER) { inclusive = true } } },
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                onOpenIntake = { hintType, hintRole ->
                    mainVm.hintContractType = hintType
                    mainVm.hintRole = hintRole
                    navController.navigate(Routes.INTAKE)
                },
                onOpenSaved = { navController.navigate(Routes.SAVED) },
            )
        }
        composable(Routes.INTAKE) {
            IntakeScreen(
                hintContractType = mainVm.hintContractType,
                hintRole = mainVm.hintRole,
                onUploaded = { docId, jobId ->
                    mainVm.lastDocumentId = docId
                    navController.navigate("processing/$docId/$jobId")
                },
            )
        }
        composable(
            route = Routes.PROCESSING,
            arguments = listOf(
                navArgument("documentId") { type = NavType.StringType },
                navArgument("jobId") { type = NavType.StringType },
            ),
        ) { entry ->
            val documentId = entry.arguments?.getString("documentId")!!
            val jobId = entry.arguments?.getString("jobId")!!
            ProcessingScreen(
                documentId = documentId,
                jobId = jobId,
                onDone = {
                    navController.navigate("confirm/$documentId") {
                        popUpTo(Routes.HOME)
                    }
                },
            )
        }
        composable(
            route = Routes.CONFIRM,
            arguments = listOf(navArgument("documentId") { type = NavType.StringType }),
        ) { entry ->
            val documentId = entry.arguments?.getString("documentId")!!
            ConfirmScreen(
                documentId = documentId,
                onContinue = {
                    navController.navigate("dashboard/$documentId") {
                        popUpTo(Routes.HOME)
                    }
                },
                onReanalyze = { newJobId ->
                    navController.navigate("processing/$documentId/$newJobId") {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(
            route = Routes.DASHBOARD,
            arguments = listOf(navArgument("documentId") { type = NavType.StringType }),
        ) { entry ->
            val documentId = entry.arguments?.getString("documentId")!!
            DashboardScreen(
                documentId = documentId,
                onClause = { cid -> navController.navigate("clause/$documentId/$cid") },
                onQuestions = { navController.navigate("questions/$documentId") },
                onHome = { navController.popBackStack(Routes.HOME, false) },
            )
        }
        composable(
            route = Routes.CLAUSE,
            arguments = listOf(
                navArgument("documentId") { type = NavType.StringType },
                navArgument("clauseId") { type = NavType.StringType },
            ),
        ) { entry ->
            val documentId = entry.arguments?.getString("documentId")!!
            val clauseId = entry.arguments?.getString("clauseId")!!
            ClauseDetailScreen(
                documentId = documentId,
                clauseId = clauseId,
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.QUESTIONS,
            arguments = listOf(navArgument("documentId") { type = NavType.StringType }),
        ) { entry ->
            val documentId = entry.arguments?.getString("documentId")!!
            QuestionsScreen(
                documentId = documentId,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.SAVED) {
            SavedScreen(
                onOpen = { docId ->
                    navController.navigate("dashboard/$docId")
                },
                onBack = { navController.popBackStack() },
            )
        }
    }
}
