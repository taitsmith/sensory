package com.taitsmith.sensory.ui

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
class MainActivity : ComponentActivity(), SensorEventListener {

    //we need one of each producer / model to compile the chart
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

    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)!!

        setContent {
            //hide the slider and show the text views, as well as changing the fab icon
            //based on sensor state
            val hideSlider by viewModel.isRecording.observeAsState(initial = false)

            Scaffold(
                floatingActionButtonPosition = FabPosition.End,
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            if (viewModel.isRecording.value == true) {
                                viewModel.updateSensorStatus(false)
                                registerSensorListener(false)

                            } else {
                                viewModel.updateSensorStatus(true)
                                registerSensorListener(true)
                            }
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
                                    contentDescription = "Activate the senors",
                                    tint = Color.White
                                )
                            }
                        }
                    )
                }
            ) {
                SensoryTheme {
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
                                ) { //use an xml view in our compose layout to hold
                                    AndroidView( //textviews to be updated with new background colors
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

    private fun registerSensorListener(shouldRegister: Boolean) {
        if (shouldRegister) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        } else sensorManager.unregisterListener(this)
    }

    override fun onPause() {
        super.onPause()
        viewModel.updateSensorStatus(false)
        sensorManager.unregisterListener(this)
    }

    private fun setObservers() {
        //xyz array is updated every time new sensor data comes in. watch it for updates,
        //do a little string manipulation in the background, and set our textviews to that color
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

        //chart entries are only updated on the frequency selected by the user. the way the chart
        //library works requires both a list of entries for each object being charted, as well as
        //a producer. observe the entries list in the viewmodel and act accordingly
        viewModel.chartEntries.observe(this) {
            entriesModelX.add(it[0])
            entriesModelY.add(it[1])
            entriesModelZ.add(it[2])
            chartEntryModelProducerX.setEntries(entriesModelX)
            chartEntryModelProducerY.setEntries(entriesModelY)
            chartEntryModelProducerZ.setEntries(entriesModelZ)
        }
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        if (p0 != null) {
            viewModel.updateArray(p0.values.asList())
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        //i need to be here
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
            Text(text = "$updatesPerSecond updates per second")
        }
    }
}