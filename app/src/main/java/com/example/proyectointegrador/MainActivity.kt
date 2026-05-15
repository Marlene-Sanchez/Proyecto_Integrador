package com.example.proyectointegrador

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.proyectointegrador.data.remote.RetrofitClient
import com.example.proyectointegrador.data.remote.TokenManager
import com.example.proyectointegrador.data.repository.ReportRepository
import com.example.proyectointegrador.ui.screens.CreateReportScreen
import com.example.proyectointegrador.ui.screens.ReportListScreen
import com.example.proyectointegrador.ui.screens.LoginScreen
import com.example.proyectointegrador.ui.theme.ProyectoIntegradorTheme
import com.example.proyectointegrador.ui.viewmodel.ReportViewModel
import com.example.proyectointegrador.ui.viewmodel.ReportViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Carga token guardado (si existe) antes de usar RetrofitClient
        TokenManager.load(this)

        val repository = ReportRepository(RetrofitClient.reportApi)
        val viewModelFactory = ReportViewModelFactory(repository)

        setContent {
            ProyectoIntegradorTheme {
                val navController = rememberNavController()
                val reportViewModel: ReportViewModel = viewModel(factory = viewModelFactory)
                val start = if (TokenManager.token.isNullOrEmpty()) "login" else "report_list"

                NavHost(navController = navController, startDestination = start) {
                    composable("report_list") {
                        ReportListScreen(
                            viewModel = reportViewModel,
                            onAddReportClick = { navController.navigate("create_report") },
                            onReportClick = { }
                        )
                    }
                    
                    composable("create_report") {
                        CreateReportScreen(
                            viewModel = reportViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("login") {
                        LoginScreen(onLoggedIn = {
                            // recarga token y navega a la lista
                            TokenManager.load(this@MainActivity)
                            navController.navigate("report_list") {
                                popUpTo("login") { inclusive = true }
                            }
                        })
                    }
                }
            }
        }
    }
}
