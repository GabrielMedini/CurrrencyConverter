package com.example.valutacalculator

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.valutacalculator.data.*
import com.example.valutacalculator.ui.theme.CurrencyConverterTheme
import com.example.valutacalculator.utils.CurrencyCalculator
import kotlinx.coroutines.launch
import java.math.BigDecimal


val currencyListFrom = setCurrencyList()
val currencyListTo = setCurrencyList()


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home() {
    var shouldShowOnBoarding by rememberSaveable { mutableStateOf(true) }
    Scaffold(
        topBar = { AppBar() }
    ) { padding ->
        MainCurrencyBox(
            context = LocalContext.current,
            currencyList = currencyListFrom,
            Modifier.padding(padding)
        )
    }
}

@Composable
fun MainCurrencyBox(
    context: Context,
    currencyList: List<Currency>,
    modifier: Modifier = Modifier
) {
    var firstLaunch by rememberSaveable{
        mutableStateOf(true)
    }

    //Setting state for showing currency lists:
    var showFromCurrencyList by rememberSaveable{
        mutableStateOf(false)
    }
    var showToCurrencyList by rememberSaveable{
        mutableStateOf(false)
    }
    var satSelectedFromCurrency by rememberSaveable{
        mutableStateOf(selectedFromCurrency)
    }
    var satSelectedToCurrency by rememberSaveable{
        mutableStateOf(selectedToCurrency)
    }

    val coroutineScope = rememberCoroutineScope()

    var calculationNumber by rememberSaveable{
        mutableStateOf(BigDecimal(0))
    }

    var calculatedFromValue by rememberSaveable{
        mutableStateOf(BigDecimal(100))
    }

    var calculatedToValue by rememberSaveable{
        mutableStateOf(BigDecimal(0))
    }


    val calculateToValue: () -> Unit = {
        coroutineScope.launch {
            calculatedToValue = calculatedFromValue * calculationNumber
        }
    }


    val updateCurrencyCalculationNumberOnClick: () -> Unit = {
        coroutineScope.launch {
            calculationNumber = CurrencyCalculator().calculateCurrency(
                context = context,
                fromCurrency = satSelectedFromCurrency,
                toCurrency = satSelectedToCurrency
            )
            calculateToValue()
        }
    }

    if(firstLaunch) {
        updateCurrencyCalculationNumberOnClick()
        firstLaunch = false
    }



    Column(modifier = modifier
        .padding(horizontal = 12.dp, vertical = 16.dp)
    ) {
        //Selected from currency:
        SelectedCurrencyText(
            selectedCurrency = satSelectedFromCurrency,
            onClick = { showFromCurrencyList = !showFromCurrencyList }
        )

        //From currency value:
        CurrencyValueTextField(
            onValueChange = {fromValue ->
                if(fromValue != "") {
                    calculatedFromValue = fromValue.toBigDecimal()
                    calculateToValue()
                }
        },
            textFieldValue = calculatedFromValue.toString()
        )
        //Conversion rate Text:
        ConversionRateText(conversionRate = calculationNumber.toString(),)



        //Selected to currency:
        SelectedCurrencyText(
            selectedCurrency = satSelectedToCurrency,
            onClick = { showToCurrencyList = !showToCurrencyList }
        )

        //To currency value:
        CalculatedToText(text = calculatedToValue.toString())
    }
    //Select currency from list
    if(showFromCurrencyList) {
        CurrencyList(currencyList, onclick = { title ->
            satSelectedFromCurrency = title
            selectedFromCurrency = title
            showFromCurrencyList = !showFromCurrencyList
            println(title)
            updateCurrencyCalculationNumberOnClick()
        })
    }
    //Select currency to list
    if(showToCurrencyList) {
        CurrencyList(currencyList, onclick = { title ->
            satSelectedToCurrency =  title
            selectedToCurrency = title
            showToCurrencyList = !showToCurrencyList
            updateCurrencyCalculationNumberOnClick()
        })
    }

}







@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppBar() {
    TopAppBar(
        navigationIcon = {
            Icon(
                imageVector = Icons.Default.AttachMoney,
                contentDescription = null,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        },
        title = {
            Text(text = stringResource(R.string.app_name))
        }
    )
}

@Composable
fun Header(
    text: String,
    modifier: Modifier = Modifier
) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium,
            modifier = modifier
                .fillMaxWidth()
                .semantics { heading() }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
}




@Composable
fun SelectedCurrencyText(
    selectedCurrency: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.onSurface.copy(0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = selectedCurrency,
            style = MaterialTheme.typography.headlineMedium,
            modifier = modifier
                .fillMaxWidth()
                .semantics { heading() }
                .clickable { onClick() }
                .padding(16.dp)
        )
    }
}

@Composable
fun ConversionRateText(
    conversionRate: String = "",
    modifier: Modifier = Modifier

) {
    var text = ""
    if(conversionRate != "") {
        text += " ${stringResource(id = R.string.conversion_rate)}: $conversionRate"
    }
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        modifier = modifier
            .fillMaxWidth()
            .semantics { heading() }
            .padding(16.dp)
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyValueTextField(
    onValueChange: (String) -> Unit,
    textFieldValue: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        value = textFieldValue,
        onValueChange = {onValueChange(it)},
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
        )
}

@Composable
fun CalculatedToText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(16.dp)
    )
}


@Composable
fun CurrencyList(
    currencyList: List<Currency>,
    onclick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = Modifier.padding(horizontal = 20.dp)) {
        LazyColumn(modifier = modifier) {
            items(currencyList) {
                CurrencyListItem(
                    label = it.shortLabel,
                    title = it.title,
                    modifier.clickable {onclick(it.shortLabel)}
                )
            }
        }
    }
}






@Composable
fun CurrencyListItem(
    label: String,
    title: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Surface(shape = MaterialTheme.shapes.small) {
            Text(text = "$label - $title",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}




@Composable
fun CurrencyTextField() {

}

@Preview(showBackground = true)
@Composable
fun AppBarPreview() {
    CurrencyConverterTheme {
        AppBar()
    }
}


@Preview(showBackground = true)
@Composable
fun MainPreview() {
    CurrencyConverterTheme {
        Home()
    }
}

@Preview(showBackground = true)
@Composable
fun SelectedCurrencyTextPreview() {
    CurrencyConverterTheme {
        SelectedCurrencyText("EUR", {})
    }
}

@Preview(showBackground = true)
@Composable
fun CurrencyListPreview() {
    CurrencyConverterTheme {
        CurrencyList(currencyListFrom, {})
    }
}

@Preview(showBackground = true)
@Composable
fun CurrencyTextFieldPreview() {
    CurrencyConverterTheme {
        CurrencyTextField()
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CurrencyConverterTheme {

    }
}