package com.example.androidopenglesdemo.camera;

import android.content.Context;

import com.example.androidopenglesdemo.camera.impl.CameraImpl;

public class CameraFactory {

    public static ICamera newCamera(Context context){
        return new CameraImpl(context);
    }
}
