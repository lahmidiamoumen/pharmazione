package com.example.pharmazione.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pharmazione.databinding.ShowFragmentPreviewItemLayoutBinding;

import java.util.List;

public class ShowFragmentImagesAdapter extends RecyclerView.Adapter<ImagesViewHolder>{
    List<String> path;
    @NonNull
    @Override
    public ImagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ShowFragmentPreviewItemLayoutBinding ds = ShowFragmentPreviewItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ImagesViewHolder(ds);
    }

    @Override
    public void onBindViewHolder(@NonNull ImagesViewHolder holder, int position) {
        holder.onBind(path.get(position));
    }

    @Override
    public int getItemCount() {
        return path == null ? 0 : path.size();
    }

    void submitList(List<String> list) {
        path = list;
        notifyDataSetChanged();
    }
}

class ImagesViewHolder extends RecyclerView.ViewHolder{
    ShowFragmentPreviewItemLayoutBinding binding;

    public ImagesViewHolder(ShowFragmentPreviewItemLayoutBinding ds) {
        super(ds.getRoot());
        binding = ds;
    }
    void onBind(String g){
        binding.setEmailAttachment(g);
    }
}