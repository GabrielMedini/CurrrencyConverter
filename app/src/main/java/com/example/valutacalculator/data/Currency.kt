package com.example.valutacalculator.data

import java.text.DateFormat
import java.util.*

class Currency(val shortLabel: String, val title: String)

//Start currency values
const val START_FROM_CURRENCY = "NOK"
const val START_TO_CURRENCY = "USD"

fun setCurrencyList() = listOf(
        Currency("EUR", "Euro"),
        Currency("USD", "US Dollar"),
        Currency("JPY", "Japanese Yen"),
        Currency("BGN", "Bulgarian Lev"),
        Currency("CZK", "Czech Republic Koruna"),
        Currency("DKK", "Danish Krone"),
        Currency("GBP", "British Pound Sterling"),
        Currency("HUF", "Hungarian Forint"),
        Currency("PLN", "Polish Zloty"),
        Currency("RON", "Romanian Leu"),
        Currency("SEK", "Swedish Krona"),
        Currency("CHF", "Swiss Franc"),
        Currency("ISK", "Icelandic Króna"),
        Currency("NOK", "Norwegian Krone"),
        Currency("HRK", "Croatian Kuna"),
        Currency("RUB", "Russian Ruble"),
        Currency("TRY", "Turkish Lira"),
        Currency("AUD", "Australian Dollar"),
        Currency("BRL", "Brazilian Real"),
        Currency("CAD", "Canadian Dollar"),
        Currency("CNY", "Chinese Yuan"),
        Currency("HKD", "Hong Kong Dollar"),
        Currency("IDR", "Indonesian Rupiah"),
        Currency("ILS", "Israeli New Sheqel"),
        Currency("INR", "Indian Rupee"),
        Currency("KRW", "South Korean Won"),
        Currency("MXN", "Mexican Peso"),
        Currency("MYR", "Malaysian Ringgit"),
        Currency("NZD", "New Zealand Dollar"),
        Currency("PHP", "Philippine Peso"),
        Currency("SGD", "Singapore Dollar"),
        Currency("THB", "Thai Baht"),
        Currency("ZAR", "South African Rand")
)