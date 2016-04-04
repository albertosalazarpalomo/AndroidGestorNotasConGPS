package com.albertosalazarpalomo.geonotas;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.albertosalazarpalomo.geonotas.fragment.NotasListFragment;
import com.albertosalazarpalomo.geonotas.fragment.NotasMapFragment;

/**
 * Activity que muestra en un Listview todas las notas, con sus datos principales
 */
public class MainActivity extends BaseActivity implements INotasListFragment {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // La barra de navegación
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configuramos el botón que nos llevará al listado de notas en el mapa
        Button mapaBoton = (Button) findViewById(R.id.mapaBoton);

        mapaBoton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Cambiamos el estado
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, MapaActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                startActivity(intent);
            }
        });

        // Configuramos el botón flotante (de añadir nota)
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        final Activity thisFinal = this;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                crearNuevoNota();
            }
        });

        // Para configurar el menu lateral
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(0).setChecked(true);
        navigationView.setNavigationItemSelectedListener(this);

        // Visualizamos el fragment de listado de notas (texto)
        mostrarNotas();
    }

    // Para cuando pulsemos el botón de "volver", de la barra de navegación

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // Métodos relacionados con los botones de la barra de navegación

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds notas to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar nota clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    // Método que debo implementar para comunicarme con NotasListFragment o NotasMapFragment
    @Override
    public void onPulsarMostrarNota(long idNota) {
        // Cambiamos el estado
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, DetallesNotaActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        // Abrimos la Activity para editar una nota ya existente
        intent.putExtra(DetallesNotaActivity.ESTADO_TAG, DetallesNotaActivity.ESTADO_MOSTRAR);
        intent.putExtra(DetallesNotaActivity.ID_TAG, idNota);

        startActivity(intent);
    }

    // Métodos propios

    private void mostrarNotas() {
        FragmentManager fm = getSupportFragmentManager();

        // Solo añadimos el fragment... ¡si no ha sido añadido ya!
        NotasListFragment f = (NotasListFragment) fm.findFragmentById(R.id.marcoFragments);

        if (f == null) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.marcoFragments, new NotasListFragment());
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.commit();
        }
    }

    private void crearNuevoNota() {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, DetallesNotaActivity.class);

        // Abrimos la Activity para crear un nuevo Nota
        intent.putExtra(DetallesNotaActivity.ESTADO_TAG, DetallesNotaActivity.ESTADO_CREAR);
        startActivity(intent);
    }
}