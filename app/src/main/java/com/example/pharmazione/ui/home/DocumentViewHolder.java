package com.example.pharmazione.ui.home;

import androidx.recyclerview.widget.RecyclerView;
import com.example.pharmazione.databinding.EmailItemLayoutBinding;
import com.example.pharmazione.persistance.Document;
import com.example.pharmazione.utils.ItemClickListener;


public class DocumentViewHolder extends RecyclerView.ViewHolder
{
    EmailItemLayoutBinding binding;


    public DocumentViewHolder(EmailItemLayoutBinding view, ItemClickListener itemClickListener) {
        super(view.getRoot());
        binding =  view;
        binding.setHandler(itemClickListener);

    }

    public void bindTo(Document word)
    {
        binding.setItem(word);
        ShowFragmentImagesAdapter adapter = new ShowFragmentImagesAdapter(null);
        if(word.path != null){
            adapter.submitList(word.path);
            binding.attachmentRecyclerView.setAdapter(adapter);
        }
        binding.setUrlEmpty("https://firebasestorage.googleapis.com/v0/b/data-278806.appspot.com/o/images%2Fempty.png?alt=media&token=7015119e-0356-4387-9f0f-3c807db65861");
    }
}