package com.asistente.planificador

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.asistente.planificador.ui.screens.MainCalendar
import com.asistente.planificador.ui.screens.TaskForm
import com.asistente.planificador.ui.screens.TaskView
import com.asistente.planificador.ui.screens.TimeSlotForm
import com.asistente.planificador.ui.theme.TrabajoFinDeGradoTheme
import com.asistente.planificador.ui.viewmodels.ShowCategoriesViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.asistente.planificador.ui.screens.TimeSlotListScreen


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
                    // ── Calendario principal ──────────────────────────────────
                    composable("main_calendar") {
                        val viewModel: CalendarViewModel = hiltViewModel()
                        val categoriesViewModel: ShowCategoriesViewModel = hiltViewModel()
                        MainCalendar(
                            viewModel = viewModel,
                            categoriesViewModel = categoriesViewModel,
                            onNavigateToTask = { navController.navigate("task_form") },
                            onNavigateToCategory = { navController.navigate("category_form") },
                            onNavigateToDetail = { taskId ->
                                navController.navigate("task_detail/$taskId")
                            },
                            onNavigateToEditCategory = { categoryId ->
                                navController.navigate("edit_category/$categoryId")
                            },
                            onNavigateToActivityForm = { navController.navigate("activity_form") },
                            onNavigateToTimeSlots = { calendarId, calendarName ->
                                navController.navigate("timeslot_list/$calendarId/$calendarName")
                            }
                        )
                    }

                    // ── Nueva tarea ───────────────────────────────────────────
                    composable("task_form") {
                        TaskForm(onBack = { navController.popBackStack() })
                    }

                    // ── Detalle de tarea ──────────────────────────────────────
                    composable(
                        route = "task_detail/{taskId}",
                        arguments = listOf(navArgument("taskId") { type = NavType.StringType })
                    ) { entry ->
                        val taskId = entry.arguments?.getString("taskId") ?: ""
                        TaskView(
                            onBack = { navController.popBackStack() },
                            onNavigateToEditTask = { navController.navigate("edit_task/$taskId") },
                            onDelete = { navController.popBackStack() }
                        )
                    }

                    // Deep link para notificaciones
                    composable(
                        route = "task_detail_deep/{taskId}",
                        deepLinks = listOf(
                            navDeepLink { uriPattern = "asistente://task/{taskId}" }
                        ),
                        arguments = listOf(navArgument("taskId") { type = NavType.StringType })
                    ) { entry ->
                        val taskId = entry.arguments?.getString("taskId") ?: ""
                        TaskView(
                            onBack = { navController.popBackStack() },
                            onNavigateToEditTask = { navController.navigate("edit_task/$taskId") }, // ← con taskId
                            onDelete = { navController.popBackStack() }
                        )
                    }

                    // ── Editar tarea ──────────────────────────────────────────
                    composable(
                        route = "edit_task/{taskId}",
                        arguments = listOf(navArgument("taskId") { type = NavType.StringType })
                    ) { entry ->
                        val taskId = entry.arguments?.getString("taskId")
                        TaskForm(
                            taskId   = taskId,
                            onBack   = { navController.popBackStack() },
                            onDelete = { navController.popBackStack("main_calendar", inclusive = false) }
                        )                    }

                    // ── Actividad ─────────────────────────────────────────────
                    composable("activity_form") {
                        ActivityForm(onBack = { navController.popBackStack() })
                    }

                    // ── Nueva categoría ───────────────────────────────────────
                    composable("category_form") {
                        CategoryForm(categoryId = null, onBack = { navController.popBackStack() })
                    }

                    // ── Editar categoría ──────────────────────────────────────
                    composable(
                        route = "edit_category/{categoryId}",
                        arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
                    ) { entry ->
                        val categoryId = entry.arguments?.getString("categoryId")
                        CategoryForm(
                            categoryId = categoryId,
                            onBack = { navController.popBackStack() })
                    }

                    // ── Lista de franjas ──────────────────────────────────────────────────
                    composable(
                        route = "timeslot_list/{calendarId}/{calendarName}",
                        arguments = listOf(
                            navArgument("calendarId") { type = NavType.StringType },
                            navArgument("calendarName") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val calendarName = backStackEntry.arguments?.getString("calendarName") ?: ""
                        val calendarId = backStackEntry.arguments?.getString("calendarId") ?: ""
                        TimeSlotListScreen(
                            calendarName = calendarName,
                            onNavigateToForm = { existingSlot ->
                                val route = if (existingSlot != null)
                                    "timeslot_form/$calendarId?editSlotId=${existingSlot.id}"
                                else
                                    "timeslot_form/$calendarId"
                                navController.navigate(route)
                            },
                            onNavigateToEditTask = { taskId ->
                                navController.navigate("edit_task/$taskId")
                            },
                            onNavigateToViewTask = { taskId ->
                                navController.navigate("task_detail/$taskId")
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // ── Form de franja ────────────────────────────────────────
                    composable(
                        route = "timeslot_form/{calendarId}?editSlotId={editSlotId}",
                        arguments = listOf(
                            navArgument("calendarId") { type = NavType.StringType },
                            navArgument("editSlotId") {
                                type = NavType.StringType; nullable = true; defaultValue = null
                            }
                        )
                    ) { backStackEntry ->
                        val editSlotId = backStackEntry.arguments?.getString("editSlotId")
                        TimeSlotForm(
                            editSlotId = editSlotId,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}