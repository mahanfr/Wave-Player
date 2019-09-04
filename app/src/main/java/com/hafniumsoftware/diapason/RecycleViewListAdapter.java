package com.hafniumsoftware.diapason;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class RecycleViewListAdapter extends RecyclerView.Adapter {
    private ArrayList<Audio> audios;
    private ArrayList<Category> categories;
    private Context context;
    private boolean isCategorised;

    public RecycleViewListAdapter(ArrayList<Audio> audios, Context context) {
        this.audios = audios;
        this.context = context;
    }
    public RecycleViewListAdapter(ArrayList<Category> categories, Context context,Boolean isCategorised) {
        this.categories = categories;
        this.context = context;
        this.isCategorised = isCategorised;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if(isCategorised){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.artist_list_element, parent, false);
        }else{
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_list_element, parent, false);
        }
        return new RecycleViewListAdapter.ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((RecycleViewListAdapter.ListViewHolder) holder).bindView(position);
    }

    @Override
    public int getItemCount() {
        if(isCategorised){
            return categories.size();
        }
        return audios.size();
    }


    private class ListViewHolder extends RecyclerView.ViewHolder {
        private TextView albumName;
        private TextView albumSongCount;
        private Button playlistAddBtn;
        private View thisView;


        public ListViewHolder(View itemView) {
            super(itemView);
            if(!isCategorised){
                albumName = itemView.findViewById(R.id.song_title);
                albumSongCount = itemView.findViewById(R.id.song_artist);
                playlistAddBtn = itemView.findViewById(R.id.btn_playlist);
            }else {
                albumName = itemView.findViewById(R.id.song_title);
                albumSongCount = itemView.findViewById(R.id.song_artist);
                //playlistAddBtn = itemView.findViewById(R.id.btn_playlist);
            }
            thisView = itemView;
        }

        public void bindView(int position) {
            if(!isCategorised){
                albumName.setText(audios.get(position).getTitle());
                albumSongCount.setText(String.valueOf(audios.get(position).getArtist()));
            }else {
                albumName.setText(categories.get(position).getCategoryName());
                albumSongCount.setText(String.valueOf(categories.get(position).getCategoryAudios().size()));
            }

           //playlistAddBtn.setTag(position);
            thisView.setTag(position);
            //itemView.setTag(position);
        }
    }
}

