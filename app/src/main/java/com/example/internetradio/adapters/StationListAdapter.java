package com.example.internetradio.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.internetradio.R;
import com.example.internetradio.data.RadioStation;
import java.util.List;

public class StationListAdapter extends RecyclerView.Adapter<StationListAdapter.StationViewHolder> {

    private List<RadioStation> stations;

    public void setStations(List<RadioStation> stations) {
        this.stations = stations;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_radio_station, parent, false);
        return new StationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StationViewHolder holder, int position) {
        if (stations != null) {
            RadioStation currentStation = stations.get(position);
            holder.stationNameTextView.setText(currentStation.getName());
            holder.stationCountryTextView.setText(currentStation.getCountry());
        }
    }

    @Override
    public int getItemCount() {
        return stations != null ? stations.size() : 0;
    }

    static class StationViewHolder extends RecyclerView.ViewHolder {
        private final TextView stationNameTextView;
        private final TextView stationCountryTextView;

        public StationViewHolder(View itemView) {
            super(itemView);
            stationNameTextView = itemView.findViewById(R.id.text_station_name);
            stationCountryTextView = itemView.findViewById(R.id.text_station_country);
        }
    }
}