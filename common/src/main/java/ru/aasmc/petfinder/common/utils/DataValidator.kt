package ru.aasmc.petfinder.common.utils

import java.util.regex.Pattern

class DataValidator {
    companion object {
        private const val EMAIL_REGEX = "^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,4}$"

        fun isValidEmailString(emailString: String): Boolean {
            return emailString.isNotEmpty()
                    && Pattern.compile(EMAIL_REGEX).matcher(emailString).matches()
        }


    }
}