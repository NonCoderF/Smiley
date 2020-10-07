package com.multilevelcircularprogress.myapplication

import android.animation.ValueAnimator
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.animation.addListener

class CircleObject {
    var barWidth = 0F
    var backgroundWidth = 0F
    var barColor = -1
    var backgroundColor = -1

    var amountValue = 0F
    var totalAmount = 0F
    var prevAmount = 0F
    var animDuration = 3000

    var suffix = ""
    var prefix = ""
    var textColor = -1

    var observer: ((String, Float) -> Unit?)? = null
    var invalidator: (() -> Unit?)? = null

    lateinit var barPaint: Paint
    lateinit var backgroundPaint: Paint
    lateinit var textPaint: Paint
    lateinit var rectF: RectF

    var animator: ValueAnimator? = null

    fun build() {
        barPaint = Paint()
        barPaint.isAntiAlias = true
        barPaint.style = Paint.Style.STROKE
        barPaint.let {
            it.strokeWidth =
                if (barWidth in CircleProgress.DEFAULTS.barRange) barWidth else CircleProgress.DEFAULTS.defBarWidth
        }
        barPaint.strokeCap = Paint.Cap.ROUND
        barPaint.let { it.color = if (barColor != -1) barColor else CircleProgress.DEFAULTS.defBarColor }

        backgroundPaint = Paint()
        backgroundPaint.isAntiAlias = true
        backgroundPaint.style = Paint.Style.STROKE
        backgroundPaint.let {
            it.strokeWidth =
                if (backgroundWidth in CircleProgress.DEFAULTS.barRange) backgroundWidth else CircleProgress.DEFAULTS.defBarWidth
        }
        backgroundPaint.strokeCap = Paint.Cap.ROUND
        backgroundPaint.let {
            it.color =
                if (backgroundColor != -1) backgroundColor else CircleProgress.DEFAULTS.defBackgroundColor
        }

        textPaint = Paint()
        textPaint.isAntiAlias = true
        textPaint.style = Paint.Style.FILL
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.isFakeBoldText = true
        textPaint.let { it.color = if (textColor != -1) textColor else CircleProgress.DEFAULTS.defTextColor }

        rectF = RectF()
    }

    fun update() {
        amountValue.compareTo(totalAmount).also { if (it > 0) amountValue = totalAmount }

        animator?.let {
            it.isRunning.let { _ ->
                it.cancel()
            }.also { _ ->
                it.setFloatValues(prevAmount, amountValue)
            }
        } ?: kotlin.run {
            animator = ValueAnimator.ofFloat(prevAmount, amountValue)
        }

        animator?.duration = animDuration.toLong()
        animator?.addUpdateListener { animation ->
            prevAmount = animation.animatedValue as Float
            invalidator?.invoke()
            invokeObserver(CircleProgress.ANIMATION.RUNNING)
        }
        animator?.addListener(
            { invokeObserver(CircleProgress.ANIMATION.ENDED) },
            { invokeObserver(CircleProgress.ANIMATION.STARTED) },
            { invokeObserver(CircleProgress.ANIMATION.CANCELLED) }, {}
        )
        animator?.start()
    }

    private fun invokeObserver(state: String) {
        observer?.invoke(state, prevAmount)
    }
}