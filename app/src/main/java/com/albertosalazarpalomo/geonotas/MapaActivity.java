package com.albertosalazarpalomo.geonotas;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.albertosalazarpalomo.geonotas.fragment.NotasMapFragment;

/**
 * Activity encargada de mostrar en un mapa todas las notas
 */
public class MapaActivity extends BaseActivity implements INotasListFragment {
    private static final String TAG = "MapaActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);

        // Configuramos el título de la Activity
        this.setTitle(R.string.title_activity_mapa);

        // Configuramos la Action Bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configuramos el botón de "Volver"
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Visualizamos el fragment
        mostrarNotas();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            // Por si pulsamos el botón de "volver"
            case android.R.id.home:
                this.finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Método que debo implementar para comunicarme con NotasListFragment o NotasMapFragment
    @Override
    public void onPulsarMostrarNota(long idNota) {
        // Cambiamos el estado
        Intent intent = new Intent();
        intent.setClass(MapaActivity.this, DetallesNotaActivity.class);

        // Abrimos la Activity para editar una nota ya existente
        intent.putExtra(DetallesNotaActivity.ESTADO_TAG, DetallesNotaActivity.ESTADO_MOSTRAR);
        intent.putExtra(DetallesNotaActivity.ID_TAG, idNota);

        startActivity(intent);
    }

    private void mostrarNotas() {
        FragmentManager fm = getSupportFragmentManager();

        // Solo añadimos el fragment... ¡si no ha sido añadido ya!
        NotasMapFragment f = (NotasMapFragment) fm.findFragmentById(R.id.marcoFragments);

        if (f == null) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.marcoFragments, new NotasMapFragment());
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.commit();
        }
    }
}