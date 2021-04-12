package com.moumen.pharmazione.ui.home;

import androidx.recyclerview.widget.RecyclerView;

import com.moumen.pharmazione.databinding.CardviewListBinding;
import com.moumen.pharmazione.persistance.MedicamentList;
import com.moumen.pharmazione.utils.MedClickListener;


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