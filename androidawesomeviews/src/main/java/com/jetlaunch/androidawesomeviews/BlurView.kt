package com.jetlaunch.androidawesomeviews

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.cardview.widget.CardView
import androidx.renderscript.Allocation
import androidx.renderscript.Element
import androidx.renderscript.RenderScript
import androidx.renderscript.ScriptIntrinsicBlur
import java.lang.Exception

class BlurView: CardView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs){
        initView(attrs)
    }
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle){
        initView(attrs, defStyle)
    }
    private var internalBitmap: Bitmap? = null
    private var blurredBitmap: Bitmap? = null

    private var desRect: RectF? = null
    private var srcRect: Rect? = null
    private val paintClear = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    }
    private val paintStorke = Paint().apply{
        style = Paint.Style.STROKE
        strokeWidth = blurStrokeWidth
        color = Color.WHITE

    }
    private val paintSolid = Paint().apply {
        style = Paint.Style.FILL
        color = blurSolidColor
        alpha = blurColorAlpha
    }
    private val paint = Paint()

    private fun initView(attrs: AttributeSet, defStyle: Int = 0){
        val a = context.obtainStyledAttributes(attrs, R.styleable.BlurView, defStyle,0)
        isInit = true
        if(a.hasValue(R.styleable.BlurView_blurStrokeColor)){
            blurStrokeColor = a.getColor(R.styleable.BlurView_blurStrokeColor, defStyle)
        }
        if(a.hasValue(R.styleable.BlurView_blurStrokeEnabled)){
            strokeEnabled = a.getBoolean(R.styleable.BlurView_blurStrokeEnabled, false)
        }
        if(a.hasValue(R.styleable.BlurView_blurStrokeWidth)){
            blurStrokeWidth = a.getFloat(R.styleable.BlurView_blurStrokeWidth, 4F)
        }

        if(a.hasValue(R.styleable.BlurView_blurLevel)){
            var level = a.getFloat(R.styleable.BlurView_blurLevel, 0F)
            level = if(level < 1F) 1F else level
            level = if(level > 25F) 25F else level
            blurLevel = level
        }

        if(a.hasValue(R.styleable.BlurView_blurColorAlpha)){
            blurColorAlpha = a.getInt(R.styleable.BlurView_blurColorAlpha, 90)
        }

        if(a.hasValue(R.styleable.BlurView_blurColor)){
            blurSolidColor = a.getColor(R.styleable.BlurView_blurColor, defStyle)
        }

        if(a.hasValue(R.styleable.BlurView_blurCornerRadius)){
            cornerRadius = a.getFloat(R.styleable.BlurView_blurCornerRadius, 0F)
        }
        a.recycle()
        isInit = false
    }

    var cornerRadius = 0F
        set(value) {
            field = value
            super.setRadius(field)
            upd()

        }

    var blurLevel = 1F
        set(value) {
            field =   if(value > 24F) 24F else  if( value < 1F) 1F else value
            upd()
        }

    var blurSolidColor = Color.BLACK
        set(value) {
            field = value
            paintSolid.color = value
            paintSolid.alpha = blurColorAlpha
            upd()
        }

    var blurStrokeColor = Color.TRANSPARENT
        set(value) {
            field = value
            paintStorke.color = field
            upd()
        }

    var strokeEnabled: Boolean = false
        set(value) {
            field = value
            upd()
        }

    var blurStrokeWidth = 4F
        set(value) {
            field = value
            paintStorke.strokeWidth = field
            upd()
        }

    var blurColorAlpha = 90
        set(value) {
            field = value
            paintSolid.alpha = field
            upd()
        }

    var isInit: Boolean = false

    init {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        setWillNotDraw(false)
        clipChildren = true
        viewTreeObserver.addOnPreDrawListener {
            upd()
            true
        }
    }
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if(w == 0 || h == 0)return
        desRect = RectF(0F,0F, w.toFloat(), h.toFloat() )
        srcRect = Rect(0,0, w , h)
        upd()
    }

    private fun upd(){
        if (isInit || width <= 0 || height <= 0) return
        if(rootView.width == 0 || rootView.height == 0) return
        internalBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val tmpBmp = Bitmap.createBitmap(rootView.width, rootView.height, Bitmap.Config.ARGB_8888)
        val internalCanvas =  Canvas(tmpBmp)
        visibility = View.INVISIBLE
        rootView.draw(internalCanvas)
        visibility = View.VISIBLE
        internalBitmap = crop(tmpBmp)
        blur()
    }
    private fun crop(src: Bitmap): Bitmap?{
        val rect = Rect()
        getGlobalVisibleRect(rect)
        if(rect.right > src.width || rect.bottom > src.height|| width <= 0) return  null
        return Bitmap.createBitmap(src, rect.left, rect.top, rect.width(), rect.height())
    }
    private fun blur(){

        if(internalBitmap == null) return


        blurredBitmap= Bitmap.createBitmap(internalBitmap!!)
        //Only from 1 to 25
        if(blurLevel < 1 || blurLevel > 25){
            invalidate()
            return
        }
        try {


            val rs = RenderScript.create(context)
            val blurr =  ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
            val tmpIn = Allocation.createFromBitmap(rs, internalBitmap)
            val tmpOut = Allocation.createFromBitmap(rs, blurredBitmap)
            blurr.setRadius(blurLevel)
            blurr.setInput(tmpIn)
            blurr.forEach(tmpOut)
            tmpOut.copyTo(blurredBitmap)
        }catch (e: Exception){
            e.printStackTrace()
        }
        invalidate()
    }
    private fun getBitmap(w: Int, h: Int, color: Int = Color.BLACK, rounded: Boolean = false): Bitmap {
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val bitmapCanvas =  Canvas(bitmap)
        val paint =  Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.color = color
        val rect = RectF(0F, 0F,w.toFloat(), h.toFloat())
        if(rounded)
            bitmapCanvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
        else bitmapCanvas.drawRect(rect, paint)
        return bitmap
    }



    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        blurredBitmap?.let {
            val crop = getBitmap(it.width, it.height, Color.BLACK, true)
            canvas?.drawBitmap(crop,0F,0F, paint)
            canvas?.drawBitmap(it, srcRect, desRect!!, paintClear)

            if(blurSolidColor != Color.TRANSPARENT)
                canvas?.drawRoundRect(desRect!!, cornerRadius,cornerRadius, paintSolid)

            if(strokeEnabled)
                (desRect)?.let {dst ->
                    val stroke =  blurStrokeWidth / 2
                    canvas?.drawRoundRect(dst.left + stroke,
                        dst.top + stroke,
                        dst.right - stroke,
                        dst.bottom - stroke,
                        cornerRadius, cornerRadius, paintStorke)
                }

        }
    }



}