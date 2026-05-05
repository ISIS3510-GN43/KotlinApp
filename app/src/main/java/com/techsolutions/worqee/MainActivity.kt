package com.techsolutions.worqee

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.techsolutions.worqee.models.storage.LocalStorageManager
import com.techsolutions.worqee.models.storage.PendingSyncManager
import com.techsolutions.worqee.viewModel.MainUiState
import com.techsolutions.worqee.viewModel.MainViewModel
import com.techsolutions.worqee.views.fragments.FriendsFragment
import com.techsolutions.worqee.views.fragments.GradesFragment
import com.techsolutions.worqee.views.fragments.LoginFragment
import com.techsolutions.worqee.views.fragments.ScheduleFragment
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_main)

        LocalStorageManager.init(applicationContext)
        PendingSyncManager.init(applicationContext)

        if (savedInstanceState == null) {
            lifecycleScope.launch {
                viewModel.uiState.collect { state ->
                    when (state) {
                        MainUiState.Loading -> Unit
                        MainUiState.Authenticated -> mostrarSchedule()
                        MainUiState.Unauthenticated -> mostrarLogin()
                    }
                }
            }

            viewModel.restoreSession()
        }
    }

    fun mostrarLogin() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, LoginFragment())
            .commit()
    }

    fun mostrarFriends() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, FriendsFragment())
            .addToBackStack(null)
            .commit()
    }

    fun mostrarSchedule() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ScheduleFragment())
            .commit()
    }

    fun mostrarGrades() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, GradesFragment())
            .addToBackStack(null)
            .commit()
    }
}