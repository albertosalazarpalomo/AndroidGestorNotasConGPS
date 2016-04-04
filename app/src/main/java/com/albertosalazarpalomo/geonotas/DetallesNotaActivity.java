package com.albertosalazarpalomo.geonotas;

import android.app.Activity;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.albertosalazarpalomo.geonotas.dataprovider.URIUtils;
import com.albertosalazarpalomo.geonotas.fragment.DetallesNotaFragment;
import com.albertosalazarpalomo.geonotas.fragment.FotosNotaFragment;
import com.albertosalazarpalomo.geonotas.fragment.GeolocNotaFragment;
import com.albertosalazarpalomo.geonotas.fragment.IDetalles;
import com.albertosalazarpalomo.geonotas.fragment.INotaFragment;
import com.albertosalazarpalomo.geonotas.fragment.NotasMapFragment;
import com.albertosalazarpalomo.geonotas.model.Coordenadas;
import com.albertosalazarpalomo.geonotas.model.CoordenadasNota;
import com.albertosalazarpalomo.geonotas.model.Nota;
import com.albertosalazarpalomo.geonotas.util.DateTimestamp;

/**
 * Activity donde muestro los detalles de cada nota
 */
public class DetallesNotaActivity extends AppCompatActivity
        implements INotaFragment {
    private static final String TAG = "DetallesNotaAct";

    // Variables relativas al estado (Edición, Visualización, o Creación de una Nota nueva)
    public static final String ESTADO_TAG = "estado";
    public static final int ESTADO_MOSTRAR = 1;
    public static final int ESTADO_CREAR = 2;
    public static final int ESTADO_EDITAR = 3;

    // Variables relacionadas con los nombres de las pestañas
    public static final String TAG_DETALLES_ITEM = "DETALLES";
    public static final String TAG_FOTO = "FOTO";
    public static final String TAG_LOCALIZACION = "LOCALIZACION";

    // Variable relacionadascon el id (en caso de que haya que editar-visualizar una nota)
    public static final String ID_TAG = "id";

    // Constantes usadas para salvar-recuperar el estado de la Activity
    public static final String TAG_SAVED_ESTADO = "TAG_SAVED_ESTADO";
    public static final String TAG_SAVED_NOTA = "TAG_SAVED_NOTA";
    public static final String TAG_SAVED_COORDENADAS = "TAG_SAVED_COORDENADAS";
    public static final String TAG_SAVED_COORDENADAS_NOTA = "TAG_SAVED_COORDENADAS_NOTA";

    // Colores para las tabs
    private final String unselectedFont = "#BDBDBD"; //9E9E9E
    private final String selectedFont = "#FFFFFF";

    // ...aquí las variables que utilizaremos dentro de los fragmentos, y en la activity
    private Integer estado;
    private Nota nota;
    private Coordenadas coordenadas;
    private CoordenadasNota coordenadasNota;

    private FragmentTabHost tabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles_nota);

        // Configuramos el Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        // Configuramos el botón de "Volver" a "Home"
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_action_home);

        // Configuramos las Tab
        tabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        tabHost.setup(this,
                getSupportFragmentManager(), android.R.id.tabcontent);

        tabHost.addTab(tabHost.newTabSpec(TAG_DETALLES_ITEM).setIndicator(getString(R.string.tab_detalles)),
                DetallesNotaFragment.class, null);
        tabHost.addTab(tabHost.newTabSpec(TAG_FOTO).setIndicator(getString(R.string.tab_foto)),
                FotosNotaFragment.class, null);
        tabHost.addTab(tabHost.newTabSpec(TAG_LOCALIZACION).setIndicator(getString(R.string.tab_mapa)),
                GeolocNotaFragment.class, null);

        tabHost.setCurrentTab(0);

        // Se colorean las tabs
        colorearTabs();

        tabHost.setOnTabChangedListener(new FragmentTabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                // Se recolorean las tabs
                colorearTabs();
            }
        });

        // Establecemos las configuraciones pertinentes, por el Intent recibido
        Bundle parametros = getIntent().getExtras();

        int estadoRecibido = parametros.getInt(DetallesNotaActivity.ESTADO_TAG);
        setEstado(estadoRecibido);

        switch (estadoRecibido) {
            case ESTADO_EDITAR:
            case ESTADO_MOSTRAR:
                int idRecibido = (int) parametros.getLong(DetallesNotaActivity.ID_TAG);
                cargarDatosDB(idRecibido);
                break;

            case ESTADO_CREAR:
                // Inicializamos el objeto "nota" (cadenas vacías)
                iniVariables();
                break;
        }

        // Y le ponemos un título adecuado a la Activity
        switch (estadoRecibido) {
            case ESTADO_EDITAR:
                this.setTitle(R.string.title_activity_detalles_nota_editar);
                break;

            case ESTADO_CREAR:
                this.setTitle(R.string.title_activity_detalles_nota_crear);
                break;

            case ESTADO_MOSTRAR:
                this.setTitle(R.string.title_activity_detalles_nota_mostrar);
                break;
        }
    }

    // Para salvar-recuperar el estado de la actividad
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(TAG_SAVED_ESTADO, estado);
        savedInstanceState.putParcelable(TAG_SAVED_NOTA, nota);
        savedInstanceState.putParcelable(TAG_SAVED_COORDENADAS, coordenadas);
        savedInstanceState.putParcelable(TAG_SAVED_COORDENADAS_NOTA, coordenadasNota);

        // Siempre debemos llamar a la clase padre aquí
        super.onSaveInstanceState(savedInstanceState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Siempre llamaremos a la clase padre aquí
        super.onRestoreInstanceState(savedInstanceState);

        /* Para restaurar el estado no debemos comprobar si "savedInstanceState" es null: nunca
           será null aquí  */
        estado = savedInstanceState.getInt(TAG_SAVED_ESTADO);
        nota = savedInstanceState.getParcelable(TAG_SAVED_NOTA);
        coordenadas = savedInstanceState.getParcelable(TAG_SAVED_COORDENADAS);
        coordenadasNota = savedInstanceState.getParcelable(TAG_SAVED_COORDENADAS_NOTA);
    }

    // Métodos para las opciones disponibles en la barra de navegación

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds notas to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detalles_nota, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        int estado = getEstado();

        if (estado == ESTADO_CREAR || estado == ESTADO_EDITAR) {
            // Ahora cambiamos por "Guardar" la palabra "Editar"
            menu.findItem(R.id.opcionCambio)
                    .setTitle(R.string.guardar);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar nota clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            // Por si hemos pulsado el botón Editar / Guardar
            case R.id.opcionCambio:
                switch (getEstado()) {
                    case ESTADO_CREAR:
                    case ESTADO_EDITAR:
                        if (guardarDatosDB()) {
                            Toast.makeText(this, R.string.guardado_exito, Toast.LENGTH_LONG)
                                    .show();
                        } else {
                            Toast.makeText(this, R.string.error_guardar, Toast.LENGTH_LONG)
                                    .show();
                        }
                        break;

                    case ESTADO_MOSTRAR:
                            // Iniciamos esta Activity, pero para editar
                            Intent intent = new Intent();
                            intent.setClass(DetallesNotaActivity.this, DetallesNotaActivity.class);
                            intent.putExtra(ESTADO_TAG, ESTADO_EDITAR);
                            intent.putExtra(ID_TAG, (long) getNota().getId());

                            startActivity(intent);
                        break;
                }
                return true;

            // Por si hemos pulsado para volver al Inicio
            case android.R.id.home:
                this.finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Para obtener el fragmento actual, de la pestaña marcada
    private IDetalles getActualTabFragment() {
        IDetalles fragmento = null;

        try {
            fragmento = (IDetalles) getSupportFragmentManager()
                    .findFragmentById(android.R.id.tabcontent);
        } catch (ClassCastException castException) {
            Log.e(TAG, "El fragmento no implementa los métodos adecuados");
        }

        return fragmento;
    }

    // Para cambiar el color-estilo de las tabs, al cambiar de tab
    private void colorearTabs() {
        int currentTabNumber = tabHost.getCurrentTab();
        for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
            TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title); //Unselected Tabs
            //tabHost.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor(unselected)); // unselected

            if (i == currentTabNumber) {
                tv.setTextColor(Color.parseColor(selectedFont));
            } else {
                tv.setTextColor(Color.parseColor(unselectedFont));
            }
        }
    }

    // Para inicializar el objeto "nota", al principio
    private void iniVariables() {
        nota = Nota.newBuilder()
                .withTitulo("")
                .withDescripcion("")
                .withId(null)
                .build();

        coordenadas = Coordenadas.newBuilder()
                .withLatitud(null)
                .withLongitud(null)
                .withId(null)
                .build();

        coordenadasNota = CoordenadasNota.newBuilder()
                .withFecha(null)
                .withIdCoordenadas(null)
                .withIdNota(null)
                .withId(null)
                .build();
    }

    // Para cargar los datos de la Nota a mostrar-modificar
    private boolean cargarDatosDB(Integer idI) {
        boolean exito = false;

        try {
            // Obtenemos el valor de los campos, de la DB
            String idNota = Integer.toString(idI);
            Uri uri = URIUtils.UNota.buildUriWith(idNota);
            String[] projection = {URIUtils.UNota.TITULO, URIUtils.UNota.DESCRIPCION,
                    URIUtils.UNota.BLOBIMAGE};

            Cursor c = this
                    .getContentResolver()
                    .query(uri, projection, null, null, null);

            if (c == null)
                return false;

            try {
                c.moveToFirst();

                // Ponemos un valor a los campos, en la pantalla
                String nombreV = c.getString(0);
                String descripcionV = c.getString(1);
                byte[] imageBlob = c.getBlob(2);

                nota = Nota.newBuilder()
                        .withTitulo(nombreV)
                        .withDescripcion(descripcionV)
                        .withBlobImage(imageBlob)
                        .withId(idI)
                        .build();

                /* Obtenemos el último par Coordenadas-Nota (con el id Nota proporcionado) */
                Uri uri2 = URIUtils.UCoordenadasNotaLast.buildUriWith(idNota);
                Cursor c2 = this
                        .getContentResolver()
                        .query(uri2, null, null, null, null);

                // Si hay algún resultado, operamos con él
                if (c2.moveToFirst()) {
                    Integer idCoorNota = (int) c2.getLong(0);
                    Integer idDelNota = (int) c2.getLong(1);
                    Long fecha = c2.getLong(2);
                    Integer idCoor = (int) c2.getLong(3);
                    Double latitud = c2.getDouble(4);
                    Double longitud = c2.getDouble(5);

                    // Guardamos CoordenadasNota
                    coordenadasNota = CoordenadasNota.newBuilder()
                            .withIdNota(idDelNota)
                            .withIdCoordenadas(idCoor)
                            .withFecha(fecha)
                            .withId(idCoorNota)
                            .build();

                    // Y Coordenadas
                    coordenadas = Coordenadas.newBuilder()
                            .withLatitud(latitud)
                            .withLongitud(longitud)
                            .withId(idCoor)
                            .build();
                }

                exito = true;
            } finally {
                c.close();
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "No se ha realizado correctamente la operacion");
        }

        return exito;
    }

    private boolean guardarDatosDB() {
        // Obtenemos todos los cambios necesarios del actual fragment visualizado, respecto a datos
        getActualTabFragment().onClickGuardar();

        // Comprobamos que todos los campos están rellenados
        String nombreV = getNota().getTitulo();
        String descripcionV = getNota().getDescripcion();

        // Si el nombre y/o la descripción están en blanco, mensaje de error y salimos
        if (nombreV.isEmpty() && descripcionV.isEmpty()) {
            Toast.makeText(this, R.string.error_campos_vacios
                    , Toast.LENGTH_LONG).show();
            return false;
        } else {
              /*
                Si hay algún fallo, en alguno de los pasos, devolveré "false"
                Además, solo creamos "Coordenadas" si hemos creado "Nota", y así con
                "CoordenadasNota"
              */
            if (!guardarNota()) {
                return false;
            }

            /*
             ¿Creamos una "Coordenadas" nueva?, ¿la actualizamos?, ¿o la borramos?
             Estos tres valores, aislados o en combinación, nos lo dirán:
             NADA     -> idCoor == null, latitud == null, longitud == null
             CREAR    -> idCoor == null, latitud != null, longitud != null
             INSERTAR -> idCoor != null, latitud != null, longitud != null
             BORRAR   -> idCoor != null, latitud == null, longitud == null
            */
            if (coordenadas != null) {
                Integer idCheck = coordenadas.getId();
                Double latCheck = coordenadas.getLatitud();
                Double lonCheck = coordenadas.getLongitud();

                if (!(idCheck == null && latCheck == null && lonCheck == null)) {
                    boolean exito = guardarCoordenadas();

                    if (!exito) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    // Para guardar los cambios en Nota
    private boolean guardarNota() {
        boolean resultado = false;

        String nombreV = nota.getTitulo();
        String descripcionV = nota.getDescripcion();
        byte[] blobImage = nota.getBlobImage();

        // ¿Creamos una Nota nueva?, ¿o la actualizamos? (¿INSERT o UPDATE?)
        Integer idNota = nota.getId();

        // Si es null, tenemos que crear una Nota
        if (idNota == null) {
            Uri uri = URIUtils.UNota.CONTENT_URI;
            Uri uriRes = this.getContentResolver()
                    .insert(uri, nota.toContentValues());

            int idNotaCreado = (int) ContentUris.parseId(uriRes);

            if (idNotaCreado != -1) {
                setEstado(ESTADO_EDITAR);

                /* Ahora no guardamos el estado de "foto", porque en teoría primero debe crear
                una nota, antes de permitirle guardar fotos */
                Nota notaCreado = Nota.newBuilder()
                        .withTitulo(nombreV)
                        .withDescripcion(descripcionV)
                        .withBlobImage(blobImage)
                        .withId(idNotaCreado)
                        .build();
                setNota(notaCreado);

                getActualTabFragment().onCambioEstado();

                resultado = true;
            }
        } else {
            // Tenemos que modificar la Nota, si existe un id ya
            int filasActualizadas = 0;

            final String id = Integer.toString(nota.getId());
            final Uri uri2 = URIUtils.UNota.buildUriWith(id);

            filasActualizadas = this.getContentResolver()
                    .update(uri2, getNota().toContentValues(), null, null);

            if (filasActualizadas != 0) {
                resultado = true;
            }
        }

        return resultado;
    }

    // Para guardar los cambios en Coordenadas
    private boolean guardarCoordenadas() {
        boolean resultado = false;

        /* ¡Recordamos!
         ¿Creamos una "Coordenadas" nueva?, ¿la actualizamos?, ¿o la borramos?
         Estos tres valores, aislados o en combinación, nos lo dirán:
         CREAR    -> idCoor == null, latitud != null, longitud != null
         INSERTAR -> idCoor != null, latitud != null, longitud != null
         BORRAR   -> idCoor != null, latitud == null, longitud == null
        */
        Integer idCoor = coordenadas.getId();
        Double latitud = coordenadas.getLatitud();
        Double longitud = coordenadas.getLongitud();

        // Si es null, tenemos que crear una Coordenadas nuevas
        if (idCoor == null) {
            Uri uri = URIUtils.UCoordenadas.CONTENT_URI;
            Uri uriRes = this.getContentResolver()
                    .insert(uri, coordenadas.toContentValues());

            int idCoorCreado = (int) ContentUris.parseId(uriRes);

            // Si hemos creado con éxito unas "Coordenadas"
            if (idCoorCreado != -1) {
                Coordenadas coorCreado = Coordenadas.newBuilder()
                        .withLatitud(latitud)
                        .withLongitud(longitud)
                        .withId(idCoorCreado)
                        .build();

                setCoordenadas(coorCreado);

                /*
                Y ahora, sólo si hemos creado un nuevo "Coordenadas", creamos un nuevo
                "CoordenadasNota"
                 */
                long fechaActual = DateTimestamp.generarTimestamp();

                CoordenadasNota coordenadasNotaPorCrear = CoordenadasNota.newBuilder()
                        .withIdNota(nota.getId())
                        .withIdCoordenadas(idCoorCreado)
                        .withFecha(fechaActual)
                        .build();

                Uri uri2 = URIUtils.UCoordenadasNota.CONTENT_URI;
                Uri uriRes2 = this.getContentResolver()
                        .insert(uri2, coordenadasNotaPorCrear.toContentValues());

                int idCoorNotaCreado = (int) ContentUris.parseId(uriRes2);
                if (idCoorNotaCreado != -1) {
                    resultado = true;
                }
            }
        } else {
            // En este caso actualizamos
            if (latitud != null & longitud != null) {
                // Tenemos que modificar Coordenadas, si existe un id ya
                int filasActualizadas = 0;

                final String id = Integer.toString(idCoor);
                final Uri uri = URIUtils.UCoordenadas.buildUriWith(id);

                filasActualizadas = this.getContentResolver()
                        .update(uri, coordenadas.toContentValues(), null, null);

                if (filasActualizadas != 0) {
                    resultado = true;
                }

                // En este caso borramos
            } else if (latitud == null & longitud == null) {
                // Borramos Coordenadas
                final String id = Integer.toString(idCoor);
                final Uri uri = URIUtils.UCoordenadas.buildUriWith(id);
                int filasBorradas = getApplicationContext()
                        .getContentResolver()
                        .delete(uri, null, null);

                if (filasBorradas != 0) {
                    // Borramos CoordenadasNota
                    int idCoordenadasNota = coordenadasNota.getId();

                    final String id2 = Integer.toString(idCoordenadasNota);
                    final Uri uri2 = URIUtils.UCoordenadasNota.buildUriWith(id2);
                    int filasBorradas2 = getApplicationContext()
                            .getContentResolver()
                            .delete(uri2, null, null);

                    if (filasBorradas2 != 0) {
                        resultado = true;
                    }
                }
            }
        }

        return resultado;
    }

    // Para llamarlos desde el fragmento de las fotos de las notas:
    // Hacer foto - coger foto de galería
    @Override
    public FloatingActionButton getFloatingButtonTakeFoto() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.takeFoto);

        return fab;
    }

    // Borrar foto
    public FloatingActionButton getFloatingButtonBorrarFoto() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.borrarFoto);

        return fab;
    }

    // De esta forma, manejaremos el evento tb desde el fragmento que lo quiera implementar
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
    }

    @Override
    public void onClickBorrar() {
        final Nota nota = getNota();

        final String id = Integer.toString(nota.getId());

        final Activity mActivity = this;

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.cuidado))
                .setMessage(getString(R.string.preguntar_si_borrar))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(getString(R.string.si), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        boolean exito = true;

                        // Borramos las posibles coordenadas relacionadas con la nota
                        final Uri uri2 = URIUtils.UCoordenadasNotaWithIdNota.buildUriWith(id);

                        int filasBorradasCoor = getApplicationContext()
                                .getContentResolver()
                                .delete(uri2, null, null);

                        // Borramos la Nota
                        final Uri uri = URIUtils.UNota.buildUriWith(id);
                        int filasBorradas = getApplicationContext()
                                .getContentResolver()
                                .delete(uri, null, null);

                        if (filasBorradas != 0) {
                            Toast.makeText(getApplicationContext(), R.string.nota_borrada_ok,
                                    Toast.LENGTH_LONG).show();

                            mActivity.finish();
                        } else {
                            exito = false;
                        }

                        if (!exito) {
                            Log.e(TAG, "Error 82347");
                            Toast.makeText(getApplicationContext(), R.string.error_borrar_nota,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }

    // Getters y Setters

    @Override
    public int getEstado() {
        return estado;
    }

    @Override
    public void setEstado(int estado) {
        this.estado = estado;
    }

    public Nota getNota() {
        return nota;
    }

    public void setNota(Nota nota) {
        this.nota = nota;
    }

    @Override
    public Coordenadas getCoordenadas() {
        return coordenadas;
    }

    @Override
    public void setCoordenadas(Coordenadas coordenadasV) {
        coordenadas = coordenadasV;
    }

    @Override
    public CoordenadasNota getCoordenadasNota() {
        return coordenadasNota;
    }

    @Override
    public void setCoordenadasNota(CoordenadasNota coordenadasNotaV) {
        coordenadasNota = coordenadasNotaV;
   }
}