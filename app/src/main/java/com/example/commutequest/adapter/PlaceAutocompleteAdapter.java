package com.example.commutequest.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.commutequest.R;
import com.example.commutequest.model.PlaceItem;

import java.util.List;

public class PlaceAutocompleteAdapter extends RecyclerView.Adapter<PlaceAutocompleteAdapter.PlaceViewHolder> {

    private List<PlaceItem> places;
    private final OnPlaceClickListener listener;

    public interface OnPlaceClickListener {
        void onPlaceClick(PlaceItem place);
    }

    public PlaceAutocompleteAdapter(List<PlaceItem> places, OnPlaceClickListener listener) {
        this.places = places;
        this.listener = listener;
    }

    public void updatePlaces(List<PlaceItem> newPlaces) {
        this.places = newPlaces;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_place_suggestion, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        PlaceItem place = places.get(position);
        holder.bind(place, listener);
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    static class PlaceViewHolder extends RecyclerView.ViewHolder {
        private final TextView placeNameText;
        private final TextView placeAddressText;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            placeNameText = itemView.findViewById(R.id.place_name);
            placeAddressText = itemView.findViewById(R.id.place_address);
        }

        public void bind(final PlaceItem place, final OnPlaceClickListener listener) {
            // Split the place text into parts (name and address)
            // For simplicity, we'll just use the first comma as a delimiter
            String fullText = place.getText();
            String[] parts = fullText.split(",", 2);

            if (parts.length > 0) {
                placeNameText.setText(parts[0].trim());

                if (parts.length > 1) {
                    placeAddressText.setText(parts[1].trim());
                    placeAddressText.setVisibility(View.VISIBLE);
                } else {
                    placeAddressText.setVisibility(View.GONE);
                }
            } else {
                placeNameText.setText(fullText);
                placeAddressText.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPlaceClick(place);
                }
            });
        }
    }
}