package ir.persianvoicetyping

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
        setContent { VoiceTypingApp() }
    }
}

@Composable
private fun VoiceTypingApp() {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    var listening by remember { mutableStateOf(false) }
    val recognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val intent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fa-IR")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
        }
    }

    DisposableEffect(Unit) {
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { listening = true }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { listening = false }
            override fun onError(error: Int) { listening = false }
            override fun onResults(results: Bundle?) {
                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let { text = it }
                listening = false
            }
            override fun onPartialResults(partialResults: Bundle?) {
                partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let { text = it }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        onDispose { recognizer.destroy() }
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(32.dp))
                Text("تایپ صوتی فارسی", style = MaterialTheme.typography.headlineMedium)
                Text("صحبت کن؛ متن هم‌زمان نوشته می‌شود", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(28.dp))
                Card(modifier = Modifier.fillMaxWidth().weight(1f), shape = RoundedCornerShape(24.dp)) {
                    Text(
                        text = if (text.isBlank()) "متن شما اینجا نمایش داده می‌شود…" else text,
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                        textAlign = TextAlign.Right,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = {
                        if (listening) { recognizer.stopListening(); listening = false }
                        else recognizer.startListening(intent)
                    },
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = RoundedCornerShape(18.dp)
                ) { Text(if (listening) "توقف" else "شروع صحبت") }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}
