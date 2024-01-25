package com.sampletv.spagreen.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.recyclerview.widget.RecyclerView;

import com.sampletv.spagreen.R;
import com.sampletv.spagreen.model.Videos;

import java.util.List;

public class ServerAdapter extends RecyclerView.Adapter<ServerAdapter.OriginalViewHolder> {

    private String type;
    private List<Videos> videosses;
    private Context ctx;

    private OnItemClickListener mOnItemClickListener;

    private OriginalViewHolder viewHolder;


    public interface OnItemClickListener {
        void onItemClick(View view, Videos obj, int position, OriginalViewHolder holder);
    }

    public void setOnItemClickListener(OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public ServerAdapter(Context context, List<Videos> videosses, String type) {
        this.videosses = videosses;
        ctx = context;
        this.type = type;
    }


    @Override
    public OriginalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        OriginalViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_server_tv_item, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final OriginalViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Videos obj = videosses.get(position);
        holder.serverNameTv.setText(obj.getLabel());

        holder.serverNameTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(v, videosses.get(position), position, holder);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return videosses.size();
    }

    public static class OriginalViewHolder extends RecyclerView.ViewHolder {

        public Button serverNameTv;

        public OriginalViewHolder(View v) {
            super(v);
            serverNameTv = v.findViewById(R.id.s_name_tv);
        }
    }

}
