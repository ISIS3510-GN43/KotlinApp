package com.techsolutions.worqee.ui.screens.friends

import android.content.Intent
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.techsolutions.worqee.ui.components.BottomNavBar
import com.techsolutions.worqee.ui.components.NavBarItem
import com.techsolutions.worqee.ui.screens.GradesScreen.GradesActivity
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import android.annotation.SuppressLint
import com.techsolutions.worqee.MainActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    viewModel: FriendsViewModel = viewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val context = LocalContext.current

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) @androidx.annotation.RequiresPermission(anyOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION]) { granted ->
        if (granted) {
            val client = LocationServices.getFusedLocationProviderClient(context)

            client.lastLocation.addOnSuccessListener { location ->
                @SuppressLint("MissingPermission")
                if (location != null) {
                    val url = viewModel.construirUrlMapa(location.latitude, location.longitude)
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                }
            }
        }
    }

    fun abrirMapa() {
        val permiso = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permiso == PackageManager.PERMISSION_GRANTED) {
            val client = LocationServices.getFusedLocationProviderClient(context)

            client.lastLocation.addOnSuccessListener { location ->
                @SuppressLint("MissingPermission")
                val lat = location?.latitude ?: 4.6097
                val lng = location?.longitude ?: -74.0817
                val url = viewModel.construirUrlMapa(lat, lng)
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
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
                        NavBarItem.GRADES -> {
                            val intent = Intent(context, GradesActivity::class.java)
                            context.startActivity(intent)
                        }
                        NavBarItem.SCHEDULE -> {
                            val intent = Intent(context, MainActivity::class.java)
                            context.startActivity(intent)
                        }
                        NavBarItem.FRIENDS -> {

                        }
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

            // Lista de amigos
            items(allFriends) { friend ->
                FriendCard(
                    friend = friend,
                    onMessage = { viewModel.onMessageFriend(friend.id) },
                    onShareLocation = { viewModel.onShareLocation(friend.id) }
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
            item {
                Button(
                    onClick = { abrirMapa() },
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
                    Text("Ver ubicación de amigos")
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
    onMessage: (() -> Unit)? = null,
    onShareLocation: (() -> Unit)? = null
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

            if (onShareLocation != null) {
                IconButton(onClick = onShareLocation) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = "Share location",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (onMessage != null) {
                TextButton(onClick = onMessage) {
                    Text("Message", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}