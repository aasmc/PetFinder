package ru.aasmc.petfinder.common.data.api.model.mappers

import ru.aasmc.petfinder.common.data.api.model.ApiColors
import ru.aasmc.petfinder.common.domain.model.animal.details.Colors
import javax.inject.Inject

class ApiColorsMapper @Inject constructor(): ApiMapper<ApiColors?, Colors> {

  override fun mapToDomain(apiEntity: ApiColors?): Colors {
    return Colors(
        primary = apiEntity?.primary.orEmpty(),
        secondary = apiEntity?.secondary.orEmpty(),
        tertiary = apiEntity?.tertiary.orEmpty()
    )
  }
}
