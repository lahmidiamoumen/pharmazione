package com.moumen.pharmazione.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.moumen.pharmazione.databinding.MedItemBinding;
import com.moumen.pharmazione.persistance.HorizantallContent;
import com.moumen.pharmazione.utils.MedClickListener;

public class HorizontalAdapter extends ListAdapter<HorizantallContent, HorizontalViewHolder> {

    MedClickListener click;

    public HorizontalAdapter(MedClickListener cl,@NonNull DiffUtil.ItemCallback<HorizantallContent> diffCallback) {
        super(diffCallback);
        click = cl;
    }

    @NonNull
    @Override
    public HorizontalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MedItemBinding binding =  MedItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new HorizontalViewHolder(binding,click);
    }

    @Override
    public void onBindViewHolder(@NonNull HorizontalViewHolder holder, int position) {
        holder.bind(getItem(position));
    }


}

class HorizontalViewHolder extends RecyclerView.ViewHolder{

    private MedItemBinding binding ;

    HorizontalViewHolder(MedItemBinding itemView,MedClickListener click) {
        super(itemView.getRoot());
        binding = itemView;
        binding.setClickHandler(click);
    }

    void bind(HorizantallContent hc){
        binding.setCourse(hc);
        binding.executePendingBindings();
    }
}
