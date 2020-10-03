package com.multilevelcircularprogress.myapplication

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import java.util.*
import kotlin.math.min

class Smiley(context: Context?, attributeSet: AttributeSet?) : View(context, attributeSet) {
    private var animator: ValueAnimator? = null
    private var duration = 500
    private var targetMoodValues = arrayOf<Float>()
    private var prevMoodValues = arrayOf<Float>()
    private var currentMoodValues = arrayOf<Float>()
    private var min = 0
    private val paint: Paint?
    private val smilePath = Path()
    private val leftEyePath = Path()
    private val rightEyePath = Path()
    private val smilePoints: MutableList<Point> = ArrayList()
    private val leftEyePoints: MutableList<Point> = ArrayList()
    private val rightEyePoints: MutableList<Point> = ArrayList()

    private val leftX = arrayOf(0.9f, 0.9f, 1.2f, 0.8f, 1f)
    private val leftY = arrayOf(1f, 1f, 1.1f, 1.1f, 1f)
    private val controlLeftX = arrayOf(0.75f, 0.75f, 1f, 0.85f, 0.9f)
    private val controlLeftY = arrayOf(1.25f, 1.25f, 1.1f, 0.85f, 0.85f)
    private val controlRightX = arrayOf(1.25f, 1.25f, 1f, 1.15f, 1.1f)
    private val controlRightY = arrayOf(1.25f, 1.25f, 1.1f, 0.85f, 0.85f)
    private val rightX = arrayOf(1.1f, 1.1f, 0.8f, 1.2f, 1f)
    private val rightY = arrayOf(1f, 1f, 1.1f, 1.1f, 1f)
    private val eyeFactor1 = arrayOf(0.45f, 0.30f, 0.15f, -0.25f, -0.15f)
    private val eyeFactor2 = arrayOf(1f, 1f, 1f, 1f, 0.8f)

    init {
        paint = Paint()
        paint.color = Color.parseColor("#000000")
        paint.strokeWidth = 20f
        paint.strokeCap = Paint.Cap.ROUND
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE

        attachListener()
    }

    fun setStrokeWidth(stroke: Float) {
        paint?.strokeWidth = stroke
    }

    fun setStrokeColor(color: Int) {
        paint?.color = color
    }

    fun setDuration(duration: Int) {
        this.duration = duration
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        min = min(
            getDefaultSize(suggestedMinimumWidth, widthMeasureSpec),
            getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        )
        setMeasuredDimension(min, min)
        pivotX = min.toFloat() / 2
        pivotY = min.toFloat() / 2
    }

    fun setToMood(moodPercent: Int) {
        val moodIndex = (100 - moodPercent) / 25
        setTargetMoodValues(moodIndex)
        if (prevMoodValues.isEmpty()) {
            setPrevMoodValues(moodIndex)
        }
        if (animator == null) {
            animator = ValueAnimator.ofFloat(0f, 100f)
        }
        if (animator!!.isRunning) {
            animator!!.cancel()
            prevMoodValues = currentMoodValues
        }
        animator?.duration = duration.toLong()
        animator?.interpolator = AccelerateDecelerateInterpolator()
        animator?.addUpdateListener { valueAnimator ->
            val percent = valueAnimator.animatedValue as Float

            circleRadii = if (percent > 50) {
                ((100 - percent) / 100) * 0.05f
            } else {
                (percent / 100) * 0.05f
            }

            if (percent == 100f) {
                setPrevMoodValues(moodIndex)
            } else {
                calculate(percent)
                invalidate()
            }
        }
        animator?.start()
    }

    private var circleRadii: Float = 0f

    private fun calculate(percent: Float) {
        val newValues = FloatArray(prevMoodValues.size)
        for (i in prevMoodValues.indices) {
            newValues[i] =
                (targetMoodValues[i] - prevMoodValues[i]) * (percent / 100) + prevMoodValues[i]
        }
        currentMoodValues = newValues.toTypedArray()
        calcPoints(currentMoodValues)
    }

    private fun calcPoints(points: Array<Float>) {
        smilePoints.clear()
        leftEyePoints.clear()
        rightEyePoints.clear()
        smilePoints.add(Point((points[0] * min / 3).toInt(), (points[1] * 4 * min / 6).toInt()))
        smilePoints.add(Point((points[4] * min / 2).toInt(), (points[5] * 4 * min / 6).toInt()))
        smilePoints.add(Point((points[6] * min / 2).toInt(), (points[7] * 4 * min / 6).toInt()))
        smilePoints.add(
            Point(
                (min / 3 + points[2] * min / 3).toInt(),
                (points[3] * 4 * min / 6).toInt()
            )
        )
        leftEyePoints.add(Point(min / 4, (points[1] * min / 3).toInt()))
        leftEyePoints.add(
            Point(
                (min / 4 + 2 * (min / 2.5 - min / 4) / 5).toInt(),
                ((points[5] - points[8]) * min / 3).toInt()
            )
        )
        leftEyePoints.add(
            Point(
                (min / 4 + 3 * (min / 2.5 - min / 4) / 5).toInt(),
                ((points[7] - points[8]) * min / 3).toInt()
            )
        )
        leftEyePoints.add(Point((min / 2.5).toInt(), (points[3] * min / 3 * points[9]).toInt()))
        rightEyePoints.add(
            Point(
                (min - min / 2.5).toInt(),
                (points[1] * min / 3 * points[9]).toInt()
            )
        )
        rightEyePoints.add(
            Point(
                (min - (min / 4 + 3 * (min / 2.5 - min / 4) / 5)).toInt(),
                ((points[5] - points[8]) * min / 3).toInt()
            )
        )
        rightEyePoints.add(
            Point(
                (min - (min / 4 + 2 * (min / 2.5 - min / 4) / 5)).toInt(),
                ((points[7] - points[8]) * min / 3).toInt()
            )
        )
        rightEyePoints.add(Point(min - min / 4, (points[3] * min / 3).toInt()))
    }

    private fun setTargetMoodValues(mood: Int) {
        targetMoodValues = arrayOf(
            leftX[mood],
            leftY[mood],
            rightX[mood],
            rightY[mood],
            controlLeftX[mood],
            controlLeftY[mood],
            controlRightX[mood],
            controlRightY[mood],
            eyeFactor1[mood],
            eyeFactor2[mood]
        )
    }

    private fun setPrevMoodValues(mood: Int) {
        prevMoodValues = arrayOf(
            leftX[mood],
            leftY[mood],
            rightX[mood],
            rightY[mood],
            controlLeftX[mood],
            controlLeftY[mood],
            controlRightX[mood],
            controlRightY[mood],
            eyeFactor1[mood],
            eyeFactor2[mood]
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        scaleX = 1 - circleRadii
        scaleY = 1 - circleRadii

        canvas.drawCircle(min.toFloat() / 2, min.toFloat() / 2, min.toFloat() / 2 - 20, paint!!)

        if (smilePoints.size == 4) {
            smilePath.reset()
            smilePath.moveTo(smilePoints[0].x.toFloat(), smilePoints[0].y.toFloat())
            smilePath.cubicTo(
                smilePoints[1].x.toFloat(), smilePoints[1].y.toFloat(),
                smilePoints[2].x.toFloat(), smilePoints[2].y.toFloat(),
                smilePoints[3].x.toFloat(), smilePoints[3].y.toFloat()
            )
            leftEyePath.reset()
            leftEyePath.moveTo(leftEyePoints[0].x.toFloat(), leftEyePoints[0].y.toFloat())
            leftEyePath.cubicTo(
                leftEyePoints[1].x.toFloat(), leftEyePoints[1].y.toFloat(),
                leftEyePoints[2].x.toFloat(), leftEyePoints[2].y.toFloat(),
                leftEyePoints[3].x.toFloat(), leftEyePoints[3].y.toFloat()
            )
            rightEyePath.reset()
            rightEyePath.moveTo(rightEyePoints[0].x.toFloat(), rightEyePoints[0].y.toFloat())
            rightEyePath.cubicTo(
                rightEyePoints[1].x.toFloat(), rightEyePoints[1].y.toFloat(),
                rightEyePoints[2].x.toFloat(), rightEyePoints[2].y.toFloat(),
                rightEyePoints[3].x.toFloat(), rightEyePoints[3].y.toFloat()
            )
            canvas.drawPath(smilePath, paint)
            canvas.drawPath(leftEyePath, paint)
            canvas.drawPath(rightEyePath, paint)
        }
    }

    private fun attachListener(){
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener{
            override fun onViewAttachedToWindow(p0: View?) {}

            override fun onViewDetachedFromWindow(p0: View?) {
                animator?.cancel()
            }

        })
    }
}