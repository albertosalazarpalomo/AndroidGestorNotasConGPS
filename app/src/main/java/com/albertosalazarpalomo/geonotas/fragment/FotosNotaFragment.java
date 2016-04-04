package com.albertosalazarpalomo.geonotas.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.albertosalazarpalomo.geonotas.DetallesNotaActivity;
import com.albertosalazarpalomo.geonotas.R;
import com.albertosalazarpalomo.geonotas.model.Nota;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * Pestaña "Foto" de "DetallesActivity": permite añadir foto de la galería o de una cámara
 */
public class FotosNotaFragment extends Fragment implements IDetalles {
    private static final String TAG = "FotosNotaFrag";
    private static final String TAG_FOTO = "tag_foto";
    private final String NOMBRE_TEMP_IMG = "temp.jpg";
    
    protected static final int CAMERA_REQUEST = 0;
    protected static final int GALLERY_PICTURE = 1;

    ImageView imageView;

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
        View v = inflater.inflate(R.layout.fragment_fotos_nota, container, false);

        // Obtenemos el ImageView
        imageView = (ImageView) v.findViewById(R.id.foto);

        // Configuramos el botón para sacar fotos / coger de la galería
        int estado = interfaceActivity.getEstado();

        FloatingActionButton floatingButtonTakeFoto = interfaceActivity.getFloatingButtonTakeFoto();
        FloatingActionButton floatingButtonBorrarFoto = interfaceActivity.getFloatingButtonBorrarFoto();

        if (estado == DetallesNotaActivity.ESTADO_CREAR
                || estado == DetallesNotaActivity.ESTADO_EDITAR) {
            // Mostramos los botones flotantes relevantes para este fragment
            floatingButtonTakeFoto.setVisibility(View.VISIBLE);
            floatingButtonBorrarFoto.setVisibility(View.VISIBLE);
        }

        floatingButtonTakeFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            // Si no se ha creado la nota todavía, no podemos añadir fotos
            if (interfaceActivity.getEstado() == DetallesNotaActivity.ESTADO_CREAR) {
                String errorAlert = getActivity().getString(R.string.antes_debes);
                String errorMsg = getActivity().getString(R.string.pulsa_antes_guardar);
                String okText = getActivity().getString(R.string.OK);

                // Configurando Alert
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setTitle(errorAlert);
                alertDialog.setMessage(errorMsg);
                alertDialog.setIcon(R.drawable.ic_content_remove_circle);
                alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, okText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                alertDialog.show();
            } else {
                // Daremos a elegir entre si hacer una foto nueva, o cogerla de la galería
                hacerFotoOGaleria();
            }
            }
        });

        floatingButtonBorrarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            // Borramos la imagen del UI
            imageView.setImageBitmap(null);

            // Borramos los datos del modelo
            Nota notaOld = interfaceActivity.getNota();
            Nota nuevaNota = Nota.newBuilder(notaOld)
                    .withBlobImage(null)
                    .build();
            interfaceActivity.setNota(nuevaNota);
            }
        });

        return v;
    }

    // Para guardar y recuperar un estado
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            byte[] byteFoto = savedInstanceState.getByteArray(TAG_FOTO);

            if (byteFoto != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(byteFoto, 0, byteFoto.length);
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putByteArray(TAG_FOTO, interfaceActivity.getNota().getBlobImage());
    }

    @Override
    public void onStart() {
        super.onStart();

        // En el View para la imagen intentamos añadirle la imagen, si existe
        byte[] blobImage = interfaceActivity.getNota().getBlobImage();
        if (blobImage != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(blobImage, 0, blobImage.length);
            imageView.setImageBitmap(bitmap);
        }
    }

    // Para cuando hacemos click en un botón de la Activity padre
    @Override
    public boolean onClickGuardar() {
        boolean exito = false;

        exito = true;

        return exito;
    }

    // Para cuando hacemos ocurre un cambio de estado en la Activity padre
    @Override
    public boolean onCambioEstado() {
        int estado = interfaceActivity.getEstado();

        return true;
    }

    @Override
    public void onDestroyView() {
        // Fuera de este fragment, los botones flotantes propios se ocultan
        interfaceActivity.getFloatingButtonTakeFoto()
                .setVisibility(View.GONE);
        interfaceActivity.getFloatingButtonBorrarFoto()
                .setVisibility(View.GONE);

        super.onDestroyView();
    }

    // Método para mostrar Toasts
    private void mostrarToast(CharSequence texto) {
        Context context = this.getActivity().getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, texto, duration);
        toast.show();
    }

    // Métodos para captar la imagen

    private void hacerFotoOGaleria() {
        // Configurando Alert
        String errorAlert = getActivity().getString(R.string.elige_origen_foto);
        String errorMsg = getActivity().getString(R.string.elige_foto_o_galeria);
        String galeriaText = getActivity().getString(R.string.galeria);
        String camaraText = getActivity().getString(R.string.camara);

        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(getActivity());
        myAlertDialog.setTitle(errorAlert);
        myAlertDialog.setMessage(errorMsg);

        myAlertDialog.setPositiveButton(galeriaText,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent pictureActionIntent = null;

                        pictureActionIntent = new Intent(
                                Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                        startActivityForResult(
                                pictureActionIntent,
                                GALLERY_PICTURE);
                    }
                });

        myAlertDialog.setNegativeButton(camaraText,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                        File f = new File(android.os.Environment
                                .getExternalStorageDirectory(), NOMBRE_TEMP_IMG);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(f));

                        startActivityForResult(intent,
                                CAMERA_REQUEST);
                    }
                });

        myAlertDialog.show();
    }

    // Para cuando seleccione una foto para el perfil
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap bitmap = null;
        String selectedImagePath = null;

        if (resultCode == Activity.RESULT_OK && requestCode == CAMERA_REQUEST) {
            File f = new File(Environment.getExternalStorageDirectory()
                    .toString());

            for (File temp : f.listFiles()) {
                if (temp.getName().equals(NOMBRE_TEMP_IMG)) {
                    f = temp;
                    break;
                }
            }

            if (!f.exists()) {
                Toast.makeText(getActivity().getBaseContext(),
                        R.string.error_capturar_imagen, Toast.LENGTH_LONG)
                        .show();
                return;
            }

            try {
                bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());

                imageView.setImageBitmap(bitmap);

                guardarImagenEnModeloNota(bitmap);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else if (resultCode == Activity.RESULT_OK && requestCode == GALLERY_PICTURE) {
            if (data != null) {
                Uri selectedImage = data.getData();
                String[] filePath = { MediaStore.Images.Media.DATA };
                Cursor c = getActivity().getContentResolver().query(selectedImage, filePath,
                        null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                selectedImagePath = c.getString(columnIndex);
                c.close();

                bitmap = BitmapFactory.decodeFile(selectedImagePath); // load preview image

                imageView.setImageBitmap(bitmap);

                guardarImagenEnModeloNota(bitmap);
            } else {
                Toast.makeText(getActivity().getApplicationContext(), R.string.cancelado,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void guardarImagenEnModeloNota(Bitmap bitmap) {
        // Transformamos la imagen en un array de bytes
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        byte[] bufferFoto = out.toByteArray();

        // Y la guardamos en el modelo actual
        Nota notaActual = interfaceActivity.getNota();
        Nota notaNueva = Nota.newBuilder(notaActual)
                .withBlobImage(bufferFoto)
                .build();

        interfaceActivity.setNota(notaNueva);
    }
}