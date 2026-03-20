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
import kotlin.math.sqrt

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
            val usuario = Usuario.getInstance()
            val result = UsuarioRepository.getAmigos(usuario.id)

            if (result.isFailure) return@launch

            val amigos = result.getOrDefault(emptyList())

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

    /**
     * Dado la ubicación actual del usuario, encuentra el edificio universitario
     * más cercano y devuelve una URL de navegación hacia él en Google Maps.
     */
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