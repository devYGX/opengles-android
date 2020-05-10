package com.example.androidopenglesdemo.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;

import com.example.androidopenglesdemo.R;

public class ContainerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);

        Bundle bundle = getIntent().getBundleExtra("bundle");
        if (bundle != null) {
            String fragmentClassName = bundle.getString("fragmentClassName");
            Fragment fragment = newFragmentByClassName(fragmentClassName);
            if (fragment != null)
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.frameLayout, fragment)
                        .commit();
        }
    }

    private Fragment newFragmentByClassName(String fragmentClassName) {
        Fragment o = null;
        try {
            Class<?> aClass = Class.forName(fragmentClassName);
            o = (Fragment) aClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return o;
    }
}
