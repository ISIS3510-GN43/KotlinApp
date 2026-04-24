package com.techsolutions.worqee.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.techsolutions.worqee.MainActivity
import com.techsolutions.worqee.ui.screens.login.LoginScreen
import com.techsolutions.worqee.ui.theme.WorqeeTheme

class LoginFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            WorqeeTheme {
                LoginScreen(
                    onLoginSuccess = {
                        (activity as? MainActivity)?.mostrarSchedule()
                    }
                )
            }
        }
    }
}