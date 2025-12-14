package com.example.internetradio.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.internetradio.R;
import com.example.internetradio.data.RadioStation;
import com.example.internetradio.viewmodel.StationViewModel;
import java.util.UUID;

public class AddStationFragment extends Fragment {

    private StationViewModel viewModel;
    private EditText nameEditText, urlEditText, countryEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_station, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(StationViewModel.class);

        nameEditText = view.findViewById(R.id.edit_station_name);
        urlEditText = view.findViewById(R.id.edit_station_url);
        countryEditText = view.findViewById(R.id.edit_station_country);
        Button saveButton = view.findViewById(R.id.button_save_station);

        saveButton.setOnClickListener(v -> saveStation());

        return view;
    }

    private void saveStation() {
        String name = nameEditText.getText().toString().trim();
        String url = urlEditText.getText().toString().trim();
        String country = countryEditText.getText().toString().trim();

        if (name.isEmpty() || url.isEmpty()) {
            Toast.makeText(getContext(), "Nazwa i URL są wymagane!", Toast.LENGTH_SHORT).show();
            return;
        }

        String manualUuid = UUID.randomUUID().toString();

        RadioStation newStation = new RadioStation(
                manualUuid,
                name,
                url,
                country.isEmpty() ? "Użytkownika" : country,
                false
        );

        viewModel.insert(newStation);

        Toast.makeText(getContext(), "Stacja zapisana!", Toast.LENGTH_SHORT).show();

        Navigation.findNavController(requireView()).popBackStack();
    }
}