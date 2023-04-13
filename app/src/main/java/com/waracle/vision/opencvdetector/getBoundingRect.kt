package com.waracle.vision.opencvdetector

import android.graphics.Rect
import com.waracle.vision.cncddetector.MobilenetSSDNcnn

fun MobilenetSSDNcnn.Obj.toBoundingRect(): Rect {
    var left = Int.MAX_VALUE
    var top = Int.MAX_VALUE
    var right = Int.MIN_VALUE
    var bottom = Int.MIN_VALUE

    val obj = this
    if (obj.x < left) {
        left = obj.x.toInt()
    }
    if (obj.y < top) {
        top = obj.y.toInt()
    }
    if (obj.x + obj.w > right) {
        right = (obj.x + obj.w).toInt()
    }
    if (obj.y + obj.h > bottom) {
        bottom = (obj.y + obj.h).toInt()
    }

    return Rect(left, top, right, bottom)
}
