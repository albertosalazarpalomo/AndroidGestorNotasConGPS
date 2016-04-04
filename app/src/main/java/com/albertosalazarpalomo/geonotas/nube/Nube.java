package com.albertosalazarpalomo.geonotas.nube;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.albertosalazarpalomo.geonotas.MainActivity;
import com.albertosalazarpalomo.geonotas.R;
import com.albertosalazarpalomo.geonotas.dataprovider.URIUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Para realizar las copias de seguridad en la Nube, o restaurarlas. También se implementa la opción
 * de login, guardando el perfil obtenido en un SharedPreferences, aunque en esta App no es algo que
 * use.
 */
public class Nube {
    public final static String TAG = "Nube";

    public final static String TAG_USERNAME = "username";
    public final static String TAG_PASSWORD = "password";
    public final static String TAG_OP = "op";
    public final static String TAG_CONTENT = "content";
    public final static String TAG_PERFIL = "perfil";

    public final static String LOAD = "load";
    public final static String SAVE = "save";
    public final static String LOGIN = "login";

    public final static String MSG_SUCCESS = "ok_msg";
    public final static String MSG_ERROR = "error_msg";

    public static final String PREFS_NAME = "mypref";
    public static final String PROFILE_ADMIN = "mypref";
    public static final String NO_ADMIN = "no-admin";
    public static final String ADMIN = "admin";

    ProgressDialog pDialog;

    public enum TipoLogin {
        SAVE, LOAD, LOGIN
    }

    private Activity activity;
    private AlertDialog alert;

    public Nube(Activity activity) {
        this.activity = activity;

        // Sin este trozo de código, no se le da permisos para la descarga!
        /*
        android.os.StrictMode.ThreadPolicy policy = new android.os.StrictMode
                .ThreadPolicy.Builder().permitAll().build();
        android.os.StrictMode.setThreadPolicy(policy);
        */
    }

    // Para mostrar el tipo de alert que haga una acción u otra (según "TipoLogin")
    public void showLogin(final TipoLogin tipoLogin) {
        // Obtenemos la vista del diálogo
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View viewParent = layoutInflater.inflate(R.layout.fragment_nube_login, null);

        // Se la asignamos a un AlertDialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setView(viewParent);
        alert = alertDialogBuilder.create();

        // Configuramos campos del AlertDialog
        TextView tituloLogin = (TextView) viewParent.findViewById(R.id.tituloLogin);
        if (tipoLogin == TipoLogin.SAVE) {
            tituloLogin.setText(R.string.guardar_copia_nube);
        } else if (tipoLogin == TipoLogin.LOAD) {
            tituloLogin.setText(R.string.recuperar_copia_nube);
        } else if (tipoLogin == TipoLogin.LOGIN) {
            tituloLogin.setText(R.string.loguearse_sistema);
        }

        // Configuramos los botones del diálogo
        Button okButton = (Button) viewParent.findViewById(R.id.ok);

        okButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                View parent = v.getRootView();

                if (parent != null) {
                    EditText usuario = (EditText) parent.findViewById(R.id.usuario);
                    EditText password = (EditText) parent.findViewById(R.id.password);
                    String usuarioS = usuario.getText().toString();
                    String passwordS = password.getText().toString();

                    // Si tenemos que salvar en la nube la DB
                    JSONObject toSend = new JSONObject();
                    if (tipoLogin == TipoLogin.SAVE) {
                        try {
                            //Obtenemos el contenido de la DB a enviar
                            ObtenerBaseDatosEnJSON baseDatosEnJSON = new ObtenerBaseDatosEnJSON(activity);
                            String content = baseDatosEnJSON.getJSON();

                            // Construimos el objeto JSON a enviar
                            toSend.put(TAG_OP, SAVE);
                            toSend.put(TAG_USERNAME, usuarioS);
                            toSend.put(TAG_PASSWORD, passwordS);
                            toSend.put(TAG_CONTENT, content);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (tipoLogin == TipoLogin.LOAD) {
                        try {
                            // Construimos el objeto JSON a enviar
                            toSend.put(TAG_OP, LOAD);
                            toSend.put(TAG_USERNAME, usuarioS);
                            toSend.put(TAG_PASSWORD, passwordS);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (tipoLogin == TipoLogin.LOGIN) {
                        try {
                            // Construimos el objeto JSON a enviar
                            toSend.put(TAG_OP, LOGIN);
                            toSend.put(TAG_USERNAME, usuarioS);
                            toSend.put(TAG_PASSWORD, passwordS);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    new EnviarRecibirJSON(activity, alert).execute(toSend);
                }
            }
        });

        // Mostramos el diálogo, ya configurado
        alert.show();
    }

    /* Clase para enviar un json y recibir su respuesta, en un hilo secundario
       Orden de los "parámetros":
       1 = entrada (doInBackground), 2 = progress bar (osProgressUpdate), 3 = salida (onPostExecute)
    */
    private class EnviarRecibirJSON extends AsyncTask<JSONObject, Integer, Boolean> {
        public final static String URL = "http://albertosalazarpalomo.com/gestorgeo/cloud.php";

        private Context context;
        private AlertDialog dialogToHide;

        /*
         * dialogToHide sirve para ocultar un diálogo previo, que pueda estar ya (admite null)
         */
        public EnviarRecibirJSON(Context context, AlertDialog dialogToHide) {
            this.context = context;
            this.dialogToHide = dialogToHide;
        }

        // Métodos para la AsyncTask
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(activity);
            pDialog.setMessage(Html.fromHtml(activity.getString(R.string.realizando_operacion)));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * Tenemos que hacer esto con todos los objetos disponibles
         * */
        @Override
        protected Boolean doInBackground(JSONObject... enviarJSON) {
            for (int i = 0; i < enviarJSON.length; i++) {
                tryLogin(enviarJSON[i]);
            }

            return true;
        }

        /**
         * Updating progress bar
         * */
        @Override
        protected void onProgressUpdate(Integer... progress) {
        }

        /**
         * After completing background task
         * Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(Boolean d) {
            cerrarDialogos();
        }

        // Método para cerrar diálogos abiertos
        private void cerrarDialogos() {
            if(dialogToHide != null)
                dialogToHide.dismiss();

            if(pDialog != null)
                pDialog.dismiss();
        }

        // Métodos utilizados por el hilo, creados por mí
        private void tryLogin(JSONObject toSend) {
            ConnectivityManager connMgr = (ConnectivityManager) activity
                    .getSystemService(Activity.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            // Ocultamos el previo diálogo mostrado, si existía
            if (networkInfo != null && networkInfo.isConnected()) {
                JSONObject toReceive = null;
                toReceive = sendReceiveJSON(toSend, URL);

                // Terminamos sin más, si no se ha recibido nada
                if (toReceive == null) {
                    this.showAlerta(activity.getString(R.string.error), activity.getString(R.string.error_indefinido));

                    return;
                }

                // Procesamos la información recibida
                String operationSended = "";
                String operation = "";
                try {
                    operationSended = toSend.getString(TAG_OP);
                    operation = toReceive.getString(TAG_OP);
                } catch (JSONException e) {
                    e.printStackTrace();

                    this.showAlerta(activity.getString(R.string.error), activity.getString(R.string.error_indefinido));

                    return;
                }

                // Si la operación ha sido un éxito (sea cual sea)
                if (operation.equals(MSG_SUCCESS)) {
                    // Mostramos distintos mensajes, según el tipo de operación

                    // Si el mensaje era para restaurar copia en la nube
                    if (operationSended.equals(LOAD)) {
                        // Obtenemos el contenido de la DB salvado, y lo intentamos guardar
                        String content = "";
                        try {
                            content = toReceive.getString(TAG_CONTENT);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            return;
                        }

                        ContentValues contentValue = new ContentValues();
                        contentValue.put(URIUtils.WholeDatabase.ESTADO, content);

                        try {
                            Uri uri = URIUtils.WholeDatabase.CONTENT_URI;
                            Uri uriRes = activity.getContentResolver()
                                    .insert(uri, contentValue);

                            this.showAlerta(activity.getString(R.string.exito),
                                    activity.getString(R.string.msg_exito));
                        } catch (IllegalArgumentException e) {
                            this.showAlerta(activity.getString(R.string.error),
                                    activity.getString(R.string.error_restaurar_copia));
                        }
                    } else if (operationSended.equals(SAVE)) {
                        this.showAlerta(activity.getString(R.string.exito),
                                activity.getString(R.string.copia_salvada_ok));
                    } else if (operationSended.equals(LOGIN)) {
                        // Guardamos el perfil del usuario, por si lo necesitamos
                        String tipoPerfil;
                        try {
                            tipoPerfil = toReceive.getString(TAG_PERFIL);
                        } catch (JSONException e) {
                            tipoPerfil = Nube.NO_ADMIN;
                        }
                        SharedPreferences settings = activity.getApplicationContext()
                                .getSharedPreferences(PREFS_NAME, 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PROFILE_ADMIN, tipoPerfil);
                        editor.commit();

                        // Y comenzamos las actividades!
                        Intent intent = new Intent(activity, MainActivity.class);
                        activity.startActivity(intent);
                        activity.finish();
                    }
                } else if (operation.equals(MSG_ERROR)) {
                    if (operationSended.equals(LOGIN)) {
                        this.showAlertaRepiteLogin(activity.getString(R.string.error),
                                activity.getString(R.string.try_again));
                    } else {
                        this.showAlerta(activity.getString(R.string.error),
                                activity.getString(R.string.error_proceso));
                    }
                }
            } else {
                // Mostrar errores
                this.showAlerta(activity.getString(R.string.error),
                        activity.getString(R.string.no_internet_access));
            }
        }

        private JSONObject sendReceiveJSON(JSONObject toSend, String thisURL) {
            String JSONstring = toSend.toString();

            HttpURLConnection urlConnection = null;
            JSONObject result = null;

            try {
                URL url = new URL(thisURL);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Content-Type", "application/json");

                // Enviar JSON
                urlConnection.setDoOutput(true);
                urlConnection.setFixedLengthStreamingMode(JSONstring.getBytes().length); // Tamaño previamente conocido

                OutputStream out = null;

                try {
                    out = new BufferedOutputStream(urlConnection.getOutputStream());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (out != null) {
                    out.write(JSONstring.getBytes());
                    out.flush();
                    out.close();

                    // Leer respuesta
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    String inString = getStringFromInputStream(in);

                    JSONObject toReceive = null;
                    try {
                        toReceive = new JSONObject(inString);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    result = toReceive;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(urlConnection != null)
                    urlConnection.disconnect();
            }

            return result;
        }

        // Mostrar un breve mensaje de información, sobre el resultado de la operación
        private void showAlerta(String titulo, String mensaje) {
            cerrarDialogos();

            final String errorAlert = titulo;
            final String errorMsg = mensaje;
            final String okText = activity.getString(R.string.OK);

            activity.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        // Configurando Alert
                        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
                        alertDialog.setTitle(errorAlert);
                        alertDialog.setMessage(errorMsg);

                        // Setting Icon to Dialog
                        //alertDialog.setIcon(R.drawable.);
                        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, okText, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });

                        alertDialog.setOnDismissListener(
                            new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    Intent intent = new Intent(activity, MainActivity.class);
                                    activity.startActivity(intent);
                                    activity.finish();
                                }
                            }
                        );

                        alertDialog.show();
                    }
                }
            );
        }

        /* Método para una posible implamentación para un apartado de login (funcionalidad similar
           a la de "showAlert"
         */
        private void showAlertaRepiteLogin(String titulo, String mensaje) {
            cerrarDialogos();

            final String errorAlert = titulo;
            final String errorMsg = mensaje;
            final String okText = activity.getString(R.string.OK);

            activity.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {

                            // Configurando Alert
                            AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
                            alertDialog.setTitle(errorAlert);
                            alertDialog.setMessage(errorMsg);
                            // Setting Icon to Dialog
                            //alertDialog.setIcon(R.drawable.);
                            alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, okText, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });

                            alertDialog.setOnDismissListener(
                                    new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {

                                        }
                                    }
                            );

                            alertDialog.show();
                        }
                    });
            }

        private String getStringFromInputStream(InputStream stream) throws IOException
        {
            int n = 0;
            char[] buffer = new char[1024 * 4];
            InputStreamReader reader = new InputStreamReader(stream, "UTF8");
            StringWriter writer = new StringWriter();
            while (-1 != (n = reader.read(buffer))) writer.write(buffer, 0, n);
            return writer.toString();
        }
    }

    // Clase para transformar la base de datos, en una string JSON
    private class ObtenerBaseDatosEnJSON {
        private Context contexto;
        public ObtenerBaseDatosEnJSON(Context contexto) {
            this.contexto = contexto;
        }

        // Único método público
        public String getJSON() {
            // Generamos la salida a guardar
            JSONArray jsonArr = new JSONArray();
            jsonArr.put(getAllNotaJson());
            jsonArr.put(getAllCoordenadasJson());
            jsonArr.put(getAllCoordenadasNotaJson());

            return jsonArr.toString();
        }

        // Obtener la string de todos los objetos Nota
        private JSONObject getAllNotaJson() {
            JSONObject mainObj = new JSONObject();

            Uri uri2 = URIUtils.UNota.CONTENT_URI;
            String[] projection = {URIUtils.UNota.ID, URIUtils.UNota.TITULO,
                    URIUtils.UNota.DESCRIPCION, URIUtils.UNota.BLOBIMAGE};
            Cursor c = contexto.getContentResolver().query(uri2, projection, null, null, null);

            // Si no hay nada, devolvemos el objeto vacío
            if (!c.moveToFirst()) {
                return mainObj;
            }

            try {
                JSONArray jsonArr = new JSONArray();
                do {
                    Long id = c.getLong(0);
                    String titulo = c.getString(1);
                    String descripcion = c.getString(2);
                    byte[] imageBlob = c.getBlob(3);

                    JSONObject nuevoJSON = new JSONObject();
                    try {
                        nuevoJSON.put(URIUtils.UNota.ID, id);
                        nuevoJSON.put(URIUtils.UNota.TITULO, titulo);
                        nuevoJSON.put(URIUtils.UNota.DESCRIPCION, descripcion);
                        nuevoJSON.put(URIUtils.UNota.BLOBIMAGE, transformArrayByteToJSON(imageBlob));
                    } catch (JSONException e) {
                        Log.w(TAG, e.getMessage());
                    }

                    jsonArr.put(nuevoJSON);
                } while (c.moveToNext());

                try {
                    mainObj.put(URIUtils.UNota.NAME, jsonArr);
                } catch (JSONException e) {
                    Log.w(TAG, e.getMessage());
                }
            } finally {
                c.close();
            }

            return mainObj;
        }

        // Obtener la string de todos los objetos Nota
        private JSONObject getAllCoordenadasJson() {
            JSONObject mainObj = new JSONObject();

            Uri uri2 = URIUtils.UCoordenadas.CONTENT_URI;
            String[] projection = {URIUtils.UCoordenadas.ID, URIUtils.UCoordenadas.LATITUD,
                    URIUtils.UCoordenadas.LONGITUD};
            Cursor c = contexto.getContentResolver().query(uri2, projection, null, null, null);

            if (!c.moveToFirst()) {
                return mainObj;
            }

            JSONArray jsonArr = new JSONArray();
            do {
                Long id = c.getLong(0);
                Float latitud = c.getFloat(1);
                Float longitud = c.getFloat(2);

                JSONObject nuevoJSON = new JSONObject();
                try {
                    nuevoJSON.put(URIUtils.UCoordenadas.ID, id);
                    nuevoJSON.put(URIUtils.UCoordenadas.LATITUD, latitud);
                    nuevoJSON.put(URIUtils.UCoordenadas.LONGITUD, longitud);
                } catch (JSONException e) {
                    Log.w(TAG, e.getMessage());
                }

                jsonArr.put(nuevoJSON);
            } while (c.moveToNext());

            try {
                mainObj.put(URIUtils.UCoordenadas.NAME, jsonArr);
            } catch (JSONException e) {
                Log.w(TAG, e.getMessage());
            }

            return mainObj;
        }

        // Obtener la string de todos los objetos Nota
        private JSONObject getAllCoordenadasNotaJson() {
            JSONObject mainObj = new JSONObject();

            Uri uri2 = URIUtils.UCoordenadasNota.CONTENT_URI;
            String[] projection = {URIUtils.UCoordenadasNota.ID, URIUtils.UCoordenadasNota.ID_COORDENADAS,
                    URIUtils.UCoordenadasNota.ID_NOTA, URIUtils.UCoordenadasNota.FECHA};
            Cursor c = contexto.getContentResolver().query(uri2, projection, null, null, null);

            if (!c.moveToFirst()) {
                return mainObj;
            }

            JSONArray jsonArr = new JSONArray();
            do {
                Long id = c.getLong(0);
                Long idCoordenadas = c.getLong(1);
                Long idNota = c.getLong(2);
                Long fecha = c.getLong(3);

                JSONObject nuevoJSON = new JSONObject();
                try {
                    nuevoJSON.put(URIUtils.UCoordenadas.ID, id);
                    nuevoJSON.put(URIUtils.UCoordenadasNota.ID_COORDENADAS, idCoordenadas);
                    nuevoJSON.put(URIUtils.UCoordenadasNota.ID_NOTA, idNota);
                    nuevoJSON.put(URIUtils.UCoordenadasNota.FECHA, fecha);
                } catch (JSONException e) {
                    Log.w(TAG, e.getMessage());
                }

                jsonArr.put(nuevoJSON);
            } while (c.moveToNext());

            try {
                mainObj.put(URIUtils.UCoordenadasNota.NAME, jsonArr);
            } catch (JSONException e) {
                Log.w(TAG, e.getMessage());
            }

            return mainObj;
        }

        private JSONArray transformArrayByteToJSON(byte[] buffer) {
            JSONArray jsonArr = new JSONArray();

            if (buffer != null) {
                for (int i = 0; i < buffer.length; i++) {
                    jsonArr.put(buffer[i]);
                }
            }

            return jsonArr;
        }
    }
}