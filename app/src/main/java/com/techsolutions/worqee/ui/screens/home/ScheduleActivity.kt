package com.techsolutions.worqee.ui.screens.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.techsolutions.worqee.ui.theme.WorqeeTheme

class ScheduleActivity : ComponentActivity() {

    companion object {
        const val EXTRA_USER_ID = "extra_user_id"
        const val EXTRA_USER_NAME = "extra_user_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userId = intent.getStringExtra(EXTRA_USER_ID).orEmpty()
        val userName = intent.getStringExtra(EXTRA_USER_NAME).orEmpty()

        setContent {
            WorqeeTheme {

                val viewModel: ScheduleViewModel = ScheduleViewModel()
                ScheduleScreen(viewModel = viewModel)
            }
        }
    }
}