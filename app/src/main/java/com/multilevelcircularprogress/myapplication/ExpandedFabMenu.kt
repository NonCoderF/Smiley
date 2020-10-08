package com.multilevelcircularprogress.myapplication

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout


class ExpandedFabMenu(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    private var isExpanded: Boolean = false
    private var btnHook: View? = null
    private var overlayView: View? = null
    private var onExpandCollapseListener: OnExpandCollapseListener? = null

    init {
        attrs?.getAttributeBooleanValue(R.styleable.ExpandedFabMenu_state, false).let {
            if (it != null) {
                isExpanded = it
            }
        }
    }

    private val views: MutableList<View> = ArrayList()
    private val animators: MutableList<ValueAnimator> = ArrayList()
    private val yTranslateVals: MutableList<Float> = ArrayList()
    private var viewWidth = 0
    private var viewHeight = 0


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewWidth = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        viewHeight = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)

        if (views.size > 0) return
        for (i in 0 until childCount) {
            views.add(getChildAt(i))
            if (getChildAt(i) is ViewGroup) {
                val viewGroup: ViewGroup = getChildAt(i) as ViewGroup
                for (j in 0 until viewGroup.childCount) {
                    views.add(viewGroup.getChildAt(j))
                }
            }
        }
        setAnimators()

        setState(false)
    }

    private fun setAnimators() {
        views.map {
            val animator = ValueAnimator()
            animator.duration = 300
            animator.interpolator = DecelerateInterpolator()
            animators.add(animator)
        }
    }

    private fun setOnExpandCollapseListener(onExpandCollapseListener: OnExpandCollapseListener?) {
        this.onExpandCollapseListener = onExpandCollapseListener
    }

    fun setButtonHook(buttonHook: View) {
        btnHook = buttonHook
        btnHook?.setOnClickListener { switchState() }
    }

    fun setOverLayView(view: View) {
        overlayView = view
        overlayView?.setOnClickListener { switchState() }
    }

    fun switchState() {
        if (isExpanded) setState(false) else setState(true)
    }

    fun setState(expand: Boolean) {
        expandCollapse(expand)
        animateButtonHook(expand)
        animateOverLay(expand)
    }

    private fun animateButtonHook(clockwise: Boolean) {
        if (clockwise) {
            btnHook?.rotation = 0F
            btnHook?.animate()?.setDuration(150)?.rotation(90F)?.start()
        } else {
            btnHook?.rotation = 90F
            btnHook?.animate()?.setDuration(200)?.rotation(0F)?.start()
        }
    }

    private fun animateOverLay(visible: Boolean) {
        if (visible) {
            overlayView?.isClickable = true
            overlayView?.alpha = 0F
            overlayView?.animate()?.setDuration(150)?.alpha(1F)?.start()
        } else {
            overlayView?.isClickable = false
            overlayView?.alpha = 1F
            overlayView?.animate()?.setDuration(200)?.alpha(0F)?.start()
        }
    }

    private fun expandCollapse(expand: Boolean) {
        isExpanded = expand
        stopAllAnims()
        calcTranslateVals()
        for (i in views.indices) {
            if (expand) animators[i].setFloatValues(1F, 0F)
            else animators[i].setFloatValues(0F, 1F)
            animators[i].startDelay = (i * 10).toLong()
            animators[i].addUpdateListener {
                val p = it.animatedValue as Float
                views[i].translationY = p * yTranslateVals[i]
                views[i].scaleX = 1 - p
                views[i].scaleY = 1 - p
                views[i].alpha = 1 - p * 5
            }
            animators[i].start()
        }
    }

    private fun expandCollapseNoAnim(expand: Boolean) {
        isExpanded = expand
        stopAllAnims()
        calcTranslateVals()
        for (i in views.indices) {
            val p: Float = if (expand) 0F else 1F
            views[i].translationY = p * yTranslateVals[i]
            views[i].scaleX = 1 - p
            views[i].scaleY = 1 - p
            views[i].alpha = 1 - p
        }
    }

    private fun calcTranslateVals() {
        if (yTranslateVals.size != views.size) {
            for (i in views.indices) {
                yTranslateVals.add(viewHeight - views[i].y - views[i].height)
            }
        }
    }

    private fun stopAllAnims() {
        for (i in animators.indices) {
            animators[i].cancel()
        }
    }

    override fun onDetachedFromWindow() {
        stopAllAnims()
        if (isExpanded) expandCollapseNoAnim(false)
        super.onDetachedFromWindow()
    }

    interface OnExpandCollapseListener {
        fun onExpand()
        fun onCollapse()
    }
}