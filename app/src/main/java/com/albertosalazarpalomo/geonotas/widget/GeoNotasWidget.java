package com.albertosalazarpalomo.geonotas.widget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import com.albertosalazarpalomo.geonotas.DetallesNotaActivity;
import com.albertosalazarpalomo.geonotas.MainActivity;
import com.albertosalazarpalomo.geonotas.R;
import com.albertosalazarpalomo.geonotas.dataprovider.URIUtils;
import com.albertosalazarpalomo.geonotas.model.Nota;
import com.albertosalazarpalomo.geonotas.util.NumerosAleatorios;

/**
 * Widget que nos mostrará, de forma aleatoria, el título de alguna nota, junto a un botón para
 * verla en detalle. Si no hay ninguna, nos animará a empezar a introducir notas.
 *
 * Utilizaremos un servicio para actualizar el/los widget/s, puesto que la documentación dice que
 * "From within the Service, you can perform your own updates to the App Widget without worrying
 * about the AppWidgetProvider closing down due to an Application Not Responding (ANR) error."
 */
public class GeoNotasWidget extends AppWidgetProvider {
    public static final String TAG = "GEONOTASWIDGET";

    private static final String CLICK_BOTON_MOSTRAR_TAREA = "CLICK_BOTON_MOSTRAR_TAREA";
    private static final String CLICK_BOTON_MOSTRAR_LISTADO = "CLICK_BOTON_MOSTRAR_LISTADO";
    private static final String ID_NOTA = "ID_NOTA";

    // Este método se actualizará siempre que se reciba un mensaje de "UPDATE"
    @Override
    public void onUpdate(Context context,
                         AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        context.startService(new Intent(context,
                ExtraerTareaAleatoriaService.class));
    }

    // Aquí es donde manipularemos los mensajes de "mostrar tarea" o "mostrar listado"
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String accionRecibida = intent.getAction();

        // ¿Abrimos la tarea?
        if (intent.getAction().equals(CLICK_BOTON_MOSTRAR_TAREA)){
            // Extraemos el id de la Nota
            Bundle extras = intent.getExtras();
            long idNota = extras.getInt(ID_NOTA);

            // Abrimos la Activity para mostrar una nota ya existente
            Intent intentDest = new Intent();
            intentDest.setClass(context, DetallesNotaActivity.class);
            intentDest.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intentDest.putExtra(DetallesNotaActivity.ESTADO_TAG, DetallesNotaActivity.ESTADO_MOSTRAR);
            intentDest.putExtra(DetallesNotaActivity.ID_TAG, idNota);

            context.startActivity(intentDest);
        } else if (intent.getAction().equals(CLICK_BOTON_MOSTRAR_LISTADO)){
            // Abrimos el listado
            Intent intentDest = new Intent();
            intentDest.setClass(context, MainActivity.class);
            intentDest.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intentDest);
        }
    };

    /* Servicio de actualizacion del widget: así no ocupamos con mucha lógica el widget */
    public static class ExtraerTareaAleatoriaService extends Service {
        public int onStartCommand(Intent intent, int flags, int startId) {
            // Intentamos obtener una nota aleatoria disponible en la DB
            Nota nota = tryGetNotaAleataria();

            // Configuramos el texto y el botón del widget
            RemoteViews removeViews = new RemoteViews(this.getPackageName(),
                    R.layout.widget_geonotas_layout);

            // Si hay una nota, le ponemos un "enlace"
            if (nota != null) {
                removeViews.setTextViewText(R.id.texto, nota.getTitulo());
                removeViews.setTextViewText(R.id.boton, getResources()
                        .getString(R.string.ver_nota));

                removeViews.setOnClickPendingIntent(R.id.boton,
                        getPendingSelfIntent(getBaseContext(),
                                GeoNotasWidget.CLICK_BOTON_MOSTRAR_TAREA, nota.getId()));
            } else {
                // Si no hay una nota, recomendaremos introducir una nota
                removeViews.setTextViewText(R.id.texto, getResources()
                        .getString(R.string.anade_notas));
                removeViews.setTextViewText(R.id.boton, getResources()
                        .getString(R.string.anadir));

                // Preparamos el evento de mostrar nota
                removeViews.setOnClickPendingIntent(R.id.boton,
                        getPendingSelfIntent(getBaseContext(),
                                GeoNotasWidget.CLICK_BOTON_MOSTRAR_LISTADO, null));
            }

            // Y modificamos el widget del escritorio
            ComponentName thisWidget = new ComponentName(this, GeoNotasWidget.class);

            AppWidgetManager manager =
                    AppWidgetManager.getInstance(this);
            manager.updateAppWidget(thisWidget, removeViews);

            // Para que sea recreado el servicio, de quedarse sin memoria el sistema, usamos...
            return START_STICKY;
        }

        // Lo utilizaremos para actualizar el evento de onclick del botón del widget
        protected PendingIntent getPendingSelfIntent(Context context, String action, Integer idTarea) {
            Intent intent = new Intent(context, GeoNotasWidget.class);
            intent.putExtra(ID_NOTA, idTarea);
            intent.setAction(action);

            // Es VITAL indicar el flag "FLAG_UPDATE_CURRENT", si no los extras no se pasarán
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        // Intentará devolver una nota aleatoria; retorna null si no encuentra
        private Nota tryGetNotaAleataria() {
            Nota nota = null;

            // Extraemos los datos de las notas disponibles
            Uri uri2 = URIUtils.UNota.CONTENT_URI;
            String[] projection = {URIUtils.UNota.ID, URIUtils.UNota.TITULO};

            Cursor c = this.getContentResolver()
                    .query(uri2, projection, null, null, null);

            if (c == null)
                return null;

            try {
                int totalNotas = c.getCount();

                if (totalNotas != 0) {
                    int posAleatoria = NumerosAleatorios.getRandomNumberInRange(0, totalNotas - 1);

                    if (c.moveToPosition(posAleatoria)) {
                        int idNota = c.getInt(0);
                        String tituloNota = c.getString(1);

                        nota = Nota.newBuilder()
                                .withTitulo(tituloNota)
                                .withDescripcion("")
                                .withId(idNota)
                                .build();
                    } else {
                        Log.e(TAG, "Error 1323892");
                    }
                }
            } finally {
                c.close();
            }

            return nota;
        }

        @Override
        public IBinder onBind(Intent intent) {
            // No permitimos conectar con este servicio
            return null;
        }
    }
}