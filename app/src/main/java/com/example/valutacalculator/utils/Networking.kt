package com.example.valutacalculator.utils

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.util.concurrent.TimeUnit


/**
 * Main Networking Class that sets up OkHttpClient
 */

class Networking (private val tag: String) {

    init {
        //Kill earlier request with same tag:
        killEarlierCall()
    }

    private fun killEarlierCall() {
        //A) go through the queued calls and cancel if the tag matches:
        for (call in HTTP_CLIENT.dispatcher.queuedCalls()) {
            if (call.request().tag() == tag) call.cancel()
            System.gc()
        }

        //B) go through the running calls and cancel if the tag matches:
        for (call in HTTP_CLIENT.dispatcher.runningCalls()) {
            if (call.request().tag() == tag) call.cancel()
            System.gc()
        }
    }

    fun get(url: String, callback: Callback): Call {
        val request = Request.Builder()
            .url(url)
            .tag(tag)
            .build()
        val call = HTTP_CLIENT.newCall(request)
        call.enqueue(callback)
        return call
    }

    fun post(url: String, parameters: HashMap<String, String>, callback: Callback): Call {
        val builder = FormBody.Builder()
        val it = parameters.entries.iterator()
        while (it.hasNext()) {
            val pair = it.next() as Map.Entry<*, *>
            builder.add(pair.key.toString(), pair.value.toString())
        }

        val formBody = builder.build()
        val request = Request.Builder()
            .url(url)
            .tag(tag)
            .post(formBody)
            .build()
        val call = HTTP_CLIENT.newCall(request)
        call.enqueue(callback)
        return call
    }

    companion object {
        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        val HTTP_CLIENT = OkHttpClient()
            .newBuilder()
            .connectTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .build()
    }

}