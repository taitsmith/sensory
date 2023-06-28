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

    //have some private mutable stuff so we can only set values internally,
    //public live data to be observed from the outside
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
    //to be called either on activity pause or button click by user
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

    //use the first two digits of the float value that comes back from each axis rotation,
    //turn it into a hex string, then turn that into a color to be set as a background
    //as another way to visualise rotation. done on background thread because it happens a ton
    fun colorX() : Int {
        val x = "%.2f".format(xyzArray.value!![0]).takeLast(2)
        val s = "#FF" + x + "00FF"
        return android.graphics.Color.parseColor(s)
    }

    fun colorY() : Int {
        val y = "%.2f".format(xyzArray.value!![1]).takeLast(2)
        val s = "#FFFF" + y + "00"
        return android.graphics.Color.parseColor(s)
    }

    fun colorZ() : Int {
        val z = "%.2f".format(xyzArray.value!![2]).takeLast(2)
        val s = "#FF5500$z"
        return android.graphics.Color.parseColor(s)
    }

    //timer period is in millis, but it makes more sense to ask the user how many updates they'd
    //like per second, and then do the conversion.
    fun updateTimerPeriod(updatesPerSecond: Int) {
        timerPeriodInMillis =  (1000/updatesPerSecond).toLong()
    }

    //the values from the sensors get updated a million times per second, so to make things more
    //manageable on the system we ask the user how many times per second they'd like the chart updated
    //and then only update our chart entries live data that frequently
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