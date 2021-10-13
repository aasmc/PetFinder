package ru.aasmc.petfinder.common.domain.model.user

import androidx.annotation.Keep
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Keep // need to use this annotation to prevent the class name from obfuscation since it is needed for Reflection
@Root(name = "users", strict = false)
data class Users constructor(
    @field:ElementList(entry = "user", inline = true)
    @param:ElementList(entry = "user", inline = true)
    val list: List<User>? = null)