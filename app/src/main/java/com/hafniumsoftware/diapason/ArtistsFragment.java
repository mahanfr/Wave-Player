package com.hafniumsoftware.diapason;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ArtistsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artists,container,false);
        RecyclerView recyclerView = view.findViewById(R.id.artists_listview);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        StorageUtil storageUtil = new StorageUtil(getActivity());
        RecycleViewListAdapter recycleViewListAdapter = new RecycleViewListAdapter(storageUtil.loadCategory("Artist"),getActivity(),true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recycleViewListAdapter);
        return view;
    }
}
