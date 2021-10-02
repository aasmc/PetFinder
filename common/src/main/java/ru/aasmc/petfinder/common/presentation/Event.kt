package ru.aasmc.petfinder.common.presentation

// need this to be data class or override equals and hashCode for proper testing
data class Event<out T>(private val content: T) {
    private var hasBeenHandled: Boolean = false

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }
}