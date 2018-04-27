package me.okmanideep.swipeinlayout

import android.support.v4.widget.ViewDragHelper
import android.view.View


internal class SwipeInLayoutDragCallback(val layout: SwipeInLayout) : ViewDragHelper.Callback() {
    override fun tryCaptureView(child: View, pointerId: Int) = child == layout.getChild()

    override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
        return clamp(left, layout.minLeft(), layout.maxLeft())
    }

    override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
        return clamp(top, layout.minTop(), layout.maxTop())
    }

    override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
        layout.onViewReleased(releasedChild, xvel, yvel)
    }

    override fun getViewHorizontalDragRange(child: View): Int {
        return layout.maxLeft() - layout.minLeft()
    }

    override fun getViewVerticalDragRange(child: View): Int {
        return layout.maxTop() - layout.minTop()
    }

    private fun clamp(value: Int, min: Int, max: Int): Int {
        return Math.min(Math.max(min, value), max)
    }
}