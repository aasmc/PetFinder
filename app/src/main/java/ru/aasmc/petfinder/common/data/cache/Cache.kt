package ru.aasmc.petfinder.common.data.cache

import ru.aasmc.petfinder.common.data.cache.model.cachedorganization.CachedOrganization

interface Cache {
    fun storeOrganizations(organizations: List<CachedOrganization>)
}