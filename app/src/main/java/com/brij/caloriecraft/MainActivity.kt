package com.brij.caloriecraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.brij.caloriecraft.data.LogRepository
import com.brij.caloriecraft.data.local.CalorieCraftDatabase
import com.brij.caloriecraft.data.remote.GeminiService
import com.brij.caloriecraft.ui.main.MainScreen
import com.brij.caloriecraft.ui.main.MainViewModel
import com.brij.caloriecraft.ui.main.MainViewModelFactory
import com.brij.caloriecraft.ui.theme.CalorieCraftTheme
import androidx.activity.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.brij.caloriecraft.ui.history.HistoryScreen
import com.brij.caloriecraft.ui.history.HistoryViewModel
import com.brij.caloriecraft.ui.history.HistoryViewModelFactory

class MainActivity : ComponentActivity() {

    private val database by lazy { CalorieCraftDatabase.getDatabase(this) }
    private val geminiService by lazy { GeminiService() }
    private val repository by lazy {
        LogRepository(
            foodDao = database.foodDao(),
            weightDao = database.weightDao(),
            geminiService = geminiService
        )
    }

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(repository)
    }

    private val historyViewModel: HistoryViewModel by viewModels {
        HistoryViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalorieCraftTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "main") {
                        composable("main") {
                            MainScreen(
                                viewModel = mainViewModel,
                                navController = navController
                            )
                        }
                        composable("history") {
                            HistoryScreen(
                                viewModel = historyViewModel,
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }
}