package ru.aasmc.petfinder.common.presentation

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import ru.aasmc.petfinder.common.R
import ru.aasmc.petfinder.common.utils.dpToPx
import ru.aasmc.petfinder.common.utils.getTextWidth

class ProgressButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var buttonText: String = ""

    private val textPaint = Paint().apply {
        isAntiAlias = true // to smooth the edges of shapes drawn on the screen with this paint
        style = Paint.Style.FILL
        color = Color.WHITE
        textSize = context.dpToPx(16f)
    }

    private val backgroundPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.colorPrimary)
    }

    private val progressPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = Color.WHITE
        strokeWidth = context.dpToPx(2f)
    }

    private val buttonRect = RectF()
    private val progressRect = RectF()

    private var buttonRadius = context.dpToPx(16f)

    private var offset: Float = 0f

    private var widthAnimator: ValueAnimator? = null
    private var loading = false
    private var startAngle = 0f

    private var rotationAnimator: ValueAnimator? = null
    private var drawCheck = false

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ProgressButton)
        buttonText = typedArray.getString(R.styleable.ProgressButton_progressButton_text) ?: ""
        typedArray.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        buttonRadius = measuredHeight / 2f

        buttonRect.apply {
            top = 0f
            left = 0f + offset
            right = measuredWidth.toFloat() - offset
            bottom = measuredHeight.toFloat()
        }
        canvas.drawRoundRect(buttonRect, buttonRadius, buttonRadius, backgroundPaint)

        if (offset < (measuredWidth - measuredHeight) / 2f) {
            val textX = measuredWidth / 2.0f - textPaint.getTextWidth(buttonText) / 2.0f
            val textY = measuredHeight / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(buttonText, textX, textY, textPaint)
        }
    }
}

























