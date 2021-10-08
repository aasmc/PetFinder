package ru.aasmc.petfinder.common.utils

import java.io.File
import java.io.RandomAccessFile
import java.security.SecureRandom


/**
 * Deletes reference to the [file] and overwrites its contents with some random bytes.
 * This may not work for SSDs because they don't write to the same area of memory each time.
 * A better solution is to encrypt the data.
 */
private fun wipeData(file: File) {
    if (file.exists()) {
        val length = file.length()
        val random = SecureRandom()
        val randomAccessFile = RandomAccessFile(file, "rws")
        randomAccessFile.seek(0)
        randomAccessFile.filePointer
        val data = ByteArray(64)
        var position = 0
        while (position < length) {
            random.nextBytes(data)
            randomAccessFile.write(data)
            position += data.size
        }
        randomAccessFile.close()
        file.delete()
    }
}