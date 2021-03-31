package com.example.pharmazione.ui.doctors;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pharmazione.databinding.DoctItemBinding;
import com.example.pharmazione.persistance.Doctors;
import com.example.pharmazione.utils.MedClickListener;
import com.example.pharmazione.utils.Util;

public class DoctorsAdapter extends ListAdapter<Doctors, DoctorsViewHolder> {

    private MedClickListener click;

    public DoctorsAdapter(MedClickListener cl,@NonNull DiffUtil.ItemCallback<Doctors> diffCallback) {
        super(diffCallback);
        click = cl;
    }

    @NonNull
    @Override
    public DoctorsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        DoctItemBinding binding =  DoctItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new DoctorsViewHolder(binding,click);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorsViewHolder holder, int position) {
        holder.bind(getItem(position));
    }


}

class DoctorsViewHolder extends RecyclerView.ViewHolder{

    private DoctItemBinding binding ;

    DoctorsViewHolder(DoctItemBinding itemView,MedClickListener click) {
        super(itemView.getRoot());
        binding = itemView;
        binding.setClickHandler(click);
    }

    void bind(Doctors hc){
        binding.setCourse(hc);
        binding.executePendingBindings();
    }

}
