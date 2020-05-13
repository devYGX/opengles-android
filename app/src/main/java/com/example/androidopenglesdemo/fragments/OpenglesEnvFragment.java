package com.example.androidopenglesdemo.fragments;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.androidopenglesdemo.R;
import com.example.androidopenglesdemo.glenv.OpenglesEnv;
import com.example.androidopenglesdemo.sample3.TriangleRenderer;

public class OpenglesEnvFragment extends Fragment implements TextureView.SurfaceTextureListener, SurfaceHolder.Callback {

    private TextureView mTextureView;
    private OpenglesEnv openglesEnv;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_opengles_env, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTextureView = view.findViewById(R.id.textureView);
        mTextureView.setSurfaceTextureListener(this);
       //  mTextureView.getHolder().addCallback(this);
        openglesEnv = new OpenglesEnv();
        openglesEnv.initEnv();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        openglesEnv.setDisplaySurface(surface, width, height);
        openglesEnv.setRenderer(new TriangleRenderer(getContext(), width, height));
        openglesEnv.requestRender();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        openglesEnv.setDisplaySurface(surface, width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        openglesEnv.destoryEnv();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        openglesEnv.setDisplaySurface(holder, width, height);
        openglesEnv.setRenderer(new TriangleRenderer(getContext(),mTextureView.getWidth(), mTextureView.getHeight()));
        openglesEnv.requestRender();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
