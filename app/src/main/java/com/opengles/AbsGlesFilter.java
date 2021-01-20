package com.opengles;

import android.content.Context;

public abstract class AbsGlesFilter implements GlesFilter {

    private final Context context;
    private int width;
    private int height;
    private int inputTexture;

    public AbsGlesFilter(Context context){
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public void onFilterCreated(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public void onFilterSizeChanged(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void setInputTexture(int inputTexture) {
        this.inputTexture = inputTexture;
    }

    protected int getInputTexture() {
        return inputTexture;
    }
}
