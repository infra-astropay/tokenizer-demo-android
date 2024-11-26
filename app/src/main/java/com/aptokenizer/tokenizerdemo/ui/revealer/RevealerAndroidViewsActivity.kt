package com.aptokenizer.tokenizerdemo.ui.revealer

import androidx.activity.enableEdgeToEdge
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.aptokenizer.tokenizer.TokenRevealer
import com.aptokenizer.tokenizer.views.system.TRTextView
import com.aptokenizer.tokenizerdemo.R
import kotlinx.coroutines.launch

class RevealerAndroidViewsActivity : androidx.appcompat.app.AppCompatActivity() {
    private var buttonSaveCardToken: com.google.android.material.button.MaterialButton? = null
    private var buttonClearCardToken: com.google.android.material.button.MaterialButton? = null
    private var buttonRevealCardToken: com.google.android.material.button.MaterialButton? = null
    private var buttonShowData: com.google.android.material.button.MaterialButton? = null
    private var buttonCopyInfo: com.google.android.material.button.MaterialButton? = null
    private var buttonSaveAuthToken: com.google.android.material.button.MaterialButton? = null
    private var buttonClearAuthToken: com.google.android.material.button.MaterialButton? = null
    private var editTextAuthToken: com.google.android.material.textfield.TextInputEditText? = null
    private var editTextCardToken: com.google.android.material.textfield.TextInputEditText? = null

    private val tokenRevealer = TokenRevealer()

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        val numberCardToken = "tok_test_qRbdpyeRZpyCNgdZ5WFaCBrE7ws4ZtnOxbj"

        val enableButtons = androidx.lifecycle.MutableLiveData(true)

        enableEdgeToEdge()
        setContentView(R.layout.activity_android_views)
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val contentPath = "number"
        val cardTextViw = findViewById<TRTextView>(R.id.card_text_view)

        initViews()
        initListeners(contentPath, enableButtons, cardTextViw)
        editTextCardToken?.setText(numberCardToken)


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
        enableButtons: androidx.lifecycle.MutableLiveData<Boolean>,
        cardTextViw: TRTextView
    ) {
        buttonClearAuthToken?.setOnClickListener {
            editTextAuthToken?.setText(String())
            editTextAuthToken?.isEnabled = true
            buttonSaveAuthToken?.isVisible = true
            buttonClearAuthToken?.isVisible = false
        }

        buttonClearCardToken?.setOnClickListener {
            editTextCardToken?.setText(String())
            editTextCardToken?.isEnabled = true
            tokenRevealer.unsubscribe(contentPath = contentPath)
            buttonSaveCardToken?.isVisible = true
            buttonClearCardToken?.isVisible = false
        }

        buttonSaveAuthToken?.setOnClickListener {
            if (editTextAuthToken?.text?.isNotEmpty() == true) {
                editTextAuthToken?.text?.toString()?.trim()
                    ?.let { token -> tokenRevealer.setAccessToken(token) }
                showMessage(getString(R.string.authorization_token_saved))
                editTextAuthToken?.isEnabled = false
                buttonClearAuthToken?.isVisible = true
                buttonSaveAuthToken?.visibility = android.view.View.INVISIBLE
            } else {
                showMessage(getString(R.string.field_not_empty))
            }
        }

        buttonSaveCardToken?.setOnClickListener {
            if (editTextCardToken?.text?.isNotEmpty() == true) {
                editTextCardToken?.text?.toString()?.trim()
                    ?.let { token -> tokenRevealer.subscribe(
                        contentPath = contentPath,
                        token = token,
                        textView = cardTextViw
                    ) }
                showMessage(getString(R.string.card_token_saved))
                editTextCardToken?.isEnabled = false
                buttonClearCardToken?.isVisible = true
                buttonSaveCardToken?.visibility = android.view.View.INVISIBLE
            } else {
                showMessage(getString(R.string.field_not_empty))
            }
        }

        buttonRevealCardToken?.setOnClickListener {
            enableDisabledButtons(false)
            when {
                editTextAuthToken?.text.toString().trim().isEmpty() -> {
                    showMessage(getString(R.string.first_save_auth_token))
                    enableDisabledButtons(true)
                }

                editTextCardToken?.text.toString().trim().isEmpty() -> {
                    showMessage(getString(R.string.first_save_card_token))
                    enableDisabledButtons(true)
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
                                    enableDisabledButtons(true)
                                }

                                is TokenRevealer.TRResult.Error -> {
                                    var errorMessage = result.error
                                    if (result.message.isNotEmpty()) errorMessage = errorMessage.plus(" - ${result.message}")
                                    showMessage(errorMessage)
                                    enableDisabledButtons(true)
                                }
                                is TokenRevealer.TRResult.NetworkError -> {
                                    showMessage(getString(R.string.network_error))
                                    enableDisabledButtons(true)
                                }
                                is TokenRevealer.TRResult.InvalidToken -> {
                                    showMessage(getString(R.string.invalid_token))
                                    enableDisabledButtons(true)
                                }
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
            val showDataText = getString(R.string.show_data)
            val hideData = buttonShowData?.text == showDataText
            buttonShowData?.text = if (hideData) {
                getString(R.string.hide_data)
            } else showDataText
            cardTextViw.showDataRevealed(hideData)
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
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()
    }
}
