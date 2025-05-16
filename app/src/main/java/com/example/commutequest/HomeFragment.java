package com.example.commutequest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.commutequest.adapter.PlaceAutocompleteAdapter;
import com.example.commutequest.api.APIService;
import com.example.commutequest.api.RetrofitClient;
import com.example.commutequest.model.AutocompleteResponse;
import com.example.commutequest.model.MapsLinkRequest;
import com.example.commutequest.model.MapsLinkResponse;
import com.example.commutequest.model.PlaceItem;
import com.example.commutequest.model.RouteRequest;
import com.example.commutequest.model.RouteResponse;
import com.example.commutequest.util.PolylineDecoder;
import com.example.commutequest.util.SessionManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Intent;
import android.net.Uri;
import com.example.commutequest.model.MapsLinkRequest;
import com.example.commutequest.model.MapsLinkResponse;

public class HomeFragment extends Fragment implements OnMapReadyCallback {
    private Button startNavigationButton;
    private String googleMapsLink;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    // Search UI elements
    private CardView searchContainer;
    private TextView searchSummaryText;
    private CardView expandedSearchPanel;
    private EditText originEditText;
    private EditText destinationEditText;
    private ImageButton useCurrentLocationBtn;
    private Button searchRouteButton;

    // Autocomplete UI elements
    private CardView autocompleteResultsContainer;
    private RecyclerView autocompleteResults;
    private ProgressBar originLoading;
    private ProgressBar destinationLoading;

    // API and adapters
    private APIService apiService;
    private SessionManager sessionManager;
    private PlaceAutocompleteAdapter autocompleteAdapter;

    // Search state tracking
    private boolean isSearchExpanded = false;
    private boolean isSearchingOrigin = true; // Track which field is being edited
    private Handler searchHandler = new Handler();
    private static final long SEARCH_DELAY_MS = 500; // Delay before making API call
    private Runnable searchRunnable;

    // Route variables
    private Polyline currentRoute;
    private PlaceItem selectedOriginPlace;
    private PlaceItem selectedDestinationPlace;
    private boolean useCurrentLocationAsOrigin = false;
    private LatLng currentUserLocation;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Get the SupportMapFragment and request notification when the map is ready to be used
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Initialize API service and session manager
        apiService = RetrofitClient.getInstance().getApi();
        sessionManager = new SessionManager(requireContext());

        // Initialize UI elements
        initializeUI(view);

        return view;
    }

    private void initializeUI(View view) {
        // Find views from layout
        searchContainer = view.findViewById(R.id.search_container);
        searchSummaryText = view.findViewById(R.id.search_summary_text);
        expandedSearchPanel = view.findViewById(R.id.expanded_search_panel);

        // Expanded search panel views
        originEditText = view.findViewById(R.id.origin_edit_text);
        destinationEditText = view.findViewById(R.id.destination_edit_text);
        useCurrentLocationBtn = view.findViewById(R.id.use_current_location);
        searchRouteButton = view.findViewById(R.id.search_route_button);
        originLoading = view.findViewById(R.id.origin_loading);
        destinationLoading = view.findViewById(R.id.destination_loading);

        // Back button in expanded panel
        ImageView backButton = view.findViewById(R.id.back_button);

        // Autocomplete views
        autocompleteResultsContainer = view.findViewById(R.id.autocomplete_results_container);
        autocompleteResults = view.findViewById(R.id.autocomplete_results);

        //
        startNavigationButton = view.findViewById(R.id.start_navigation_button);
        startNavigationButton.setVisibility(View.GONE); // Hide initially
        startNavigationButton.setOnClickListener(v -> openGoogleMaps());



        // Setup RecyclerView for autocomplete
        autocompleteResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        autocompleteAdapter = new PlaceAutocompleteAdapter(new ArrayList<>(), place -> {
            // Handle place selection
            if (isSearchingOrigin) {
                originEditText.setText(place.getText());
                originEditText.clearFocus();
                selectedOriginPlace = place;
                useCurrentLocationAsOrigin = false;
            } else {
                destinationEditText.setText(place.getText());
                destinationEditText.clearFocus();
                selectedDestinationPlace = place;
            }
            hideAutocomplete();
        });
        autocompleteResults.setAdapter(autocompleteAdapter);

        // Set up click listener for search container to expand search
        searchContainer.setOnClickListener(v -> expandSearchPanel());

        // Set up back button to collapse search
        backButton.setOnClickListener(v -> collapseSearchPanel());

        // Set up text watchers for origin and destination fields
        setupEditTextListeners();

        // Setup use current location button
        useCurrentLocationBtn.setOnClickListener(v -> getCurrentLocationForOrigin());

        // Setup search route button
        searchRouteButton.setOnClickListener(v -> searchRoute());

        // Setup touch listeners to dismiss autocomplete when clicking outside
        setupTouchListeners(view);
    }

    private void setupTouchListeners(View rootView) {
        // Set click listener on the parent view to dismiss autocomplete when clicking outside
        View mapView = rootView.findViewById(R.id.map);
        if (mapView != null) {
            mapView.setOnClickListener(v -> {
                hideAutocomplete();
                originEditText.clearFocus();
                destinationEditText.clearFocus();
            });
        }

        // You can add more view listeners here if needed
        searchRouteButton.setOnClickListener(v -> {
            hideAutocomplete();
            searchRoute();
        });
    }

    private void setupEditTextListeners() {
        // Text watcher for origin field
        originEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                isSearchingOrigin = true;
                // Show autocomplete only if there's text
                if (originEditText.getText().length() >= 2) {
                    handleSearchTextChanged(originEditText.getText().toString());
                }
            } else {
                // Add small delay before hiding to allow for item selection
                new Handler().postDelayed(() -> {
                    if (!destinationEditText.hasFocus()) {
                        hideAutocomplete();
                    }
                }, 200);
            }
        });

        originEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isSearchingOrigin = true;
                handleSearchTextChanged(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Text watcher for destination field
        destinationEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                isSearchingOrigin = false;
                // Show autocomplete only if there's text
                if (destinationEditText.getText().length() >= 2) {
                    handleSearchTextChanged(destinationEditText.getText().toString());
                }
            } else {
                // Add small delay before hiding to allow for item selection
                new Handler().postDelayed(() -> {
                    if (!originEditText.hasFocus()) {
                        hideAutocomplete();
                    }
                }, 200);
            }
        });

        destinationEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isSearchingOrigin = false;
                handleSearchTextChanged(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void handleSearchTextChanged(String query) {
        // Remove any pending search requests
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }

        if (query.length() < 2) {
            hideAutocomplete();
            return;
        }

        // Show progress based on which field is being edited
        if (isSearchingOrigin) {
            originLoading.setVisibility(View.VISIBLE);
        } else {
            destinationLoading.setVisibility(View.VISIBLE);
        }

        // Delay search to avoid too many API calls while typing
        searchRunnable = () -> searchPlaces(query);
        searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
    }

    private void searchPlaces(String query) {
        String authHeader = sessionManager.getAuthorizationHeader();

        if (authHeader == null) {
            Toast.makeText(requireContext(), "Authentication required", Toast.LENGTH_SHORT).show();
            hideAutocomplete();
            return;
        }

        // Create request body
        AutocompleteRequest request = new AutocompleteRequest(query);

        // Make API call
        Call<AutocompleteResponse> call = apiService.getAutocompletePlaces(authHeader, request);
        call.enqueue(new Callback<AutocompleteResponse>() {
            @Override
            public void onResponse(Call<AutocompleteResponse> call, Response<AutocompleteResponse> response) {
                // Hide loading indicators
                originLoading.setVisibility(View.GONE);
                destinationLoading.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    AutocompleteResponse data = response.body();

                    if (data.isSuccess() && data.getData() != null && !data.getData().isEmpty()) {
                        // Update adapter with new places
                        autocompleteAdapter.updatePlaces(data.getData());
                        showAutocompleteResults();
                    } else {
                        hideAutocomplete();
                    }
                } else {
                    hideAutocomplete();
                    Toast.makeText(requireContext(), "Error retrieving places", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AutocompleteResponse> call, Throwable t) {
                originLoading.setVisibility(View.GONE);
                destinationLoading.setVisibility(View.GONE);
                hideAutocomplete();
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAutocompleteResults() {
        // Ensure proper positioning before showing
        updateAutocompletePosition();
        autocompleteResultsContainer.setVisibility(View.VISIBLE);
    }

    private void updateAutocompletePosition() {
        // Get the vertical position of the currently focused field
        View anchorView = isSearchingOrigin ? originEditText : destinationEditText;

        if (anchorView != null && expandedSearchPanel != null && autocompleteResultsContainer != null) {
            int[] location = new int[2];
            anchorView.getLocationInWindow(location);

            // Position autocomplete below the current field
            int topMargin = location[1] + anchorView.getHeight();

            // Apply position
            ViewGroup.MarginLayoutParams params =
                    (ViewGroup.MarginLayoutParams) autocompleteResultsContainer.getLayoutParams();

            if (params != null) {
                params.topMargin = topMargin - expandedSearchPanel.getTop();
                autocompleteResultsContainer.setLayoutParams(params);
            }
        }
    }

    private void hideAutocomplete() {
        autocompleteResultsContainer.setVisibility(View.GONE);
    }

    private void expandSearchPanel() {
        searchContainer.setVisibility(View.GONE);
        expandedSearchPanel.setVisibility(View.VISIBLE);
        isSearchExpanded = true;
    }

    private void collapseSearchPanel() {
        expandedSearchPanel.setVisibility(View.GONE);
        searchContainer.setVisibility(View.VISIBLE);
        hideAutocomplete();
        isSearchExpanded = false;

        // Clear focus from edit texts
        originEditText.clearFocus();
        destinationEditText.clearFocus();
    }

    private void getCurrentLocationForOrigin() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {

            useCurrentLocationBtn.setEnabled(false);
            originLoading.setVisibility(View.VISIBLE);

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), location -> {
                        useCurrentLocationBtn.setEnabled(true);
                        originLoading.setVisibility(View.GONE);

                        if (location != null) {
                            // Store current location
                            currentUserLocation = new LatLng(location.getLatitude(), location.getLongitude());

                            // Format and set the current location text
                            String locationText = "Current Location";
                            originEditText.setText(locationText);
                            hideAutocomplete();
                            originEditText.clearFocus();

                            // Set flag to use current location
                            useCurrentLocationAsOrigin = true;
                            selectedOriginPlace = null;
                        } else {
                            Toast.makeText(requireContext(), "Unable to get your current location", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        useCurrentLocationBtn.setEnabled(true);
                        originLoading.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Error getting location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            requestLocationPermission();
        }
    }

    private void searchRoute() {

        String origin = originEditText.getText().toString().trim();
        String destination = destinationEditText.getText().toString().trim();

        if (origin.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter an origin location", Toast.LENGTH_SHORT).show();
            return;
        }

        if (destination.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a destination location", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if we have valid coordinates for both origin and destination
        if (!useCurrentLocationAsOrigin && selectedOriginPlace == null) {
            Toast.makeText(requireContext(), "Please select a valid origin from the list", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDestinationPlace == null) {
            Toast.makeText(requireContext(), "Please select a valid destination from the list", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hide keyboard and autocomplete
        hideAutocomplete();
        originEditText.clearFocus();
        destinationEditText.clearFocus();

        // Show loading indicator
        searchRouteButton.setEnabled(false);
        searchRouteButton.setText("Searching...");

        // Get coordinates for origin and destination
        double origLat, origLng, destLat, destLng;

        // Get origin coordinates (either from selected place or current location)
        if (useCurrentLocationAsOrigin) {
            if (currentUserLocation == null) {
                Toast.makeText(requireContext(), "Current location not available. Please try again.", Toast.LENGTH_SHORT).show();
                resetSearchButton();
                return;
            }
            origLat = currentUserLocation.latitude;
            origLng = currentUserLocation.longitude;
        } else {
            origLat = selectedOriginPlace.getCoords().getLatitude();
            origLng = selectedOriginPlace.getCoords().getLongitude();
        }

        // Get destination coordinates
        destLat = selectedDestinationPlace.getCoords().getLatitude();
        destLng = selectedDestinationPlace.getCoords().getLongitude();

        // Log coordinates for debugging
        Log.d("RouteDebug", String.format("Origin: %f, %f | Destination: %f, %f",
                origLat, origLng, destLat, destLng));

        // Create route request with properly formatted coordinates
        RouteRequest routeRequest = new RouteRequest(origLat, origLng, destLat, destLng);

        // Make API call
        String authHeader = sessionManager.getAuthorizationHeader();
        if (authHeader == null) {
            Toast.makeText(requireContext(), "Authentication required", Toast.LENGTH_SHORT).show();
            resetSearchButton();
            return;
        }

        Call<RouteResponse> call = apiService.getRoute(authHeader, routeRequest);
        call.enqueue(new Callback<RouteResponse>() {

            @Override
            public void onResponse(Call<RouteResponse> call, Response<RouteResponse> response) {
                resetSearchButton();

                if (response.isSuccessful() && response.body() != null) {
                    RouteResponse routeResponse = response.body();

                    if (routeResponse.isSuccess() && routeResponse.getData() != null &&
                            routeResponse.getData().getRoutes() != null &&
                            !routeResponse.getData().getRoutes().isEmpty()) {

                        // Get the first route
                        RouteResponse.Route route = routeResponse.getData().getRoutes().get(0);

                        // Display route info
                        String distance = (route.getDistanceMeters() / 1000.0) + " km";
                        String duration = formatDuration(route.getDuration());
                        String routeInfo = distance + " • " + duration;

                        // Update the search summary text
                        searchSummaryText.setText(routeInfo);

                        // Draw the route on the map
                        drawRoute(route.getPolyline().getEncodedPolyline(), origLat, origLng, destLat, destLng);

                        // Get maps link after route is drawn
                        getMapsLink(origLat, origLng, destLat, destLng);

                        // Collapse the search panel to show the route
                        collapseSearchPanel();
                    } else {
                        Toast.makeText(requireContext(), "No routes found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle error response
                    try {
                        if (response.errorBody() != null) {
                            String errorJson = response.errorBody().string();
                            Log.e("RouteError", "Error response: " + errorJson);
                            Toast.makeText(requireContext(), "Error: " + errorJson, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Error retrieving route: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("RouteError", "Error parsing error response", e);
                        Toast.makeText(requireContext(), "Error retrieving route: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<RouteResponse> call, Throwable t) {
                resetSearchButton();
                Log.e("RouteError", "Network failure", t);
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        getMapsLink(origLat, origLng, destLat, destLng);

    }

    private void resetSearchButton() {
        searchRouteButton.setEnabled(true);
        searchRouteButton.setText("Cari Rute");
    }

    private String formatDuration(String duration) {
        // Format duration from "1124s" to "18 min"
        if (duration == null || !duration.endsWith("s")) {
            return "Unknown";
        }

        try {
            String secondsStr = duration.substring(0, duration.length() - 1);
            int seconds = Integer.parseInt(secondsStr);
            int minutes = seconds / 60;

            return minutes + " min";
        } catch (Exception e) {
            return "Unknown";
        }
    }


    private void drawRoute(String encodedPolyline, double origLat, double origLng, double destLat, double destLng) {
        if (mMap == null) return;

        // Clear previous route if exists
        if (currentRoute != null) {
            currentRoute.remove();
        }

        // Remove previous markers
        mMap.clear();

        // Decode the polyline
        List<LatLng> decodedPath = PolylineDecoder.decode(encodedPolyline);

        if (decodedPath.isEmpty()) {
            Toast.makeText(requireContext(), "Invalid route data", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create polyline options
        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(decodedPath)
                .width(12)
                .color(Color.parseColor("#4CAF50"))  // Green color
                .geodesic(true);

        // Add the polyline to the map
        currentRoute = mMap.addPolyline(polylineOptions);

        // Add markers for origin and destination
        LatLng origin = new LatLng(origLat, origLng);
        LatLng destination = new LatLng(destLat, destLng);

        // Add origin marker
        mMap.addMarker(new MarkerOptions()
                .position(origin)
                .title("Origin")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        // Add destination marker
        mMap.addMarker(new MarkerOptions()
                .position(destination)
                .title("Destination")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        // Build bounds to include both origin and destination
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boundsBuilder.include(origin);
        boundsBuilder.include(destination);

        // Include all points in the polyline
        for (LatLng point : decodedPath) {
            boundsBuilder.include(point);
        }

        // Create bounds with padding
        final LatLngBounds bounds = boundsBuilder.build();

        // Animate camera to show the entire route
        int padding = 100; // Padding in pixels
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

        // Make sure the navigation button is visible after drawing the route
        if (startNavigationButton != null && googleMapsLink != null && !googleMapsLink.isEmpty()) {
            startNavigationButton.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Default location (Yogyakarta City Center)
        LatLng yogyakarta = new LatLng(-7.797068, 110.370529);

        // Set the map's initial position and zoom level
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(yogyakarta, 13));

        // Initialize and add markers for bus routes
        initializeBusRouteData();
        addMarkersForBusRoutes();

        // Check for location permission and enable user location if granted
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            // Zoom to user's location once map is ready
            getUserLocation();
        } else {
            // Request location permission if not granted
            requestLocationPermission();
        }

        // Add map click listener to dismiss autocomplete and keyboard
        mMap.setOnMapClickListener(latLng -> {
            hideAutocomplete();
            originEditText.clearFocus();
            destinationEditText.clearFocus();
        });
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void addMarkersForBusRoutes() {
        // Implementation for adding bus route markers
    }

    private void initializeBusRouteData() {
        // Initialize bus route data
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        // Save current location
                        currentUserLocation = new LatLng(location.getLatitude(), location.getLongitude());

                        // Add marker at user's current location
                        mMap.addMarker(new MarkerOptions()
                                .position(currentUserLocation)
                                .title("Lokasi Anda Saat Ini")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                        // Zoom to user location with zoom level 15
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUserLocation, 15));
                    } else {
                        Toast.makeText(requireContext(), "Unable to get your current location", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error getting location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location permission granted, enable location and zoom to user location
                if (mMap != null) {
                    try {
                        mMap.setMyLocationEnabled(true);
                        getUserLocation();
                    } catch (SecurityException e) {
                        Toast.makeText(requireContext(), "Location permission exception", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // Autocomplete request class
    public static class AutocompleteRequest {
        private String text;

        public AutocompleteRequest(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    private void openGoogleMaps() {
        if (googleMapsLink != null && !googleMapsLink.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(googleMapsLink));
            intent.setPackage("com.google.android.apps.maps");

            // Check if Google Maps is installed
            if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                // If Google Maps is not installed, open in browser
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(googleMapsLink));
                startActivity(browserIntent);
            }
        } else {
            Toast.makeText(requireContext(), "Navigation link not available", Toast.LENGTH_SHORT).show();
        }
    }



    // Also modify the getMapsLink method to ensure button visibility is updated
    private void getMapsLink(double origLat, double origLng, double destLat, double destLng) {
        String authHeader = sessionManager.getAuthorizationHeader();
        if (authHeader == null) {
            return;
        }

        // Create request for maps link
        MapsLinkRequest request = new MapsLinkRequest(origLat, origLng, destLat, destLng);

        Call<MapsLinkResponse> call = apiService.getMapsLink(authHeader, request);
        call.enqueue(new Callback<MapsLinkResponse>() {
            @Override
            public void onResponse(Call<MapsLinkResponse> call, Response<MapsLinkResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MapsLinkResponse mapsResponse = response.body();

                    if (mapsResponse.isSuccess() && mapsResponse.getData() != null) {
                        // Store the link
                        googleMapsLink = mapsResponse.getData().getLink();

                        // Show the navigation button
                        if (startNavigationButton != null) {
                            startNavigationButton.setVisibility(View.VISIBLE);
                            Log.d("NavigationButton", "Button should be visible now");
                        }
                    }
                } else {
                    Log.e("MapsLink", "Failed to get maps link: " +
                            (response.errorBody() != null ? response.errorBody().toString() : "unknown error"));
                }
            }

            @Override
            public void onFailure(Call<MapsLinkResponse> call, Throwable t) {
                Log.e("MapsLink", "Network error getting navigation link", t);
                Toast.makeText(requireContext(), "Error getting navigation link", Toast.LENGTH_SHORT).show();
            }
        });
    }
}