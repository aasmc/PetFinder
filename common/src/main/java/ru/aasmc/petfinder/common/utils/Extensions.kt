package ru.aasmc.petfinder.common.utils

import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.aasmc.logging.Logger
import ru.aasmc.petfinder.common.R

fun ImageView.setImageWithCrossFade(url: String) {
    Glide.with(this.context)
        .load(url.ifEmpty { null })
        .error(R.drawable.dog_placeholder)
        .centerCrop()
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

fun ImageView.setImage(url: String) {
    Glide.with(this.context)
        .load(url.ifEmpty { null })
        .error(R.drawable.dog_placeholder)
        .into(this)
}

inline fun CoroutineScope.createExceptionHandler(
    message: String,
    crossinline action: (throwable: Throwable) -> Unit
) = CoroutineExceptionHandler { _, throwable ->
    Logger.e(throwable, message)
    throwable.printStackTrace()
    /**
     * A [CoroutineExceptionHandler] can be called from any thread. So, if [action] is supposed to
     * run in the main thread, you need to be careful and call this function on a scope that
     * runs in the main thread, such as a [viewModelScope]
     */
    launch {
        action(throwable)
    }
}

fun Boolean.toEnglish() = if (this) "Yes" else "No"

const val CHECK_EMOJI = 0x2714
const val CROSS_EMOJI = 0x274C
const val QUESTION_EMOJI = 0x2753

fun Boolean?.toEmoji() = if (this != null) {
    String(Character.toChars(if (this) CHECK_EMOJI else CROSS_EMOJI))
} else {
    String(Character.toChars(QUESTION_EMOJI))
}

fun Context.dpToPx(dp: Float) = this.getResources().getDisplayMetrics().density * dp

fun Paint.getTextWidth(string: String): Float {
    val rect = Rect()
    this.getTextBounds(string, 0, string.length, rect)
    return rect.width().toFloat()
}
















