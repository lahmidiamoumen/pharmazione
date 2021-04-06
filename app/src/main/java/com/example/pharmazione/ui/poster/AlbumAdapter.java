/*
 * Copyright 2016 Yan Zhenjie.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.pharmazione.ui.poster;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pharmazione.R;
import com.example.pharmazione.utils.ClickListener;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumFile;
import com.yanzhenjie.album.impl.OnItemClickListener;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * <p>Image adapter.</p>
 * Created by Yan Zhenjie on 2016/10/30.
 */
public class AlbumAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private LayoutInflater mInflater;
    private OnItemClickListener mItemClickListener;
    private ClickListener itemClickListener;
    private int buttonEnd = 0;

    private List<AlbumFile> mAlbumFiles;

    public AlbumAdapter(Context context, ClickListener itemClickListener1, OnItemClickListener itemClickListener, int bool) {
        this.mInflater = LayoutInflater.from(context);
        this.mItemClickListener = itemClickListener;
        this.itemClickListener = itemClickListener1;
        buttonEnd = bool;
    }

    void notifyDataSetChanged(List<AlbumFile> imagePathList) {
        this.mAlbumFiles = imagePathList;
        super.notifyDataSetChanged();
    }

    void clear(List<AlbumFile> imagePathList){
        this.mAlbumFiles = imagePathList;
        notifyItemRangeRemoved(0, imagePathList.size());
    }



    @Override
    public int getItemViewType(int position) {
        if( position  == mAlbumFiles.size() && buttonEnd == 1){
            return R.layout.button_at_end;
        }
        else if (mAlbumFiles.get(position).getMediaType() == AlbumFile.TYPE_IMAGE) {
            return AlbumFile.TYPE_IMAGE;
        }
        return AlbumFile.TYPE_VIDEO;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case AlbumFile.TYPE_IMAGE: {
                return new ImageViewHolder(mInflater.inflate(R.layout.item_content_image, parent, false), mItemClickListener);
            }
            case R.layout.button_at_end:
               View v =  mInflater.inflate(R.layout.button_at_end, parent, false);
               return new ButtonViewHolder(v);
            default: {
                throw new AssertionError("This should not be the case.");
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == AlbumFile.TYPE_IMAGE) {
            ((ImageViewHolder) holder).setData(mAlbumFiles.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mAlbumFiles == null ? 0 : mAlbumFiles.size() + buttonEnd;
    }

    private static class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final OnItemClickListener mItemClickListener;
        private ImageView mIvImage;

        ImageViewHolder(View itemView, OnItemClickListener itemClickListener) {
            super(itemView);
            this.mItemClickListener = itemClickListener;
            this.mIvImage = itemView.findViewById(R.id.iv_album_content_image);
            itemView.setOnClickListener(this);
        }

        void setData(AlbumFile albumFile) {
            Album.getAlbumConfig().
                    getAlbumLoader().
                    load(mIvImage, albumFile);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getAdapterPosition());
            }
        }
    }

    private class ButtonViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ButtonViewHolder(@NonNull View itemView) {
            super(itemView);
             itemView.findViewById(R.id.button_at_end)
                    .setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {
                itemClickListener.onClick();
            }
        }
    }
}
