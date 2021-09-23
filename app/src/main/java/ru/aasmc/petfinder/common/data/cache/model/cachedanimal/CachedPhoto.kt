package ru.aasmc.petfinder.common.data.cache.model.cachedanimal

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ru.aasmc.petfinder.common.domain.model.animal.Media

@Entity(
    tableName = "photos",
    foreignKeys = [
        ForeignKey(
            entity = CachedAnimalWithDetails::class,
            parentColumns = ["animalId"],
            childColumns = ["animalId"],
            onDelete = ForeignKey.CASCADE // delete this entity if the parent gets deleted
        )
    ],
    indices = [Index("animalId")] // for performance reasons need to add
    // foreign key (animalId) as an index. Having indices speeds up SELECT queries, and slows down
    // INSERT and UPDATE queries. Here we are going to mostly read from the db
)
data class CachedPhoto(
    @PrimaryKey(autoGenerate = true)
    val photoId: Long = 0,
    val animalId: Long,
    val medium: String,
    val full: String
) {
    companion object {
        fun fromDomain(animalId: Long, photo: Media.Photo): CachedPhoto {
            val (medium, full) = photo

            return CachedPhoto(animalId, animalId, medium, full)
        }
    }

    fun toDomain(): Media.Photo = Media.Photo(medium, full)
}