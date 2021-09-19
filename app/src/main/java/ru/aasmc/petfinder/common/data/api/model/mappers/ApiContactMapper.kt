package ru.aasmc.petfinder.common.data.api.model.mappers

import ru.aasmc.petfinder.common.data.api.model.ApiContact
import ru.aasmc.petfinder.common.data.api.model.mappers.ApiAddressMapper
import ru.aasmc.petfinder.common.domain.model.organization.Organization
import javax.inject.Inject

class ApiContactMapper @Inject constructor(
    private val apiAddressMapper: ApiAddressMapper
): ApiMapper<ApiContact?, Organization.Contact> {

  override fun mapToDomain(apiEntity: ApiContact?): Organization.Contact {
    return Organization.Contact(
        email = apiEntity?.email.orEmpty(),
        phone = apiEntity?.phone.orEmpty(),
        address = apiAddressMapper.mapToDomain(apiEntity?.address)
    )
  }
}