package com.multilevelcircularprogress.myapplication

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import java.util.*

class ExpandedFabMenu(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    private val views: MutableList<View> = ArrayList()
    private val animators: MutableList<ValueAnimator> = ArrayList()
    private var isExpanded: Boolean = false
    private var btnHook: View? = null;
    private var onExpandCollapseListener: OnExpandCollapseListener? = null

    init {
        attrs?.getAttributeBooleanValue(R.styleable.ExpandedFabMenu_state, false).let {
            if (it != null) {
                isExpanded = it
            }
        }
    }

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        val animator = ValueAnimator()
        animator.duration = 150
        animators.add(animator)
        views.add(child)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setState()
    }

    fun setOnExpandCollapseListener(onExpandCollapseListener: OnExpandCollapseListener?) {
        this.onExpandCollapseListener = onExpandCollapseListener
    }

    fun setOnItemClicklistener(onItemClicklistener: OnItemClicklistener) {
        for (i in views.indices) {
            views[i].setOnClickListener { onItemClicklistener.onClick(i) }
        }
    }

    fun setButtonHook(buttonHook: View) {
        btnHook = buttonHook;
        btnHook?.setOnClickListener { switchState() }
    }

    private fun switchState() {
        if (isExpanded) {
            collapse()
            animateButtonHook(false)
        } else {
            expand()
            animateButtonHook(true)
        }
    }

    private fun setState() {
        if (isExpanded) {
            expand()
            animateButtonHook(true)
        } else {
            collapse()
            animateButtonHook(false)
        }
    }

    fun animateButtonHook(clockwise: Boolean){
        if (clockwise){
            btnHook?.rotation = 0F;
            btnHook?.animate()?.setDuration(150)?.rotation(90F)?.start()
        }else{
            btnHook?.rotation = 90F;
            btnHook?.animate()?.setDuration(200)?.rotation(0F)?.start()
        }
    }

    fun expand() {
        isExpanded = true
        stopAllAnims()
        for (i in views.indices) {
            animators[i].setFloatValues(1f, 0f)
            val translateY = getChildsHeight(i)
            animators[i].interpolator = OvershootInterpolator()
            animators[i].addUpdateListener { valueAnimator: ValueAnimator ->
                val p = valueAnimator.animatedValue as Float
                views[i].translationY = p * translateY
                views[i].scaleX = 1 - p
                views[i].scaleY = 1 - p
            }
            animators[i].start()
        }
        if (onExpandCollapseListener != null) onExpandCollapseListener?.onExpand()
    }

    fun collapse() {
        isExpanded = false
        stopAllAnims()
        for (i in views.indices) {
            animators[i].setFloatValues(0f, 1f)
            val translateY = getChildsHeight(i)
            animators[i].duration = 300
            animators[i].interpolator = DecelerateInterpolator()
            animators[i].addUpdateListener { valueAnimator: ValueAnimator ->
                val p = valueAnimator.animatedValue as Float
                views[i].translationY = p * translateY
                views[i].scaleX = 1 - p
                views[i].scaleY = 1 - p
            }
            animators[i].start()
        }
        if (onExpandCollapseListener != null) onExpandCollapseListener?.onCollapse()
    }

    fun collapseNoAnim() {
        isExpanded = false
        stopAllAnims()
        for (i in views.indices) {
            views[i].translationY = getChildsHeight(i).toFloat()
            views[i].scaleX = 0f
            views[i].scaleY = 0f
        }
        if (onExpandCollapseListener != null) onExpandCollapseListener?.onCollapse()
    }

    fun expandNoAnim() {
        isExpanded = true
        stopAllAnims()
        for (i in views.indices) {
            views[i].translationY = 0f
            views[i].scaleX = 1f
            views[i].scaleY = 1f
        }
        if (onExpandCollapseListener != null) onExpandCollapseListener?.onExpand()
    }

    private fun stopAllAnims() {
        for (i in animators.indices) {
            if (animators[i].isRunning) animators[i].cancel()
        }
    }

    private fun getChildsHeight(index: Int): Int {
        var height = 0
        for (i in index until views.size) {
            height += views[i].measuredHeight
            val params = views[i].layoutParams as LayoutParams
            height += params.topMargin
        }
        return height
    }

    override fun onDetachedFromWindow() {
        stopAllAnims()
        if (isExpanded) collapseNoAnim()
        super.onDetachedFromWindow()
    }

    interface OnExpandCollapseListener {
        fun onExpand()
        fun onCollapse()
    }

    interface OnItemClicklistener {
        fun onClick(index: Int)
    }
}