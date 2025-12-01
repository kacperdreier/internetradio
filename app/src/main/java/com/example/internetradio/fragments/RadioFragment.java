package com.example.internetradio.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.internetradio.R;

public class RadioFragment extends Fragment {

    private TextView stationNameTextView;
    private TextView trackInfoTextView;
    private ImageButton playPauseButton;
    private ImageButton prevButton;
    private ImageButton nextButton;
    private ProgressBar progressBar;
    private boolean isPlaying = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_radio, container, false);

        stationNameTextView = root.findViewById(R.id.station_name_text);
        trackInfoTextView = root.findViewById(R.id.track_info_text);
        playPauseButton = root.findViewById(R.id.button_play_pause);
        prevButton = root.findViewById(R.id.button_prev_station);
        nextButton = root.findViewById(R.id.button_next_station);
        progressBar = root.findViewById(R.id.playback_progress);

        stationNameTextView.setText("Radio Domyslne");
        trackInfoTextView.setText("Czekam na sygnal...");
        progressBar.setProgress(0);

        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    isPlaying = false;
                    playPauseButton.setImageResource(R.drawable.ic_play);
                    Toast.makeText(getContext(), "Zatrzymano odtwarzanie", Toast.LENGTH_SHORT).show();
                } else {
                    isPlaying = true;
                    playPauseButton.setImageResource(R.drawable.ic_pause);
                    Toast.makeText(getContext(), "Rozpoczynam buforowanie...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Poprzednia stacja", Toast.LENGTH_SHORT).show();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Nastepna stacja", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }
}