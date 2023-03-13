package com.example.valutacalculator.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast
import com.example.valutacalculator.R
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.math.BigDecimal
import java.util.concurrent.TimeUnit


/**
 * CurrencyCalculator
 * Checks internet connection status and contacts API for getting currency rates:
 */

class CurrencyCalculator {

    /**
     * Main function for calculating Currency
     * 1. Check internet
     * 2. make call to server
     * 3. return with conversation rate.
     * @param context: Application Context
     * @param fromCurrency: String with "from" currency for.ex "EUR"
     * @param toCurrency: String with "to" currency for.ex "USD"
     * @return BigDecimal: if error 0 else conversion rate.
     */
    suspend fun calculateCurrency(
        context: Context,
        fromCurrency: String,
        toCurrency: String
    ): BigDecimal {
        var jsonObject = JSONObject()
        if(calculateCurrencyJob.isActive || calculateCurrencyJob.isCompleted) {
            calculateCurrencyJob.cancel("reset calculating job")
            calculateCurrencyJob = Job()
        }
        CoroutineScope(IO + calculateCurrencyJob).launch(COROUTINE_EXCEPTION_HANDLER) {
            val setCurrencyJob = launch {
                jsonObject = JSONObject(serverCall(context, "GET CURRENCY", fromCurrency, toCurrency))
            }
            setCurrencyJob.join()
            calculateCurrencyJob.complete()
        }

        calculateCurrencyJob.invokeOnCompletion {
            if(it != null) {
                Log.i(TAG, "Error finding recipes: $it")
            }
        }
        calculateCurrencyJob.join()
        //Check response:
        //the return will have a message if error, else good:
        return if(!jsonObject.has("message")) {
            val element = jsonObject["data"].toString()
            JSONObject(element)[toCurrency].toString().toBigDecimal()
            //For testing error toast:
            //BigDecimal(0)

        } else {
            BigDecimal(0)
        }
    }

    /**
     * Makes the call to a server:
     * @param context: Application context
     * @param tag: tag for debugging
     * @param fromCurrency: String with "from" currency for.ex "EUR"
     * @param toCurrency: String with "to" currency for.ex "USD"
     */
    private suspend fun serverCall(
        context: Context,
        tag: String,
        fromCurrency: String,
        toCurrency: String): String
    {
        //check if online and wait if not:
        internetConnectLooper(context, tag)
        //Do stuff
        var returnString = String()
        val request = Networking(HTTP_CLIENT, tag)
        val url = """
            $SERVER_ADDRESS_URL
            &base_currency=$fromCurrency
            &currencies=$toCurrency
            """.trimIndent()
        val serverResponse = Job()
        request.get(url, object : Callback {
            override fun onResponse(call: Call, response: Response) {
                CoroutineScope(IO + serverResponse).launch {
                        returnString = response.body?.string().toString()
                        //Log.i(tag, "Got response from server: $returnString")
                        serverResponse.complete()
                    }
                }
                override fun onFailure(call: Call, e: IOException) {
                    Log.i(tag, "error contacting server: $e")
                    returnString = ""
                }
            })

        //Wait for server response and return:
        serverResponse.join()
        return returnString
    }


    /**
     * Checks for internet connection (async and waits and tries connection on a timed connection loop).
     * Timing is defined in companion.
     */
    private suspend fun internetConnectLooper(context: Context, tag: String) {
        isConnectedToInternet = false
        if(!isOnline(context)) {
            val currentTime = System.currentTimeMillis().toInt()
            val timeDiff = currentTime-toastTimestamp
            if(firstDisconnect || timeDiff>=NO_CONNECTION_WAIT_TIME) {
                withContext(Dispatchers.Main) {
                    toastTimestamp = System.currentTimeMillis().toInt()
                    Toast.makeText(context, R.string.no_internet, Toast.LENGTH_SHORT).show()
                }
            }
            if(firstDisconnect) {
                firstDisconnect = false
                CoroutineScope(IO + waitingForConnectionJob).launch(COROUTINE_EXCEPTION_HANDLER) {
                    val connectionTesterLoop = Job()
                    CoroutineScope(IO + connectionTesterLoop).launch {
                        while(!isOnline(context)) {
                            delay(3000)
                        }
                        isConnectedToInternet = true
                        connectionTesterLoop.complete()
                    }
                    //Toast for missing connection:
                    /*
                    while (!isConnectedToInternet) {
                        println("TEST 7, tag: $tag context: $context, First connect: $firstDisconnect")
                        withContext(Main) {
                            Toast.makeText(context, R.string.no_internet, Toast.LENGTH_SHORT).show()
                        delay(20000)
                    }
                    */
                    connectionTesterLoop.join()
                    waitingForConnectionJob.complete()
                    firstDisconnect = true
                }
            }
            waitingForConnectionJob.join()
        }
    }

    /**
     * Checks device networking capabilities:
     */
    private fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                return true
            }
        }
        return false
    }

    companion object {
        private var calculateCurrencyJob = Job()
        private const val TAG = "CURRENCY CALCULATING JOB"
        private val COROUTINE_EXCEPTION_HANDLER = CoroutineExceptionHandler { _, exception -> Log.i(TAG, "Error: $exception")}
        private const val SERVER_ADDRESS_URL = "https://api.freecurrencyapi.com/v1/latest?apikey=TIVsbtVazp1JvyHbowJ6StsOMIQN06CQT9ubWuB1"
        var isConnectedToInternet = false
        var firstDisconnect = true
        private val waitingForConnectionJob = Job()
        private var toastTimestamp = 0
        const val NO_CONNECTION_WAIT_TIME = 5000
        val HTTP_CLIENT = OkHttpClient()
            .newBuilder()
            .connectTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .build()
    }

}