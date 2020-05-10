package com.example.androidopenglesdemo.sample1;

import android.content.Context;
import android.opengl.GLSurfaceView;

public abstract class ShapeRender implements GLSurfaceView.Renderer {

    private final Context context;

    public ShapeRender(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

}
