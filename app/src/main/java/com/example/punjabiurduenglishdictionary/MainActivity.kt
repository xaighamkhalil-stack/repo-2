package com.example.punjabiurduenglishdictionary

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                DictionaryScreen()
            }
        }
    }
}

data class DictionaryEntry(
    val punjabi: String,
    val urdu: String,
    val english: String
)

private val dictionary = listOf(
    DictionaryEntry("ਸਤ ਸ੍ਰੀ ਅਕਾਲ", "السلام علیکم", "Hello"),
    DictionaryEntry("ਧੰਨਵਾਦ", "شکریہ", "Thank you"),
    DictionaryEntry("ਪਾਣੀ", "پانی", "Water"),
    DictionaryEntry("ਕਿਤਾਬ", "کتاب", "Book"),
    DictionaryEntry("ਪਿਆਰ", "محبت", "Love"),
    DictionaryEntry("ਰੋਟੀ", "روٹی", "Bread"),
    DictionaryEntry("ਦੋਸਤ", "دوست", "Friend"),
    DictionaryEntry("ਘਰ", "گھر", "Home")
)

@Composable
private fun DictionaryScreen() {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<DictionaryEntry?>(null) }
    var errorMessage by remember { mutableStateOf("") }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            val spokenText = activityResult.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                .orEmpty()
            inputText = spokenText
            val entry = findEntry(spokenText)
            result = entry
            errorMessage = if (entry == null) "No translation found in local dictionary." else ""
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val speechIntent = buildSpeechIntent()
            speechLauncher.launch(speechIntent)
        } else {
            errorMessage = "Microphone permission denied."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Punjabi → Urdu → English") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Type Punjabi text or use speech input.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Punjabi") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val entry = findEntry(inputText)
                        result = entry
                        errorMessage = if (entry == null) "No translation found in local dictionary." else ""
                    }
                ) {
                    Text("Translate")
                }

                Button(
                    onClick = {
                        when {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                speechLauncher.launch(buildSpeechIntent())
                            }

                            else -> audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                ) {
                    Text("Speech to Text")
                }
            }

            if (errorMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            }

            result?.let { entry ->
                Spacer(modifier = Modifier.height(16.dp))
                TranslationCard(entry)
            }
        }
    }
}

private fun buildSpeechIntent(): Intent {
    return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pa-PK")
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak Punjabi")
    }
}

private fun findEntry(input: String): DictionaryEntry? {
    val normalized = input.trim().lowercase(Locale.getDefault())
    return dictionary.firstOrNull { it.punjabi.lowercase(Locale.getDefault()) == normalized }
}

@Composable
private fun TranslationCard(entry: DictionaryEntry) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Punjabi: ${entry.punjabi}", fontWeight = FontWeight.SemiBold)
            Text("Urdu: ${entry.urdu}")
            Text("English: ${entry.english}")
        }
    }
}
