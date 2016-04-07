package com.myapplication;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Administrator on 2016/4/6.
 */
public class WallpaperActivity extends Activity{
    private ImageView mWallpaper;
    private WallpaperChooseCoverView mCoverImage;
    private View mCoverView;
    private Bitmap mBitmap;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallpaper_layout);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBitmap.recycle();
    }

    private void initView() {
        mWallpaper = (ImageView)findViewById(R.id.iv_wallpaper);
        mCoverView = findViewById(R.id.iv_cover);
        mCoverImage = (WallpaperChooseCoverView)findViewById(R.id.view_cover);

        mWallpaper.post(new Runnable() {
            //这里才能获取到mWallpaper的宽高
            @Override
            public void run() {
                int width = mWallpaper.getWidth();
                int height = mWallpaper.getHeight();
                loadBitmap(width, height);
                mWallpaper.setImageBitmap(mBitmap);
                showCropCover(width, height);
            }
        });
    }

    private void loadBitmap(int width, int height) {
        final Bitmap bitmap = Wallpaper.loadBitmap(getApplicationContext(), R.drawable.wall);
        mBitmap = Wallpaper.imageScaleAndCrop(WallpaperActivity.this, bitmap, width, height);
        if (mBitmap != bitmap) {
            bitmap.recycle();
        }
    }

    private void showCropCover(int viewWidth, int viewHeight) {
        mCoverView.setVisibility(View.VISIBLE);
        mCoverImage.setVisibility(View.VISIBLE);
        mCoverImage.setBitmap(mBitmap);

        int wallWidth = Wallpaper.getMinWallpaperAttr(this)[0];
        int wallHeight = Wallpaper.getMinWallpaperAttr(this)[1];

        mCoverImage.initRect(wallWidth, wallHeight, viewWidth, viewHeight);
    }
}

