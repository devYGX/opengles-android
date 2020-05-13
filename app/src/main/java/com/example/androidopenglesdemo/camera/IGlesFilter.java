package com.example.androidopenglesdemo.camera;

import android.content.Context;

import com.example.androidopenglesdemo.glenv.GlesRenderer;

public abstract class IGlesFilter extends GlesRenderer implements IOutputFilter {

    private int texture;
    private int width;
    private int height;

    public IGlesFilter(Context context) {
        super(context, 0, 0);
    }

    public final void setFilterSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public final void setInputTexture(int texture, int width, int height) {
        this.texture = texture;
        setFilterSize(width, height);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public int getInputTexture() {
        return texture;
    }
}
