package com.techsolutions.worqee.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.techsolutions.worqee.MainActivity
import com.techsolutions.worqee.views.screens.FriendsScreen
import com.techsolutions.worqee.views.theme.WorqeeTheme

class FriendsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            WorqeeTheme {
                FriendsScreen(
                    onNavigateToGrades = {
                        (activity as? MainActivity)?.mostrarGrades()
                    },
                    onNavigateToSchedule = {
                        (activity as? MainActivity)?.mostrarSchedule()
                    }
                )
            }
        }
    }
}