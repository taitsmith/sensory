package com.taitsmith.sensory.viewmodels

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.entryOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application), SensorEventListener {

    private var sensorManager: SensorManager =
        application.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private var sensor: Sensor =
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)!!

    private val _xyzArray = MutableLiveData<List<Float>>()
    var xyzArray: LiveData<List<Float>> = _xyzArray


    private val _chartEntries = MutableLiveData<MutableList<ChartEntry>>()
    var chartEntries: LiveData<MutableList<ChartEntry>> = _chartEntries

    private val _isRecording = MutableLiveData<Boolean>()
    val isRecording: LiveData<Boolean> = _isRecording

    private lateinit var timer: Timer

    private var timerPeriodInMillis = 500L

    //this gets called ~10 times per second
    override fun onSensorChanged(p0: SensorEvent?) {
        if (p0 != null) {
            updateArray(p0.values.asList())
        }
    }

    private fun updateArray(valueList: List<Float>) {
        viewModelScope.launch {
          _xyzArray.postValue(valueList)
        }
    }

    //needs to be here, doesn't need to do anything
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    //take a boolean to tell us if we should turn on the sensors and start listening,
    //to be called either on activity pause / resume or button click by user
    fun updateSensorStatus(isRecording: Boolean) {
        if (isRecording) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            _isRecording.value = true
            updateValues()
        } else {
            sensorManager.unregisterListener(this)
            _isRecording.value = false
            timer.cancel()
        }
    }

    //timer period is in millis, but it makes more sense to ask the user how many updates they'd
    //like per second, and then do the conversion.
    fun updateTimerPeriod(updatesPerSecond: Int) {
        timerPeriodInMillis =  (1000/updatesPerSecond).toLong()
    }

    private fun updateValues() {
        var i = 0
        _chartEntries.value?.clear()
        timer = fixedRateTimer("update values", false, 0L, timerPeriodInMillis) {
            viewModelScope.launch(Dispatchers.IO) {
                if (!xyzArray.value.isNullOrEmpty()) {
                    val list = mutableListOf<ChartEntry>(
                        entryOf(i, xyzArray.value!![0]),
                        entryOf(i, xyzArray.value!![1]),
                        entryOf(i, xyzArray.value!![2])
                    )
                    _chartEntries.postValue(list)
                    i++
                }
            }
        }
    }
}