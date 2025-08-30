package com.brij.caloriecraft.ui.main
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brij.caloriecraft.data.local.FoodLog
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    // Collect the state from the ViewModel in a lifecycle-aware manner
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // State for the text field
    var userInput by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Display error messages in a Snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearErrorMessage() // Clear message after showing
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("CalorieCraft") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Input Section
            OutlinedTextField(
                value = userInput,
                onValueChange = { userInput = it },
                label = { Text("Enter your meal (e.g., 'for lunch I had 2 eggs')") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (userInput.isNotBlank()) {
                        viewModel.parseAndLog(userInput)
                        userInput = ""
                        focusManager.clearFocus() // Hide keyboard
                    }
                })
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (userInput.isNotBlank()) {
                        viewModel.parseAndLog(userInput)
                        userInput = ""
                        focusManager.clearFocus() // Hide keyboard
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = userInput.isNotBlank() && !uiState.isLoading
            ) {
                Text("Log Food")
            }

            // Loading Indicator
            if (uiState.isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Display Section
            Text(
                "Today's Total Calories: ${uiState.totalCaloriesToday}",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            Divider()

            if (uiState.todaysLogs.isEmpty() && !uiState.isLoading) {
                Spacer(modifier = Modifier.weight(1f))
                Text("No food logged for today.", textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(uiState.todaysLogs) { log ->
                        FoodLogItem(log = log)
                    }
                }
            }
        }
    }
}

@Composable
fun FoodLogItem(log: FoodLog) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(log.foodName, style = MaterialTheme.typography.bodyLarge)
                Text(
                    "${log.quantity} ${log.unit} • ${log.mealType}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Text(
                "${log.calories} kcal",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 18.sp
            )
        }
    }
}