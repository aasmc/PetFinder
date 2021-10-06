package ru.aasmc.petfinder.common.domain.repositories

import android.content.Context
import android.util.Base64
import org.simpleframework.xml.core.Persister
import ru.aasmc.petfinder.common.domain.model.user.User
import ru.aasmc.petfinder.common.domain.model.user.Users
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.lang.Exception


class UserRepository {
    companion object {
        fun createDataSource(context: Context, outFile: File, password: ByteArray) {
            val inputStream = context.assets.open("users.xml")
            val serializer = Persister()
            val users = try {
                serializer.read(Users::class.java, inputStream)
            } catch (e: Exception) {
                null
            }
            users?.list?.let {
                val userList = ArrayList(it) as? ArrayList
                if (userList is ArrayList<User>) {
                    val firstUser = userList.first() as? User
                    if (firstUser is User) {
                        firstUser.password = Base64.encodeToString(password, Base64.NO_WRAP)
                        val fileOutputStream = FileOutputStream(outFile)
                        val objectOutputStream = ObjectOutputStream(fileOutputStream)
                        objectOutputStream.writeObject(userList)

                        objectOutputStream.close()
                        fileOutputStream.close()
                    }
                }
            }
            inputStream.close()
        }
    }
}