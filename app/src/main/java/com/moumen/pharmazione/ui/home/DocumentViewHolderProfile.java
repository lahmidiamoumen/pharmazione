package com.moumen.pharmazione.ui.home;

import android.text.format.DateUtils;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import com.moumen.pharmazione.databinding.CardviewProfileBinding;
import com.moumen.pharmazione.persistance.Document;
import com.moumen.pharmazione.utils.ItemClickListener;

import java.util.Calendar;

import static com.moumen.pharmazione.utils.Util.EMPTY_IMAGE;


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
        binding.setItem(word);
        ShowFragmentImagesAdapter adapter = new ShowFragmentImagesAdapter(null);
        String niceDateStr = DateUtils.getRelativeTimeSpanString(word.scanned.toDate().getTime() , Calendar.getInstance().getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS).toString();
        binding.setTimeAgo(niceDateStr);
        if(word.path != null){
            adapter.submitList(word.path);
            binding.attachmentRecyclerView.setAdapter(adapter);
        }
    }

}