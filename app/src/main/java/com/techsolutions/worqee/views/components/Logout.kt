package com.techsolutions.worqee.views.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme

@Composable
fun Logout(
    onLogout: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        Icon(
            imageVector = Icons.Outlined.MoreVert,
            contentDescription = "Más opciones",
            modifier = Modifier
                .padding(end = 16.dp)
                .clickable { showMenu = true },
            tint = MaterialTheme.colorScheme.onBackground
        )

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Cerrar sesión") },
                onClick = {
                    showMenu = false
                    onLogout()
                }
            )
        }
    }
}