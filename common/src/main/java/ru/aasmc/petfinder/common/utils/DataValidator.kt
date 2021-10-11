package ru.aasmc.petfinder.common.utils

import java.io.RandomAccessFile
import java.util.regex.Pattern

class DataValidator {
    companion object {
        private const val EMAIL_REGEX = "^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,4}$"

        fun isValidEmailString(emailString: String): Boolean {
            return emailString.isNotEmpty()
                    && Pattern.compile(EMAIL_REGEX).matcher(emailString).matches()
        }

        /**
         * Checks if a file pointed to by the [pathString] is a valid JPEG format file.
         * For a JPEG format the first two bytes and the last two bytes of a valid image
         * are always FF D8 and FF D9. This method checks for that.
         */
        fun isValidJPEGAtPath(pathString: String?): Boolean {
            var randomAccessFile: RandomAccessFile? = null
            try {
                randomAccessFile = RandomAccessFile(pathString, "r")
                val length = randomAccessFile.length()
                if (length < 10L) {
                    return false
                }
                val start = ByteArray(2)
                randomAccessFile.readFully(start)
                randomAccessFile.seek(length - 2)
                val end = ByteArray(2)
                randomAccessFile.readFully(end)
                return start[0].toInt() == -1 && start[1].toInt() == -40 &&
                        end[0].toInt() == -1 && end[1].toInt() == -39

            } finally {
                randomAccessFile?.close()
            }
        }
    }
}





















