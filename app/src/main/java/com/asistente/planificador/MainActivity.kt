package com.asistente.planificador

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.asistente.core.ui.viewmodels.CalendarViewModel
import com.asistente.planificador.ui.screens.ActivityForm
import com.asistente.planificador.ui.screens.CategoryForm
import com.asistente.planificador.ui.screens.DayViewScreen
import com.asistente.planificador.ui.screens.MainCalendar
import com.asistente.planificador.ui.screens.TaskForm
import com.asistente.planificador.ui.screens.TaskView
import com.asistente.planificador.ui.theme.TrabajoFinDeGradoTheme
import com.asistente.planificador.ui.viewmodels.ShowCategoriesViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        setContent {
            TrabajoFinDeGradoTheme {
                val navController = rememberNavController()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissions(
                        arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                        1001
                    )
                }

                NavHost(
                    navController = navController,
                    startDestination = "main_calendar"
                ) {
                    composable("main_calendar") {
                        val viewModel: CalendarViewModel = hiltViewModel()
                        val categoriesViewModel: ShowCategoriesViewModel = hiltViewModel()
                        MainCalendar(
                            viewModel = viewModel,
                            categoriesViewModel = categoriesViewModel,
                            onNavigateToTask = { navController.navigate("task_form") },
                            onNavigateToCategory = { navController.navigate("category_form") },
                            onNavigateToDetail = { taskId -> navController.navigate("task_detail/$taskId") },
                            onNavigateToEditCategory = { categoryId -> navController.navigate("edit_category/$categoryId")},
                            onNavigateToActivityForm = { navController.navigate("activity_form") }
                            // ← onNavigateToDayView eliminado
                        )
                    }
                    composable("task_form") {
                        TaskForm(onBack = { navController.popBackStack() })
                    }
                    composable("activity_form") {
                        ActivityForm(onBack = { navController.popBackStack() })
                    }
                    composable("category_form") {
                        CategoryForm(categoryId = null, onBack = { navController.popBackStack() })
                    }
                    composable("task_detail/{taskId}") {
                        TaskView(onBack = { navController.popBackStack() })
                    }
                    composable(
                        route = "task_detail/{taskId}",
                        deepLinks = listOf(navDeepLink { uriPattern = "asistente://task/{taskId}" })
                    ) {
                        TaskView(onBack = { navController.popBackStack() })
                    }
                    composable(
                        "edit_category/{categoryId}",
                        arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
                    ) { entry ->
                        val categoryId = entry.arguments?.getString("categoryId")
                        CategoryForm(categoryId = categoryId, onBack = { navController.popBackStack() })
                    }
                    // ← composable "day_view/{date}" eliminado
                }
            }
        }
    }
}

