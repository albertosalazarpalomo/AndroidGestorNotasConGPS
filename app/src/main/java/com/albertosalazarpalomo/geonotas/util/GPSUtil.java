package com.albertosalazarpalomo.geonotas.util;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;

import com.albertosalazarpalomo.geonotas.R;

/**
 * Created by AlbertoSP on 16/03/2016.
 */
public class GPSUtil {
    /**
     * Muestra un diálogo pidiendo la activación del GPS, y llevándote a la Activity de conf.
     */
    public static void mostrarDialogoGPS(Context context) {
        final Context mContext = context;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setTitle(mContext.getString(R.string.titulo_gps_alerta));
        builder.setMessage(mContext.getString(R.string.gps_activar_msg));
        builder.setInverseBackgroundForced(true);

        builder.setPositiveButton(mContext.getString(R.string.activar), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mContext.startActivity(
                        new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });

        builder.setNegativeButton(mContext.getString(R.string.ignorar), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }
}
