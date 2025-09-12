
package com.brij.caloriecraft.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brij.caloriecraft.data.local.DailyCalorieSummary
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DailyCalorieChart(summaries: List<DailyCalorieSummary>) {
    // We'll show the last 7 days. Reverse to get chronological order for the chart.
    val recentSummaries = summaries.take(7).reversed()
    val maxCalories = recentSummaries.maxOfOrNull { it.totalCalories } ?: 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp)
            .drawBehind { // Draw background guide lines
                val lineCount = 4
                val sectionHeight = size.height / lineCount
                for (i in 1..lineCount) {
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.5f),
                        start = Offset(0f, size.height - sectionHeight * i),
                        end = Offset(size.width, size.height - sectionHeight * i),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        recentSummaries.forEach { summary ->
            Bar(
                calories = summary.totalCalories,
                maxCalories = maxCalories,
                date = summary.entryDate
            )
        }
    }
}

@Composable
fun RowScope.Bar(calories: Int, maxCalories: Int, date: Date) {
    val barHeight = if (maxCalories > 0) (calories.toFloat() / maxCalories.toFloat()) else 0f
    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = calories.toString(),
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxHeight(barHeight)
                .width(25.dp)
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = dayFormat.format(date),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}