package com.oxootv.spagreen.ui.presenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.leanback.widget.AbstractDetailsDescriptionPresenter;

import com.oxootv.spagreen.R;
import com.oxootv.spagreen.model.MovieSingleDetails;


public class DetailsDescriptionPresenter extends AbstractDetailsDescriptionPresenter {


    private final Context mContext;

    private DetailsDescriptionPresenter() {
        super();
        this.mContext = null;
    }

    public DetailsDescriptionPresenter(Context ctx) {
        super();
        this.mContext = ctx;
    }


    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindDescription(ViewHolder viewHolder, Object item) {
        MovieSingleDetails video = (MovieSingleDetails) item;

        if (video != null) {
            Log.d("Presenter", String.format("%s, %s, %s", video.getTitle(), video.getThumbnailUrl(), video.getDescription()));
            viewHolder.getTitle().setText(video.getTitle());
            viewHolder.getSubtitle().setBackground(mContext.getResources().getDrawable(R.drawable.rounded_black_transparent));
            viewHolder.getSubtitle().setText(mContext.getString(R.string.rating) + 5);
            viewHolder.getBody().setText(video.getDescription());
        }
    }


}
