package com.example.pharmazione;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pharmazione.databinding.CardviewNotificationBinding;
import com.example.pharmazione.persistance.DonBesoin;
import com.example.pharmazione.utils.MedClickListener;

public class ProfileNotifAdapter extends ListAdapter<DonBesoin,ProfileNotifAdapter.MyViewHolder> {
    private MedClickListener click;


    public ProfileNotifAdapter(MedClickListener<DonBesoin> cl, @NonNull DiffUtil.ItemCallback<DonBesoin> diffCallback) {
        super(diffCallback);
        click = cl;
    }


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    class MyViewHolder extends RecyclerView.ViewHolder{

        private CardviewNotificationBinding binding ;

        MyViewHolder(CardviewNotificationBinding itemView, MedClickListener<DonBesoin> click) {
            super(itemView.getRoot());
            binding = itemView;
            binding.setHandler(click);
        }

        void bind(DonBesoin hc){
            binding.setItem(hc);
            binding.setUrlEmpty("https://firebasestorage.googleapis.com/v0/b/data-278806.appspot.com/o/images%2Fempty.png?alt=media&token=7015119e-0356-4387-9f0f-3c807db65861");
            binding.executePendingBindings();
        }
    }


    // Create new views (invoked by the layout manager)
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        CardviewNotificationBinding binding =  CardviewNotificationBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new MyViewHolder(binding,click);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

}