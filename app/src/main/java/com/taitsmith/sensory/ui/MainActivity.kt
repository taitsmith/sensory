package com.taitsmith.sensory.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.composed.plus
import com.patrykandpatrick.vico.core.entry.entriesOf
import com.taitsmith.sensory.R
import com.taitsmith.sensory.ui.theme.SensoryTheme
import com.taitsmith.sensory.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var chartEntryModelProducerX = ChartEntryModelProducer(entriesOf(0f))
    private var chartEntryModelProducerY = ChartEntryModelProducer(entriesOf(0f))
    private var chartEntryModelProducerZ = ChartEntryModelProducer(entriesOf(0f))

    private var entriesModelX: MutableList<ChartEntry> = mutableListOf()
    private var entriesModelY: MutableList<ChartEntry> = mutableListOf()
    private var entriesModelZ: MutableList<ChartEntry> = mutableListOf()

    private var composedChartEntryModelProducer = (
            chartEntryModelProducerX +
            chartEntryModelProducerY +
            chartEntryModelProducerZ
            )

    private val viewModel: MainViewModel by viewModels()

    private lateinit var textViewX: TextView
    private lateinit var textViewY: TextView
    private lateinit var textViewZ: TextView

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "RememberReturnType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val hideSlider by viewModel.isRecording.observeAsState(initial = false)

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
                            if (hideSlider) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_stop_24),
                                    contentDescription = "Stop the sensors",
                                    tint = Color.White
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_play_arrow_24),
                                    contentDescription = "Activate the sensors",
                                    tint = Color.White
                                )
                            }
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
                                ShowChart(chartProducer = composedChartEntryModelProducer)
                            }
                            AnimatedVisibility(visible = !hideSlider) {
                                Row(
                                    Modifier
                                        .fillMaxHeight(.6f)
                                        .fillMaxWidth(),
                                    Arrangement.Center
                                ) {
                                    SliderView(viewModel = viewModel)
                                }
                            }
                            AnimatedVisibility(visible = hideSlider) {
                                Row(
                                    Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    Arrangement.SpaceEvenly
                                ) {
                                    AndroidView(
                                        factory = {
                                            val view = LayoutInflater.from(it)
                                                .inflate(R.layout.images_layout, null, false)
                                            textViewX = view.findViewById(R.id.textViewX)
                                            textViewY = view.findViewById(R.id.imageViewY)
                                            textViewZ = view.findViewById(R.id.textViewZ)
                                            view
                                        },
                                        modifier = Modifier.fillMaxSize(),
                                        update = {}//needs to be here
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        setObservers()
    }

    override fun onPause() {
        super.onPause()
        viewModel.updateSensorStatus(false)
    }

    private fun setObservers() {
        viewModel.xyzArray.observe(this) {
            try {
            lifecycleScope.launch {
                textViewX.setBackgroundColor(viewModel.colorX())
                textViewY.setBackgroundColor(viewModel.colorY())
                textViewZ.setBackgroundColor(viewModel.colorZ())
            }
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }
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