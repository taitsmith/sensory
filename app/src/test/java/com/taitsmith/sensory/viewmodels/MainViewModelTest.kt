package com.taitsmith.sensory.viewmodels

import android.app.Application
import android.graphics.Color
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.patrykandpatrick.vico.core.entry.entryOf
import com.taitsmith.sensory.MainDispatchRule
import com.taitsmith.sensory.getOrAwaitValue
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@RunWith(JUnit4::class)
class MainViewModelTest {

    @get:Rule
    var rule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatchRule()

    @Mock
    private lateinit var application: Application

    private lateinit var mainViewModel: MainViewModel
    private lateinit var mockedFloatArray: List<Float>

    private val testDispatcher = StandardTestDispatcher()

    private var colorX = 0
    private var colorY = 0
    private var colorZ = 0

    private fun createMockedFloatArray() {
        mockedFloatArray = listOf(.34f, .12f, .82f)
    }


    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        createMockedFloatArray()

        colorX = Color.parseColor("#FF340FF")
        colorY = Color.parseColor("#FFFF1200")
        colorZ = Color.parseColor("#FF550082")

        mainViewModel = MainViewModel(application)
    }

    @After
    fun teardown() {
        testDispatcher.cancel()
    }

    @Test
    fun `test update sensor status livedata updates`() = testDispatcher.run {
        mainViewModel.updateSensorStatus(true)
        assertEquals(true, mainViewModel.isRecording.getOrAwaitValue())
        mainViewModel.updateSensorStatus(false)
        assertEquals(false, mainViewModel.isRecording.getOrAwaitValue())
    }

    @Test
    fun `test updatemillis updates timer value`() {
        mainViewModel.updateTimerPeriod(5)
        assertEquals(200L, mainViewModel.timerPeriodInMillis)
    }

    @Test
    fun `test array livedata updates`() = testDispatcher.run {
        mainViewModel.updateArray(mockedFloatArray)
        assertEquals(mockedFloatArray, mainViewModel.xyzArray.getOrAwaitValue())
    }

    @Test
    fun `test xyzArray updates chart entries live data`() = testDispatcher.run {
        mainViewModel.updateArray(mockedFloatArray)
        mainViewModel.updateValues()
        assertEquals(entryOf(0, .34f), mainViewModel.chartEntries.getOrAwaitValue()[0])
        assertEquals(entryOf(0, .12f), mainViewModel.chartEntries.getOrAwaitValue()[1])
    }

    @Test
    fun `test colors return properly`() = testDispatcher.run {
        mainViewModel.updateArray(mockedFloatArray)

        assertEquals(colorX, mainViewModel.colorX())
        assertEquals(colorY, mainViewModel.colorY())
        assertEquals(colorZ, mainViewModel.colorZ())
    }
}