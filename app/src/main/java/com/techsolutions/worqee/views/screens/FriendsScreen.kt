package com.techsolutions.worqee.views.screens

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
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.techsolutions.worqee.MainActivity
import com.techsolutions.worqee.views.components.BottomNavBar
import com.techsolutions.worqee.views.components.NavBarItem
import com.techsolutions.worqee.views.fragments.GradesFragment
import com.techsolutions.worqee.viewModel.FriendsViewModel
import com.techsolutions.worqee.views.states.AddFriendSearchStatus
import com.techsolutions.worqee.views.states.FoundUserUiModel
import com.techsolutions.worqee.views.states.FriendStatus
import com.techsolutions.worqee.views.states.FriendUiModel
import com.techsolutions.worqee.views.states.SendRequestStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    onNavigateToGrades: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {},
    viewModel: FriendsViewModel = viewModel()

) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val context = LocalContext.current

    // permiso GPS
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
                        NavBarItem.GRADES   -> onNavigateToGrades()
                        NavBarItem.SCHEDULE -> onNavigateToSchedule()
                        NavBarItem.FRIENDS  -> Unit
                    }
                }
            )
            if (uiState.showAddFriendDialog) {
                AddFriendDialog(
                    username          = uiState.addFriendUsername,
                    searchStatus      = uiState.addFriendSearchStatus,
                    foundUser         = uiState.foundUser,
                    sendStatus        = uiState.sendRequestStatus,
                    onUsernameChange  = viewModel::onAddFriendUsernameChanged,
                    onSearch          = viewModel::onSearchFriendByUsername,
                    onSendRequest     = viewModel::onSendFriendRequest,
                    onDismiss         = viewModel::onDismissAddFriendDialog
                )
            }
        }

    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                OutlinedButton(
                    onClick = { viewModel.onOpenAddFriendDialog() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                        Icon(
                            imageVector = Icons.Outlined.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add friend")
                    }
            }

            // Lista de amigos — solo botón Message, sin ícono de ubicación. Futuros sprints, implementar esto.
            items(allFriends) { friend ->
                FriendCard(
                    friend = friend,
                    onMessage = { viewModel.onMessageFriend(friend.id) }
                )
            }

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
                    Text("Go to the nearest building")
                }
            }

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
            //Mismo comentario que arriba, sprint futuros.
            if (onMessage != null) {
                TextButton(onClick = onMessage) {
                    Text("Message", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
@Composable
fun AddFriendDialog(
    username: String,
    searchStatus: AddFriendSearchStatus,
    foundUser: FoundUserUiModel?,
    sendStatus: SendRequestStatus,
    onUsernameChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSendRequest: () -> Unit,
    onDismiss: () -> Unit
) {
    val isBusy = searchStatus == AddFriendSearchStatus.LOADING
            || sendStatus == SendRequestStatus.LOADING

    AlertDialog(
        onDismissRequest = { if (!isBusy) onDismiss() },
        title = { Text("Add friend") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // — Campo + botón buscar —
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = onUsernameChange,
                        label = { Text("Username") },
                        singleLine = true,
                        enabled = !isBusy && sendStatus != SendRequestStatus.SUCCESS,
                        isError = searchStatus == AddFriendSearchStatus.NOT_FOUND
                                || searchStatus == AddFriendSearchStatus.ERROR,
                        modifier = Modifier.weight(1f)
                    )
                    FilledTonalButton(
                        onClick = onSearch,
                        enabled = username.isNotBlank() && !isBusy
                                && sendStatus != SendRequestStatus.SUCCESS
                    ) {
                        Text("Search")
                    }
                }

                // — Feedback de búsqueda —
                when (searchStatus) {
                    AddFriendSearchStatus.LOADING ->
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

                    AddFriendSearchStatus.NOT_FOUND ->
                        Text(
                            "User not found.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )

                    AddFriendSearchStatus.ERROR ->
                        Text(
                            "Search failed. Try again.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )

                    AddFriendSearchStatus.SUCCESS -> {
                        // — Preview del usuario encontrado —
                        foundUser?.let { user ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = user.username.first().uppercaseChar().toString(),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    Text(
                                        text = user.username,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }

                        // — Feedback del envío —
                        when (sendStatus) {
                            SendRequestStatus.LOADING ->
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

                            SendRequestStatus.SUCCESS ->
                                Text(
                                    "Friend request sent!",
                                    color = Color(0xFF4CAF50),
                                    style = MaterialTheme.typography.bodySmall
                                )

                            SendRequestStatus.ERROR ->
                                Text(
                                    "Could not send request. Try again.",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )

                            SendRequestStatus.IDLE -> {}
                        }
                    }

                    AddFriendSearchStatus.IDLE -> {}
                }
            }
        },
        confirmButton = {
            // Solo aparece cuando hay un usuario encontrado y aún no se envió
            if (foundUser != null && sendStatus != SendRequestStatus.SUCCESS) {
                Button(
                    onClick = onSendRequest,
                    enabled = sendStatus != SendRequestStatus.LOADING
                ) {
                    Text("Send request")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isBusy) {
                Text(if (sendStatus == SendRequestStatus.SUCCESS) "Close" else "Cancel")
            }
        }
    )
}