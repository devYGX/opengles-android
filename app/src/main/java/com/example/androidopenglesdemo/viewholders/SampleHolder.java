package com.example.androidopenglesdemo.viewholders;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidopenglesdemo.R;
import com.example.androidopenglesdemo.bean.OpenglesSample;

public class SampleHolder extends RecyclerView.ViewHolder {

    private TextView tvTitle;
    private OpenglesSample sample;

    public SampleHolder(@NonNull View itemView) {
        super(itemView);
        onViewCreated(itemView);
    }

    private void onViewCreated(View view){
        tvTitle = view.findViewById(R.id.tvTitle);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenglesSample sample = SampleHolder.this.sample;
                if (sample == null) {
                    return;
                }
                Class<? extends Activity> targetActivity = sample.getTargetActivity();
                Context context = v.getContext();
                context.startActivity(new Intent(context, targetActivity));
            }
        });
    }

    public void bindHolder(OpenglesSample sample){
        this.sample = sample;
        tvTitle.setText(sample.getTitle());
    }
}
