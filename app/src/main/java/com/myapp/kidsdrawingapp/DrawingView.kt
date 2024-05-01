package com.myapp.kidsdrawingapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

//@Suppress("UNREACHABLE_CODE")
class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {


//CustomPath is the content you want to draw.
    private var mDrawPath : CustomPath? = null
    //CustomPath: Represents what you want to draw. It can be a sequence of lines, curves, or other shapes. It's essentially the content or design you want to put on your canvas.

    //Bitmap is where you want to draw it.
    private var mBitmap : Bitmap? = null
    //Bitmap: Provides the space where you can draw. It's like the canvas or paper where your drawing (specified by the CustomPath and drawn with the Paint) will appear.

    //Paint is the way you want to draw it.
    private var mDrawPaint: Paint? = null
    //Paint: Specifies how you want to draw. It includes properties like color, line thickness, and style. It's like choosing the artistic tools and styles you want to use to create your drawing.

    private var mCanvasPaint : Paint? = null
    private var mBrushSize : Float = 0.toFloat()
    private var mColor = Color.BLACK
    private var mCanvas : Canvas? = null
//    Bitmap is the canvas, providing a space to draw (like a piece of paper).
//Canvas is the tool, allowing you to perform drawing operations on that canvas (like an artist's hand with a pencil).

    private val mPaths = ArrayList<CustomPath>()
    private val mUndoPaths = ArrayList<CustomPath>()


    init {
        setUpDrawing()
    }

    private fun setUpDrawing() {
        mDrawPaint = Paint()
        mDrawPath = CustomPath(mColor, mBrushSize)
        mDrawPaint!!.color = mColor
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND // So, these are for the corners
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND // And these are for the end points.
        mCanvasPaint = Paint(Paint.DITHER_FLAG) // Dithering is a technique that helps smooth color transitions on your digital canvas.
//        mBrushSize = 20.toFloat()
    }

    fun onUndoClicked() {

        if(mPaths.isNotEmpty()) {
            mUndoPaths.add(mPaths.removeAt(mPaths.size - 1))
            invalidate()
        }
    }

    fun onRedoClicked() {

        if(mUndoPaths.isNotEmpty()) {
            val redoPath = mUndoPaths.removeAt(mUndoPaths.size-1)
            mPaths.add(redoPath)
            invalidate()
        }

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap!!)
//        Now, you have a fresh canvas (mCanvas) that matches the size of your custom view, and any drawings you make on this canvas will be reflected on the Bitmap.
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawBitmap(mBitmap!!, 0f, 0f, mDrawPaint)
//        The first part (canvas.drawBitmap(...)) draws the background, which is the content of the bitmap. Think of it as putting a picture or a pattern on your drawing board.

        for(path in mPaths) {
            mDrawPaint!!.color = path.color
            mDrawPaint!!.strokeWidth = path.brushThickness
            canvas.drawPath(path, mDrawPaint!!)
        }

        if(!mDrawPath!!.isEmpty) {

            mDrawPaint!!.color = mDrawPath!!.color
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness
            canvas.drawPath(mDrawPath!!, mDrawPaint!!)
//            If there is, it draws the path on top of the background. Imagine drawing lines or shapes on top of the picture you placed on the drawing board.
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {


        val touchX = event?.x
        val touchY = event?.y

        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {

                Log.d("DrawingView", "onTouchEvent: Action - ${event?.action}")
                Log.d("DrawingView", "Number of Paths: ${mPaths.size}")
                mDrawPath!!.color = mColor
                mDrawPath!!.brushThickness = mBrushSize

                mDrawPath!!.reset()
                mDrawPath!!.moveTo(touchX!!, touchY!!)
            }
            MotionEvent.ACTION_MOVE -> {
                mDrawPath?.lineTo(touchX!!, touchY!!)
            }
            MotionEvent.ACTION_UP -> {
                mPaths.add(mDrawPath!!)
                mDrawPath = CustomPath(mColor, mBrushSize)
            }
            else -> return false
        }

        invalidate()
        return true
    }

    fun setBrushSize(newSize : Float) {
//        TypedValue.applyDimension is a method used to convert a size value from a device-independent unit (like dp or sp) to actual pixels. This ensures that the brush size looks consistent across different devices with varying screen densities.
        mBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, // TypedValue.COMPLEX_UNIT_DIP specifies that the size value (newSize) is in device-independent pixels (dp).
            newSize,
            resources.displayMetrics)  // resources.displayMetrics provides information about the device's display, including its density.

        mDrawPath!!.brushThickness = mBrushSize
    }

    fun setBrushColor(newColor: String) {
        mColor = Color.parseColor(newColor)
        mDrawPaint!!.color = mColor
    }

    internal inner class CustomPath(var color: Int,
                                    var brushThickness: Float) : Path(){

//  Now, the code you provided is creating a custom class called CustomPath that extends the built-in Path class. This custom class adds extra features by including properties for color and brush thickness. So, in addition to drawing shapes, you can also specify the color and thickness of the lines you're drawing.
//
//  In summary, a Path is like a set of drawing instructions, and the CustomPath class you provided extends that idea by allowing you to customize the appearance of your drawings by specifying color and brush thickness.

    }

}