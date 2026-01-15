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
import com.example.internetradio.data.RadioStation;
import com.example.internetradio.viewmodel.StationViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.navigation.Navigation;

public class StationListFragment extends Fragment implements StationListAdapter.OnStationClickListener{

    private StationViewModel stationViewModel;
    private StationListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_station_list, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.station_list_recycler_view);
        adapter = new StationListAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setOnStationClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        stationViewModel = new ViewModelProvider(this).get(StationViewModel.class);

        stationViewModel.getAllStations().observe(getViewLifecycleOwner(), stations -> {
            adapter.setStations(stations);
        });

        FloatingActionButton fab = view.findViewById(R.id.fab_add_station);
        fab.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_nav_station_list_to_addStationFragment);
        });

        return view;
    }
    @Override
    public void onStationClick(RadioStation station) {
        com.example.internetradio.MainActivity mainActivity = (com.example.internetradio.MainActivity) getActivity();

        Bundle bundle = new Bundle();
        bundle.putString("stationUuid", station.getStationUuid());

        Navigation.findNavController(requireView()).navigate(R.id.stationDetailsFragment, bundle);
    }
}