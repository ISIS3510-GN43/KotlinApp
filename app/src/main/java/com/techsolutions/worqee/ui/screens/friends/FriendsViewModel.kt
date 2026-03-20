package com.techsolutions.worqee.ui.screens.friends

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import androidx.lifecycle.viewModelScope
import com.techsolutions.worqee.models.Dia
import com.techsolutions.worqee.models.Usuario
import com.techsolutions.worqee.repository.UsuarioRepository
import kotlinx.coroutines.launch

class FriendsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    init {
        loadFriends()
    }

    private fun loadFriends() {
        viewModelScope.launch {
            val usuario = Usuario.getInstance()
            val result = UsuarioRepository.getAmigos(usuario.id)

            if (result.isFailure) return@launch

            val amigos = result.getOrDefault(emptyList())

            // ← CAMBIO: mapIndexed para asignar ubicaciones de prueba cerca de Bogotá
            // TODO: reemplazar lat/lng con ubicaciones reales desde Firebase
            val allFriends = amigos.mapIndexed { index, amigo ->
                FriendUiModel(
                    id = amigo.id,
                    name = amigo.username,
                    avatarUrl = amigo.foto,
                    status = FriendStatus.AVAILABLE,
                    lat = 4.6097 + (index * 0.01),
                    lng = -74.0817 + (index * 0.01)
                )
            }
            // DATOS DE PRUEBA - borrar cuando Firebase funcione
            val amigosFalsos = listOf(
                FriendUiModel(
                    id = "1",
                    name = "Samuel",
                    status = FriendStatus.AVAILABLE,
                    lat = 4.6200,
                    lng = -74.0700
                ),
                FriendUiModel(
                    id = "2",
                    name = "Ana",
                    status = FriendStatus.BUSY,
                    lat = 4.6300,
                    lng = -74.0900
                )
            )
            updateState(amigosFalsos, _uiState.value.searchQuery)
            return@launch
// FIN DATOS DE PRUEBA
            updateState(allFriends, _uiState.value.searchQuery)
        }
    }

    fun onSearchQueryChanged(query: String) {
        val allFriends = getAllFriends()
        updateState(allFriends, query)
    }

    fun onFindCommonTime() {
        // TODO: lógica para encontrar tiempo libre en común
    }

    fun onMessageFriend(friendId: String) {
        // TODO: abrir chat con el amigo
    }

    private fun getAllFriends(): List<FriendUiModel> {
        val current = _uiState.value
        return current.availableFriends + current.busyFriends + current.offlineFriends
    }

    private fun updateState(friends: List<FriendUiModel>, query: String) {
        val filtered = if (query.isBlank()) friends
        else friends.filter { it.name.contains(query, ignoreCase = true) }

        _uiState.value = FriendsUiState(
            searchQuery = query,
            availableFriends = filtered.filter { it.status == FriendStatus.AVAILABLE },
            busyFriends = filtered.filter { it.status == FriendStatus.BUSY },
            offlineFriends = filtered.filter { it.status == FriendStatus.OFFLINE }
        )
    }

    fun onShareLocation(friendId: String) {
        // TODO: obtener GPS y enviar al amigo
    }


    fun construirUrlMapa(miLat: Double, miLng: Double): String {
        val amigos = getAllFriends()
        val waypoints = amigos.joinToString("|") { "${it.lat},${it.lng}" }
        return "https://www.google.com/maps/dir/?api=1" +
                "&origin=$miLat,$miLng" +
                "&waypoints=$waypoints" +
                "&travelmode=walking"
    }

    fun onFindCommonFreeTime() {
        viewModelScope.launch {
            val usuario = Usuario.getInstance()
            val result = UsuarioRepository.getAmigos(usuario.id)

            if (result.isFailure) return@launch

            val amigos = result.getOrDefault(emptyList())

            data class Bloque(val dia: Dia, val inicio: Int, val fin: Int)

            val bloquesOcupados = amigos.flatMap { amigo ->
                val horarioActivo = amigo.horarios.firstOrNull { it.activo }
                    ?: amigo.horarios.firstOrNull()
                horarioActivo?.materias?.flatMap { materia ->
                    materia.dias.mapIndexed { i, dia ->
                        Bloque(
                            dia = dia,
                            inicio = materia.horaInicio.getOrElse(i) { 0 },
                            fin = materia.horaFin.getOrElse(i) { 0 }
                        )
                    }
                } ?: emptyList()
            }

            val franjas = (600..2100 step 100).map { hora ->
                val diasSemana = listOf(Dia.LUNES, Dia.MARTES, Dia.MIERCOLES, Dia.JUEVES, Dia.VIERNES)
                diasSemana.map { dia ->
                    val ocupados = bloquesOcupados.count { bloque ->
                        bloque.dia == dia && bloque.inicio <= hora && bloque.fin > hora
                    }
                    Triple(dia, hora, amigos.size - ocupados)
                }
            }.flatten()

            val mejorHueco = franjas.maxByOrNull { it.third }

            if (mejorHueco != null) {
                _uiState.value = _uiState.value.copy(
                    commonFreeTimeResult = "Best time: ${mejorHueco.first} at ${formatHora(mejorHueco.second)} — ${mejorHueco.third}/${amigos.size} friends free"
                )
            }
        }
    }

    private fun formatHora(hora: Int): String {
        val h = hora / 100
        val suffix = if (h >= 12) "PM" else "AM"
        val h12 = when {
            h == 0 -> 12
            h > 12 -> h - 12
            else -> h
        }
        return "%02d:00 %s".format(h12, suffix)
    }
}