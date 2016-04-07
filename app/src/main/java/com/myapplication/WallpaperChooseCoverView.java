package com.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

/**
 * Created by Administrator on 2016/4/6.
 */
public class WallpaperChooseCoverView extends View{
    private Context mContext = null;
    private Rect mRect;
    private int mRectWidth;
    private int mRectHeight;
    private Point mCenterPt;
    private Bitmap mBitmap;
    private float mRectXYRatio;

    private int mMaxWidth;
    private int mMaxHeight;
    private int mMinWidth;
    private int mMinHeight;
    private int mLeft;
    private int mRight;
    private int mTop;
    private int mBottom;

    private Paint mPaint;
    private Paint mPaintCrop;
    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;

    private boolean mShowCropRect = false;

    private static final int CROP_COLOF = 0xffffb24d;
    private static final int CROP_STROKE_WIDTH = 2;
    private static final int RECT_MARGIN = 3;
    private static final long TIME_LONG_PRESS = 5000;

    public WallpaperChooseCoverView(Context context) {
        this(context, null);
    }

    public WallpaperChooseCoverView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WallpaperChooseCoverView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;

        mPaint = new Paint();
        mPaint.setFilterBitmap(true);

        mPaintCrop = new Paint();
        mPaintCrop.setStyle(Paint.Style.STROKE);
        mPaintCrop.setColor(CROP_COLOF);
        mPaintCrop.setStrokeWidth(CROP_STROKE_WIDTH);

        mCenterPt = new Point();
        mRect = new Rect();

        mGestureDetector = new GestureDetector(mContext, new GestureListener());
        mScaleGestureDetector = new ScaleGestureDetector(mContext, new GestureListener());
    }

    public void initRect(int wallWidth, int wallHeight, int imageViewWidth, int imageViewHeight) {
        mMaxWidth = imageViewWidth - 2 * RECT_MARGIN;
        mMaxHeight = imageViewHeight - 2 * RECT_MARGIN;

        mLeft = RECT_MARGIN;
        mRight = mMaxWidth + RECT_MARGIN;
        mTop = RECT_MARGIN;
        mBottom = mMaxHeight + RECT_MARGIN;

        mRectXYRatio = (float)wallWidth / (float)wallHeight;

        mCenterPt.x = imageViewWidth / 2;
        mCenterPt.y = imageViewHeight / 2;

        float scale = getInitScale(wallWidth, wallHeight, mMaxWidth, mMaxHeight);
        mRectWidth = (int)(wallWidth * scale);
        mRectHeight = (int)(mRectWidth / mRectXYRatio);

        mMinWidth = mRectWidth / 2;
        mMinHeight = mRectHeight / 2;

        resetRect();
        invalidate();
    }

    private float getInitScale(int wallWidth, int wallHeight, int maxWidth, int maxHeight) {
        float scale = Math.min((float) maxWidth / (float) wallWidth, (float) maxHeight / (float) wallHeight);
        return scale > 1.0f ? 1.0f : scale;
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    private Rect resetRect() {
        mRect.set(mCenterPt.x - mRectWidth / 2, mCenterPt.y - mRectHeight / 2, mCenterPt.x + mRectWidth / 2, mCenterPt.y + mRectHeight / 2);
        return mRect;
    }

    private boolean scaleRect(float scale) {
        if (scale == 1.0f) {
            return false;
        }
        int nTempWidth = (int)(mRectWidth * scale);
        int nTempHeight = (int)(nTempWidth / mRectXYRatio);

        if (nTempWidth > mMaxWidth || nTempHeight > mMaxHeight
                || nTempWidth < mMinWidth || nTempHeight < mMinHeight) {
            return false;
        }

        mRectWidth = nTempWidth;
        mRectHeight = nTempHeight;
        return true;
    }

    private void resetCenter(float distanceX, float distanceY) {
        mCenterPt.x -= distanceX;
        mCenterPt.y -= distanceY;

        if (mCenterPt.x - mRectWidth / 2 < mLeft) {
            mCenterPt.x = mRectWidth / 2 + mLeft;
        }
        if (mCenterPt.x + mRectWidth / 2 > mRight) {
            mCenterPt.x = mRight - mRectWidth / 2;
        }
        if (mCenterPt.y - mRectHeight / 2 < mTop) {
            mCenterPt.y = mRectHeight / 2 + mTop;
        }
        if (mCenterPt.y + mRectHeight / 2 > mBottom) {
            mCenterPt.y = mBottom - mRectHeight / 2;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (null == mBitmap || mBitmap.isRecycled()) {
            return;
        }

        canvas.drawBitmap(mBitmap, mRect, mRect, mPaint);

        if (mShowCropRect) {
            canvas.drawRect(mRect, mPaintCrop);
        }
    }

    private class GestureListener implements GestureDetector.OnGestureListener, ScaleGestureDetector.OnScaleGestureListener {
        public boolean onDown(MotionEvent e) {
            if (!isInRect(e.getX(), e.getY())) {
                return false;
            }
            return true;
        }

        public void onShowPress(MotionEvent e) {
        }

        public boolean onSingleTapUp(MotionEvent e) {
            return true;
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            showCropRect(false);
            resetCenter(distanceX, distanceY);
            resetRect();
            invalidate();
            return true;
        }

        public void onLongPress(MotionEvent e) {
            if (!isInRect(e.getX(), e.getY())) {
                return;
            }
            long time = e.getEventTime();
            if (time < TIME_LONG_PRESS) {
                return;
            }
            showCropRect(true);
            invalidate();
            setWallPaper();
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            return true;
        }

        public boolean onScale(ScaleGestureDetector detector) {
            showCropRect(false);
            float scale = detector.getScaleFactor();
            if (!scaleRect(scale)) {
                return true;
            }
            resetCenter(0, 0);
            resetRect();
            return true;
        }

        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        public void onScaleEnd(ScaleGestureDetector detector) {
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        mScaleGestureDetector.onTouchEvent(event);
        return true;
    }

    private boolean isInRect(float x, float y) {
        if (x >= mRect.left && x <= mRect.right && y >= mRect.top && y <= mRect.bottom) {
            return true;
        } else {
            return false;
        }
    }

    private void showCropRect(boolean show) {
        mShowCropRect = show;
    }

    private void setWallPaper() {
        Bitmap bitmap = Bitmap.createBitmap(mBitmap, mRect.left, mRect.top, mRectWidth, mRectHeight);
        Wallpaper.changeWall(bitmap, (WallpaperActivity)mContext);
    }
}
