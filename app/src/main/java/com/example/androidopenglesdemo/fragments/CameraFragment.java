package com.example.androidopenglesdemo.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.androidopenglesdemo.R;
import com.example.androidopenglesdemo.camera.CameraDisplayWindow;
import com.example.androidopenglesdemo.camera.CameraManager;
import com.example.androidopenglesdemo.camera.IFrameExportCallback;
import com.example.androidopenglesdemo.camera.PreviewCallback;

import java.nio.ByteBuffer;

public class CameraFragment extends Fragment implements TextureView.SurfaceTextureListener, View.OnClickListener, IFrameExportCallback {
    private static final String TAG = "CameraFragment";
    private TextureView mTextureView;
    private ImageView imageView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTextureView = view.findViewById(R.id.textureView);
        view.findViewById(R.id.btnTakePic)
                .setOnClickListener(this);
        imageView = view.findViewById(R.id.imageView);

        mTextureView.setSurfaceTextureListener(this);
        CameraManager manager = CameraManager.getManager();
        manager.init(getContext());
        manager.start();
        manager.registerPreviewCallback(mPreviewCallback);
        manager.registerFrameExportCallback(this);
    }

    private PreviewCallback mPreviewCallback = new PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] buf, int width, int height, int degree, int cameraId) {
            // Log.d(TAG, "onPreviewFrame: ");
        }
    };

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        CameraManager.getManager().setDisplay(new CameraDisplayWindow(surface, width, height));
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
       CameraManager.getManager().setDisplay(new CameraDisplayWindow(surface, width, height));
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        CameraManager.getManager().setDisplay(null);
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CameraManager manager = CameraManager.getManager();
        manager.unregisterFrameExportCallback(this);
        manager.unregisterPreviewCallback(mPreviewCallback);
        CameraManager.getManager().release();
    }

    @Override
    public void onClick(View v) {
        CameraManager.getManager()
                .takePicture(new IFrameExportCallback() {
                    @Override
                    public void onFrameExport(ByteBuffer byteBuffer, int width, int height) {
                        final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                        try {
                            bitmap.copyPixelsFromBuffer(byteBuffer);
                            imageView.post(new Runnable() {
                                @Override
                                public void run() {
                                    imageView.setImageBitmap(bitmap);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private Bitmap bmp;

    @Override
    public void onFrameExport(ByteBuffer byteBuffer, int width, int height) {

        if (bmp == null) {
            bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }
        bmp.copyPixelsFromBuffer(byteBuffer);
        imageView.post(new Runnable() {
            @Override
            public void run() {
                imageView.setImageBitmap(bmp);
            }
        });
    }
}
