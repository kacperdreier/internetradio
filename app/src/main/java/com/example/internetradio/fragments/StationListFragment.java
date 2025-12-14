package com.example.internetradio.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.internetradio.R;
import com.example.internetradio.adapters.StationListAdapter;
import com.example.internetradio.viewmodel.StationViewModel;

public class StationListFragment extends Fragment {

    private StationViewModel stationViewModel;
    private StationListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_station_list, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.station_list_recycler_view);
        adapter = new StationListAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        stationViewModel = new ViewModelProvider(this).get(StationViewModel.class);

        stationViewModel.getAllStations().observe(getViewLifecycleOwner(), stations -> {
            adapter.setStations(stations);
        });

        return view;
    }
}