package com.sampletv.spagreen.ui.activity;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.sampletv.spagreen.R;


public class VideoDetailsActivity extends FragmentActivity {
    public static final String TRANSITION_NAME = "t_for_transition";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_details);
    }
}
