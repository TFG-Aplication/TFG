package com.asistente.planificador

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.asistente.core.ui.viewmodels.CalendarViewModel
import com.asistente.planificador.ui.screens.MainCalendar
import com.asistente.planificador.ui.screens.TaskForm
import com.asistente.planificador.ui.theme.TrabajoFinDeGradoTheme
import com.asistente.planificador.ui.viewmodels.ShowCategoriesViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        setContent {
            TrabajoFinDeGradoTheme {
                val navController = rememberNavController()

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
                            onNavigateToTask = { navController.navigate("task_form") }
                        )
                    }
                    composable("task_form") {
                        TaskForm(
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}

