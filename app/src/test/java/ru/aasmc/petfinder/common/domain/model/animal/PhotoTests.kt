package ru.aasmc.petfinder.common.domain.model.animal

import org.junit.Assert.assertEquals
import org.junit.Test

class PhotoTests {

    private val mediumPhoto = "mediumPhoto"
    private val fullPhoto = "fullPhoto"
    private val invalidPhoto = ""

    @Test
    fun photo_getSmallestAvailablePhoto_hasMediumPhoto() {
        // given
        val photo = Media.Photo(mediumPhoto, fullPhoto)
        val expectedValue = mediumPhoto

        // when
        val smallestPhoto = photo.getSmallestAvailablePhoto()

        // then
        assertEquals(smallestPhoto, expectedValue)
    }

    @Test
    fun photo_getSmallestAvailablePhoto_noMediumPhoto() {
        // given
        val photo = Media.Photo(invalidPhoto, fullPhoto)
        val expectedValue = fullPhoto

        // when
        val smallestPhoto = photo.getSmallestAvailablePhoto()

        // then
        assertEquals(smallestPhoto, expectedValue)
    }

    @Test
    fun photo_getSmallestAvailablePhoto_noPhotos() {
        // given
        val photo = Media.Photo(invalidPhoto, invalidPhoto)
        val expectedValue = Media.Photo.EMPTY_PHOTO

        // when
        val smallestPhoto = photo.getSmallestAvailablePhoto()

        // then
        assertEquals(smallestPhoto, expectedValue)
    }
}