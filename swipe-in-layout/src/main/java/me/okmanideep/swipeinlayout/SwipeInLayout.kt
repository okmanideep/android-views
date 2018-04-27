package me.okmanideep.swipeinlayout

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.view.ViewCompat
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout


class SwipeInLayout
@JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attributeSet, defStyleAttr) {
    private var isExpanded: Boolean = false
    private val dragCallback = SwipeInLayoutDragCallback(this)
    private val dragHelper: ViewDragHelper
    private var slideEdge = SlideEdge.LEFT
    private var collapsedWidth: Int = 0
    private var collapsedHeight: Int = 0

    init {
        dragHelper = ViewDragHelper.create(this, dragCallback)
        if (attributeSet != null) {
            val attrs = context.theme.obtainStyledAttributes(attributeSet, R.styleable.SwipeInLayout, 0, 0)
            try {
                val slideEdgeAttr = attrs.getInt(R.styleable.SwipeInLayout_slideEdge, 0)
                slideEdge = when(slideEdgeAttr) {
                    SlideEdge.LEFT.ordinal -> SlideEdge.LEFT
                    SlideEdge.RIGHT.ordinal -> SlideEdge.RIGHT
                    SlideEdge.TOP.ordinal -> SlideEdge.TOP
                    SlideEdge.BOTTOM.ordinal -> SlideEdge.BOTTOM
                    else -> SlideEdge.LEFT
                }
                isExpanded = attrs.getBoolean(R.styleable.SwipeInLayout_expanded, false)
                collapsedWidth = attrs.getDimensionPixelSize(R.styleable.SwipeInLayout_collapsedWidth, 0)
                collapsedHeight = attrs.getDimensionPixelSize(R.styleable.SwipeInLayout_collapsedHeight, 0)
            } finally {
                attrs.recycle()
            }
        }
    }

    fun expand(animate: Boolean = true) {
        isExpanded = true
        val child = getChild() ?: return
        if (animate) {
            if (slideEdge.isHorizontal()) {
                val finalLeft = if (slideEdge.isLeft()) maxLeft() else minLeft()
                dragHelper.smoothSlideViewTo(child, finalLeft, child.top)
            } else {
                val finalTop = if (slideEdge.isTop()) maxTop() else minTop()
                dragHelper.smoothSlideViewTo(child, child.left, finalTop)
            }
        } else {
            requestLayout()
        }
    }

    fun collapse(animate: Boolean = true) {
        isExpanded = false
        val child = getChild() ?: return
        if (animate) {
            if (slideEdge.isHorizontal()) {
                val finalLeft = if (slideEdge.isLeft()) minLeft() else maxLeft()
                dragHelper.smoothSlideViewTo(child, finalLeft, child.top)
            } else {
                val finalTop = if (slideEdge.isTop()) minTop() else maxTop()
                dragHelper.smoothSlideViewTo(child, child.left, finalTop)
            }
        } else {
            requestLayout()
        }
    }

    internal fun getChild(): View? {
        return getChildAt(0)
    }

    internal fun onViewReleased(child: View, xvel: Float, yvel: Float) {
        if (slideEdge.isHorizontal()) {
            val currentPos = child.left
            val start = minLeft()
            val end = maxLeft()
            val finalPos = getFinalPosByVel(currentPos, start, end, xvel)
            isExpanded = (finalPos == end && slideEdge.isLeft()) ||
                    (finalPos == start && slideEdge.isRight())
            if (dragHelper.settleCapturedViewAt(finalPos, child.top)) {
                ViewCompat.postInvalidateOnAnimation(this)
            }
        } else {
            val currentPos = child.top
            val start = minTop()
            val end = maxTop()
            val finalPos = getFinalPosByVel(currentPos, start, end, yvel)
            isExpanded = (finalPos == end && slideEdge.isTop()) ||
                    (finalPos == start && slideEdge.isBottom())
            if (dragHelper.settleCapturedViewAt(child.left, finalPos)) {
                ViewCompat.postInvalidateOnAnimation(this)
            }
        }
    }

    private fun getFinalPosByVel(currentPos: Int, start: Int, end: Int, vel: Float): Int {
        val mid = (start + end) / 2
        return if (currentPos < mid) {
            val minVel = (end - currentPos) / 0.25f
            if (vel > minVel) end else start
        } else {
            // negative velocities
            val minVel = (start - currentPos) / 0.25f
            if (vel < minVel) start else end
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return dragHelper.shouldInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        super.onTouchEvent(ev)
        dragHelper.processTouchEvent(ev)
        return true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val child = getChild() ?: return
        if (isExpanded) {
            child.left = paddingLeft + (child.layoutParams as FrameLayout.LayoutParams).leftMargin
            child.top = paddingTop + (child.layoutParams as FrameLayout.LayoutParams).topMargin
        } else {
            when (slideEdge) {
                SlideEdge.LEFT -> {
                    child.offsetLeftAndRight(collapsedWidth - child.width)
                }
                SlideEdge.RIGHT -> {
                    child.offsetLeftAndRight(width - collapsedWidth)
                }
                SlideEdge.TOP -> {
                    child.offsetTopAndBottom(collapsedHeight - child.height)
                }
                SlideEdge.BOTTOM -> {
                    child.offsetTopAndBottom(height - collapsedHeight)
                }
            }
        }
    }

    override fun addView(child: View?) {
        super.addView(child)
        onChildrenChanged()
    }

    override fun addView(child: View?, index: Int) {
        super.addView(child, index)
        onChildrenChanged()
    }

    override fun addView(child: View?, params: ViewGroup.LayoutParams?) {
        super.addView(child, params)
        onChildrenChanged()
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        super.addView(child, index, params)
        onChildrenChanged()
    }

    override fun addView(child: View?, width: Int, height: Int) {
        super.addView(child, width, height)
        onChildrenChanged()
    }

    override fun addViewInLayout(child: View?, index: Int, params: ViewGroup.LayoutParams?): Boolean {
        val ret = super.addViewInLayout(child, index, params)
        onChildrenChanged()
        return ret
    }

    override fun addViewInLayout(child: View?, index: Int, params: ViewGroup.LayoutParams?, preventRequestLayout: Boolean): Boolean {
        val ret = super.addViewInLayout(child, index, params, preventRequestLayout)
        onChildrenChanged()
        return ret
    }

    override fun computeScroll() {
        super.computeScroll()
        if (dragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    private fun onChildrenChanged() {
        if (childCount > 1) {
            throw IllegalStateException("SwipeInLayout cannot have more than one child")
        }
    }

    internal fun minLeft(): Int {
        val child = getChild() ?: return 0

        return if (slideEdge.isLeft()) {
            collapsedWidth - child.width
        } else {
            paddingLeft + (child.layoutParams as FrameLayout.LayoutParams).leftMargin
        }
    }

    internal fun maxLeft(): Int {
        val child = getChild() ?: return 0

        return if (slideEdge.isRight()) {
            width - collapsedWidth
        } else {
            paddingLeft + (child.layoutParams as FrameLayout.LayoutParams).leftMargin
        }
    }

    internal fun minTop(): Int {
        val child = getChild() ?: return 0

        return if (slideEdge.isTop()) {
            collapsedHeight - child.height
        } else {
            paddingTop + (child.layoutParams as FrameLayout.LayoutParams).topMargin
        }
    }

    internal fun maxTop(): Int {
        val child = getChild() ?: return 0

        return if (slideEdge.isBottom()) {
            height - collapsedHeight
        } else {
            paddingTop + (child.layoutParams as FrameLayout.LayoutParams).topMargin
        }
    }
}

internal enum class SlideEdge {
    LEFT, RIGHT, TOP, BOTTOM;

    fun isLeft() = this == LEFT

    fun isRight() = this == RIGHT

    fun isTop() = this == TOP

    fun isBottom() = this == BOTTOM

    fun isHorizontal() = isLeft() || isRight()

    fun isVertical() = isTop() || isBottom()
}
