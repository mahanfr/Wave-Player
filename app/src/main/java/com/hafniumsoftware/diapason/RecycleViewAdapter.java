package com.hafniumsoftware.diapason;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;


public class RecycleViewAdapter extends RecyclerView.Adapter {

    private ArrayList<Category> audios;
    private Context context;

    public RecycleViewAdapter(ArrayList<Category> audios, Context context) {
        this.audios = audios;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_list_element, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ListViewHolder) holder).bindView(position);
    }

    @Override
    public int getItemCount() {
        return audios.size();
    }

    private  class ListViewHolder extends RecyclerView.ViewHolder {
        private TextView albumName;
        private TextView albumSongCount;
        private View thisView;
        private ImageView imageView;

        public ListViewHolder(View itemView) {
            super(itemView);
            albumName = itemView.findViewById(R.id.textView3);
            albumSongCount = itemView.findViewById(R.id.textView4);
            imageView = itemView.findViewById(R.id.imageView);
            thisView = itemView;
        }

        public void bindView(int position){
            albumName.setText(audios.get(position).getCategoryName());
            albumSongCount.setText(audios.get(position).getArtist());
            try {
                imageView.setImageBitmap(audios.get(position).getImage(context));
            }catch (Exception e){
                e.printStackTrace();
            }
            thisView.setTag(position);
        }
    }
}
