package com.albertosalazarpalomo.geonotas.fragment;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.albertosalazarpalomo.geonotas.DetallesNotaActivity;
import com.albertosalazarpalomo.geonotas.R;
import com.albertosalazarpalomo.geonotas.model.Coordenadas;
import com.albertosalazarpalomo.geonotas.util.GPSUtil;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Pestaña "MAPA" de "DetallesActivity":
 */
public class GeolocNotaFragment extends Fragment implements IDetalles,
        OnMapReadyCallback, GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMyLocationButtonClickListener,
        LocationListener {
    private static final String TAG = "GeolocNotaFrag";
    private static final String TAG_LATITUD = "1";
    private static final String TAG_LONGITUD = "2";
    private static final String TAG_GUARDAR_ANTIGUA_POS = "3";
    private static final String TAG_MARCADOS_CREADO = "4";

    // Con esta interface, nos comunicaremos con la Activity
    private INotaFragment interfaceActivity;
    private GoogleMap mMap;

    // Posición guardada (o por guardar!)
    private MarkerOptions posicionMarcador;
    private boolean marcadorCreado; // Tiene un valor true, cuando hay un marcador

    // ¿Guadaremos la anterior posición en un histórico de posiciones?
    private boolean guardarAntiguaPosHistorico = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            interfaceActivity = (INotaFragment) context;
        } catch (ClassCastException castException) {
            Log.e(TAG, "La actividad no implementa el método adecuado");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_geoloc_nota, container, false);

        // Configuramos un poco el mapa, y lo visualizamos
        setupAndShowMapa();

        // Lo configuramos all por si se intenta visualizar o editar una Nota
        int estadoActivity = interfaceActivity.getEstado();

        switch (estadoActivity) {
            case DetallesNotaActivity.ESTADO_EDITAR:
            case DetallesNotaActivity.ESTADO_MOSTRAR:
                cargarCampos();
                break;
        }

        // Configuramos el switch que nos permite cambiar del "Modo listado-texto" al "Modo Mapa"
        SwitchCompat switchHG = (SwitchCompat) v.findViewById(R.id.switchHistoricoGeoloc);
        switchHG.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    guardarAntiguaPosHistorico = true;
                } else {
                    guardarAntiguaPosHistorico = false;
                }
            }
        });

        // Este switch solo estará disponibles si estamos editando una nota, como es lógico
        if (estadoActivity != DetallesNotaActivity.ESTADO_EDITAR) {
            switchHG.setEnabled(false);
        }

        // Y configuramos el botón de borrar pantalla
        FloatingActionButton floatingButtonBorrarFoto = interfaceActivity.getFloatingButtonBorrarFoto();

        if (estadoActivity == DetallesNotaActivity.ESTADO_CREAR
                || estadoActivity == DetallesNotaActivity.ESTADO_EDITAR) {
            floatingButtonBorrarFoto.setVisibility(View.VISIBLE);
        }

        floatingButtonBorrarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Borramos el mapa, y el marcador de la memoria, ¡si hay marcador!
                if (marcadorCreado) {
                    mMap.clear();
                    marcadorCreado = false;

                    // Y ponemos a null tanto longitud como latitud, en Coordenadas
                    // (si su id tiene un valor, lo interpretará la Activity como "eliminar")
                    // Sólo si pulsamos "GUARDAR"!
                    Coordenadas coordenadas = interfaceActivity.getCoordenadas();

                    if (coordenadas != null) {
                        Integer id = coordenadas.getId();

                        Coordenadas borrarCoordenadas = Coordenadas.newBuilder()
                                .withLatitud(null)
                                .withLongitud(null)
                                .withId(id)
                                .build();
                        interfaceActivity.setCoordenadas(borrarCoordenadas);
                    }
                }
            }
        });

        return v;
    }

    private void setupAndShowMapa() {
        // Solo añadimos el fragment... ¡si no ha sido añadido ya!
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        SupportMapFragment f = (SupportMapFragment) fm.findFragmentById(R.id.mapFrameLayout);

        if (f == null) {
            f = SupportMapFragment.newInstance();
            ft.replace(R.id.mapFrameLayout, f);
            ft.commit();
        }

        // Fuera null o no, ahora ya podemos asignarle un handler
        f.getMapAsync(this);
    }

    // Para cuando hacemos click en un botón de la Activity padre
    @Override
    public boolean onClickGuardar() {
        boolean exito = false;

        guardarDatosNotaActivity();

        exito = true;

        return exito;
    }

    // Para cuando hacemos ocurre un cambio de estado en la Activity padre
    @Override
    public boolean onCambioEstado() {
        return true;
    }

    @Override
    public void onDestroyView() {
        int estado = interfaceActivity.getEstado();

        // Solo guardamos cambios si no estamos en modo "MOSTRAR"
        if (estado == DetallesNotaActivity.ESTADO_CREAR
                || estado == DetallesNotaActivity.ESTADO_EDITAR) {
            guardarDatosNotaActivity();
        }

        // Fuera de este fragment, los botones flotantes propios se ocultan
        interfaceActivity.getFloatingButtonBorrarFoto()
                .setVisibility(View.GONE);

        super.onDestroyView();
    }

    private void guardarDatosNotaActivity() {
        // Guardamos las coordenadas establecidas
        Coordenadas coordenadasActivity = interfaceActivity.getCoordenadas();

        // Si no ha sido creado un marcador, entonces se mandarán a "null" latitud y longitud,
        // y así la Activity "sabrá" que no hay que guardar las Coordenadas
        Integer idCoordenadas = null;
        Double latitud = null;
        Double longitud = null;

        if (coordenadasActivity != null) {
            idCoordenadas = coordenadasActivity.getId();
            latitud = coordenadasActivity.getLatitud();
            longitud = coordenadasActivity.getLongitud();
        }

        // Si hemos creado un marcador, entonces ponemos sus datos de geoloc
        if (marcadorCreado) {
            LatLng latLng = posicionMarcador.getPosition();
            latitud = latLng.latitude;
            longitud = latLng.longitude;
        }

        // Si está activo el Switch "Guardar Antigua Posición", crea uno nuevo
        if (guardarAntiguaPosHistorico) {
            idCoordenadas = null;
        }

        Coordenadas coordenadas = Coordenadas.newBuilder()
                .withLatitud(latitud)
                .withLongitud(longitud)
                .withId(idCoordenadas)
                .build();

        interfaceActivity.setCoordenadas(coordenadas);
    }

    // Método para cargar los datos de una nota que se va a editar / visualizar
    private void cargarCampos() {
        // Establecemos las coordenadas
        Coordenadas coordenadasActivity = interfaceActivity.getCoordenadas();

        // Y configuramos el marcador si hemos obtenido valores
        if (coordenadasActivity != null && coordenadasActivity.getLatitud() != null
                && coordenadasActivity.getLongitud() != null) {
            // Preparamos el marcador
            posicionMarcador = new MarkerOptions();

            Double latitude = coordenadasActivity.getLatitud();
            Double longitude = coordenadasActivity.getLongitud();

            LatLng latLng = new LatLng(latitude, longitude);
            posicionMarcador.position(latLng);
        }
    }

    /*
        Inicializamos el mapa con la última posición registrada en la DB, y preparamos el
        evento de "click largo"
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (googleMap != null) {
            mMap = googleMap;

            // Comprobamos si tenemos permiso para acceder al GPS
            if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Establecemos un evento al pulsar en el botón de "Sitúame según el GPS"
                mMap.setMyLocationEnabled(true);
                mMap.setOnMyLocationButtonClickListener(this);
            }

            // Creamos un evento para cuando se realiza un click largo
            mMap.setOnMapLongClickListener(this);

            // Mostramos el marcador
            if (posicionMarcador != null) {
                mMap.addMarker(posicionMarcador);
                marcadorCreado = true;

                mMap.moveCamera(CameraUpdateFactory.newLatLng(posicionMarcador.getPosition()));
            }
        }
    }

    // Se dispara cuando se pulsa el mapa de forma prolongada
    @Override
    public void onMapLongClick(final LatLng point) {
        // Solo permitiremos cambiar de posición si no estamos en estado CREAR
        int estado = interfaceActivity.getEstado();

        if (estado == DetallesNotaActivity.ESTADO_EDITAR
                || estado == DetallesNotaActivity.ESTADO_CREAR) {
            // Borramos del mapa el antiguo marcador...
            mMap.clear();

            // Y creamos una posición nueva, mostrándola en el mapa
            posicionMarcador = new MarkerOptions();
            posicionMarcador.position(point);
            mMap.addMarker(posicionMarcador);
            marcadorCreado = true;
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        LocationManager mgr = (LocationManager) getActivity()
                .getSystemService(Context.LOCATION_SERVICE);

        if (!mgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(getActivity()
                    .getApplicationContext(), R.string.gps_desactivado, Toast.LENGTH_SHORT)
                    .show();

            GPSUtil.mostrarDialogoGPS(getActivity());
        }

        return false;
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.w(TAG, "Pasa por restaurar 2");

        if (savedInstanceState != null) {
            // Recuperamos todos los valores del mapa
            Double latitudS = savedInstanceState.getDouble(TAG_LATITUD);
            Double longitudS = savedInstanceState.getDouble(TAG_LONGITUD);
            boolean marcadorCreadoS = savedInstanceState.getBoolean(TAG_MARCADOS_CREADO);
            boolean guardarAntiguaPosHistoricoS = savedInstanceState.getBoolean(TAG_GUARDAR_ANTIGUA_POS);

            // Restauramos los estados imprescindibles
            marcadorCreado = marcadorCreadoS;
            guardarAntiguaPosHistorico = guardarAntiguaPosHistoricoS;

            // Y restablecemos el estado del marcador, si existe
            if (marcadorCreado) {
                LatLng latLng = new LatLng(latitudS, longitudS);
                posicionMarcador = new MarkerOptions();
                posicionMarcador.position(latLng);
            } else {
                posicionMarcador = null;
            }

            if (guardarAntiguaPosHistorico) {
                // No olvidemos restablecer el switch!
                final SwitchCompat switchHG = (SwitchCompat) getView()
                        .findViewById(R.id.switchHistoricoGeoloc);


                                switchHG.setChecked(true);

            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the fragment's state here
        Log.w(TAG, "Pasa por salvar 2");

        // Le ponemos unos valores tontos, para inicializarlos de todos modos
        Double latitud = 0.0;
        Double longitud = 0.0;

        // Por si se debe salvar latitud y longitud
        if (marcadorCreado) {
            LatLng latLng = posicionMarcador.getPosition();
            latitud = latLng.latitude;
            longitud = latLng.longitude;
        }

        outState.putDouble(TAG_LATITUD, latitud);
        outState.putDouble(TAG_LONGITUD, longitud);
        outState.putBoolean(TAG_MARCADOS_CREADO, marcadorCreado);
        outState.putBoolean(TAG_GUARDAR_ANTIGUA_POS, guardarAntiguaPosHistorico);
    }
}