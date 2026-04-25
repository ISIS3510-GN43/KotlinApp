package com.techsolutions.worqee.viewModel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import com.techsolutions.worqee.models.clases.Dia
import com.techsolutions.worqee.models.clases.Usuario
import com.techsolutions.worqee.models.repository.UsuarioRepository
import kotlinx.coroutines.launch
import kotlin.math.sqrt
import android.util.Log
import com.techsolutions.worqee.views.states.FriendStatus
import com.techsolutions.worqee.views.states.FriendUiModel
import com.techsolutions.worqee.views.states.FriendsUiState
import com.techsolutions.worqee.views.states.SendRequestStatus
import com.techsolutions.worqee.views.states.AddFriendSearchStatus
import com.techsolutions.worqee.views.states.FoundUserUiModel

data class EdificioUniversidad(
    val nombre: String,
    val lat: Double,
    val lng: Double
)

class FriendsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    private val edificios = listOf(
        EdificioUniversidad("Edificio ML (Matemáticas)", 4.60178, -74.06582),
        EdificioUniversidad("Edificio W (Ingeniería)", 4.60215, -74.06618),
        EdificioUniversidad("Edificio SD (Santo Domingo)", 4.60143, -74.06601),
        EdificioUniversidad("Biblioteca General", 4.60165, -74.06555),
        EdificioUniversidad("Edificio RGA", 4.60190, -74.06540),
        EdificioUniversidad("Centro Deportivo Uniandes", 4.60250, -74.06490),
        EdificioUniversidad("Edificio AU (Artes)", 4.60120, -74.06570),
        EdificioUniversidad("Edificio C (Ciencias)", 4.60200, -74.06650)
    )

    init {
        loadFriends()
    }

    private fun loadFriends() {
        viewModelScope.launch {
            val usuario = try {
                Usuario.getInstance()
            } catch (e: IllegalStateException) {
                return@launch
            }
            val result = UsuarioRepository.getAmigos(usuario.id)
            if (result.isFailure) return@launch

            val amigos = result.getOrDefault(emptyList())

            val ahora = java.util.Calendar.getInstance()
            val diaActual = when (ahora.get(java.util.Calendar.DAY_OF_WEEK)) {
                java.util.Calendar.MONDAY    -> Dia.LUNES
                java.util.Calendar.TUESDAY   -> Dia.MARTES
                java.util.Calendar.WEDNESDAY -> Dia.MIERCOLES
                java.util.Calendar.THURSDAY  -> Dia.JUEVES
                java.util.Calendar.FRIDAY    -> Dia.VIERNES
                else -> null
            }

            val horaActual = ahora.get(java.util.Calendar.HOUR_OF_DAY) * 100 +
                    ahora.get(java.util.Calendar.MINUTE)

            val allFriends = amigos.mapIndexed { index, amigo ->
                val horarioActivo = amigo.horarios.firstOrNull { it.activo }
                    ?: amigo.horarios.firstOrNull()

                val status = if (horarioActivo == null) {
                    // Business Rule: Siempre esta libre si no tiene horario
                    FriendStatus.AVAILABLE
                } else if (diaActual == null) {
                    // Business Rule: Fin de semana
                    FriendStatus.AVAILABLE
                } else {
                    val estaOcupado = horarioActivo.materias.any { materia ->
                        materia.dias.forEachIndexed { i, dia ->
                            if (dia == diaActual) {
                                val inicio = materia.horaInicio.getOrElse(i) { 0 }
                                val fin    = materia.horaFin.getOrElse(i) { 0 }
                                if (horaActual in inicio until fin) return@any true
                            }
                        }
                        false
                    }
                    if (estaOcupado) FriendStatus.BUSY else FriendStatus.AVAILABLE
                }

                val freeAtLabel = if (status == FriendStatus.BUSY) {
                    val proximaLibre = horarioActivo?.materias
                        ?.flatMap { materia ->
                            materia.dias.mapIndexed { i, dia ->
                                Pair(dia, materia.horaFin.getOrElse(i) { 0 })
                            }
                        }
                        ?.filter { (dia, fin) -> dia == diaActual && fin > horaActual }
                        ?.minByOrNull { it.second }
                        ?.second

                    if (proximaLibre != null) "Free at ${formatHora(proximaLibre)}" else "Busy"
                } else null


                FriendUiModel(
                    id = amigo.id,
                    name = amigo.username,
                    avatarUrl = amigo.foto,
                    status = status,
                    freeAtLabel = freeAtLabel,
                    lat = 4.6097 + (index * 0.01),
                    lng = -74.0817 + (index * 0.01)
                )
            }

            updateState(allFriends, _uiState.value.searchQuery)
        }
    }

    fun onSearchQueryChanged(query: String) {
        val allFriends = getAllFriends()
        updateState(allFriends, query)
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

    fun construirUrlEdificioMasCercano(miLat: Double, miLng: Double): Pair<String, String> {
        val edificioCercano = edificios.minByOrNull { edificio ->
            val dLat = edificio.lat - miLat
            val dLng = edificio.lng - miLng
            sqrt(dLat * dLat + dLng * dLng)
        } ?: edificios.first()

        val url = "https://www.google.com/maps/dir/?api=1" +
                "&origin=$miLat,$miLng" +
                "&destination=${edificioCercano.lat},${edificioCercano.lng}" +
                "&travelmode=walking"

        return Pair(url, edificioCercano.nombre)
    }

    fun onFindCommonFreeTime() {
        viewModelScope.launch {
            val usuario = try {
                Usuario.getInstance()
            } catch (e: IllegalStateException) {
                return@launch
            }

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
            Log.i("Bloques", "$bloquesOcupados")

            val franjas = (800..2000 step 100).map { hora ->
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
    fun onOpenAddFriendDialog() {
        _uiState.value = _uiState.value.copy(
            showAddFriendDialog = true,
            addFriendUsername = "",
            addFriendSearchStatus = AddFriendSearchStatus.IDLE,
            foundUser = null,
            sendRequestStatus = SendRequestStatus.IDLE
        )
    }

    fun onDismissAddFriendDialog() {
        _uiState.value = _uiState.value.copy(
            showAddFriendDialog = false,
            addFriendUsername = "",
            addFriendSearchStatus = AddFriendSearchStatus.IDLE,
            foundUser = null,
            sendRequestStatus = SendRequestStatus.IDLE
        )
    }

    fun onAddFriendUsernameChanged(username: String) {
        _uiState.value = _uiState.value.copy(
            addFriendUsername = username,
            addFriendSearchStatus = AddFriendSearchStatus.IDLE,
            foundUser = null,
            sendRequestStatus = SendRequestStatus.IDLE
        )
    }

    fun onSearchFriendByUsername() {
        val username = _uiState.value.addFriendUsername.trim()
        if (username.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                addFriendSearchStatus = AddFriendSearchStatus.LOADING
            )
            try {
                val result = UsuarioRepository.getUsuarioPorId(username)
                if (result.isSuccess) {
                    val usuario = result.getOrNull()
                    if (usuario != null) {
                        _uiState.value = _uiState.value.copy(
                            addFriendSearchStatus = AddFriendSearchStatus.SUCCESS,
                            foundUser = FoundUserUiModel(
                                uid = usuario.id,
                                username = usuario.username,
                                foto = usuario.foto
                            )
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            addFriendSearchStatus = AddFriendSearchStatus.NOT_FOUND
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        addFriendSearchStatus = AddFriendSearchStatus.NOT_FOUND
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    addFriendSearchStatus = AddFriendSearchStatus.ERROR
                )
            }
        }
    }

    fun onSendFriendRequest() {
        val foundUser = _uiState.value.foundUser ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                sendRequestStatus = SendRequestStatus.LOADING
            )
            try {
                val usuarioActual = try {
                    Usuario.getInstance()
                } catch (e: IllegalStateException) {
                    _uiState.value = _uiState.value.copy(
                        sendRequestStatus = SendRequestStatus.ERROR
                    )
                    return@launch
                }

                val result = UsuarioRepository.enviarSolicitudAmistad(
                    fromId = usuarioActual.id,
                    toId = foundUser.uid
                )
                _uiState.value = _uiState.value.copy(
                    sendRequestStatus = if (result.isSuccess)
                        SendRequestStatus.SUCCESS
                    else
                        SendRequestStatus.ERROR
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    sendRequestStatus = SendRequestStatus.ERROR
                )
            }
        }
    }
}