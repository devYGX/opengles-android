package com.example.androidopenglesdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import com.example.androidopenglesdemo.activities.ContainerActivity;
import com.example.androidopenglesdemo.adapters.SampleAdapter;
import com.example.androidopenglesdemo.bean.OpenglesSample;
import com.example.androidopenglesdemo.fragments.CameraFragment;
import com.example.androidopenglesdemo.fragments.GlRendererShapeFragment;
import com.example.androidopenglesdemo.fragments.OpenglesEnvFragment;
import com.example.androidopenglesdemo.sample1.OpenglesHelloWorldActivity;
import com.example.androidopenglesdemo.utils.IAction;
import com.example.androidopenglesdemo.utils.PermissionUtils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<OpenglesSample> openglesSamples;

    String[] PERMISSIONS = new String[]{
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        openglesSamples = new ArrayList<>();
        prepareSamples();
        recyclerView.setAdapter(new SampleAdapter(openglesSamples));

        requestPerm();
    }

    private void requestPerm() {
        int checkSelfPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if(checkSelfPermission == PackageManager.PERMISSION_GRANTED){
            return;
        }
        ActivityCompat.requestPermissions(this,PERMISSIONS,100);
    }

    private void prepareSamples() {
        openglesSamples.add(new OpenglesSample(getString(R.string.sample_1_draw_triangle), OpenglesHelloWorldActivity.class));
        Bundle bundle = new Bundle();
        bundle.putString("fragmentClassName", GlRendererShapeFragment.class.getName());
        openglesSamples.add(new OpenglesSample(getString(R.string.sample_2_draw_shape), ContainerActivity.class, bundle));
        bundle = new Bundle();
        bundle.putString("fragmentClassName", OpenglesEnvFragment.class.getName());
        openglesSamples.add(new OpenglesSample(getString(R.string.sample_3_GlSurfaceView), ContainerActivity.class, bundle));
        bundle = new Bundle();
        bundle.putString("fragmentClassName", CameraFragment.class.getName());
        openglesSamples.add(new OpenglesSample(getString(R.string.sample_4_CameraOpengles), ContainerActivity.class, bundle));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
