package com.taitsmith.sensory.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.chart.composed.plus
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.composed.plus
import com.patrykandpatrick.vico.core.entry.entriesOf
import com.taitsmith.sensory.R
import com.taitsmith.sensory.ui.theme.SensoryTheme
import com.taitsmith.sensory.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val COLOR_1_CODE = 0xffb983ff
    private val COLOR_2_CODE = 0xff91b1fd
    private val COLOR_3_CODE = 0xff8fdaff

    private val color1 = Color(COLOR_1_CODE)
    private val color2 = Color(COLOR_2_CODE)
    private val color3 = Color(COLOR_3_CODE)
    private val chartColors = listOf(color1, color2, color3)

    var chartEntryModelProducerX = ChartEntryModelProducer(entriesOf(0f))
    var chartEntryModelProducerY = ChartEntryModelProducer(entriesOf(0f))
    private var chartEntryModelProducerZ = ChartEntryModelProducer(entriesOf(0f))

    private var composedChartEntryModelProducer = (
            chartEntryModelProducerX
                    + chartEntryModelProducerY
                    + chartEntryModelProducerZ
            )

    var entriesModelX: MutableList<ChartEntry> = mutableListOf()
    var entriesModelY: MutableList<ChartEntry> = mutableListOf()
    var entriesModelZ: MutableList<ChartEntry> = mutableListOf()

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setObservers()

        setContent {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                floatingActionButtonPosition = FabPosition.End,
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            if (viewModel.isRecording.value == true) {
                                viewModel.updateSensorStatus(false)
                            } else viewModel.updateSensorStatus(true)
                        },
                        content = {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_emergency_recording_24),
                                contentDescription = "Activate the sensors",
                                tint = Color.White
                            )
                        }
                    )
                }
            ) {
                SensoryTheme {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                ProvideChartStyle(rememberChartStyle(chartColors)) {
                                    val lineChartX = lineChart()
                                    val lineChartY = lineChart()
                                    val lineChartZ = lineChart()
                                    Chart(
                                        chart = remember(
                                            lineChartX,
                                            lineChartY,
                                            lineChartZ
                                        ) { lineChartX + lineChartY + lineChartZ },
                                        chartModelProducer = composedChartEntryModelProducer,
                                        startAxis = startAxis(),
                                        bottomAxis = bottomAxis(),
                                    )
                                }

                            }
                        }
                    }
                }
            }
        }

    }

    override fun onPause() {
        super.onPause()
        viewModel.updateSensorStatus(false)
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateSensorStatus(true)
    }

    private fun setObservers() {
        viewModel.chartEntries.observe(this) {
            entriesModelX.add(it[0])
            entriesModelY.add(it[1])
            entriesModelZ.add(it[2])
            chartEntryModelProducerX.setEntries(entriesModelX)
            chartEntryModelProducerY.setEntries(entriesModelY)
            chartEntryModelProducerZ.setEntries(entriesModelZ)
            Log.d("OBSERVER", "observed")
        }
    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SensoryTheme {
    }
}