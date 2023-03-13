package com.example.valutacalculator.utils

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull


class Networking(private val client: OkHttpClient, private val tag: String) {


    init {
        //Kill earlier request with same tag:
        killEarlierCall()
    }

    private fun killEarlierCall() {
        //A) go through the queued calls and cancel if the tag matches:
        for (call in client.dispatcher.queuedCalls()) {
            if (call.request().tag() == tag) call.cancel()
            System.gc()
        }

        //B) go through the running calls and cancel if the tag matches:
        for (call in client.dispatcher.runningCalls()) {
            if (call.request().tag() == tag) call.cancel()
            System.gc()
        }
    }


    fun get(url: String, callback: Callback): Call {
        val request = Request.Builder()
            .url(url)
            .tag(tag)
            .build()
        val call = client.newCall(request)
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
        val call = client.newCall(request)
        call.enqueue(callback)
        return call
    }

    companion object {
        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
    }

}