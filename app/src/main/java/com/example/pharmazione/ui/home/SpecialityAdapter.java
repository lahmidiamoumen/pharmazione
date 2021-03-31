package com.example.pharmazione.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pharmazione.databinding.ItemSpecialityBinding;
import com.example.pharmazione.ui.speciality.Speciality;
import com.example.pharmazione.utils.MedClickListener;

public class SpecialityAdapter extends ListAdapter<Speciality, SpecialityViewHolder> {

    private MedClickListener click;

    public SpecialityAdapter(MedClickListener cl,@NonNull DiffUtil.ItemCallback<Speciality> diffCallback) {
        super(diffCallback);
        click = cl;
    }

    @NonNull
    @Override
    public SpecialityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSpecialityBinding binding =  ItemSpecialityBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new SpecialityViewHolder(binding,click);
    }

    @Override
    public void onBindViewHolder(@NonNull SpecialityViewHolder holder, int position) {
        holder.bind(getItem(position));
    }


}

class SpecialityViewHolder extends RecyclerView.ViewHolder{

    private ItemSpecialityBinding binding ;

    SpecialityViewHolder(ItemSpecialityBinding itemView,MedClickListener click) {
        super(itemView.getRoot());
        binding = itemView;
        binding.setHandler(click);
    }

    void bind(Speciality hc){
        binding.setItem(hc);
        binding.executePendingBindings();
    }

}
