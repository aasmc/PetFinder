package ru.aasmc.petfinder.common.domain.model.user

import androidx.annotation.Keep
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root
import java.io.Serializable

@Keep
@Root(name = "user", strict = false)
data class User constructor(
    @field:Element(name = "name")
    @param:Element(name = "name")
    var name: String,

    @field:Element(name = "password")
    @param:Element(name = "password")
    var password: String,

    @field:Element(name = "id")
    @param:Element(name = "id")
    var id: String,

    @field:Element(name = "extra")
    @param:Element(name = "extra")
    var extra: String) : Serializable