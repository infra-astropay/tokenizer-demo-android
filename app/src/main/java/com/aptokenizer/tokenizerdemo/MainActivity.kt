package com.aptokenizer.tokenizerdemo

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aptokenizer.tokenizer.TokenizerConfig
import com.aptokenizer.tokenizer.domain.models.Environment
import com.aptokenizer.tokenizerdemo.ui.collect.CollectAndroidViewsActivity
import com.aptokenizer.tokenizerdemo.ui.collect.CollectComposeActivity
import com.aptokenizer.tokenizerdemo.ui.revealer.RevealerAndroidViewsActivity
import com.aptokenizer.tokenizerdemo.ui.revealer.RevealerComposeActivity
import com.aptokenizer.tokenizerdemo.ui.revealer.RevealerMixActivity
import com.google.android.material.button.MaterialButton

class MainActivity  : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Application class project
        TokenizerConfig.init(environment = Environment.Sandbox, seeLogs = true)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val buttonRevealComposeActivity = findViewById<MaterialButton>(R.id.button_reveal_compose_activity)
        val buttonRevealMixActivity = findViewById<MaterialButton>(R.id.button_reveal_mix_activity)
        val buttonRevealAndroidViewActivity = findViewById<MaterialButton>(R.id.button_reveal_android_view_activity)
        val buttonCollectComposeActivity = findViewById<MaterialButton>(R.id.button_collect_compose_activity)
        val buttonCollectAndroidViewActivity = findViewById<MaterialButton>(R.id.button_collect_android_view_activity)

        buttonRevealComposeActivity.setOnClickListener {
            startActivity(
                Intent(this, RevealerComposeActivity::class.java)
            )
        }

        buttonRevealMixActivity.setOnClickListener {
            startActivity(
                Intent(this, RevealerMixActivity::class.java)
            )
        }

        buttonRevealAndroidViewActivity.setOnClickListener {
            startActivity(
                Intent(this, RevealerAndroidViewsActivity::class.java)
            )
        }

        buttonCollectComposeActivity.setOnClickListener {
            startActivity(
                Intent(this, CollectComposeActivity::class.java)
            )
        }

        buttonCollectAndroidViewActivity.setOnClickListener {
            startActivity(
                Intent(this, CollectAndroidViewsActivity::class.java)
            )
        }
    }
}