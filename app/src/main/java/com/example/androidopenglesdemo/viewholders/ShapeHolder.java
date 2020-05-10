package com.example.androidopenglesdemo.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidopenglesdemo.R;
import com.example.androidopenglesdemo.glrendershape.ShapeRender;

public class ShapeHolder extends RecyclerView.ViewHolder {

    private final View itemView;
    private TextView tvTitle;

    public ShapeHolder(@NonNull View itemView) {
        super(itemView);
        this.itemView = itemView;
        onViewCreated(itemView);
    }

    private void onViewCreated(View root) {
        tvTitle = root.findViewById(R.id.tvTitle);
    }

    public View getItemView() {
        return itemView;
    }

    public void bindHolder(ShapeRender render) {
        tvTitle.setText(render.getClass().getSimpleName());
    }
}
