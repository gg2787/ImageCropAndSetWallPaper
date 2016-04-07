package com.myapplication;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.widget.Toast;
import java.io.IOException;

/**
 * Created by Administrator on 2016/3/25.
 */
public class Wallpaper {
    public static void changeWall(final Bitmap bitmapSrc, final Activity activity) {
        if (null == bitmapSrc || bitmapSrc.isRecycled()) {
            return;
        }
        final Bitmap bitmap = imageScaleAndCrop(activity, bitmapSrc, getMinWallpaperAttr(activity)[0], getMinWallpaperAttr(activity)[1]);
        if (null == bitmap) {
            return;
        }

        new Thread(new Runnable() {
            public void run() {
                setWallpaper(activity.getApplicationContext(), bitmap);
                bitmap.recycle();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, activity.getString(R.string.wallpaper_set_success), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
    }

    private static void setWallpaper (Context context, Bitmap bitmap) {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);

        try {
            wallpaperManager.setBitmap(bitmap);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static Bitmap imageScaleAndCrop(Activity activity, Bitmap bitmapSrc, int dstWidth, int dstHeight) {
        if (null == activity || null == bitmapSrc || bitmapSrc.isRecycled()) {
            return null;
        }

        int srcWidth = bitmapSrc.getWidth();
        int srcHeight = bitmapSrc.getHeight();

        if (srcWidth == dstWidth && srcHeight == dstHeight) {
            return bitmapSrc;
        }

        int centerX = srcWidth / 2;
        int centerY = srcHeight / 2;
        float fScale = Math.max((float) dstWidth / (float) srcWidth, (float) dstHeight / (float) srcHeight);

        int srcX = (int)(Math.min(srcWidth, dstWidth / fScale));
        int srcY = (int)(Math.min(srcHeight, dstHeight / fScale));
        int srcStartX = getStart(centerX, srcWidth, srcX);
        int srcStartY = getStart(centerY, srcHeight, srcY);

        Bitmap bitmapDst = bitmapScaleAndCrop(bitmapSrc, srcX, srcY,
                srcStartX, srcStartY, dstWidth, dstHeight, bitmapSrc.getConfig());

        return bitmapDst;
    }

    private static int getStart(int center, int src, int dst) {
        int start = center - dst / 2 - 1;
        int end = center + dst / 2 + 1;
        if (end > src) {
            start = src - dst;
        }
        if (start < 0) {
            start = 0;
        }
        return start;
    }

    public static int[] getMinWallpaperAttr(Context context) {
        if (null == context) {
            return null;
        }
        int nWallWidth, nWallHeight;

        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        if (wallpaperDrawable != null) {
            nWallWidth = wallpaperDrawable.getMinimumWidth();
            nWallHeight = wallpaperDrawable.getMinimumHeight();
        } else {
            nWallWidth = wallpaperManager.getDesiredMinimumWidth();
            nWallHeight = wallpaperManager.getDesiredMinimumHeight();
        }

        int[] wallSize = new int[2];
        wallSize[0] = nWallWidth;
        wallSize[1] = nWallHeight;

        return wallSize;
    }

    private static Bitmap bitmapScaleAndCrop(Bitmap bitmapSrc, int srcWidth, int srcHeight, int srcOffsetX, int srcOffsetY, int dstWidth, int dstHeight, Bitmap.Config config) {
        if (null == bitmapSrc || bitmapSrc.isRecycled()) {
            return null;
        }
        try {
            Bitmap bitmapDst = Bitmap.createBitmap(dstWidth, dstHeight, config);
            Rect rectDst = new Rect(0, 0, dstWidth, dstHeight);
            Rect rectSrc = new Rect(srcOffsetX, srcOffsetY, Math.min(srcOffsetX + srcWidth, bitmapSrc.getWidth()), Math.min(srcHeight + srcOffsetY, bitmapSrc.getHeight()));

            Canvas canvas = new Canvas(bitmapDst);
            Paint p = new Paint();
            p.setFilterBitmap(true);
            canvas.drawBitmap(bitmapSrc, rectSrc, rectDst, p);
            canvas = null;
            return bitmapDst;
        } catch (OutOfMemoryError e) {
            System.gc();
            return null;
        }
    }

    public static Bitmap loadBitmap(Context context, int id) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = 1;
        opts.inScaled = false;
        return BitmapFactory.decodeResource(context.getResources(), id, opts);
    }

}
