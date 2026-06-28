package com.ydh.salvio.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ydh.salvio.ui.screen.branch.BranchScreen
import com.ydh.salvio.ui.screen.dashboard.DashboardScreen
import com.ydh.salvio.ui.screen.login.LoginScreen
import com.ydh.salvio.ui.screen.pullrequest.PullRequestScreen
import com.ydh.salvio.ui.screen.repos.RepoListScreen
import com.ydh.salvio.ui.screen.stats.StatsScreen
import com.ydh.salvio.viewmodel.AuthState
import com.ydh.salvio.viewmodel.AuthViewModel
import com.ydh.salvio.viewmodel.DashboardViewModel
import com.ydh.salvio.viewmodel.RepoViewModel

object Routes {
    const val LOGIN = "login"
    const val REPO_LIST = "repos"
    const val DASHBOARD = "dashboard/{owner}/{repo}"
    const val PULL_REQUESTS = "pullrequests/{owner}/{repo}"
    const val BRANCHES = "branches/{owner}/{repo}"
    const val STATS = "stats/{owner}/{repo}"

    fun dashboard(owner: String, repo: String) = "dashboard/$owner/$repo"
    fun pullRequests(owner: String, repo: String) = "pullrequests/$owner/$repo"
    fun branches(owner: String, repo: String) = "branches/$owner/$repo"
    fun stats(owner: String, repo: String) = "stats/$owner/$repo"
}

@Composable
fun SalvioNavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val repoViewModel: RepoViewModel = viewModel()
    val dashboardViewModel: DashboardViewModel = viewModel()

    val authState by authViewModel.authState.collectAsState()

    val startDestination = when (authState) {
        is AuthState.Success -> Routes.REPO_LIST
        else -> Routes.LOGIN
    }

    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.REPO_LIST) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REPO_LIST) {
            RepoListScreen(
                authViewModel = authViewModel,
                repoViewModel = repoViewModel,
                onRepoSelected = { owner, repo ->
                    navController.navigate(Routes.dashboard(owner, repo))
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            Routes.DASHBOARD,
            arguments = listOf(
                navArgument("owner") { type = NavType.StringType },
                navArgument("repo") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val owner = backStackEntry.arguments?.getString("owner") ?: ""
            val repo = backStackEntry.arguments?.getString("repo") ?: ""
            DashboardScreen(
                owner = owner,
                repoName = repo,
                dashboardViewModel = dashboardViewModel,
                onNavigateToPRs = { navController.navigate(Routes.pullRequests(owner, repo)) },
                onNavigateToBranches = { navController.navigate(Routes.branches(owner, repo)) },
                onNavigateToStats = { navController.navigate(Routes.stats(owner, repo)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Routes.PULL_REQUESTS,
            arguments = listOf(
                navArgument("owner") { type = NavType.StringType },
                navArgument("repo") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val owner = backStackEntry.arguments?.getString("owner") ?: ""
            val repo = backStackEntry.arguments?.getString("repo") ?: ""
            PullRequestScreen(
                owner = owner,
                repoName = repo,
                dashboardViewModel = dashboardViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Routes.BRANCHES,
            arguments = listOf(
                navArgument("owner") { type = NavType.StringType },
                navArgument("repo") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val owner = backStackEntry.arguments?.getString("owner") ?: ""
            val repo = backStackEntry.arguments?.getString("repo") ?: ""
            BranchScreen(
                owner = owner,
                repoName = repo,
                dashboardViewModel = dashboardViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Routes.STATS,
            arguments = listOf(
                navArgument("owner") { type = NavType.StringType },
                navArgument("repo") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val owner = backStackEntry.arguments?.getString("owner") ?: ""
            val repo = backStackEntry.arguments?.getString("repo") ?: ""
            StatsScreen(
                owner = owner,
                repoName = repo,
                dashboardViewModel = dashboardViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
