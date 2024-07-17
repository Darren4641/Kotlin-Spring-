package com.example.kopring.exception.dto

import java.io.Serializable

data class FieldErrorDetail (
    var field : String,
    var message : String
)


data class ExceptionMsg (
    val serialVersionUID : Long = 1L,
    val message : String,
    val code : String,
    val success : Boolean,
    val errors : List<FieldErrorDetail>

) : Serializable

