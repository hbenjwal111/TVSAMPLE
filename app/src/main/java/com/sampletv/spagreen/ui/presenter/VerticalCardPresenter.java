package com.oxootv.spagreen.ui.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.Presenter;

import com.oxootv.spagreen.R;
import com.oxootv.spagreen.fragments.CountryFragment;
import com.oxootv.spagreen.fragments.FavouriteFragment;
import com.oxootv.spagreen.fragments.GenreFragment;
import com.oxootv.spagreen.fragments.MoviesFragment;
import com.oxootv.spagreen.fragments.TvSeriesFragment;
import com.oxootv.spagreen.model.CountryModel;
import com.oxootv.spagreen.model.Genre;
import com.oxootv.spagreen.model.Movie;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class VerticalCardPresenter extends Presenter {

    private static int CARD_WIDTH = 185;
    private static int CARD_HEIGHT = 278;
    private String type;
    private static Context mContext;

    public VerticalCardPresenter(String type) {
        this.type = type;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Log.d("onCreateViewHolder", "creating viewholder");
        mContext = parent.getContext();
        ImageCardView cardView = new ImageCardView(mContext);
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        cardView.requestLayout();
        cardView.setInfoVisibility(View.GONE);
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        if (type.equals(TvSeriesFragment.TV_SERIES)) {
            Movie movie = (Movie) item;
            ((ViewHolder) viewHolder).mCardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
            ((ViewHolder) viewHolder).updateCardViewImage(movie.getThumbnailUrl());
        } else if (type.equals(MoviesFragment.MOVIE)) {
            Movie movie = (Movie) item;
            ((ViewHolder) viewHolder).mCardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
            ((ViewHolder) viewHolder).updateCardViewImage(movie.getThumbnailUrl());
        } else if (type.equals(FavouriteFragment.FAVORITE)) {
            Movie movie = (Movie) item;
            ((ViewHolder) viewHolder).mCardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
            ((ViewHolder) viewHolder).updateCardViewImage(movie.getThumbnailUrl());
        } else if (type.equals(GenreFragment.GENRE)) {
            Genre genre = (Genre) item;
            ((ViewHolder) viewHolder).mCardView.setMainImageDimensions(200, 200);
            ((ViewHolder) viewHolder).updateCardViewImage(genre.getImageUrl());
        } else if (type.equals(CountryFragment.COUNTRY)) {
            CountryModel countryModel = (CountryModel) item;
            ((ViewHolder) viewHolder).mCardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
            ((ViewHolder) viewHolder).updateCardViewImage(countryModel.getImageUrl());
        }
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {

    }


    class ViewHolder extends Presenter.ViewHolder {

        private ImageCardView mCardView;
        private Drawable mDefaultCardImage;
        private PicassoImageCardViewTarget mImageCardViewTarget;

        public ViewHolder(View view) {
            super(view);
            mCardView = (ImageCardView) view;
            mImageCardViewTarget = new PicassoImageCardViewTarget(mCardView);
            mDefaultCardImage = mContext
                    .getResources()
                    .getDrawable(R.drawable.logo);
        }

        public ImageCardView getCardView() {
            return mCardView;
        }

        protected void updateCardViewImage(String url) {

            Picasso.get()
                    .load(url)
                    .placeholder(R.drawable.poster_placeholder)
                    .error(mDefaultCardImage)
                    .into(mCardView.getMainImageView());

        }
    }

    static class PicassoImageCardViewTarget implements Target {


        private ImageCardView mImageCardView;

        public PicassoImageCardViewTarget(ImageCardView mImageCardView) {
            this.mImageCardView = mImageCardView;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            Drawable bitmapDrawable = new BitmapDrawable(mContext.getResources(), bitmap);
            mImageCardView.setMainImage(bitmapDrawable);
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
            mImageCardView.setMainImage(errorDrawable);
        }


        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    }

}

