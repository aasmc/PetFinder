package ru.aasmc.petfinder.common.data.api.model.mappers

import ru.aasmc.petfinder.common.data.api.model.ApiPagination
import ru.aasmc.petfinder.common.data.api.model.mappers.ApiMapper
import ru.aasmc.petfinder.common.domain.model.pagination.Pagination
import javax.inject.Inject

class ApiPaginationMapper @Inject constructor(): ApiMapper<ApiPagination?, Pagination> {

  override fun mapToDomain(apiEntity: ApiPagination?): Pagination {
    return Pagination(
        currentPage = apiEntity?.currentPage ?: 0,
        totalPages = apiEntity?.totalPages ?: 0
    )
  }
}