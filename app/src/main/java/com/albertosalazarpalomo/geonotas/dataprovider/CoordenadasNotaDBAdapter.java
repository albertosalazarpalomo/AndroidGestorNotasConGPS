package com.albertosalazarpalomo.geonotas.dataprovider;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;

import com.albertosalazarpalomo.geonotas.model.CoordenadasNota;

/**
 * Adaptador de la DB encargado de manipular los pares "nota" en una "coordenada": se ha creado
 * una tabla intermedia con el objetivo de que en el futuro pueda darse una verdadera relación de
 * "muchos a muchos" entre "coordenadas" y "notas". Puesto que distintas notas, en un futuro,
 * podrían estar en una misma localización. Resumiendo: escalabilidad del código creado.
 */
public class CoordenadasNotaDBAdapter {
    private SQLiteDatabase db;

    public CoordenadasNotaDBAdapter(SQLiteDatabase db) {
        this.db = db;
    }

    public ResQuery create(CoordenadasNota coordenadasNota) {
        ResQuery res = new ResQuery();

        String sql = "INSERT INTO " + URIUtils.UCoordenadasNota.NAME + " ("
                + URIUtils.UCoordenadasNota.ID_COORDENADAS + ", "
                + URIUtils.UCoordenadasNota.ID_NOTA + ", "
                + URIUtils.UCoordenadasNota.FECHA
                + ") VALUES(?,?,?);";

        try {
            SQLiteStatement insertStmt = db.compileStatement(sql);

            insertStmt.bindLong(1, coordenadasNota.getIdCoordenadas());
            insertStmt.bindLong(2, coordenadasNota.getIdNota());
            insertStmt.bindLong(3, coordenadasNota.getFecha());

            long idGenerado = insertStmt.executeInsert();
            res.setIdRowInsertada(idGenerado);
        } catch (SQLiteException e) {
            res.setErrorCod(1);
            e.printStackTrace();
        }

        return res;
    }

    public ResQuery createWithId(CoordenadasNota coordenadasNota) {
        ResQuery res = new ResQuery();

        String sql = "INSERT INTO " + URIUtils.UCoordenadasNota.NAME + " ("
                + URIUtils.UCoordenadasNota.ID_COORDENADAS + ", "
                + URIUtils.UCoordenadasNota.ID_NOTA + ", "
                + URIUtils.UCoordenadasNota.FECHA + ", "
                + URIUtils.UCoordenadasNota.ID
                + ") VALUES(?,?,?,?);";

        try {
            SQLiteStatement insertStmt = db.compileStatement(sql);

            insertStmt.bindLong(1, coordenadasNota.getIdCoordenadas());
            insertStmt.bindLong(2, coordenadasNota.getIdNota());
            insertStmt.bindLong(3, coordenadasNota.getFecha());
            insertStmt.bindLong(4, coordenadasNota.getId());

            insertStmt.executeInsert();
        } catch (SQLiteException e) {
            res.setErrorCod(1);
            e.printStackTrace();
        }

        return res;
    }

    public boolean deleteAllWithIdNota(int idNota){
        boolean ok = true;

        // Al borrar al "padre" (coordenadas), se borran los "hijos" (coordenadas_nota)
        String sql = "DELETE FROM coordenadas WHERE _id IN (SELECT id_coordenadas FROM coordenadas_nota WHERE id_nota = ?);";

        try {
            //db.execSQL("PRAGMA foreign_keys = ON;");

            SQLiteStatement stmt = db.compileStatement(sql);
            stmt.bindLong(1, idNota);
            stmt.execute();
        } catch (SQLiteException e) {
            ok = false;
            e.printStackTrace();
        }

        return ok;
    }

    public boolean delete(int idCoordenadasNotaBuscar){
        boolean ok = true;

        String sql = "DELETE FROM " + URIUtils.UCoordenadasNota.NAME
                + " WHERE " + URIUtils.UCoordenadasNota.ID + " = ?;";

        try {
            SQLiteStatement stmt = db.compileStatement(sql);
            stmt.bindLong(1, idCoordenadasNotaBuscar);
            stmt.execute();
        } catch (SQLiteException e) {
            ok = false;
            e.printStackTrace();
        }

        return ok;
    }

    public CoordenadasNota get(int idCoordenadasNotaBuscar) {
        CoordenadasNota coordenadasNota = null;

        String sql = "SELECT "
                + URIUtils.UCoordenadasNota.ID_COORDENADAS + ", "
                + URIUtils.UCoordenadasNota.ID_NOTA + ", "
                + URIUtils.UCoordenadasNota.FECHA
                + " FROM " + URIUtils.UCoordenadasNota.NAME
                + " WHERE " + URIUtils.UCoordenadasNota.ID + " = ?;";

        String[] args = new String[] {Integer.toString(idCoordenadasNotaBuscar)};

        try {
            Cursor c = db.rawQuery(sql, args);

            c.moveToFirst();

            Integer idCoordenadas = c.getInt(0);
            Integer idNota = c.getInt(1);
            Long fecha = c.getLong(2);

            coordenadasNota = CoordenadasNota.newBuilder()
                    .withIdCoordenadas(idCoordenadas)
                    .withIdNota(idNota)
                    .withFecha(fecha)
                    .withId(idCoordenadasNotaBuscar)
                    .build();
        } catch (SQLiteException e) {
            coordenadasNota = null;
            e.printStackTrace();
        }

        return coordenadasNota;
    }

    // Para obtener un listado de todas las coordenadas donde se encuentran actualmente las notas
    public Cursor getAllActualCoordenadas() {
        Cursor cursor = null;

        String sql = "SELECT a._id, a.id_nota, a.fecha, c.latitud, c.longitud FROM coordenadas_nota AS a " +

                "LEFT JOIN coordenadas_nota AS b " +
                "ON a.id_nota = b.id_nota AND a._id < b._id " +

                "LEFT JOIN coordenadas AS c " +
                "ON a.id_coordenadas = c._id " +

                "WHERE b._id is NULL;"; // Los obtengo todos

        try {
            cursor = db.rawQuery(sql, null);
        } catch (SQLiteException e) {
            cursor = null;
            e.printStackTrace();
        }

        return cursor;
    }

    // Para obtener la coordenada donde se encuentra actualmente, la nota con id "idNota"
    public Cursor getLastActualCoordenadas(int idNota) {
        Cursor cursor = null;

        String sql = "SELECT a._id, a.id_nota, a.fecha, c._id, c.latitud, c.longitud FROM coordenadas_nota AS a " +

                "LEFT JOIN coordenadas_nota AS b " +
                "ON a.id_nota = b.id_nota AND a._id < b._id " +

                "LEFT JOIN coordenadas AS c " +
                "ON a.id_coordenadas = c._id " +

                "WHERE b._id is NULL " +
                "AND a.id_nota  = ?;"; // Los obtengo todos

        String[] args = new String[] {Integer.toString(idNota)};

        try {
            cursor = db.rawQuery(sql, args);
        } catch (SQLiteException e) {
            cursor = null;
            e.printStackTrace();
        }

        return cursor;
    }
}