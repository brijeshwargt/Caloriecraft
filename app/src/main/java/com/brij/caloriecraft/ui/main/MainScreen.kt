package com.brij.caloriecraft.ui.main
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    // Collect the state from the ViewModel in a lifecycle-aware manner
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // State for the text field
    var userInput by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    var logToDelete by remember { mutableStateOf<FoodLog?>(null) }

    // --- NEW: State for tracking list scroll position and coroutine scope ---
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // --- NEW: A derived state to efficiently check if the button should be shown ---
    val showButton by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0
        }
    }

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
        // CHANGE 1: The main Column is now a LazyColumn.
        // This makes the entire content area scrollable.
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp), // Apply horizontal padding for content
                contentPadding = paddingValues,    // Apply padding from the Scaffold
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // CHANGE 2: Each non-list element is now wrapped in an item { ... } block.
                item {
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
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
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
                }

                // Loading Indicator
                if (uiState.isLoading) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator()
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Daily Nutritional Totals Card
                item {
                    DailyNutritionCard(uiState)
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    Divider()
                }

                // CHANGE 3: The old nested LazyColumn is gone. The logic is now directly
                // inside the main LazyColumn.
                if (uiState.todaysLogs.isEmpty() && !uiState.isLoading) {
                    item {
                        // Modifier.weight is removed as it doesn't work in a LazyColumn.
                        // We add padding to give it some space.
                        Text(
                            "No food logged for today.",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 48.dp)
                        )
                    }
                } else {
                    // Meal Sections are now built directly into this LazyColumn
                    val mealGroups = groupFoodLogsByMeal(uiState.todaysLogs)

                    mealGroups.forEach { (mealType, logs) ->
                        item {
                            MealSectionHeader(mealType, logs.sumOf { it.calories })
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(logs) { log ->
                            FoodLogItem(
                                log = log,
                                onDeleteClick = { logToDelete = log }
                                )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
            AnimatedVisibility(
                visible = showButton,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            lazyListState.animateScrollToItem(0)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Scroll to top",
                        modifier = Modifier.size(36.dp) // Make the icon a standard size
                    )
                }
            }
        }
    }

    logToDelete?.let { log ->
        AlertDialog(
            onDismissRequest = { logToDelete = null },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete '${log.foodName}' from your log?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteFoodLog(log)
                        logToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { logToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

}
// Helper function to group food logs by meal type
private fun groupFoodLogsByMeal(logs: List<FoodLog>): List<Pair<String, List<FoodLog>>> {
    val mealOrder = listOf("Breakfast", "Lunch", "Dinner", "Snack")
    return mealOrder.mapNotNull { mealType ->
        val mealLogs = logs.filter { it.mealType.equals(mealType, ignoreCase = true) }
        if (mealLogs.isNotEmpty()) {
            mealType to mealLogs
        } else null
    }
}

@Composable
fun DailyNutritionCard(uiState: MainUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Today's Nutrition Summary",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Calories row with highlight
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Total Calories",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "${uiState.totalCaloriesToday} kcal",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nutrients grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NutrientItem("Protein", "%.1fg".format(uiState.totalProteinToday), MaterialTheme.colorScheme.primary)
                NutrientItem("Carbs", "%.1fg".format(uiState.totalCarbsToday), MaterialTheme.colorScheme.secondary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NutrientItem("Fiber", "%.1fg".format(uiState.totalFibersToday), MaterialTheme.colorScheme.tertiary)
                NutrientItem("Fats", "%.1fg".format(uiState.totalFatsToday), MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun NutrientItem(label: String, value: String, color: Color) {
    Card(
        modifier = Modifier.padding(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                color = color
            )
        }
    }
}

@Composable
fun MealSectionHeader(mealName: String, totalCalories: Int) {
    val mealColor = when (mealName.lowercase()) {
        "breakfast" -> MaterialTheme.colorScheme.primary
        "lunch" -> MaterialTheme.colorScheme.secondary
        "dinner" -> MaterialTheme.colorScheme.tertiary
        "snack" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }

    val mealIcon = when (mealName.lowercase()) {
        "breakfast" -> Icons.Default.Home
        "lunch" -> Icons.Default.Star
        "dinner" -> Icons.Default.Check
        "snack" -> Icons.Default.Favorite
        else -> Icons.Default.Home
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = mealColor.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = mealIcon,
                    contentDescription = mealName,
                    tint = mealColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    mealName,
                    style = MaterialTheme.typography.titleMedium,
                    color = mealColor
                )
            }
            Text(
                "$totalCalories kcal",
                style = MaterialTheme.typography.bodyMedium,
                color = mealColor
            )
        }
    }
}

@Composable
fun FoodLogItem(log: FoodLog, onDeleteClick: () -> Unit) { // CHANGED: Added parameter
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(log.foodName, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "${log.quantity} ${log.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Text(
                    "${log.calories} kcal",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 8.dp) // Added padding
                )

                // --- NEW: The delete button ---
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Log",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NutrientChip("Protein", "%.1fg".format(log.protein), MaterialTheme.colorScheme.primary)
                NutrientChip("Carbs", "%.1fg".format(log.carbs), MaterialTheme.colorScheme.secondary)
                NutrientChip("Fiber", "%.1fg".format(log.fibers), MaterialTheme.colorScheme.tertiary)
                NutrientChip("Fats", "%.1fg".format(log.fats), MaterialTheme.colorScheme.error)
            }
        }
    }
}
@Composable
fun NutrientChip(label: String, value: String, color: Color) {
    Card(
        modifier = Modifier.padding(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                fontSize = 10.sp
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                color = color,
                fontSize = 12.sp
            )
        }
    }
}