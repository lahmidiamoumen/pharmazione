package com.moumen.pharmazione.ui.doctors;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.moumen.pharmazione.databinding.DoctItemBinding;
import com.moumen.pharmazione.databinding.PharmaItemBinding;
import com.moumen.pharmazione.persistance.Doctors;
import com.moumen.pharmazione.persistance.User;
import com.moumen.pharmazione.utils.MedClickListener;

public class DoctorsAdapter extends ListAdapter<User, DoctorsViewHolder> {

    private MedClickListener click;

    public DoctorsAdapter(MedClickListener cl,@NonNull DiffUtil.ItemCallback<User> diffCallback) {
        super(diffCallback);
        click = cl;
    }

    @NonNull
    @Override
    public DoctorsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PharmaItemBinding binding =  PharmaItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new DoctorsViewHolder(binding,click, null);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorsViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

}

class DoctorsViewHolder extends RecyclerView.ViewHolder{

    private PharmaItemBinding binding ;
    private Context c;

    DoctorsViewHolder(PharmaItemBinding itemView, MedClickListener click, Context c) {
        super(itemView.getRoot());
        this.c = c;
        binding = itemView;
        binding.setClickHandler(click);
    }

    void bind(User hc){
        binding.setCourse(hc);
        binding.courseInstructor.setOnClickListener(o ->{
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", hc.mPhoneNumber, null));
            c.startActivity(intent);
        });
        binding.executePendingBindings();
    }
}
