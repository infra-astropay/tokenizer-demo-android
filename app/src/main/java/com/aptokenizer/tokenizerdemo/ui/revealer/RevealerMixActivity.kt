package com.aptokenizer.tokenizerdemo.ui.revealer

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.aptokenizer.tokenizer.TokenRevealer
import com.aptokenizer.tokenizer.TokenizerConfig
import com.aptokenizer.tokenizer.domain.models.Environment
import com.aptokenizer.tokenizer.views.compose.TRText
import com.aptokenizer.tokenizer.views.models.RegexReplaceField
import com.aptokenizer.tokenizerdemo.R
import com.aptokenizer.tokenizerdemo.ui.theme.TokenizerDemoTheme
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class RevealerMixActivity : AppCompatActivity() {
    private var buttonSaveCardToken: MaterialButton? = null
    private var buttonClearCardToken: MaterialButton? = null
    private var buttonRevealCardToken: MaterialButton? = null
    private var buttonShowData: MaterialButton? = null
    private var buttonCopyInfo: MaterialButton? = null
    private var buttonSaveAuthToken: MaterialButton? = null
    private var buttonClearAuthToken: MaterialButton? = null
    private var editTextAuthToken: TextInputEditText? = null
    private var editTextCardToken: TextInputEditText? = null

    private val tokenRevealer = TokenRevealer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val numberCardToken = "tok_test_qRbdpyeRZpyCNgdZ5WFaCBrE7ws4ZtnOxbj"
        TokenizerConfig.init(environment = Environment.Sandbox, seeLogs = true)

        val showData = MutableLiveData(false)
        val enableButtons = MutableLiveData(true)

        enableEdgeToEdge()
        setContentView(R.layout.activity_revealer_mix)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val contentPath = "number"
        val composeView = findViewById<ComposeView>(R.id.compose_view)

        initViews()
        initListeners(contentPath, enableButtons, showData)
        editTextCardToken?.setText(numberCardToken)

        composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {

                TokenizerDemoTheme {
                    val showState by showData.observeAsState()
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        TRText(
                            tokenRevealer = tokenRevealer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(35.dp),
                            contentPath = contentPath,
                            color = Color.White,
                            hintText = "• • • •  • • • •  • • • •  • • • •",
                            showDataRevealed = showState ?: false,
                            fontSize = 18.sp,
                            regexReplaceField = RegexReplaceField(
                                pattern = "(\\d{4})(?=\\d)",
                                replacement = "$1 "
                            )
                        )
                    }

                }
            }
        }

        enableButtons.observe(this) {
            enableDisabledButtons(it)
        }
    }

    private fun initViews() {
        editTextAuthToken = findViewById(R.id.text_authorization_token)
        editTextCardToken = findViewById(R.id.text_number_card_token)
        buttonSaveAuthToken = findViewById(R.id.button_save_auth_token)
        buttonSaveCardToken = findViewById(R.id.button_save_card_token)
        buttonClearAuthToken = findViewById(R.id.button_clear_auth_token)
        buttonSaveCardToken = findViewById(R.id.button_save_card_token)
        buttonClearCardToken = findViewById(R.id.button_clear_card_token)
        buttonRevealCardToken = findViewById(R.id.button_reveal_token)
        buttonShowData = findViewById(R.id.button_show_data)
        buttonCopyInfo = findViewById(R.id.button_copy_info_token)
    }

    private fun initListeners(
        contentPath: String,
        enableButtons: MutableLiveData<Boolean>,
        showData: MutableLiveData<Boolean>
    ) {
        buttonClearAuthToken?.setOnClickListener {
            editTextAuthToken?.setText(String())
            editTextAuthToken?.isEnabled = true
            buttonSaveAuthToken?.isVisible = true
        }

        buttonClearCardToken?.setOnClickListener {
            editTextCardToken?.setText(String())
            editTextCardToken?.isEnabled = true
            tokenRevealer.unsubscribe(contentPath = contentPath)
            buttonSaveCardToken?.isVisible = true
        }

        buttonSaveAuthToken?.setOnClickListener {
            if (editTextAuthToken?.text?.isNotEmpty() == true) {
                editTextAuthToken?.text?.toString()?.trim()
                    ?.let { token -> tokenRevealer.setAccessToken(token) }
                showMessage(getString(R.string.authorization_token_saved))
                editTextAuthToken?.isEnabled = false
                buttonClearAuthToken?.isVisible = true
                buttonSaveAuthToken?.visibility = View.INVISIBLE
            } else {
                showMessage(getString(R.string.field_not_empty))
            }
        }

        buttonSaveCardToken?.setOnClickListener {
            if (editTextCardToken?.text?.isNotEmpty() == true) {
                editTextCardToken?.text?.toString()?.trim()
                    ?.let { token -> tokenRevealer.subscribe(contentPath = contentPath, token = token) }
                showMessage(getString(R.string.card_token_saved))
                editTextCardToken?.isEnabled = false
                buttonClearCardToken?.isVisible = true
                buttonSaveCardToken?.visibility = View.INVISIBLE
            } else {
                showMessage(getString(R.string.field_not_empty))
            }
        }

        buttonRevealCardToken?.setOnClickListener {
            when {
                editTextAuthToken?.text.toString().trim().isEmpty() -> {
                    showMessage(getString(R.string.first_save_auth_token))
                }

                editTextCardToken?.text.toString().trim().isEmpty() -> {
                    showMessage(getString(R.string.first_save_card_token))
                }

                else -> {
                    buttonRevealCardToken?.text = getString(R.string.loading)
                    enableButtons.value = false
                    tokenRevealer.reveal { result ->
                        lifecycleScope.launch {
                            buttonRevealCardToken?.text = getString(R.string.reveal_data)
                            enableButtons.value = true
                            when (result) {
                                is TokenRevealer.TRResult.Success -> {
                                    buttonShowData?.isVisible = true
                                    buttonCopyInfo?.isVisible = true
                                    showData.value = true
                                }

                                is TokenRevealer.TRResult.Error -> {
                                    var errorMessage = result.error
                                    if (result.message.isNotEmpty()) errorMessage = errorMessage.plus(" - ${result.message}")
                                    showMessage(errorMessage)
                                }
                                is TokenRevealer.TRResult.NetworkError -> showMessage(getString(R.string.network_error))
                                is TokenRevealer.TRResult.InvalidToken -> showMessage(getString(R.string.invalid_token))
                            }
                        }
                    }
                }
            }
        }

        buttonCopyInfo?.setOnClickListener {
            tokenRevealer.copy(this, contentPath)
            showMessage(getString(R.string.text_copied))
        }

        buttonShowData?.setOnClickListener {
            showData.value = showData.value?.not()
            buttonShowData?.text = if (showData.value == true) {
                getString(R.string.hide_data)
            } else getString(R.string.show_data)
        }
    }

    private fun enableDisabledButtons(enabled: Boolean) {
        buttonSaveCardToken?.isEnabled = enabled
        buttonRevealCardToken?.isEnabled = enabled
        buttonShowData?.isEnabled = enabled
        buttonCopyInfo?.isEnabled = enabled
        buttonSaveAuthToken?.isEnabled = enabled
        buttonClearCardToken?.isEnabled = enabled
        buttonClearAuthToken?.isEnabled = enabled
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
