package com.example.androidopenglesdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import com.example.androidopenglesdemo.activities.ContainerActivity;
import com.example.androidopenglesdemo.adapters.SampleAdapter;
import com.example.androidopenglesdemo.bean.OpenglesSample;
import com.example.androidopenglesdemo.fragments.GlRendererShapeFragment;
import com.example.androidopenglesdemo.sample1.OpenglesHelloWorldActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<OpenglesSample> openglesSamples;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        openglesSamples = new ArrayList<>();
        prepareSamples();
        recyclerView.setAdapter(new SampleAdapter(openglesSamples));
    }

    private void prepareSamples() {
        openglesSamples.add(new OpenglesSample(getString(R.string.sample_1_draw_triangle), OpenglesHelloWorldActivity.class));
        Bundle bundle = new Bundle();
        bundle.putString("fragmentClassName", GlRendererShapeFragment.class.getName());
        openglesSamples.add(new OpenglesSample(getString(R.string.sample_2_draw_shape), ContainerActivity.class, bundle));
    }
}
