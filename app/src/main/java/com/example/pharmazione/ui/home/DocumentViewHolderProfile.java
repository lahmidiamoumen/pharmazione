package com.example.pharmazione.ui.home;

import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pharmazione.databinding.CardviewProfileBinding;
import com.example.pharmazione.persistance.Document;
import com.example.pharmazione.utils.ItemClickListener;

import static com.example.pharmazione.utils.Util.EMPTY_IMAGE;


public class DocumentViewHolderProfile extends  RecyclerView.ViewHolder
{
    private CardviewProfileBinding binding;

    public DocumentViewHolderProfile(CardviewProfileBinding view, ItemClickListener itemClickListener) {
        super(view.getRoot());
        binding = view;
        binding.setHandler(itemClickListener);
    }

    public void bindToProfile(Document word)
    {
        if(word.satisfied)
            binding.isSatisfiedText.setVisibility(View.VISIBLE);
        else
            binding.waiting.setVisibility(View.VISIBLE);

        binding.setItem(word);
        binding.setUrlEmpty(EMPTY_IMAGE);
    }


}