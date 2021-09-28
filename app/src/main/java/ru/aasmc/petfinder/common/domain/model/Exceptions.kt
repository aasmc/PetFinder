package ru.aasmc.petfinder.common.domain.model

import okio.IOException

class NoMoreAnimalsException(message: String) : Exception(message)

class NetworkUnavailableException(message: String = "No network connection :(") :
    IOException(message)

class NetworkException(message: String) : Exception(message)