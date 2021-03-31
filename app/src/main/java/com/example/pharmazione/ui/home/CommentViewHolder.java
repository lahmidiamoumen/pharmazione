package com.example.pharmazione.ui.home;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pharmazione.R;
import com.example.pharmazione.persistance.Comment;

import static com.example.pharmazione.utils.Util.load;


class CommentViewHolder extends RecyclerView.ViewHolder
{
    private TextView nameTxt;
    private TextView propTxt;
    private ImageView sliderLayout;

    CommentViewHolder(View view) {
        super(view);
        nameTxt = view.findViewById(R.id.comment_name);
        propTxt = view.findViewById(R.id.message_body);
        sliderLayout = view.findViewById(R.id.comment_image);
        //nameTxt.setTypeface(Util.getFontThin(view.getContext().getAssets()));
    }

    void bindTo(Comment word)
    {
        propTxt.setText(word.getContent());
        nameTxt.setText( word.getUserName());
        load(sliderLayout,word.getUserURL());

    }



}