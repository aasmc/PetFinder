package ru.aasmc.petfinder.common.data.cache.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Single
import ru.aasmc.petfinder.common.data.cache.model.cachedorganization.CachedOrganization

@Dao
interface OrganizationsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(organizations: List<CachedOrganization>)

    @Query("SELECT * FROM organizations WHERE organizationId IS :organizationId")
    fun getOrganization(organizationId: String): Single<CachedOrganization>
}