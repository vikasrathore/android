package com.cube26.trendingapps.ui;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;

public class ImageNonViewAware implements ImageAware{

    protected final ImageSize imageSize;
    protected final ViewScaleType scaleType;

    public ImageNonViewAware(ImageSize imageSize, ViewScaleType scaleType) {
        this.imageSize = imageSize;
        this.scaleType = scaleType;
    }

    @Override
    public int getWidth() {
        return imageSize.getWidth();
    }

    @Override
    public int getHeight() {
        return imageSize.getHeight();
    }

    @Override
    public ViewScaleType getScaleType() {
        return scaleType;
    }

    @Override
    public View getWrappedView() {
        return null;
    }

    @Override
    public boolean isCollected() {
        return false;
    }

    @Override
    public int getId() {
        return super.hashCode();
    }

    @Override
    public boolean setImageDrawable(Drawable drawable) { // Do nothing
        return true;
    }

    @Override
    public boolean setImageBitmap(Bitmap bitmap) { // Do nothing
        return true;
    }
}
