package com.multilevelcircularprogress.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.O)
class CircleProgress constructor(
    context: Context,
    attributeSet: AttributeSet?
) : View(context, attributeSet) {

    object ANIMATION {
        const val STARTED = "started"
        const val ENDED = "ended"
        const val CANCELLED = "cancelled"
        const val RUNNING = "running"
    }

    object DEFAULTS {
        var barRange = 5F..50F
        var gapRange = 0F..25F
        var defBarWidth = 25f
        var defGap = 15f
        var defTextColor = Color.parseColor("#000000")
        var defBarColor = Color.parseColor("#000000")
        var defBackgroundColor = Color.parseColor("#33000000")
    }

    var fontFamily: Typeface? = null
    var circleGap = 0F
    var startAngle = 0F
    var textVisible = true

    private var circlesList: ArrayList<CircleObject> = ArrayList()

    init {
        init(context, attributeSet)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun init(context: Context, attrs: AttributeSet?) {
        val attributeSet =
            context.theme.obtainStyledAttributes(attrs, R.styleable.CircleProgress, 0, 0)
        attributeSet.getFont(R.styleable.CircleProgress_textFont)?.let { fontFamily = it }
        attributeSet.getFloat(R.styleable.CircleProgress_circleGap, 0F)
            .let { circleGap = if (it in DEFAULTS.gapRange) it else DEFAULTS.defGap }
        attributeSet.getFloat(R.styleable.CircleProgress_startAngle, -90F).let { startAngle = it }
        textVisible = attributeSet.getBoolean(R.styleable.CircleProgress_textVisible, true)

        attachListener()
    }

    fun addCircle(circle: CircleObject) {
        circle.build()
        circle.invalidator = { invalidate() }
        circlesList.add(circle)
    }

    fun render() {
        circlesList.forEach { it.update() }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val min = Math.min(width, height)
        setMeasuredDimension(min, min)

        for (i in circlesList.indices) {
            with(circlesList[i]) {
                val leftTop = (i + 1) * barWidth + circleGap * (i + 1).toFloat()
                val right = width - (i + 1) * barWidth - (circleGap * (i + 1))
                val bottom = height - (i + 1) * barWidth - (circleGap * (i + 1))
                rectF.set(leftTop, leftTop, right, bottom)
                textPaint.textSize = ((height / 2 - i * 60) / circlesList.size).toFloat() / 2
                fontFamily?.let { textPaint.typeface = it }
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        circlesList.mapIndexed { i, it ->
            val sweepAngle = 360 * (it.prevAmount / it.totalAmount)
            canvas?.drawArc(it.rectF, 0F, 360F, false, it.backgroundPaint)
            canvas?.drawArc(it.rectF, startAngle, sweepAngle, false, it.barPaint)

            if (!textVisible) return

            val xPos = width / 2
            var yPos = (height / 2 - (it.textPaint.descent() + it.textPaint.ascent()) / 2
                    - (circlesList.size + i) * it.textPaint.descent()).toInt()

            if (i != 0) yPos += (i * circlesList[i - 1].textPaint.textSize + i * 18).toInt()

            val text = it.suffix + " " + it.prevAmount.toInt().toString() + " " + it.prefix
            canvas?.drawText(text, xPos.toFloat(), yPos.toFloat(), it.textPaint)
        }
    }


    private fun attachListener(){
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener{
            override fun onViewAttachedToWindow(p0: View?) {}

            override fun onViewDetachedFromWindow(p0: View?) {
                circlesList.forEach {
                    it.animator?.removeAllUpdateListeners()
                    it.animator?.cancel()
                }
            }

        })
    }


}