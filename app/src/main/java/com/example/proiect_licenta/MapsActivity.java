package com.example.proiect_licenta;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.FragmentActivity;

import com.example.proiect_licenta.roadtrip.TravelingSalesmanProblem;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    //Inițializare variabile
    EditText editText;
    Button button_address;
    Button button_trip;
    SupportMapFragment mapFragment;
    Place currentPlace = null;

    private ArrayList<Place> placesToVisit = new ArrayList<>();

    public static final String GOOGLE_API_KEY = "AIzaSyDBV7v63UOCBlaJRN7B7IubAHUNXo_sfi4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //Asignare variabile
        editText = findViewById(R.id.edit_text);
        button_address = findViewById(R.id.button_add_address);
        button_trip = findViewById(R.id.button_make_trip);

        // Inițializare locuri(places)
        Places.initialize(getApplicationContext(), GOOGLE_API_KEY);
        //Setare EditText nefocusabil
        editText.setFocusable(false);
        editText.setOnClickListener(v -> {
            // Inițializare lista de câmpuri de locuri(places)
            List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);

            // Creare intenție
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(MapsActivity.this);

            // Start activitate
            startActivityForResult(intent, 100);
        });

        // Adăugare adrese la buton
        button_address.setOnClickListener(v -> {
            if(currentPlace != null && !placesToVisit.contains(currentPlace)){
                placesToVisit.add(currentPlace);
                this.addNewMarker(currentPlace);
            }
        });

        // Desenare polilinii între adrese
        button_trip.setOnClickListener(v -> {
            int placesCount = placesToVisit.size();
            double[][] distances = new double[placesCount][placesCount];
            String[][] polylines = new String[placesCount][placesCount];
            // Pentru fiecare loc i în fiecare loc j
            for(int i = 0; i < placesCount; ++i){
                for(int j = 0; j < placesCount; ++j){
                    if(i == j){
                        distances[i][j] = 0.0;
                        polylines[i][j] = "";
                    }else{
                        // Face o cerere pentru a obține distanța de la un loc la celălalt
                        // Și salvează distanța și polilinia în ele în matrice
                        Pair<Double, String> ways = getOnRoadDistanceBetween(
                                Objects.requireNonNull(placesToVisit.get(i).getLatLng()),
                                Objects.requireNonNull(placesToVisit.get(j).getLatLng()));
                        distances[i][j] = ways.first;
                        polylines[i][j] = ways.second;
                    }
                }
            }

            TravelingSalesmanProblem tsp = new TravelingSalesmanProblem(distances);
            tsp.solve();

            int[] path = tsp.getShortestPath();
            String text = "Distance: " + tsp.getDistance() + " meters";
            ((TextView)findViewById(R.id.textViewDistance)).setText(text);

            // Face o listă cu toate poliliniile
            ArrayList<LatLng> paths = new ArrayList<>();

            // Pentru fiecare punct din cea mai scurtă cale(shortest path)
            for(int i = 0; i < path.length; ++i){
                // Adaugă polilinia între un punct și următorul
                // Pentru ultimul punct trage calea de la acesta la primul, de aceea modulo lungime
                // Realizează un circuit hamiltonian
                List<LatLng> polyline = decodePoly(polylines[path[i]][path[(i+1) % path.length]]);
                paths.addAll(polyline);
            }

            mapFragment.getMapAsync(googleMap -> {
                int count = paths.size();
                // Pentru fiecare punct
                for(int i = 0; i < count; ++i){
                    PolylineOptions polylineOptions = new PolylineOptions();

                    // Setați culoarea să înceapă de la verde și să termine la roșu
                    // În HSL: 120, 1,0, 0,5 până la 0,0, 1,0, 0,5
                    // În timp ce bucle de la 0 la count - 1
                    float[] hsl = {120.0F - (float)i/count*120.0F, 1.0F, 0.5F};
                    // Converteste culoarea în RGB
                    int color = ColorUtils.HSLToColor(hsl);

                    // Setați lățimea și culoarea la polilinie
                    polylineOptions.width(10.0F);
                    polylineOptions.color(color);
                    // Și desenează de la un punct la următorul
                    polylineOptions.add(paths.get(i));
                    // Observați, de asemenea, modulo count, astfel încât ultimul obține o linie cu primul
                    polylineOptions.add(paths.get((i+1) % count));
                    // Desenează polilinia pe mapa
                    googleMap.addPolyline(polylineOptions);
                }
            });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100 && resultCode == RESULT_OK){
            // Când are succes
            // Inițializare loc(place)
            Place place = Autocomplete.getPlaceFromIntent(data);
            // Setează addresa în EditText
            editText.setText(place.getAddress());
            currentPlace = place;
        }else if(resultCode == AutocompleteActivity.RESULT_ERROR){
            // Când are eroare
            // Inițializare status
            Status status = Autocomplete.getStatusFromIntent(data);
            // Mesaj toast
            Toast.makeText(getApplicationContext(),status.getStatusMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    public void addNewMarker(Place location){
        mapFragment.getMapAsync(googleMap -> {
            googleMap.addMarker(new MarkerOptions().position(Objects.requireNonNull(location.getLatLng())).title(location.getName()));
            // Creează un constructor de limite(bounds builder)
            LatLngBounds.Builder builder = LatLngBounds.builder();
            for(Place p : placesToVisit){
                // Adaugă fiecare loc la constructorul de limite(bounds builder)
                builder.include(Objects.requireNonNull(p.getLatLng()));
            }
            // De la constructor generează limitele camerei pentru a include toate punctele din vedere
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
        });
    }

    public Pair<Double, String> getOnRoadDistanceBetween(LatLng pos1, LatLng pos2){
        String urlAddress = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                pos1.latitude + "," + pos1.longitude + "&destination=" + pos2.latitude + "," + pos2.longitude +
                "&units=metric&mode=driving&key=" + GOOGLE_API_KEY;
        // Referință atomică, deoarece acele valori vor fi setate dintr-un alt fir(thread)
        // după ce se face solicitarea
        AtomicReference<Double> distanceValue = new AtomicReference<>(0.0);
        AtomicReference<String> overviewPolyline = new AtomicReference<>("");
        // Creare fir(thread) nou pentru a efectua solicitarea către Serviciile Google
        Thread t = new Thread(() -> {
            try {
                URL url = new URL(urlAddress);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                int c;
                // Citeste răspunsul char de char până când EOF a fost găsit
                while((c = reader.read()) != -1){
                    responseBuilder.append((char)c);
                }
                String response = responseBuilder.toString();

                // Răspunsul este un obiect JSON
                // Creare un obiect JSON
                JSONObject jsonObject = new JSONObject(response);
                JSONArray array = jsonObject.getJSONArray("routes");
                JSONObject routes = array.getJSONObject(0);
                JSONArray legs = routes.getJSONArray("legs");
                JSONObject steps = legs.getJSONObject(0);
                JSONObject distance = steps.getJSONObject("distance");
                // Setare valori corespunzătoare ale distanței și ale punctelor rutiere până la destinație
                distanceValue.set(distance.getDouble("value"));
                overviewPolyline.set(routes.getJSONObject("overview_polyline").getString("points"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new Pair<>(distanceValue.get(), overviewPolyline.get());
    }

    private List<LatLng> decodePoly(String encoded) {

        // Această funcție decodează o polilinie codată din
        // Google Maps și produce o serie de puncte
        // Definit ca LatLng
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}