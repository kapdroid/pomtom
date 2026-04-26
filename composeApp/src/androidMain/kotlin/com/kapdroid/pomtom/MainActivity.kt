package com.kapdroid.pomtom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // installSplashScreen MUST run before super.onCreate so the AndroidX
        // SplashScreen lib can swap the activity from Theme.PomTom.SplashScreen
        // (cream + tomato icon, declared in themes.xml) to Theme.PomTom for the
        // running app. Hands the splash off cleanly on every Android version ≥ 23.
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}
