package com.taskhero.feature.reports.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.taskhero.domain.report.model.BurndownPoint
import kotlinx.collections.immutable.ImmutableList
import java.time.format.DateTimeFormatter

/**
 * Composable that displays a burndown chart using Vico.
 * Shows pending tasks count over time as a line chart.
 *
 * @param burndownData List of burndown data points
 * @param modifier Modifier for the chart container
 */
@Composable
fun BurndownChart(
    burndownData: ImmutableList<BurndownPoint>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface

    val chartEntryModelProducer = remember(burndownData) {
        ChartEntryModelProducer(
            burndownData.mapIndexed { index, point ->
                FloatEntry(
                    x = index.toFloat(),
                    y = point.pendingCount.toFloat()
                )
            }
        )
    }

    val dateFormatter = remember(burndownData) {
        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
            val index = value.toInt()
            if (index in burndownData.indices) {
                burndownData[index].date.format(DateTimeFormatter.ofPattern("MM/dd"))
            } else {
                ""
            }
        }
    }

    ProvideChartStyle {
        Chart(
            chart = lineChart(
                lines = listOf(
                    LineChart.LineSpec(
                        lineColor = primaryColor.hashCode(),
                        lineBackgroundShader = DynamicShaders.fromBrush(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(
                                    primaryColor.copy(alpha = 0.5f),
                                    Color.Transparent
                                )
                            )
                        )
                    )
                )
            ),
            chartModelProducer = chartEntryModelProducer,
            startAxis = rememberStartAxis(),
            bottomAxis = rememberBottomAxis(
                valueFormatter = dateFormatter
            ),
            modifier = modifier
                .fillMaxWidth()
                .height(300.dp)
        )
    }
}
