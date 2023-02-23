package com.example.iot

import com.google.firebase.Timestamp


data class ModelReservation(
    var morning: Timestamp? = null,
    var lunch: Timestamp? = null,
    var dinner: Timestamp? = null,

    var ismorning: String? = null,
    var islunch: String? = null,
    var isdinner: String? = null
)

