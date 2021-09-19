package ru.aasmc.petfinder.common.data.api.model.mappers

import ru.aasmc.petfinder.common.data.api.model.ApiVideoLink
import ru.aasmc.petfinder.common.data.api.model.mappers.ApiMapper
import ru.aasmc.petfinder.common.domain.model.animal.Media
import javax.inject.Inject

class ApiVideoMapper @Inject constructor(): ApiMapper<ApiVideoLink?, Media.Video> {

  override fun mapToDomain(apiEntity: ApiVideoLink?): Media.Video {
    return Media.Video(video = apiEntity?.embed.orEmpty())
  }
}
