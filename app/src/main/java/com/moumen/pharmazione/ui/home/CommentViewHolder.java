package com.moumen.pharmazione.ui.home;

import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.moumen.pharmazione.R;
import com.moumen.pharmazione.persistance.Comment;

import java.util.Calendar;

import static com.moumen.pharmazione.utils.Util.load;


class CommentViewHolder extends RecyclerView.ViewHolder
{
    private final TextView nameTxt;
    private final TextView propTxt;
    private final TextView dateTxt;
    private final ImageView sliderLayout;

    CommentViewHolder(View view) {
        super(view);
        nameTxt = view.findViewById(R.id.comment_name);
        propTxt = view.findViewById(R.id.message_body);
        sliderLayout = view.findViewById(R.id.comment_image);
        dateTxt = view.findViewById(R.id.comment_date);
        //nameTxt.setTypeface(Util.getFontThin(view.getContext().getAssets()));
    }

    void bindTo(Comment word)
    {
        if( word.created != null) {
            String niceDateStr = DateUtils.getRelativeTimeSpanString(word.created.toDate().getTime() , Calendar.getInstance().getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS).toString();
            dateTxt.setText(niceDateStr);
        }
        propTxt.setText(word.getContent());
        nameTxt.setText( word.getUserName());
        load(sliderLayout,word.getUserURL());

    }



}