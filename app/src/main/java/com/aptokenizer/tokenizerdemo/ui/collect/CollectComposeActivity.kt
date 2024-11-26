package com.aptokenizer.tokenizerdemo.ui.collect

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aptokenizer.tokenizer.TokenCollect
import com.aptokenizer.tokenizer.views.compose.TRTextField
import com.aptokenizer.tokenizer.views.models.RegexRuleValidation
import com.aptokenizer.tokenizer.views.models.TextInputType
import com.aptokenizer.tokenizerdemo.ui.theme.TokenizerDemoTheme
import kotlinx.coroutines.launch
import java.util.Calendar

class CollectComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tokenCollect = TokenCollect()

        setContent {
            TokenizerDemoTheme {
                val scope = rememberCoroutineScope()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    ValueTokenizerFormExample(tokenCollect) {
                        scope.launch {
                            Toast.makeText(this@CollectComposeActivity, it, Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ValueTokenizerFormExample(tokenCollect: TokenCollect, showMessage: (String) -> Unit) {
    var loading by remember { mutableStateOf(false) }
    var cardToken by remember { mutableStateOf(String()) }
    var pinToken by remember { mutableStateOf(String()) }
    var cvvToken by remember { mutableStateOf(String()) }
    var tokenSaved by remember { mutableStateOf(false) }
    var authorizationToken by remember { mutableStateOf(String()) }
    var lengthCardNumber by remember { mutableIntStateOf(0) }
    var lengthPin by remember { mutableIntStateOf(0) }
    var lengthDate by remember { mutableIntStateOf(7) }
    val errorPin = remember { mutableStateListOf<String>() }
    val cardFieldName = "number"
    val pinFieldName = "pin"
    val cvvFieldName = "cvv"
    val dateFieldName = "date"
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = {
                    Text(text = "Authorization Token")
                },
                value = authorizationToken,
                onValueChange = {
                    tokenSaved = false
                    authorizationToken = it
                }
            )

            Button(
                modifier = Modifier.padding(start = 8.dp),
                onClick = {
                    if (authorizationToken.isNotEmpty()) {
                        tokenSaved = true
                        tokenCollect.setAccessToken(authorizationToken.trim())
                        showMessage("Authorization Token Saved")
                    }
                },
                enabled = loading.not()
            ) {
                Text(text = "Save")
            }
        }

        Column(
            modifier = Modifier.padding(top = 20.dp)
        ) {
            TRTextField(
                tokenCollect = tokenCollect,
                modifier = Modifier
                    .fillMaxWidth(),
                fieldName = cardFieldName,
                enabled = tokenSaved,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                onFieldStateChange = { fieldState ->
                    lengthCardNumber = fieldState.contentLength
                },
                showBrand = true,
                textInputType = TextInputType.CARD_NUMBER,
                placeholder = {
                    Text(
                        text = "Enter the 16 digits of the card"
                    )
                }
            )

            TRTextField(
                tokenCollect = tokenCollect,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth(),
                fieldName = pinFieldName,
                enabled = tokenSaved,
                isError = errorPin.isNotEmpty(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.NumberPassword
                ),
                onFieldStateChange = { fieldState ->
                    lengthPin = fieldState.contentLength
                    errorPin.clear()
                    fieldState.regexRuleValidationResult.forEach { validationPin ->
                        if (validationPin.isValid.not()) {
                            validationPin.errorMessage?.let {
                                errorPin.add(it)
                            }
                        }
                    }
                },
                regexRulesValidation = listOf(
                    RegexRuleValidation(
                        pattern = "^(?!.*(0123|1234|2345|3456|4567|5678|6789|7890|3210|4321|5432|6543|7654|8765|9876|0987)).*\$",
                        errorMessage = "The pin is not valid, it must not contain sequential numbers"
                    ),
                    RegexRuleValidation(
                        pattern = "^(?!.*(\\d)\\1{3}).*\$",
                        errorMessage = "The pin is not valid, it must not contain repeated numbers"
                    ),
                    RegexRuleValidation(
                        pattern = "^(\\d{4})?\$",
                        errorMessage = "The pin is not valid, it must have exactly 4 digits"
                    )
                ),
                placeholder = {
                    Text(
                        text = "Enter the 4 digits of the pin"
                    )
                },
                keyboardActions = KeyboardActions.Default
            )

            TRTextField(
                tokenCollect = tokenCollect,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth(),
                fieldName = cvvFieldName,
                enabled = tokenSaved,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                placeholder = {
                    Text(
                        text = "Enter the cvv card"
                    )
                },
                textInputType = TextInputType.CVV,
                keyboardActions = KeyboardActions.Default,
                volatile = true
            )

            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            TRTextField(
                tokenCollect = tokenCollect,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth(),
                fieldName = dateFieldName,
                enabled = tokenSaved,
                enableTokenization = false,
                onFieldStateChange = { fieldState ->
                    lengthDate = fieldState.contentLength
                },
                regexRulesValidation = listOf(
                    RegexRuleValidation(
                        pattern = "^(0[1-9]|1[0-2])/(${currentYear}|[${currentYear.toString()[0]}-${currentYear.toString()[0] + 1}]\\d{3})$",
                        errorMessage = "Fecha no válida. Use el formato MM/YYYY y asegúrese de que el año sea mayor o igual al actual."
                    )
                ),
                placeholder = {
                    Text(
                        text = "MM/YYYY"
                    )
                },
                textInputType = TextInputType.EXPIRATION_DATE,
                keyboardActions = KeyboardActions.Default
            )

            var errorFinal = String()
            errorPin.forEach { errorFinal = errorFinal.plus(it.plus("\n")) }

            Text(modifier = Modifier.fillMaxWidth(), text = errorFinal)
        }

        Button(
            onClick = {
                when {
                    authorizationToken.isEmpty() -> {
                        showMessage("Please first save an authorization token")
                    }

                    else -> {
                        loading = true
                        tokenCollect.collect { result ->
                            cardToken = String()
                            pinToken = String()
                            cvvToken = String()
                            when (result) {
                                is TokenCollect.TRResult.Success -> {
                                    cardToken = result.listTokenizedData[cardFieldName] ?: String()
                                    pinToken = result.listTokenizedData[pinFieldName] ?: String()
                                    cvvToken = result.listTokenizedData[cvvFieldName] ?: String()
                                }

                                is TokenCollect.TRResult.Error -> {
                                    var errorMessage = result.error
                                    if (result.message.isNotEmpty()) errorMessage = errorMessage.plus(" - ${result.message}")
                                    showMessage(errorMessage)
                                }
                                is TokenCollect.TRResult.NetworkError -> showMessage("Network Error")
                                is TokenCollect.TRResult.InvalidToken -> showMessage("Invalid Token")
                                is TokenCollect.TRResult.ValueNoValid -> showMessage(result.errorMessage)
                            }
                            loading = false
                        }
                    }
                }
            },
            enabled = lengthCardNumber > 12 && lengthPin == 4
        ) {
            Text(
                text = if (loading) "Loading..." else "Tokenize data"
            )
        }

        if (cardToken.isNotEmpty()) {
            Text(text = "Card Token: $cardToken")
        }

        if (pinToken.isNotEmpty()) {
            Text(text = "Pin Token: $pinToken")
        }

        if (cvvToken.isNotEmpty()) {
            Text(text = "CVV Token: $cvvToken")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FormExamplePreview() {
    TokenizerDemoTheme {
        ValueTokenizerFormExample(TokenCollect()) {}
    }
}