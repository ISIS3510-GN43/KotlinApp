package com.techsolutions.worqee.models.clases

import com.google.firebase.firestore.PropertyName

data class Metric(
    @get:PropertyName("Evento")
    @set:PropertyName("Evento")
    var event: String = "",

    @get:PropertyName("FechaActividad")
    @set:PropertyName("FechaActividad")
    var activityDate: String = "",

    @get:PropertyName("IdUsuario")
    @set:PropertyName("IdUsuario")
    var userId: String = "",

    @get:PropertyName("Plataforma")
    @set:PropertyName("Plataforma")
    var platform: String = ""
)
