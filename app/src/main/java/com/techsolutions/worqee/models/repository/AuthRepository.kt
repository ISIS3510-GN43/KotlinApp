package com.techsolutions.worqee.models.repository
import com.google.firebase.auth.FirebaseAuth
import com.techsolutions.worqee.models.clases.Usuario
import com.techsolutions.worqee.models.storage.LocalStorageManager
import kotlinx.coroutines.tasks.await

object AuthRepository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    suspend fun login(gmail: String, password: String): Result<Usuario> {
        return try {
            val authResult = firebaseAuth
                .signInWithEmailAndPassword(gmail, password)
                .await()

            val userId = authResult.user?.uid
                ?: return Result.failure(Exception("No se pudo obtener el ID del usuario autenticado"))

            LocalStorageManager.guardarUserId(userId)

            val usuarioResult = UsuarioRepository.cargarUsuarioDesdeServidor(userId)

            if (usuarioResult.isSuccess) {
                val usuario = usuarioResult.getOrThrow()

                Usuario.setInstance(usuario) // temporal mientras eliminamos el Singleton de Usuario
                UsuarioRepository.guardarEnCaché(usuario)

                Result.success(usuario)
            } else {
                Result.failure(
                    usuarioResult.exceptionOrNull()
                        ?: Exception("No se pudo cargar el usuario desde el servidor")
                )
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        gmail: String,
        password: String,
        username: String,
        cumpleanios: String
    ): Result<Usuario> {
        return try {
            val fotoDefault =
                "https://firebasestorage.googleapis.com/v0/b/techsolutions-eb89a.firebasestorage.app/o/Fotos%20de%20perfil%2Fuser_profile_photo.png?alt=media&token=63c45849-4d40-4805-a222-0e130c588bc8"

            val nuevoUsuario = Usuario(
                gmail = gmail,
                password = password,
                username = username,
                cumpleanios = cumpleanios,
                foto = fotoDefault
            )

            val result = UsuarioRepository.register(nuevoUsuario)

            if (result.isSuccess) {
                val usuarioCreado = result.getOrThrow()

                LocalStorageManager.guardarUserId(usuarioCreado.id)
                Usuario.setInstance(usuarioCreado) // temporal
                UsuarioRepository.guardarEnCaché(usuarioCreado)

                Result.success(usuarioCreado)
            } else {
                Result.failure(
                    result.exceptionOrNull()
                        ?: Exception("Error al registrar usuario")
                )
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        firebaseAuth.signOut()

        Usuario.clearInstance() // temporal
        LocalStorageManager.limpiarSesion()
        UsuarioRepository.limpiarCaché()
    }
}