package com.techsolutions.worqee

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.techsolutions.worqee.models.clases.Usuario
import com.techsolutions.worqee.models.repository.UsuarioRepository
import com.techsolutions.worqee.models.storage.LocalStorageManager
import com.techsolutions.worqee.views.fragments.FriendsFragment
import com.techsolutions.worqee.views.fragments.GradesFragment
import com.techsolutions.worqee.views.fragments.LoginFragment
import com.techsolutions.worqee.views.fragments.ScheduleFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Bloqueo de rotación también por código
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_main)

        LocalStorageManager.init(applicationContext)

        val prefs = getSharedPreferences("worqee_prefs", Context.MODE_PRIVATE)
        val userId = prefs.getString("userId", null)

        CoroutineScope(Dispatchers.Main).launch {
            val logueado = if (userId != null) {
                val usuarioCache = UsuarioRepository.cargarDelCaché()
                if (usuarioCache != null) {
                    Usuario.setInstance(usuarioCache)
                    Log.d("MainActivity", "Usuario cargado desde caché")
                    true
                } else {
                    Log.d("MainActivity", "Cargando usuario del servidor")
                    UsuarioRepository.cargarSingletonUsuario(userId)
                }
            } else {
                false
            }

            if (savedInstanceState == null) {
                if (logueado) mostrarSchedule() else mostrarLogin()
            }
        }
    }

    fun mostrarLogin() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, LoginFragment())
            .commit()
    }
    fun mostrarFriends() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, FriendsFragment())
            .addToBackStack(null)
            .commit()
    }

    fun mostrarSchedule() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ScheduleFragment())
            .commit()
    }
    fun mostrarGrades() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, GradesFragment())
            .addToBackStack(null)
            .commit()
    }
}