package com.jetlaunch.androidawesomeviews

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class GradientCircleChart : View {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        fill(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        fill(context, attrs)
    }

    private var color0: Int = Color.BLACK
    private var color1: Int = Color.WHITE
    private var strokeColor: Int = Color.WHITE
    private var circleRadius: Float = 200F
    private var stroke: Float = 20F
    var angle = 0.0
        set(value) {
            field = value
            invalidate()
        }

    var percent = 0
        set(value) {
            if (value in 0..100) {
                field = value
                angle = field * 3.6
            }
        }

    private fun fill(context: Context, attrs: AttributeSet) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.GradientCircleChart)
        color0 = ta.getColor(R.styleable.GradientCircleChart_startColor, color0)
        color1 = ta.getColor(R.styleable.GradientCircleChart_endColor, color1)
        circleRadius = ta.getDimension(R.styleable.GradientCircleChart_circleRadius, circleRadius)
        stroke = ta.getDimension(R.styleable.GradientCircleChart_strokeWidth, stroke)
        strokeColor = ta.getColor(R.styleable.GradientCircleChart_strokeColor, strokeColor)
        angle = ta.getFloat(R.styleable.GradientCircleChart_angle, angle.toFloat()).toDouble()
        paint.strokeWidth = stroke
        paint2.strokeWidth = stroke
        paint2.color = strokeColor
        ta.recycle()
    }

    private val paint: Paint = Paint().apply {
        this.style = Paint.Style.STROKE
        strokeWidth = stroke
        color = Color.BLACK
    }
    private val paint2 = Paint(paint).apply {
        color = strokeColor
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val x = width / 2F
        val y = height / 2f
        val rads = Math.toRadians(angle)

        val startX = x - cos(rads) * (circleRadius + stroke / 2)
        val startY = y - sin(rads) * (circleRadius + stroke / 2)
        val endX = x + cos(rads) * (circleRadius + stroke / 2)
        val endY = y + sin(rads) * (circleRadius + stroke / 2)


        val shader = LinearGradient(
            startX.toFloat(), startY.toFloat(), endX.toFloat(), endY.toFloat(),
            color0, color1, Shader.TileMode.REPEAT
        )


        paint.shader = shader
        val hs = stroke / 2
        canvas.drawCircle(x, y, circleRadius - hs, paint2)
        canvas.drawArc(
            x - circleRadius + hs, y - circleRadius + hs,
            x + circleRadius - hs, y + circleRadius - hs,
            270F, angle.toFloat(), false, paint
        )


    }
}