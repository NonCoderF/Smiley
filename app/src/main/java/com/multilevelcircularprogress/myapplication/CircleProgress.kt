package com.multilevelcircularprogress.myapplication

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.animation.addListener

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
                    if (barWidth in DEFAULTS.barRange) barWidth else DEFAULTS.defBarWidth
            }
            barPaint.strokeCap = Paint.Cap.ROUND
            barPaint.let { it.color = if (barColor != -1) barColor else DEFAULTS.defBarColor }

            backgroundPaint = Paint()
            backgroundPaint.isAntiAlias = true
            backgroundPaint.style = Paint.Style.STROKE
            backgroundPaint.let {
                it.strokeWidth =
                    if (backgroundWidth in DEFAULTS.barRange) backgroundWidth else DEFAULTS.defBarWidth
            }
            backgroundPaint.strokeCap = Paint.Cap.ROUND
            backgroundPaint.let {
                it.color =
                    if (backgroundColor != -1) backgroundColor else DEFAULTS.defBackgroundColor
            }

            textPaint = Paint()
            textPaint.isAntiAlias = true
            textPaint.style = Paint.Style.FILL
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.isFakeBoldText = true
            textPaint.let { it.color = if (textColor != -1) textColor else DEFAULTS.defTextColor }

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
                invokeObserver(ANIMATION.RUNNING)
            }
            animator?.addListener(
                { invokeObserver(ANIMATION.ENDED) },
                { invokeObserver(ANIMATION.STARTED) },
                { invokeObserver(ANIMATION.CANCELLED) }, {}
            )
            animator?.start()
        }

        private fun invokeObserver(state: String) {
            observer?.invoke(state, prevAmount)
        }
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