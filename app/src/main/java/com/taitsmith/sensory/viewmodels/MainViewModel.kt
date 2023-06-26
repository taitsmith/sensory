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

    private var xyzArray = mutableListOf(0F, 0F, 0F)

    private val _chartEntries = MutableLiveData<MutableList<ChartEntry>>()
    var chartEntries: LiveData<MutableList<ChartEntry>> = _chartEntries

    private val _isRecording = MutableLiveData<Boolean>()
    val isRecording: LiveData<Boolean> = _isRecording

    private lateinit var timer: Timer

    private var timerPeriodInMillis = 500L

    //the sensor update returns a float out to seven or eight places, so the
    // "simplest" thing to do  is format it as a string to two places, then go back to a float.
    private fun updateX(x: Float) {
        if (xyzArray[0] != x) {
            xyzArray.add(0, "%.2f".format(x).toFloat())
        }
    }

    private fun updateY(y: Float) {
        if (xyzArray[1] != y) {
            xyzArray.add(1, "%.2f".format(y).toFloat())
        }
    }

    private fun updateZ(z: Float) {
        if (xyzArray[2] != z) {
            xyzArray.add(2, "%.2f".format(z).toFloat())
        }
    }

    //this gets called ~10 times per second
    override fun onSensorChanged(p0: SensorEvent?) {
        if (p0 != null) {
            updateX(p0.values[0])
            updateY(p0.values[1])
            updateZ(p0.values[2])
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
                val list = mutableListOf<ChartEntry>(
                    entryOf(i, xyzArray[0]),
                    entryOf(i, xyzArray[1]),
                    entryOf(i, xyzArray[2])
                )
                _chartEntries.postValue(list)
                i++
            }
        }
    }
}