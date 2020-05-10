package com.example.androidopenglesdemo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidopenglesdemo.R;
import com.example.androidopenglesdemo.glrendershape.ShapeRender;
import com.example.androidopenglesdemo.viewholders.ShapeHolder;

import java.util.List;

public class ShapeAdapter extends RecyclerView.Adapter<ShapeHolder> {

    private final List<ShapeRender> renders;
    private OnItemClickListener onItemClickListener;

    public ShapeAdapter(List<ShapeRender> renders) {
        this.renders = renders;
    }

    @NonNull
    @Override
    public ShapeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ShapeHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_glrender_shape, null, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ShapeHolder holder, int position) {
        holder.bindHolder(renders.get(position));
        View itemView = holder.getItemView();
        itemView.setTag(position);
        itemView.setOnClickListener(mOnItemClickListener);
    }

    private View.OnClickListener mOnItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            OnItemClickListener onItemClickListener = ShapeAdapter.this.onItemClickListener;
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(v, (Integer) v.getTag());
            }
        }
    };



    @Override
    public int getItemCount() {
        return renders == null ? 0 : renders.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener{
        void onItemClick(View itemView, int position);
    }
}
