package com.techsolutions.worqee.ui.screens.friends

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.techsolutions.worqee.MainActivity
import com.techsolutions.worqee.ui.components.BottomNavBar
import com.techsolutions.worqee.ui.components.NavBarItem
import com.techsolutions.worqee.ui.screens.GradesScreen.GradesActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    viewModel: FriendsViewModel = viewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val context = LocalContext.current

    // Launcher de permiso GPS → al concederse, navega al edificio más cercano
    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            @SuppressLint("MissingPermission")
            val client = LocationServices.getFusedLocationProviderClient(context)
            client.lastLocation.addOnSuccessListener { location ->
                val lat = location?.latitude ?: 4.60178
                val lng = location?.longitude ?: -74.06582
                val (url, nombreEdificio) = viewModel.construirUrlEdificioMasCercano(lat, lng)
                Toast.makeText(context, "Navegando a: $nombreEdificio", Toast.LENGTH_SHORT).show()
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }
    }

    fun navegarAlEdificioMasCercano() {
        val permiso = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permiso == PackageManager.PERMISSION_GRANTED) {
            @SuppressLint("MissingPermission")
            val client = LocationServices.getFusedLocationProviderClient(context)
            client.lastLocation.addOnSuccessListener { location ->
                val lat = location?.latitude ?: 4.60178
                val lng = location?.longitude ?: -74.06582
                val (url, nombreEdificio) = viewModel.construirUrlEdificioMasCercano(lat, lng)
                Toast.makeText(context, "Navegando a: $nombreEdificio", Toast.LENGTH_SHORT).show()
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        } else {
            locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val allFriends = uiState.availableFriends + uiState.busyFriends + uiState.offlineFriends

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Friends", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BottomNavBar(
                selectedItem = NavBarItem.FRIENDS,
                onItemSelected = { item ->
                    when (item) {
                        NavBarItem.GRADES -> context.startActivity(
                            Intent(context, GradesActivity::class.java)
                        )
                        NavBarItem.SCHEDULE -> context.startActivity(
                            Intent(context, MainActivity::class.java)
                        )
                        NavBarItem.FRIENDS -> {}
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search bar
            item {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    placeholder = { Text("Search friends...") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )
            }

            // Lista de amigos — solo botón Message, sin ícono de ubicación
            items(allFriends) { friend ->
                FriendCard(
                    friend = friend,
                    onMessage = { viewModel.onMessageFriend(friend.id) }
                )
            }

            // Resultado del hueco en común
            uiState.commonFreeTimeResult?.let { result ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = result,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Botón GPS → edificio universitario más cercano
            item {
                Button(
                    onClick = { navegarAlEdificioMasCercano() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ir al edificio más cercano")
                }
            }

            // Botón Find Best Free Time
            item {
                Button(
                    onClick = { viewModel.onFindCommonFreeTime() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.EventAvailable,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Find Best Free Time")
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun FriendCard(
    friend: FriendUiModel,
    onMessage: (() -> Unit)? = null
) {
    val statusColor = when (friend.status) {
        FriendStatus.AVAILABLE -> Color(0xFF4CAF50)
        FriendStatus.BUSY -> Color(0xFFFF9800)
        FriendStatus.OFFLINE -> Color(0xFF9E9E9E)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar con inicial del nombre
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = friend.name.first().toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(friend.name, style = MaterialTheme.typography.bodyLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = friend.freeAtLabel ?: when (friend.status) {
                            FriendStatus.AVAILABLE -> "Free now"
                            FriendStatus.BUSY -> "Busy"
                            FriendStatus.OFFLINE -> "Offline"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Solo botón Message — sin ícono de ubicación
            if (onMessage != null) {
                TextButton(onClick = onMessage) {
                    Text("Message", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}