package com.albertosalazarpalomo.geonotas.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.albertosalazarpalomo.geonotas.INotasListFragment;
import com.albertosalazarpalomo.geonotas.R;
import com.albertosalazarpalomo.geonotas.dataprovider.URIUtils;
import com.albertosalazarpalomo.geonotas.util.GPSUtil;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Aquí mostraremos todas las picas de las notas, en su última localización disponible;
 * no implemento salvar y recuperar estado porque los marcadores no pueden ser cambiados
 * por el usuario
 */
public class NotasMapFragment extends SupportMapFragment implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        LocationListener
{
    private static final String TAG = "NotasMapFrag";

    private GoogleMap myMap;
    private INotasListFragment fragmentListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            fragmentListener = (INotasListFragment) context;
        } catch (ClassCastException castException) {
            Log.e(TAG, "La actividad no implementa el método adecuado");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (googleMap != null) {
            myMap = googleMap;

            // Comprobamos si tenemos permiso para acceder al GPS
            if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Establecemos un evento al pulsar en el botón de "Sitúame según el GPS"
                myMap.setMyLocationEnabled(true);
                myMap.setOnMyLocationButtonClickListener(this);
            }

            // Set the color of the marker to red
            BitmapDescriptor defaultMarker =
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);

            // Inicializamos las coordenadas de todas las notas (la actual)
            /* Obtenemos el último par Coordenadas-Nota (con el id Nota proporcionado) */
            Uri uri2 = URIUtils.UCoordenadasNotaLast.CONTENT_URI;
            Cursor c2 = getActivity()
                    .getContentResolver()
                    .query(uri2, null, null, null, null);

            LatLng lastLatLng = null;

            // Si hay algún resultado, operamos con él
            if (c2 != null && c2.moveToFirst()) {
                do {
                    //a._id, a.id_nota, a.titulo, a.descripcion, a.fecha, c.latitud, c.longitud
                    Integer idDelNota = (int) c2.getLong(1);
                    Double latitud = c2.getDouble(3);
                    Double longitud = c2.getDouble(4);

                    LatLng latLng = new LatLng(latitud, longitud);

                    // Create the marker on the fragment
                    Marker mapMarker = myMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("")
                            .snippet(String.valueOf(idDelNota))
                            .icon(defaultMarker));
                    final long id = idDelNota;

                    // Attach marker click listener to the map here
                    myMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        public boolean onMarkerClick(Marker marker) {
                            // Handle marker click here
                            String snippet = marker.getSnippet();
                            Long id = Long.parseLong(snippet);

                            fragmentListener.onPulsarMostrarNota(id);
                            return true;
                        }
                    });

                    // Almacenamos el valor de la última posición
                    lastLatLng = latLng;
                } while (c2.moveToNext());

                // Si hemos obtenido la posición de la última nota, enfocamos ahí
                if (lastLatLng != null) {
                    //myMap.moveCamera(CameraUpdateFactory.newLatLng(lastLatLng));
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(lastLatLng);
                    myMap.animateCamera(cameraUpdate);
                }
            }
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        LocationManager mgr = (LocationManager) getActivity()
                .getSystemService(Context.LOCATION_SERVICE);

        if (!mgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            GPSUtil.mostrarDialogoGPS(getActivity());
        }

        return false;
    }

    @Override
    public void onLocationChanged(Location location) {
    }
}