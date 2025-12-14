package com.example.internetradio.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.internetradio.R;
import com.example.internetradio.adapters.StationListAdapter;
import com.example.internetradio.viewmodel.StationViewModel;

public class FavoriteListFragment extends Fragment implements StationListAdapter.OnStationClickListener {

    private StationViewModel viewModel;
    private StationListAdapter adapter;
    private TextView emptyTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite_list, container, false); // Użycie nowego layoutu

        RecyclerView recyclerView = view.findViewById(R.id.favorite_list_recycler_view);
        emptyTextView = view.findViewById(R.id.text_empty_favorites);

        adapter = new StationListAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setOnStationClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        viewModel = new ViewModelProvider(this).get(StationViewModel.class);

        viewModel.getFavoriteStations().observe(getViewLifecycleOwner(), favorites -> {
            adapter.setStations(favorites);

            if (favorites == null || favorites.isEmpty()) {
                emptyTextView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyTextView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }

    @Override
    public void onStationClick(String stationUuid) {
        Bundle bundle = new Bundle();
        bundle.putString("stationUuid", stationUuid);
        Navigation.findNavController(requireView()).navigate(R.id.stationDetailsFragment, bundle);
    }
}