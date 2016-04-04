package com.albertosalazarpalomo.geonotas.dataprovider;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.albertosalazarpalomo.geonotas.model.Coordenadas;
import com.albertosalazarpalomo.geonotas.model.CoordenadasNota;
import com.albertosalazarpalomo.geonotas.model.Nota;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Nos permite realizar operaciones en la DB relacionadas con la operación de la Nube "recuperar
 * copia de seguridad de las notas"
 */
public class NubeDBAdapter {
    private SQLiteDatabase db;
    private GsonBuilder builder;
    private Gson gson;

    public NubeDBAdapter(SQLiteDatabase db) {
        this.db = db;

        // Y transformamos las cadenas JSON en objectos reales
        builder = new GsonBuilder();
        builder.excludeFieldsWithoutExposeAnnotation();
        gson = builder.create();
    }

    public boolean restart(String stringArrayJSON) {
        boolean exito = true;

        // Obtenemos el objeto array, a partir de la string
        JSONArray arrayJSON = new JSONArray();
        try {
            arrayJSON = new JSONArray(stringArrayJSON);
        } catch (JSONException e) {
            return false;
        }

        // Lo hacemos en una transacción, para no dejar la DB en estado roto, "a medias"
        db.beginTransaction();

        try {
            if (!borrarDB()) {
                exito = false;
            }

            // Volcamos información sobre NOTAS
            JSONObject preNotasJSON = arrayJSON.getJSONObject(0);
            JSONArray notasJSON = preNotasJSON.getJSONArray(URIUtils.UNota.NAME);

            if (notasJSON.length() > 0 && exito && !guardarNotas(notasJSON)) {
                exito = false;
            }

            // Volcamos información sobre COORDENADAS
            JSONObject preCoorsJSON = arrayJSON.getJSONObject(1);
            JSONArray coorsJSON = preCoorsJSON.getJSONArray(URIUtils.UCoordenadas.NAME);

            if (coorsJSON.length() > 0 && exito && !guardarCoordenadas(coorsJSON)) {
                exito = false;
            }

            // Volcamos información sobre COORDENADAS_NOTAS
            JSONObject preCoorNotasJSON = arrayJSON.getJSONObject(2);
            JSONArray coorNotasJSON = preCoorNotasJSON.getJSONArray(URIUtils.UCoordenadasNota.NAME);

            if (coorNotasJSON.length() > 0 && exito && !guardarCoordenadasNotas(coorNotasJSON)) {
                exito = false;
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            // Si no ha habido éxito, no se salvarán los cambios en la DB
            if (exito) {
                db.setTransactionSuccessful();
            }

            db.endTransaction();
        }

        return exito;
    }

    private boolean borrarDB() {
        try {
            db.execSQL("DELETE FROM " + URIUtils.UCoordenadasNota.NAME + ";");
            db.execSQL("DELETE FROM " + URIUtils.UCoordenadas.NAME + ";");
            db.execSQL("DELETE FROM " + URIUtils.UNota.NAME + ";");
        } catch (SQLiteException e) {
            e.printStackTrace();

            return false;
        }

        return true;
    }

    private boolean guardarNotas(JSONArray arrayJSON) {
        NotaDBAdapter notaDBAdapter = new NotaDBAdapter(db);

        for (int i = 0; i < arrayJSON.length(); i++) {
            try {
                // Creamos el objeto desde el JSON
                JSONObject notaJSON = arrayJSON.getJSONObject(i);
                Nota nota = gson.fromJson(notaJSON.toString(), Nota.class);

                ResQuery res = notaDBAdapter.createWithId(nota);

                if (res.isError()) {
                    return false;
                }
            } catch (JSONException e) {
                return false;
            }
        }

        return true;
    }

    private boolean guardarCoordenadas(JSONArray arrayJSON) {
        CoordenadasDBAdapter coorDBAdapter = new CoordenadasDBAdapter(db);

        for (int i = 0; i < arrayJSON.length(); i++) {
            try {
                // Creamos el objeto desde el JSON
                JSONObject notaJSON = arrayJSON.getJSONObject(i);
                Coordenadas coor = gson.fromJson(notaJSON.toString(),
                        Coordenadas.class);

                ResQuery res = coorDBAdapter.createWithId(coor);

                if (res.isError()) {
                    return false;
                }
            } catch (JSONException e) {
                return false;
            }
        }

        return true;
    }

    private boolean guardarCoordenadasNotas(JSONArray arrayJSON) {
        CoordenadasNotaDBAdapter coorNotaDBAdapter = new CoordenadasNotaDBAdapter(db);

        for (int i = 0; i < arrayJSON.length(); i++) {
            try {
                // Creamos el objeto desde el JSON
                JSONObject coorNotaJSON = arrayJSON.getJSONObject(i);
                CoordenadasNota coorNota = gson.fromJson(coorNotaJSON.toString(),
                        CoordenadasNota.class);

                ResQuery res = coorNotaDBAdapter.createWithId(coorNota);

                if (res.isError()) {
                    return false;
                }
            } catch (JSONException e) {
                return false;
            }
        }

        return true;
    }
}