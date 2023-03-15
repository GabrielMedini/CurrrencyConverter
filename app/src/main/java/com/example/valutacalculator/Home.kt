package com.example.valutacalculator

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
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
import com.example.valutacalculator.data.Currency
import com.example.valutacalculator.ui.theme.CurrencyConverterTheme
import com.example.valutacalculator.utils.CurrencyCalculator
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DateFormat.getDateInstance
import java.util.*


val currencyListFrom = setCurrencyList()

/**
 * Main function on home screen containing all home screen UI:
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home() {
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

/**
 * Main Holder for all UI and logic:
 */
@Composable
fun MainCurrencyBox(
    context: Context,
    currencyList: List<Currency>,
    modifier: Modifier = Modifier
) {
    //For updating start values one time:
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
    //Contains selected from currency:
    var satSelectedFromCurrency by rememberSaveable{
        mutableStateOf(START_FROM_CURRENCY)
    }
    //Contains selected to currency:
    var satSelectedToCurrency by rememberSaveable{
        mutableStateOf(START_TO_CURRENCY)
    }
    //General coroutine scope for async task:
    val coroutineScope = rememberCoroutineScope()
    //Conversation number that is used to calculate to currency:
    var calculationNumber by rememberSaveable{
        mutableStateOf(BigDecimal(0))
    }
    //Number value from (currency):
    var calculatedFromValue by rememberSaveable{
        mutableStateOf(BigDecimal(100))
    }
    //Number value to (currency):
    var calculatedToValue by rememberSaveable{
        mutableStateOf(BigDecimal(0))
    }
    //Text for "" instead of 0 on empty value to (currency):
    var calculatedToValueText by rememberSaveable{
        mutableStateOf("100")
    }
    //Calculates the to currency value:
    val calculateToValue: () -> Unit = {
        coroutineScope.launch {
            calculatedToValue = (calculatedFromValue * calculationNumber).setScale(2, RoundingMode.HALF_UP)
        }
    }
    //Fetching conversation number from server, Class: CurrencyCalculator:
    val updateCurrencyCalculationNumberOnClick: () -> Unit = {
        coroutineScope.launch {
            //If 0 error, else good:
            val serverCalculationNumber = CurrencyCalculator().calculateCurrency(
                context = context,
                fromCurrency = satSelectedFromCurrency,
                toCurrency = satSelectedToCurrency
            )
            if(serverCalculationNumber != BigDecimal(0)) {
                calculationNumber = serverCalculationNumber
                calculateToValue()
            } else {
                Toast.makeText(context, R.string.trouble_fetching, Toast.LENGTH_SHORT).show()
            }
        }
    }
    //Does one initial calculation based on default startup values:
    if(firstLaunch) {
        updateCurrencyCalculationNumberOnClick()
        firstLaunch = false
    }

    //Date updated from on server, this value is static on server:
    //Format time:
    val dateFormat = getDateInstance()
    val cal = Calendar.getInstance()
    cal.add(Calendar.DATE, -1)
    val updatedDate = dateFormat.format(cal.time)


    //Main UI windows:
    Column(modifier = modifier
        .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        //From Currency box:
        FromRow(
            selectedCurrency = satSelectedFromCurrency,
            onClick = { showFromCurrencyList = !showFromCurrencyList },
            onValueChange = { fromValue ->
                if( fromValue.toBigDecimalOrNull() != null ) {
                    calculatedFromValue = fromValue.toBigDecimal()
                    calculatedToValueText = fromValue
                    calculateToValue()
                } else {
                    calculatedFromValue = BigDecimal(0)
                    calculatedToValueText = ""
                    calculateToValue()
                } },
            textFieldValue = calculatedToValueText
        )
        //Conversion rate Text:
        ConversionRateText(
            conversionRate = calculationNumber.toString(),
            fromCurrency = satSelectedFromCurrency,
            toCurrency = satSelectedToCurrency,
            updatedDate = updatedDate
        )
        //Converted currency box:
        ToRow(
            selectedCurrency = satSelectedToCurrency,
            onClick = { showToCurrencyList = !showToCurrencyList },
            calculatedCurrencyValue = calculatedToValue.toString(),
        )

    }

    //Opens Select currency from list:
    if(showFromCurrencyList) {
        CurrencyList(currencyList, onclick = { title ->
            satSelectedFromCurrency = title
            showFromCurrencyList = !showFromCurrencyList
            updateCurrencyCalculationNumberOnClick()
        })
    }
    //Opens Select currency to list:
    if(showToCurrencyList) {
        CurrencyList(currencyList, onclick = { title ->
            satSelectedToCurrency =  title
            showToCurrencyList = !showToCurrencyList
            updateCurrencyCalculationNumberOnClick()
        })
    }

}

/**
 * Top app bar
 */
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
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium
                )
            },
        )
}

/**
 * Box containing elements:
 * FROM currency with popup list onClick and Text field with value to be calculated.
 * Including params for hosting elements.
 */
@Composable
fun FromRow(
    selectedCurrency: String,
    onClick: () -> Unit,
    onValueChange: (String) -> Unit,
    textFieldValue: String,
    modifier: Modifier = Modifier

) {
    Row( horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        SelectedCurrencyText(
            selectedCurrency = selectedCurrency,
            onClick = { onClick() },
            modifier = modifier
        )
        CurrencyValueTextField(
            onValueChange =  onValueChange,
            textFieldValue = textFieldValue,
            modifier = modifier
        )
    }
}

/**
 *
 * Box containing elements:
 * TO currency with popup list onClick and Text with calculated converted value:
 * @param selectedCurrency: The currency that is selected
 * @param onClick: Lambda function to trigger showing currency list
 * @param calculatedCurrencyValue: the number value of calculated "to" currency.
 *
 */
@Composable
fun ToRow(
    selectedCurrency: String,
    onClick: () -> Unit,
    calculatedCurrencyValue: String,
    modifier: Modifier = Modifier
) {
    Row( horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        SelectedCurrencyText(
            selectedCurrency = selectedCurrency,
            onClick = { onClick() },
            modifier = modifier
        )
        //To currency value text:
        CalculatedToText(text = calculatedCurrencyValue.toString())
    }
}

/**
 * Surface with selected Currency text:
 * @param selectedCurrency: String (String with selected Currency).
 * @param onClick: Lambda function to trigger showing currency list.
 */
@Composable
fun SelectedCurrencyText(
    selectedCurrency: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = selectedCurrency,
            style = MaterialTheme.typography.headlineMedium,
            modifier = modifier
                .semantics { heading() }
                .clickable { onClick() }
                .padding(vertical = 12.dp, horizontal = 16.dp)
                .width(100.dp)
        )
    }
}

/**
 * Text with info about conversion rate for selected currency from and to:
 * @param conversionRate: String gotten from server with conversion rate.
 * @param fromCurrency: From selected currency
 * @param toCurrency: To selected currency
 * @param updatedDate: Date currency was last updated
 */
@Composable
fun ConversionRateText(
    modifier: Modifier = Modifier,
    conversionRate: String = "",
    fromCurrency: String,
    toCurrency: String,
    updatedDate: String
) {
    var text = ""
    val conversionCalculation =  BigDecimal(1) * conversionRate.toBigDecimal()
    if(conversionRate != "") {
        text += "1 $fromCurrency ${stringResource(id = R.string.equals)} $conversionCalculation $toCurrency.\n" +
                "Updated $updatedDate 23:59:59"
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier
            .fillMaxWidth()
            .semantics { heading() }
            .padding(vertical = 16.dp, horizontal = 12.dp)
    )
}

/**
 * Text Field where user can write number to be calculated:
 * @param onValueChange: Lambda function that triggers new calculation and updates the UI.
 * @param textFieldValue: Value of the TextField
 */
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
        )
}

/**
 * Text that shows the calculated value:
 * @param text: String with calculated value.
 */
@Composable
fun CalculatedToText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        modifier = modifier
            .padding(top = 12.dp, bottom = 8.dp)
    )
}

/**
 * CurrencyList
 * List with all currencies
 * @param currencyList: List containing all available values. List is defined in Currency file.
 * @param onclick: lambda function that triggers on selection of currency in list
 */
@Composable
fun CurrencyList(
    currencyList: List<Currency>,
    onclick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = Modifier.padding(horizontal = 16.dp, vertical = 70.dp)) {
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

/**
 * Contains one Currency item for use in currency list.
 * @param label: Short string label with for.ex (EUR)
 * @param title: long name of currency for.ex (
 */
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


/**
 * COMPOSABLE PREVIEWS:
 */
@Preview(showBackground = true)
@Composable
fun FromRowPreview() {
    CurrencyConverterTheme {
        FromRow("EUR", {}, {}, "100")
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
        //CurrencyTextField()
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CurrencyConverterTheme {

    }
}