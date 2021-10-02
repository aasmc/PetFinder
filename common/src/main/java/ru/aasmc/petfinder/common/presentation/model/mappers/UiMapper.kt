package ru.aasmc.petfinder.common.presentation.model.mappers

interface UiMapper<E, V> {

    fun mapToView(input: E): V
}