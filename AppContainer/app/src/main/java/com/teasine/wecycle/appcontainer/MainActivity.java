package com.teasine.wecycle.appcontainer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.PopupMenu;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    //DECLARACION DE VARIABLES GLOBALES
    SpeedDialView speedDialView;
    Integer[] showOnMap = new Integer[6];
    private LocationManager locationManager;
    private Location location;
    private Context context;
    private FusedLocationProviderClient fusedLocationClient;
    private MapsMarkerActivity map;
    private LottieAnimationView animCargando;
    private View animBackground;
    private static MainActivity instance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Making status bar transparent (There is a bug with the filter pop up)
       /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }*/

        instance = this;
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        animCargando = findViewById(R.id.animTrash);
        animCargando.playAnimation();

        animBackground = findViewById(R.id.background);

        // acceder speed dial
        speedDialView = findViewById(R.id.speedDial);

        context = this;

        //------------------------------------ Animación cargando ------------------------------------------------

        animCargando.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Log.e("Animation:", "start");
                animCargando.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.e("Animation:", "end");
                animCargando.setVisibility(View.GONE);
                animBackground.setVisibility(View.GONE);
                speedDialView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                Log.e("Animation:", "cancel");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                Log.e("Animation:", "repeat");
            }
        });


        //------------------------------------ Obtengo mi localización ------------------------------------------------

        // Primero obtenemos la localizacion. Despues podemos usar el fusion location service pq ya hay una localizacion
        // guardada. Esto lo necesitamos pq fusion location service solo funciona si no es la primera vez que busca una
        // localizacion, es decir, ya hay una de antes en el movil (de haber abierto maps por ejemplo). Si no hacemos
        // esto, la primera vez que alguien abre la app despues de reiniciarlo, por ejemplo,
        // fusedLocationClient.getLastLocation() devuelve null ya que no tiene datos de localizacion de antes.
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.getFusedLocationProviderClient(context).requestLocationUpdates(mLocationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
            }
        }, null);

        // Ahora ya podemos trabajar con el fusion location manager (es mucho mas ligero que el normal)
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            //Creación del mapa
                            map = new MapsMarkerActivity(context, locationManager, location);
                            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                            mapFragment.getMapAsync(map);

                            //Mover controles googleMaps donde queramos
                            View toolbar = ((View) mapFragment.getView().findViewById(Integer.parseInt("1")).
                                    getParent()).findViewById(Integer.parseInt("4"));

                            // and next place it
                            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) toolbar.getLayoutParams();
                            // position on left bottom
                            rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
                            rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                            rlp.setMargins(1000000, 0, 0, 80);

                        }
                    }
                });

        // ---------------------------------- FAB SPEED DIAL -------------------------------------------------------------------

        // cambiar icono del fab principal
        speedDialView.setMainFabClosedDrawable(MaterialDrawableBuilder.with(this.getBaseContext()) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.DOTS_HORIZONTAL) // provide an icon
                .setColor(getResources().getColor(R.color.white)) // set the icon color
                .setToActionbarSize() // set the icon size
                .build());

        speedDialView.setMainFabClosedBackgroundColor(getResources().getColor(R.color.colorPrimary));
        speedDialView.setMainFabOpenedBackgroundColor(getResources().getColor(R.color.colorAccent));

        // rotacion de abrir/cerrar fab a 90º para que gire de hor a vert
        speedDialView.setMainFabAnimationRotateAngle(90);

        // action item filtro, añade icono de filter
        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder((R.id.filter), MaterialDrawableBuilder.with(this.getBaseContext()) // provide a context
                        .setIcon(MaterialDrawableBuilder.IconValue.FILTER_VARIANT) // provide an icon
                        .setColor(getResources().getColor(R.color.white))
                        .setToActionbarSize() // set the icon size
                        .build())
                        // texto al lado del fab
                        .setLabel(getString(R.string.filter))
                        .create()
        );
        /*
        // action item settings, añade icono de settings
        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.settings, MaterialDrawableBuilder.with(this.getBaseContext()) // provide a context
                        .setIcon(MaterialDrawableBuilder.IconValue.SETTINGS_OUTLINE) // provide an icon
                        .setColor(Color.WHITE) // set the icon color
                        .setToActionbarSize() // set the icon size
                        .build())
                        // texto al lado del fab
                        .setLabel(getString(R.string.settings))
                        .create()
        );*/

        // action item info, añade icono de info
        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.info, MaterialDrawableBuilder.with(this.getBaseContext()) // provide a context
                        .setIcon(MaterialDrawableBuilder.IconValue.INFORMATION_OUTLINE) // provide an icon
                        .setColor(getResources().getColor(R.color.white)) // set icon color
                        .setToActionbarSize() // set the icon size
                        .build())
                        // texto al lado del fab
                        .setLabel(getString(R.string.info))
                        .create()
        );

        // callback listener de pulsar settings o filtro
        speedDialView.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem speedDialActionItem) {
                switch (speedDialActionItem.getId()) {
                    case R.id.settings:
                        // settings action
                        /*final AjustesDialogFragment ajustesDialogFragment = new AjustesDialogFragment();
                        if (!ajustesDialogFragment.isAdded()) ajustesDialogFragment.show(getSupportFragmentManager(), "");
                        speedDialView.close();*/
                        return true;
                    case R.id.filter:
                        // filter action
                        boolean open = showFilterMenu(findViewById(R.id.filter));
                        return true; // cierra el fab sin animacion
                    case R.id.info:
                        // info fragment
                        final InfoDialogFragment infoDialogFragment = new InfoDialogFragment();
                        if (!infoDialogFragment.isAdded()) infoDialogFragment.show(getSupportFragmentManager(), "");
                        //speedDialView.close();
                        return true;
                    default:
                        return true; // true to keep the Speed Dial open
                }
            }
        });

        // ---------------------------------------------------------------------------------------------------------------

        // ------------ Array checked filtrado---------------------------------------------------

        // Hacemos checked todos los filtros al iniciar la app (aparecen todos los contenedores)
        selectShowAllBins();

    }//OnCreate()

    public static MainActivity getInstance() {
        return instance;
    }

    @SuppressLint("RestrictedApi")
    public boolean showFilterMenu(View anchor) {

        final PopupMenu popup = new PopupMenu(this, anchor, R.style.FilterPopup);

        Menu menu = popup.getMenu();

        // Forzamos la muestra de los iconos en el menu, solo funciona si las imagenes son pequenyas (max 120px aprox)
        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }

        // Infla el menu
        popup.getMenuInflater().inflate(R.menu.filter_menu, popup.getMenu());

        // Antes de mostrar el menu del popup miramos si estaba checked o no, y lo mostramos como tal
        for (int i = 0; i < showOnMap.length; i++) {
            if (showOnMap[i] == 1) {
                // Mostramos que sea checked
                popup.getMenu().getItem(i).setChecked(true);
            }
        }
        popup.show();


        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // cambia el checked del item cuando es pulsado
                item.setChecked(!item.isChecked());

                // Si deseleccionamos uno, deseleccionamos el check de 'select all'
                if (item.getItemId() != R.id.selectAllFilter && !item.isChecked()) popup.getMenu().getItem(5).setChecked(false);

                // Keep the popup menu open -------------------------------------------------------
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                item.setActionView(new View(getBaseContext()));
                item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return false;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        return false;
                    }
                });
                // --------------------------------------------------------------------------------

                // El switch cambia el checked del item dependiendo del item
                // -- falta implementar el filtrado real de los contenedores
                switch (item.getItemId()) {
                    case R.id.plasticFilter:
                        if (item.isChecked()) showOnMap[0] = 1;
                        else showOnMap[0] = 0;
                        refrescarMarcadores();
                        return false;
                    case R.id.glassFilter:
                        if (item.isChecked()) showOnMap[1] = 1;
                        else showOnMap[1] = 0;
                        refrescarMarcadores();
                        return false;
                    case R.id.organicFilter:
                        if (item.isChecked()) showOnMap[2] = 1;
                        else showOnMap[2] = 0;
                        refrescarMarcadores();
                        return false;
                    case R.id.paperFilter:
                        if (item.isChecked()) showOnMap[3] = 1;
                        else showOnMap[3] = 0;
                        refrescarMarcadores();
                        return false;
                    case R.id.wasteFilter:
                        if (item.isChecked()) showOnMap[4] = 1;
                        else showOnMap[4] = 0;
                        refrescarMarcadores();
                        return false;
                    case R.id.selectAllFilter:
                        // Si lo seleccionamos, mostramos checked todos los filtros
                        if (item.isChecked()) {
                            showOnMap[5] = 1;
                            selectShowAllBins();
                            // Mostramos todos checked
                            for (int i = 0; i < popup.getMenu().size(); i++) {
                                popup.getMenu().getItem(i).setChecked(true);
                            }
                        }
                        // Si lo deseleccionamos, deseleccionamos todos los filtros
                        else {
                            // Mostramos todos no checked
                            for (int i = 0; i < popup.getMenu().size(); i++) {
                                showOnMap[i] = 0;
                                popup.getMenu().getItem(i).setChecked(false);
                            }
                        }
                        refrescarMarcadores();
                        return false;
                    default:
                        return false;
                }
            }
        });

        return true;
    }

    //-------------------------------------------------------------------------------------------
    //
    // -> refrescarMarcadores() ->
    //
    // Refresca los marcadores en el mapa
    //-------------------------------------------------------------------------------------------
    public void refrescarMarcadores() {
        // Solo si ha cargado marcadores
        if (map.marcadores != null) {
            // Para cada item anterior, comprobamos si el filtro está seleccionado y si es así lo
            // añadimos otra vez
            for (Marker marker : map.marcadoresPlastic) {
                if (showOnMap[0] == 1) {
                    marker.setVisible(true);
                } else marker.setVisible(false);
            }
            for (Marker marker : map.marcadoresGlass) {
                if (showOnMap[1] == 1) {
                    marker.setVisible(true);
                } else marker.setVisible(false);
            }
            for (Marker marker : map.marcadoresOrganic) {
                if (showOnMap[2] == 1) {
                    marker.setVisible(true);
                } else marker.setVisible(false);
            }
            for (Marker marker : map.marcadoresPaper) {
                if (showOnMap[3] == 1) {
                    marker.setVisible(true);
                } else marker.setVisible(false);
            }
            for (Marker marker : map.marcadoresWaste) {
                if (showOnMap[4] == 1) {
                    marker.setVisible(true);
                } else marker.setVisible(false);
            }
        }
    }

    // Hacemos checked todos los filtros al iniciar la app (aparecen todos los contenedores)
    public void selectShowAllBins() {
        for (int i = 0; i < showOnMap.length; i++) {
            showOnMap[i] = 1;
        }
    }

    //-------------------------------------------------------------------------------------------
    //
    // -> hideAllBins() ->
    //
    // Escondemos todos los contenedores
    //-------------------------------------------------------------------------------------------
    public void hideAllBins() {
        // Para cada marcador, lo escondemos
        for (List<Marker> listaMarcadores : map.marcadores) {
            for (Marker marker : listaMarcadores) {
                marker.setVisible(false);
            }
        }
    }

}