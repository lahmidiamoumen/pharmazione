package com.moumen.pharmazione.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.moumen.pharmazione.databinding.ShowFragmentPreviewItemLayoutBinding;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.api.widget.Widget;

import java.util.ArrayList;
import java.util.List;

public class ShowFragmentImagesAdapter extends RecyclerView.Adapter<ImagesViewHolder>{
    List<String> path;
    Context c;

    public ShowFragmentImagesAdapter(Context c) {
        this.c = c;
    }

    @NonNull
    @Override
    public ImagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ShowFragmentPreviewItemLayoutBinding ds = ShowFragmentPreviewItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ImagesViewHolder(ds, c);
    }

    @Override
    public void onBindViewHolder(@NonNull ImagesViewHolder holder, int position) {
        holder.onBind(path, position);
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
    Context c;

    public ImagesViewHolder(ShowFragmentPreviewItemLayoutBinding ds, Context c) {
        super(ds.getRoot());
        this.c = c;
        binding = ds;
    }
    void onBind(List<String> holder, int position){
        binding.setEmailAttachment(holder.get(position));
        if(c != null){// from Show Fragment
            binding.attachmentImageView.setOnClickListener(l -> previewImage(position, holder));
        }
    }
    private void previewImage(int position, List<String> mAlbumFiles) {
        Album.gallery(c)
                .checkedList(new ArrayList<>(mAlbumFiles))
                .currentPosition(position)
                .widget(
                        Widget.newDarkBuilder(c)
                                .title("")
                                .build()
                ).start();
    }
}