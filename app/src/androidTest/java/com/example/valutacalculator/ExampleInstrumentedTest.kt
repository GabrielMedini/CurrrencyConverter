package com.example.valutacalculator

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.valutacalculator.utils.CurrencyCalculator

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import java.math.BigDecimal

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    suspend fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.valutacalculator", appContext.packageName)

        //Testing that Currency calculator is providing calculation number




    }
}