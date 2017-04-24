package ru.dimasokol.learning.painting;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import static android.content.ContentValues.TAG;

/**
 * @author Дмитрий Соколов <DPSokolov.SBT@sberbank.ru>
 */

public class PaintingView extends View {

    public static final int LINE = 1;
    public static final int RECTANGLE = 2;
    public static final int CIRCLE = 3;


    public int type=1;

    private Bitmap mBitmap;
    private Canvas mBitmapCanvas;


    private Paint[] mPredefinedPaints;
    private int mNextPaint = 0;
    boolean isPrint;
    private Paint mEditModePaint = new Paint();

    private SparseArray<PointF> mLastPoints = new SparseArray<>(10);
    private SparseArray<Paint> mPaints = new SparseArray<>(10);

    float x,y,lastx,lasty;
    private Paint MainPaint;

    public PaintingView(Context context) {
        super(context);
        init();
    }

    public PaintingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PaintingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PaintingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        if (getRootView().isInEditMode()) {
            mEditModePaint.setColor(Color.MAGENTA);
        } else {
            TypedArray ta = getResources().obtainTypedArray(R.array.paint_colors);
            mPredefinedPaints = new Paint[ta.length()];

            for (int i = 0; i < ta.length(); i++) {
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setColor(ta.getColor(i, 0));
                paint.setStrokeCap(Paint.Cap.ROUND);
                paint.setStrokeJoin(Paint.Join.ROUND);
                paint.setStrokeWidth(getResources().getDimension(R.dimen.default_paint_width));
                mPredefinedPaints[i] = paint;
            }
        }


    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (w > 0 && h > 0) {
            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            if (mBitmap != null) {
                canvas.drawBitmap(mBitmap, 0, 0, null);
                mBitmap.recycle();
            }

            mBitmap = bitmap;
            mBitmapCanvas = canvas;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                int pointerId = event.getPointerId(event.getActionIndex());
                mLastPoints.put(pointerId, new PointF(event.getX(event.getActionIndex()), event.getY(event.getActionIndex())));
                MainPaint=mPredefinedPaints[mNextPaint % mPredefinedPaints.length];
                mNextPaint++;
                if (type ==1) {
                    x = event.getX(event.getActionIndex());
                    y =  event.getY(event.getActionIndex());
                }
                return true;


            case MotionEvent.ACTION_MOVE:
                for (int i = 0; i < event.getPointerCount(); i++) {
                    PointF last = mLastPoints.get(event.getPointerId(i));

                    if (last != null) {
                        isPrint=true;

                        if (type == 1) {
                            lastx = x;
                            lasty = y;
                            x = event.getX(i);
                            y = event.getY(i);
                            drawLine(mBitmapCanvas);
                        }
                        if (type ==2) {
                            x = event.getX(i);
                            y = event.getY(i);
                            lastx = last.x;
                            lasty = last.y;
                        }
                        if (type ==3) {
                            x = event.getX(i);
                            y = event.getY(i);
                            lastx = last.x;
                            lasty = last.y;
                        }

                    }
                }

                invalidate();
                return true;


            case MotionEvent.ACTION_POINTER_UP:
                return true;

            case MotionEvent.ACTION_UP:
                for (int i = 0; i < event.getPointerCount(); i++) {
                    PointF last = mLastPoints.get(event.getPointerId(i));

                    if (last != null) {
                        isPrint=false;
                        switch (type){
                            case RECTANGLE:
                                drawRect(mBitmapCanvas);
                                break;
                            case CIRCLE:
                                drawCircle(mBitmapCanvas);
                        }

                    }
                }
                invalidate();
                mLastPoints.clear();
                return true;

        }

        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(mBitmap, 0, 0, null);

        if (isPrint) {
            switch (type){
//                case LINE:
//                    drawLine(canvas);
//                    break;
                case RECTANGLE:
                    drawRect(canvas);
                    break;
                case CIRCLE:
                    drawCircle(canvas);
            }

        }




//        canvas.drawBitmap(tmp,0, 0, null);
//        canvas.drawBitmap(mBitmap, 0, 0, null);
    }

    private void drawRect(Canvas canvas) {

        canvas.drawRect(lastx, lasty, x, y, MainPaint);
    }

    private void drawLine (Canvas canvas){
        canvas.drawLine(lastx, lasty, x, y, MainPaint);

    }

    private void drawCircle (Canvas canvas) {

        canvas.drawCircle(lastx, lasty, getRadius(lastx, lasty, x, y), MainPaint);
    }
    private float getRadius(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt( Math.pow(x1 - x2, 2) +  Math.pow(y1 - y2, 2));
    }

    /**
     * Очищает нарисованное
     */
    public void clear() {
        mBitmapCanvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR);
        invalidate();
    }
}
