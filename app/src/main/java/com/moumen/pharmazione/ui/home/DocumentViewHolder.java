package com.moumen.pharmazione.ui.home;

import androidx.recyclerview.widget.RecyclerView;
import com.moumen.pharmazione.databinding.EmailItemLayoutBinding;
import com.moumen.pharmazione.persistance.Document;
import com.moumen.pharmazione.utils.ItemClickListener;

import static com.moumen.pharmazione.utils.Util.EMPTY_IMAGE;


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
        binding.setUrlEmpty(EMPTY_IMAGE);
    }
}