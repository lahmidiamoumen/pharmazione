package com.moumen.pharmazione;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.moumen.pharmazione.databinding.CardviewNotificationBinding;
import com.moumen.pharmazione.persistance.DonBesoin;
import com.moumen.pharmazione.persistance.Notification;
import com.moumen.pharmazione.utils.MedClickListener;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

public class ProfileNotifAdapter extends ListAdapter<Notification,ProfileNotifAdapter.MyViewHolder> {
    private MedClickListener<Notification> click;


    public ProfileNotifAdapter(MedClickListener<Notification> cl, @NonNull DiffUtil.ItemCallback<Notification> diffCallback) {
        super(diffCallback);
        click = cl;
    }


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class MyViewHolder extends RecyclerView.ViewHolder{

        private final CardviewNotificationBinding binding ;

        MyViewHolder(CardviewNotificationBinding itemView, MedClickListener<Notification> click) {
            super(itemView.getRoot());
            binding = itemView;
            binding.setHandler(click);
        }

        void bind(Notification hc){
            binding.setItem(hc);
            binding.setUrlEmpty("https://firebasestorage.googleapis.com/v0/b/data-278806.appspot.com/o/images%2Fempty.png?alt=media&token=7015119e-0356-4387-9f0f-3c807db65861");
            binding.executePendingBindings();
            if( hc.created!= null) {
                String niceDateStr = DateUtils.getRelativeTimeSpanString(hc.created.toDate().getTime() , Calendar.getInstance().getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS).toString();
                binding.itemValue.setText(niceDateStr);
            }
        }
    }


    // Create new views (invoked by the layout manager)
    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(@NotNull ViewGroup parent,
                                           int viewType) {
        CardviewNotificationBinding binding =  CardviewNotificationBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new MyViewHolder(binding, click);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

}