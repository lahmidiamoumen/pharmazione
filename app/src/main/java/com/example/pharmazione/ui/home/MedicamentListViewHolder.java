package com.example.pharmazione.ui.home;

import androidx.recyclerview.widget.RecyclerView;

import com.example.pharmazione.databinding.CardviewListBinding;
import com.example.pharmazione.persistance.MedicamentList;
import com.example.pharmazione.utils.MedClickListener;


public class MedicamentListViewHolder extends RecyclerView.ViewHolder
{
    CardviewListBinding binding;
//    protected long mLastClickTime = 0;


    public MedicamentListViewHolder(CardviewListBinding view, MedClickListener<MedicamentList> itemClickListener) {
        super(view.getRoot());
        binding =  view;
        binding.setHandler(itemClickListener);

    }

    public void bindTo(MedicamentList word)
    {
        binding.setItem(word);
    }




}