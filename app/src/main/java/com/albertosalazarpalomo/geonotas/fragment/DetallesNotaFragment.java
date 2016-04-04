package com.albertosalazarpalomo.geonotas.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.albertosalazarpalomo.geonotas.DetallesNotaActivity;
import com.albertosalazarpalomo.geonotas.R;
import com.albertosalazarpalomo.geonotas.model.Nota;

/**
 * Pestaña "Detalles" de la activity "DetallesActivity": título y descripción de la nota
 */
public class DetallesNotaFragment extends Fragment implements IDetalles {
    private static final String TAG = "DetallesNotaFrag";

    private INotaFragment interfaceActivity;

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
        View rootView = inflater.inflate(R.layout.fragment_detalles_nota, container, false);

        cargarCampos(rootView);

        // Lo configuramos all por si se intenta visualizar o editar una Nota, se muestran sus datos
        int estadoActivity = interfaceActivity.getEstado();

        switch (estadoActivity) {
            case DetallesNotaActivity.ESTADO_CREAR:
                // Desactivamos la opción de borrar
                ImageButton borrarBoton = (ImageButton) rootView.findViewById(R.id.borrarBotom);
                borrarBoton.setVisibility(View.INVISIBLE);
                break;

            case DetallesNotaActivity.ESTADO_MOSTRAR:
                EditText nombre = (EditText) rootView.findViewById(R.id.nombre);
                EditText descripcion = (EditText) rootView.findViewById(R.id.descripcion);

                nombre.setEnabled(false);
                descripcion.setEnabled(false);
                break;

            case DetallesNotaActivity.ESTADO_EDITAR:
                break;
        }

        // Configuramos el botón de borrar
        ImageButton borrarBoton = (ImageButton) rootView.findViewById(R.id.borrarBotom);

        borrarBoton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                interfaceActivity.onClickBorrar();
            }
        });

        return rootView;
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
        int estado = interfaceActivity.getEstado();

        switch (estado) {
            case DetallesNotaActivity.ESTADO_EDITAR:
                ImageButton borrarBoton = (ImageButton) getView()
                        .findViewById(R.id.borrarBotom);
                borrarBoton.setVisibility(View.VISIBLE);
                break;
        }

        return true;
    }

    @Override
    public void onDestroyView() {
        int estado = interfaceActivity.getEstado();

        if (estado == DetallesNotaActivity.ESTADO_CREAR
                || estado == DetallesNotaActivity.ESTADO_EDITAR) {
            guardarDatosNotaActivity();
        }

        super.onDestroyView();
    }

    private void guardarDatosNotaActivity() {
        // Extraemos los datos
        EditText nombre = (EditText) getView().findViewById(R.id.nombre);
        EditText descripcion = (EditText) getView().findViewById(R.id.descripcion);

        String nombreV = nombre.getText().toString();
        String descripcionV = descripcion.getText().toString();

        int estado = interfaceActivity.getEstado();

        // Solo guardaremos una Nota nueva en estos casos:
        switch (estado) {
            case DetallesNotaActivity.ESTADO_CREAR:
                // Y guardamos los cambios realizados
                Nota newNotaCreado = Nota.newBuilder()
                        .withTitulo(nombreV)
                        .withDescripcion(descripcionV)
                        .build();

                interfaceActivity.setNota(newNotaCreado);
                break;

            case DetallesNotaActivity.ESTADO_MOSTRAR:
            case DetallesNotaActivity.ESTADO_EDITAR:
                Nota oldNota = interfaceActivity.getNota();

                // Y guardamos los cambios realizados
                Nota newNota = Nota.newBuilder(oldNota)
                        .withTitulo(nombreV)
                        .withDescripcion(descripcionV)
                        .build();

                interfaceActivity.setNota(newNota);
                break;
        }
    }

    // Método para cargar los datos de una nota que se va a editar / visualizar
    private void cargarCampos(View rootView) {
        Nota nota = interfaceActivity.getNota();

        // Ponemos un valor a los campos, en la pantalla
        String nombreV = nota.getTitulo();
        String descripcionV = nota.getDescripcion();

        EditText nombre = (EditText) rootView.findViewById(R.id.nombre);
        EditText descripcion = (EditText) rootView.findViewById(R.id.descripcion);
        nombre.setText(nombreV);
        descripcion.setText(descripcionV);
    }
}