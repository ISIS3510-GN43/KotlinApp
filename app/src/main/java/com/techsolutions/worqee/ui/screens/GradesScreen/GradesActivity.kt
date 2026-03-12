package com.techsolutions.worqee

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.techsolutions.worqee.ui.screens.GradesScreen
import com.techsolutions.worqee.ui.theme.WorqeeTheme

class GradesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WorqeeTheme {
                GradesScreen()
            }
        }
    }
}