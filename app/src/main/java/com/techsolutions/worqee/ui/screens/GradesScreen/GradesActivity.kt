package com.techsolutions.worqee.ui.screens.GradesScreen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.techsolutions.worqee.ui.screens.GradesScreen
import com.techsolutions.worqee.ui.theme.WorqeeTheme
import com.techsolutions.worqee.viewmodel.GradesViewModel


class GradesActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val viewModel = GradesViewModel()

        setContent {
            WorqeeTheme {
                GradesScreen(viewModel)
            }
        }
    }
}