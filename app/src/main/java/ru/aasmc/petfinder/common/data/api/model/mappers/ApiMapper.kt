package ru.aasmc.petfinder.common.data.api.model.mappers

interface ApiMapper<Entity, Domain> {

  fun mapToDomain(apiEntity: Entity): Domain

}