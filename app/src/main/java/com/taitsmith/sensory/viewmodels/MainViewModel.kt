package com.taitsmith.sensory.viewmodels

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.patrykandpatrick.vico.core.entry.ChartEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application), SensorEventListener {

    private var sensorManager: SensorManager =
        application.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private var sensor: Sensor =
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)!!

    private val _xyzArray = mutableListOf(0F, 0F, 0F)
    var xyzArray: MutableList<Float> = _xyzArray

    private val _chartEntries = MutableLiveData<List<List<ChartEntry>>>()
    var chartEntries: LiveData<List<List<ChartEntry>>> = _chartEntries

    private val _isRecording = MutableLiveData<Boolean>()
    val isRecording: LiveData<Boolean> = _isRecording

    //the sensor update returns a float out to seven or eight places, so the
    // "simplest" thing to do  is format it as a string to two places, then go back to a float.
    private fun updateX(x: Float) {
        if (xyzArray[0] != x) {
            _xyzArray.add(0, "%.2f".format(x).toFloat())
        }
    }

    private fun updateY(y: Float) {
        if (xyzArray[1] != y) {
            _xyzArray.add(1, "%.2f".format(y).toFloat())
        }
    }

    private fun updateZ(z: Float) {
        if (xyzArray[2] != z) {
            _xyzArray.add(2, "%.2f".format(z).toFloat())
            Log.d("log", z.toString())
        }
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        if (p0 != null) {
            updateX(p0.values[0])
            updateY(p0.values[1])
            updateZ(p0.values[2])
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    fun updateSensorStatus(isRecording: Boolean) {
        if (isRecording) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            _isRecording.value = true
        } else {
            sensorManager.unregisterListener(this)
            _isRecording.value = false
        }
    }
}