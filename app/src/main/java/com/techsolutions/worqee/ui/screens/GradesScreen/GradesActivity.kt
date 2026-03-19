package com.techsolutions.worqee.ui.screens.GradesScreen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.techsolutions.worqee.ui.screens.GradesScreen
import com.techsolutions.worqee.ui.theme.WorqeeTheme
import com.techsolutions.worqee.ui.screens.GradesScreen.viewmodel.GradesViewModel

class GradesActivity : ComponentActivity() {
    private val viewModel: GradesViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WorqeeTheme {
                GradesScreen(viewModel)
            }
        }
    }
}