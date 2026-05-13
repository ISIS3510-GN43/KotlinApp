package com.techsolutions.worqee.utils

import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeParseException

object RegisterValidator {

    const val MINIMUM_AGE = 13

    fun validate(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        birthday: String
    ): RegisterValidationErrors {
        val usernameError = validateUsername(username)
        val emailError = validateEmail(email)
        val passwordError = validatePassword(password)
        val confirmPasswordError = validateConfirmPassword(password, confirmPassword)
        val birthdayError = validateBirthday(birthday)

        return RegisterValidationErrors(
            usernameError = usernameError,
            emailError = emailError,
            passwordError = passwordError,
            confirmPasswordError = confirmPasswordError,
            birthdayError = birthdayError
        )
    }

    private fun validateUsername(username: String): String? {
        val trimmedUsername = username.trim()

        return when {
            trimmedUsername.isBlank() ->
                "Ingresa un nombre de usuario"

            trimmedUsername.length < 3 ->
                "El nombre de usuario debe tener al menos 3 caracteres"

            trimmedUsername.length > 20 ->
                "El nombre de usuario no puede tener más de 20 caracteres"

            !trimmedUsername.matches(Regex("^[a-zA-Z0-9._-]+$")) ->
                "El nombre de usuario solo puede contener letras, números, puntos, guiones y guiones bajos"

            else -> null
        }
    }

    private fun validateEmail(email: String): String? {
        val trimmedEmail = email.trim()

        return when {
            trimmedEmail.isBlank() ->
                "Ingresa tu correo electrónico"

            !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() ->
                "Ingresa un correo electrónico válido"

            else -> null
        }
    }

    private fun validatePassword(password: String): String? {
        val hasSpecialCharacter = password.any { !it.isLetterOrDigit() }
        val hasDigit = password.any { it.isDigit() }
        val hasLetter = password.any { it.isLetter() }

        return when {
            password.isBlank() ->
                "Ingresa una contraseña"

            password.length < 8 ->
                "La contraseña debe tener al menos 8 caracteres"

            !hasLetter ->
                "La contraseña debe incluir al menos una letra"

            !hasDigit ->
                "La contraseña debe incluir al menos un número"

            !hasSpecialCharacter ->
                "La contraseña debe incluir al menos un carácter especial"

            else -> null
        }
    }

    private fun validateConfirmPassword(
        password: String,
        confirmPassword: String
    ): String? {
        return when {
            confirmPassword.isBlank() ->
                "Confirma tu contraseña"

            password != confirmPassword ->
                "Las contraseñas no coinciden"

            else -> null
        }
    }

    private fun validateBirthday(birthday: String): String? {
        if (birthday.isBlank()) {
            return "Selecciona tu fecha de nacimiento"
        }

        val birthdayDate = try {
            LocalDate.parse(birthday)
        } catch (e: DateTimeParseException) {
            return "La fecha de nacimiento no es válida"
        }

        val today = LocalDate.now()

        if (birthdayDate.isAfter(today)) {
            return "La fecha de nacimiento no puede estar en el futuro"
        }

        val age = Period.between(birthdayDate, today).years

        if (age < MINIMUM_AGE) {
            return "Debes tener al menos $MINIMUM_AGE años para registrarte"
        }

        return null
    }
}

data class RegisterValidationErrors(
    val usernameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val birthdayError: String? = null
) {
    val hasErrors: Boolean
        get() = usernameError != null ||
                emailError != null ||
                passwordError != null ||
                confirmPasswordError != null ||
                birthdayError != null
}