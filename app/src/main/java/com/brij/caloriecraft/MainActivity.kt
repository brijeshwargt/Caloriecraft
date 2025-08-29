// In file: MainActivity.kt
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

class MainActivity : ComponentActivity() {

    // Lazily initialize all our components. They will only be created when first needed.
    private val database by lazy { CalorieCraftDatabase.getDatabase(this) }
    private val geminiService by lazy { GeminiService() }
    private val repository by lazy {
        LogRepository(
            foodDao = database.foodDao(),
            weightDao = database.weightDao(),
            geminiService = geminiService
        )
    }

    // This is the modern way to create a ViewModel with dependencies.
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalorieCraftTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Here we pass our ViewModel to the UI
                    MainScreen(viewModel = mainViewModel)
                }
            }
        }
    }
}