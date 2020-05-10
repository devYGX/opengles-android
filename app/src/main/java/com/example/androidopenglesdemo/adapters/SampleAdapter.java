package com.example.androidopenglesdemo.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidopenglesdemo.R;
import com.example.androidopenglesdemo.bean.OpenglesSample;
import com.example.androidopenglesdemo.viewholders.SampleHolder;

import java.util.List;

public class SampleAdapter extends RecyclerView.Adapter<SampleHolder> {

    private final List<OpenglesSample> samples;

    public SampleAdapter(List<OpenglesSample> samples) {
        this.samples = samples;
    }

    @NonNull
    @Override
    public SampleHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SampleHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_opengles_sample, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SampleHolder holder, int position) {
        holder.bindHolder(samples.get(position));
    }

    @Override
    public int getItemCount() {
        return samples == null ? 0 : samples.size();
    }
}
