package com.teasine.wecycle.appcontainer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class MapsMarkerActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener, GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraMoveCanceledListener {

    //DECLARACION DE VARIABLES GLOBALES
    Context context;
    private static final String TAG = MapsMarkerActivity.class.getSimpleName();
    LocationManager locationManager;
    private LogicaFake laLogica;
    private Location location;
    public List<List<Marker>> marcadores = new ArrayList<>();
    public List<Marker> marcadoresPaper = new ArrayList<>();
    public List<Marker> marcadoresPlastic = new ArrayList<>();
    public List<Marker> marcadoresWaste = new ArrayList<>();
    public List<Marker> marcadoresOrganic = new ArrayList<>();
    public List<Marker> marcadoresGlass = new ArrayList<>();
    public GoogleMap googleMap;


    public MapsMarkerActivity(Context context_, LocationManager locationManager_, Location location_) {
        this.context = context_;
        this.locationManager = locationManager_;
        this.location = location_;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;

        //Mi posición
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }

        //Quito la opcion navegacion
        googleMap.getUiSettings().setMapToolbarEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(false);

        // Asignar los listeners para los movimientos de camara
        googleMap.setOnCameraIdleListener(this);
        googleMap.setOnCameraMoveStartedListener(this);
        googleMap.setOnCameraMoveCanceledListener(this);

        // Zoom out maximo que queremos que pueda hacer el user
        // 5 -> landmass / continent
        googleMap.setMinZoomPreference(5.0f);

        laLogica = new LogicaFake();


        //Cambio de estilo de maps
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            context, R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        // Marker pngs to small bitmaps
        int height = 150;
        int width = 150;
        final Bitmap markerPlastic = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_plastic), width, height, false);
        final Bitmap markerGlass = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_glass), width, height, false);
        final Bitmap markerOrganic = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_organic), width, height, false);
        final Bitmap markerWaste = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_restos), width, height, false);
        final Bitmap markerPaper = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_paper), width, height, false);

        // zoom camera
        //googleMap.animateCamera( CameraUpdateFactory.zoomTo( 50.0f ) );

        // Animar camara a mi localizacion
        if (location != null) {
            //location.setLatitude(location.getLatitude());
            //location.setLongitude(location.getLongitude());
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                    .zoom(18)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            //Obtener contenedores de Valencia del servidor
            laLogica.obtenerContenedoresValencia(0.4f, location.getLatitude(), location.getLongitude(), new PeticionarioREST.Callback() {
                @Override
                public void respuestaRecibida(int codigo, String cuerpo) {
                    try {
                        JSONArray jsonArrayMedidas = new JSONArray(cuerpo);

                        for (int i = 0; i < jsonArrayMedidas.length(); i++) {

                            JSONObject object = jsonArrayMedidas.getJSONObject(i);

                            Integer idTipoContenedor = object.getInt("IdTipoContenedor");
                            Double longitud = object.getDouble("Longitud");
                            Double latitud = object.getDouble("Latitud");

                            LatLng marcador = new LatLng(latitud, longitud);

                            Log.d("IDTIPOCONTENEDOR", idTipoContenedor.toString());

                            switch (idTipoContenedor) {
                                case 1:
                                    // Add a marker
                                    marcadoresPlastic.add(googleMap.addMarker(new MarkerOptions().position(marcador)
                                            .title("Plastic").icon(BitmapDescriptorFactory.fromBitmap(markerPlastic))));
                                    break;
                                case 2:
                                    // Add a marker
                                    marcadoresPaper.add(googleMap.addMarker(new MarkerOptions().position(marcador)
                                            .title("Paper").icon(BitmapDescriptorFactory.fromBitmap(markerPaper))));
                                    break;
                                case 3:
                                    marcadoresOrganic.add(googleMap.addMarker(new MarkerOptions().position(marcador)
                                            .title("Organic").icon(BitmapDescriptorFactory.fromBitmap(markerOrganic))));
                                    break;
                                case 4:
                                    marcadoresGlass.add(googleMap.addMarker(new MarkerOptions().position(marcador)
                                            .title("Glass").icon(BitmapDescriptorFactory.fromBitmap(markerGlass))));
                                    break;
                                case 5:
                                    marcadoresWaste.add(googleMap.addMarker(new MarkerOptions().position(marcador)
                                            .title("Waste").icon(BitmapDescriptorFactory.fromBitmap(markerWaste))));
                                    break;
                                default:
                                    // code block
                            }
                            // Move the map's camera to the same location
                            // googleMap.moveCamera(CameraUpdateFactory.newLatLng(marcador));
                        }

                        marcadores.add(marcadoresPlastic);
                        marcadores.add(marcadoresGlass);
                        marcadores.add(marcadoresOrganic);
                        marcadores.add(marcadoresPaper);
                        marcadores.add(marcadoresWaste);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        }// si encuentra la localizacion
    }

    @Override
    public void onCameraIdle() {
        Log.d("Camera", "Camera stopped moving");
        Log.d("Camera", Float.toString(googleMap.getCameraPosition().zoom));
        if (googleMap.getCameraPosition().zoom < 17.0f) {
            Toast.makeText(context, "Zoom in to see the recycling bins", Toast.LENGTH_LONG).show();
            Log.d("Camera", "Too far away");
            MainActivity.getInstance().hideAllBins();
        } else MainActivity.getInstance().refrescarMarcadores();
    }

    @Override
    public void onCameraMoveCanceled() {
        Log.d("Camera", "Camera cancelled moving");
    }

    @Override
    public void onCameraMoveStarted(int i) {
        Log.d("Camera", "Camera started moving");
    }
}