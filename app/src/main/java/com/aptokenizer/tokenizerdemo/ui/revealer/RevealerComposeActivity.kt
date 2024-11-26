package com.aptokenizer.tokenizerdemo.ui.revealer

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aptokenizer.tokenizer.TokenRevealer
import com.aptokenizer.tokenizer.TokenizerConfig
import com.aptokenizer.tokenizer.domain.models.Environment
import com.aptokenizer.tokenizer.views.compose.TRText
import com.aptokenizer.tokenizer.views.models.RegexReplaceField
import com.aptokenizer.tokenizerdemo.R
import com.aptokenizer.tokenizerdemo.ui.theme.TokenizerDemoTheme
import kotlinx.coroutines.launch

class RevealerComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val numberCardToken = "tok_test_qRbdpyeRZpyCNgdZ5WFaCBrE7ws4ZtnOxbj"
        val tokenRevealer = TokenRevealer()
        TokenizerConfig.init(environment = Environment.Sandbox, seeLogs = true)

        setContent {
            TokenizerDemoTheme {
                val scope = rememberCoroutineScope()
                var show by remember { mutableStateOf(false) }
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    TokenRevealerFormExample(
                        tokenRevealer,
                        LocalContext.current,
                        numberCardToken,
                        show,
                        {
                            scope.launch {
                                Toast.makeText(this@RevealerComposeActivity, it, Toast.LENGTH_LONG).show()
                            }
                        }
                    ) {
                        show = show.not()
                    }
                }
            }
        }
    }
}

@Composable
fun TokenRevealerFormExample(tokenRevealer: TokenRevealer, context: Context, initialNumberCard: String, show: Boolean, showMessage: (String) -> Unit, showAction: () -> Unit) {
    val contentPath = "number"
    var loading by remember { mutableStateOf(false) }
    var success by remember { mutableStateOf(false) }
    var authTokenSaved by remember { mutableStateOf(false) }
    var cardTokenSaved by remember { mutableStateOf(false) }
    var authorizationToken by remember { mutableStateOf(String()) }
    var numberCardToken by remember { mutableStateOf(initialNumberCard) }
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
                    Text(text = context.getString(R.string.authorization_token))
                },
                value = authorizationToken,
                enabled = authTokenSaved.not(),
                onValueChange = {
                    authorizationToken = it
                }
            )

            if (authTokenSaved.not()) {
                Button(
                    modifier = Modifier.padding(start = 8.dp),
                    onClick = {
                        if (authorizationToken.trim().isNotEmpty()) {
                            tokenRevealer.setAccessToken(authorizationToken.trim())
                            showMessage(context.getString(R.string.authorization_token_saved))
                            authTokenSaved = true
                        } else {
                            showMessage(context.getString(R.string.field_not_empty))
                        }
                    },
                    enabled = loading.not()
                ) {
                    Text(text = context.getString(R.string.save))
                }
            } else {
                Button(
                    modifier = Modifier.padding(start = 8.dp),
                    onClick = {
                        authTokenSaved = false
                        authorizationToken = String()
                        cardTokenSaved = false
                    },
                    enabled = loading.not()
                ) {
                    Text(text = context.getString(R.string.clear))
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = {
                    Text(text = context.getString(R.string.number_card_token))
                },
                value = numberCardToken,
                enabled = cardTokenSaved.not(),
                onValueChange = {
                    numberCardToken = it
                }
            )

            if (cardTokenSaved.not()) {
                Button(
                    modifier = Modifier.padding(start = 8.dp),
                    onClick = {
                        if (numberCardToken.trim().isNotEmpty()) {
                            tokenRevealer.subscribe(
                                contentPath = contentPath,
                                token = numberCardToken.trim()
                            )
                            showMessage(context.getString(R.string.card_token_saved))
                            cardTokenSaved = true
                        } else {
                            showMessage(context.getString(R.string.field_not_empty))
                        }
                    },
                    enabled = loading.not()
                ) {
                    Text(text = context.getString(R.string.save))
                }
            } else {
                Button(
                    modifier = Modifier.padding(start = 8.dp),
                    onClick = {
                        cardTokenSaved = false
                        numberCardToken = String()
                        tokenRevealer.unsubscribe(contentPath = contentPath)
                    },
                    enabled = loading.not()
                ) {
                    Text(text = context.getString(R.string.clear))
                }
            }
        }

        Row(
            modifier = Modifier.padding(top = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TRText(
                tokenRevealer = tokenRevealer,
                modifier = Modifier
                    .weight(1f)
                    .height(35.dp),
                contentPath = contentPath,
                hintText = "• • • •  • • • •  • • • •  • • • •",
                showDataRevealed = show,
                fontSize = 18.sp,
                regexReplaceField = RegexReplaceField(
                    pattern = "(\\d{4})(?=\\d)",
                    replacement = "$1 "
                )
            )

            if (show) {
                Button(
                    modifier = Modifier.padding(start = 8.dp),
                    onClick = {
                        tokenRevealer.copy(context, contentPath)
                        showMessage(context.getString(R.string.text_copied))
                    },
                    enabled = loading.not()
                ) {
                    Text(text = context.getString(R.string.copy))
                }
            }
        }

        Button(
            onClick = {
                when {
                    authorizationToken.isEmpty() -> {
                        showMessage(context.getString(R.string.first_save_auth_token))
                    }

                    numberCardToken.isEmpty() -> {
                        showMessage(context.getString(R.string.first_save_card_token))
                    }

                    else -> {
                        loading = true
                        tokenRevealer.reveal { result ->
                            success = false
                            when (result) {
                                is TokenRevealer.TRResult.Success -> {
                                    success = true
                                    showAction()
                                }

                                is TokenRevealer.TRResult.Error -> {
                                    var errorMessage = result.error
                                    if (result.message.isNotEmpty()) errorMessage = errorMessage.plus(" - ${result.message}")
                                    showMessage(errorMessage)
                                }
                                is TokenRevealer.TRResult.NetworkError -> showMessage(
                                    context.getString(R.string.network_error)
                                )
                                is TokenRevealer.TRResult.InvalidToken -> showMessage(
                                    context.getString(R.string.invalid_token)
                                )
                            }
                            loading = false
                        }
                    }
                }
            },
            enabled = loading.not()
        ) {
            Text(
                text = if (loading) {
                    context.getString(R.string.loading)
                } else context.getString(R.string.reveal_data)
            )
        }

        if (success) {
            Button(
                onClick = {
                    showAction()
                },
                enabled = loading.not()
            ) {
                Text(
                    text = context.getString(
                        if (show) R.string.hide_data
                        else R.string.show_data
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FormExamplePreview() {
    TokenizerDemoTheme {
        TokenRevealerFormExample(
            tokenRevealer = TokenRevealer(),
            context = LocalContext.current,
            initialNumberCard = "",
            show = true,
            {}
        ) {}
    }
}