package com.taitsmith.sensory.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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

    private var colorsList: List<Float> = mutableListOf(.5F, .5F, .5F)

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "RememberReturnType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setObservers()

        setContent {
            Scaffold(
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
                                painter = painterResource(id = R.drawable.baseline_play_arrow_24),
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
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Column() {
                            Row() {
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
                                        modifier = Modifier.fillMaxHeight(.60f),
                                        diffAnimationSpec = snap()
                                    )
                                }

                            }
                            Row(
                                Modifier
                                    .fillMaxHeight(.7f)
                                    .fillMaxWidth(),
                                Arrangement.Center
                            ) {
                                SliderView(viewModel = viewModel)
                            }
                            Row(
                                Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                Arrangement.SpaceEvenly
                            ){
                                Box(
                                    Modifier.background(shape = CircleShape, color = Color.Green)
                                        .padding(16.dp)
                                ) {
                                    Icon(painter = painterResource(id = R.drawable.baseline_emergency_recording_24), contentDescription = "D")
                                }
                                Box(
                                    Modifier.background(shape = CircleShape, color =Color.Red)
                                        .padding(16.dp)
                                ) {
                                    Icon(painter = painterResource(id = R.drawable.baseline_emergency_recording_24), contentDescription = "D")
                                }
                                Box(
                                    Modifier.background(shape = CircleShape, color = Color.Blue)
                                        .padding(16.dp)
                                ) {
                                    Icon(painter = painterResource(id = R.drawable.baseline_emergency_recording_24), contentDescription = "D")
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

    private fun setObservers() {
        viewModel.xyzArray.observe(this) {
            colorsList = it
            Log.d("COLOR LIST", colorsList[0].toString())
        }
        viewModel.chartEntries.observe(this) {
            entriesModelX.add(it[0])
            entriesModelY.add(it[1])
            entriesModelZ.add(it[2])
            chartEntryModelProducerX.setEntries(entriesModelX)
            chartEntryModelProducerY.setEntries(entriesModelY)
            chartEntryModelProducerZ.setEntries(entriesModelZ)
        }
    }
}

@Composable
fun SliderView(viewModel: MainViewModel) {
    var updatesPerSecond by remember { mutableStateOf(5) }

    Column() {
        Row() {
            Text("1")
            Slider(
                value = updatesPerSecond.toFloat(),
                onValueChange = {
                    updatesPerSecond = it.toInt()
                    viewModel.updateTimerPeriod(it.toInt())
                    },
                valueRange = 1f..10f,
                steps = 10,
                modifier = Modifier.fillMaxWidth(.8f),
            )
            Text("10")
        }
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(.8f),
                    Arrangement.Center
        ) {
            Text(text = "$updatesPerSecond updates per second",

                )
        }
    }
}