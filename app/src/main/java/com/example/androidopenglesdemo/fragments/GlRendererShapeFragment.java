package com.example.androidopenglesdemo.fragments;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidopenglesdemo.R;
import com.example.androidopenglesdemo.adapters.ShapeAdapter;
import com.example.androidopenglesdemo.glrendershape.ShapeRender;
import com.example.androidopenglesdemo.glrendershape.impls.Circle;
import com.example.androidopenglesdemo.glrendershape.impls.MatrixTriangle;
import com.example.androidopenglesdemo.glrendershape.impls.Rectangle;
import com.example.androidopenglesdemo.glrendershape.impls.Sector;
import com.example.androidopenglesdemo.glrendershape.impls.ShapeContainer;
import com.example.androidopenglesdemo.glrendershape.impls.Trapezoid;

import java.util.ArrayList;
import java.util.List;

public class GlRendererShapeFragment extends Fragment {

    private GLSurfaceView glSurfaceView;
    private RecyclerView recyclerView;
    private List<ShapeRender> shapes;
    private ShapeAdapter adapter;
    private ShapeContainer containerRenderer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_glrenderer_shape, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        glSurfaceView = view.findViewById(R.id.glSurfaceView);
        recyclerView = view.findViewById(R.id.recyclerView);

        glSurfaceView.setEGLContextClientVersion(2);
        containerRenderer = new ShapeContainer(getContext());
        glSurfaceView.setRenderer(containerRenderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);


        shapes = new ArrayList<>();
        prepareShapes();
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        adapter = new ShapeAdapter(shapes);
        adapter.setOnItemClickListener(new ShapeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                ShapeRender shapeRender = shapes.get(position);
                containerRenderer.setRenderer(shapeRender);
                glSurfaceView.requestRender();
            }
        });
        recyclerView.setAdapter(adapter);

        if (shapes.size() > 0) {
            ShapeRender shapeRender = shapes.get(0);
            containerRenderer.setRenderer(shapeRender);
            glSurfaceView.requestRender();
        }
    }

    private void prepareShapes() {
        shapes.clear();
        shapes.add(new MatrixTriangle(getContext()));
        shapes.add(new Circle(getContext()));
        shapes.add(new Rectangle(getContext()));
        shapes.add(new Sector(getContext()));
        shapes.add(new Trapezoid(getContext()));
    }
}
