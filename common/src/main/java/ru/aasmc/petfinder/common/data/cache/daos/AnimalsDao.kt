package ru.aasmc.petfinder.common.data.cache.daos

import androidx.room.*
import io.reactivex.Flowable
import ru.aasmc.petfinder.common.data.cache.model.cachedanimal.*

@Dao
abstract class AnimalsDao {

    @Transaction
    @Query("SELECT * FROM animals")
    abstract fun getAllAnimals(): Flowable<List<CachedAnimalAggregate>>

    @Transaction
    @Query("SELECT * FROM animals WHERE animalId IS :animalId")
    abstract suspend fun getAnimal(animalId: Long): CachedAnimalAggregate

    /**
     * We can't insert a CachedAnimalAggregate because it is not a Room Entity,
     * but we can decompose it into its @Entity-annotated components and pass them
     * into this method. Since they are all Room Entities, Room will know how to insert them.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAnimalAggregate(
        animal: CachedAnimalWithDetails,
        photos: List<CachedPhoto>,
        videos: List<CachedVideo>,
        tags: List<CachedTag>
    )

    /**
     * This method triggers updates in Flowables from getAllAnimals method. And in the worst case
     * scenario it will be the reason for backpressure, when the Flowable will emit objects faster,
     * than Room will be able to consume them.
     */
    suspend fun insertAnimalsWithDetails(animalAggregates: List<CachedAnimalAggregate>) {
        for (animalAggregate in animalAggregates) {
            insertAnimalAggregate(
                animal = animalAggregate.animal,
                photos = animalAggregate.photos,
                videos = animalAggregate.videos,
                tags = animalAggregate.tags
            )
        }
    }

    @Query("SELECT DISTINCT type FROM animals")
    abstract suspend fun getAllTypes(): List<String>

    /**
     * In this method we use '%' || notation for SQLLite request to
     * search for name, age and type even if they are prefixed or suffixed with other characters.
     */
    @Transaction
    @Query(
        """
            SELECT * FROM animals
            WHERE name LIKE '%' || :name || '%' AND 
            age LIKE '%' || :age || '%' AND
            type LIKE '%' || :type || '%'
        """
    )
    abstract fun searchAnimalsBy(
        name: String,
        age: String,
        type: String
    ): Flowable<List<CachedAnimalAggregate>>
}











