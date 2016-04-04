package com.albertosalazarpalomo.geonotas.dataprovider;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;

import com.albertosalazarpalomo.geonotas.model.Coordenadas;

/**
 * Adaptador de DB de la tabla "coordenadas"
 */
public class CoordenadasDBAdapter {
    private SQLiteDatabase db;

    public CoordenadasDBAdapter(SQLiteDatabase db) {
        this.db = db;
    }

    public ResQuery create(Coordenadas coordenadas) {
        ResQuery res = new ResQuery();

        String sql = "INSERT INTO " + URIUtils.UCoordenadas.NAME + " ("
                + URIUtils.UCoordenadas.LATITUD + ", "
                + URIUtils.UCoordenadas.LONGITUD + ", "
                + URIUtils.UCoordenadas.DIRECCION_CAMPO_1 + ", "
                + URIUtils.UCoordenadas.DIRECCION_CAMPO_2 + ", "
                + URIUtils.UCoordenadas.DIRECCION_CP + ", "
                + URIUtils.UCoordenadas.LOCALIDAD + ", "
                + URIUtils.UCoordenadas.PROVINCIA
                + ") VALUES(?,?,?,?,?,?,?);";

        try {
            SQLiteStatement insertStmt = db.compileStatement(sql);

            // Campos obligatorios
            insertStmt.bindDouble(1, coordenadas.getLatitud());
            insertStmt.bindDouble(2, coordenadas.getLongitud());

            // Campos opcionales
            String direccionCampo1 = coordenadas.getDireccionCampo1();
            String direccionCampo2 = coordenadas.getDireccionCampo2();
            Integer direccionCP = coordenadas.getDireccionCP();
            String localidad = coordenadas.getLocalidad();
            String provincia = coordenadas.getProvincia();

            if (direccionCampo1 == null) {
                insertStmt.bindNull(3);
            } else {
                insertStmt.bindString(3, direccionCampo1);
            }

            if (direccionCampo2 == null) {
                insertStmt.bindNull(4);
            } else {
                insertStmt.bindString(4, direccionCampo2);
            }

            if (direccionCP == null) {
                insertStmt.bindNull(5);
            } else {
                insertStmt.bindLong(5, direccionCP);
            }

            if (localidad == null) {
                insertStmt.bindNull(6);
            } else {
                insertStmt.bindString(6, localidad);
            }

            if (provincia == null) {
                insertStmt.bindNull(7);
            } else {
                insertStmt.bindString(7, provincia);
            }

            long idGenerado = insertStmt.executeInsert();
            res.setIdRowInsertada(idGenerado);
        } catch (SQLiteException e) {
            res.setErrorCod(1);
            e.printStackTrace();
        }

        return res;
    }

    public ResQuery createWithId(Coordenadas coordenadas) {
        ResQuery res = new ResQuery();

        String sql = "INSERT INTO " + URIUtils.UCoordenadas.NAME + " ("
                + URIUtils.UCoordenadas.LATITUD + ", "
                + URIUtils.UCoordenadas.LONGITUD + ", "
                + URIUtils.UCoordenadas.DIRECCION_CAMPO_1 + ", "
                + URIUtils.UCoordenadas.DIRECCION_CAMPO_2 + ", "
                + URIUtils.UCoordenadas.DIRECCION_CP + ", "
                + URIUtils.UCoordenadas.LOCALIDAD + ", "
                + URIUtils.UCoordenadas.PROVINCIA + ", "
                + URIUtils.UCoordenadas.ID
                + ") VALUES(?,?,?,?,?,?,?,?);";

        try {
            SQLiteStatement insertStmt = db.compileStatement(sql);

            // Campos obligatorios
            insertStmt.bindDouble(1, coordenadas.getLatitud());
            insertStmt.bindDouble(2, coordenadas.getLongitud());
            insertStmt.bindLong(8, coordenadas.getId());

            // Campos opcionales
            String direccionCampo1 = coordenadas.getDireccionCampo1();
            String direccionCampo2 = coordenadas.getDireccionCampo2();
            Integer direccionCP = coordenadas.getDireccionCP();
            String localidad = coordenadas.getLocalidad();
            String provincia = coordenadas.getProvincia();

            if (direccionCampo1 == null) {
                insertStmt.bindNull(3);
            } else {
                insertStmt.bindString(3, direccionCampo1);
            }

            if (direccionCampo2 == null) {
                insertStmt.bindNull(4);
            } else {
                insertStmt.bindString(4, direccionCampo2);
            }

            if (direccionCP == null) {
                insertStmt.bindNull(5);
            } else {
                insertStmt.bindLong(5, direccionCP);
            }

            if (localidad == null) {
                insertStmt.bindNull(6);
            } else {
                insertStmt.bindString(6, localidad);
            }

            if (provincia == null) {
                insertStmt.bindNull(7);
            } else {
                insertStmt.bindString(7, provincia);
            }

            long idGenerado = insertStmt.executeInsert();
            res.setIdRowInsertada(idGenerado);
        } catch (SQLiteException e) {
            res.setErrorCod(1);
            e.printStackTrace();
        }

        return res;
    }

    public boolean delete(int idCoordenadasBuscar){
        boolean ok = true;

        String sql = "DELETE FROM " + URIUtils.UCoordenadas.NAME
                + " WHERE " + URIUtils.UCoordenadas.ID + " = ?;";

        try {
            SQLiteStatement stmt = db.compileStatement(sql);
            stmt.bindLong(1, idCoordenadasBuscar);
            stmt.execute();
        } catch (SQLiteException e) {
            ok = false;
            e.printStackTrace();
        }

        return ok;
    }

    public Coordenadas get(int idCoordenadasBuscar) {
        Coordenadas coordenadas = null;

        String sql = "SELECT "
                + URIUtils.UCoordenadas.LATITUD + ", "
                + URIUtils.UCoordenadas.LONGITUD + ", "
                + URIUtils.UCoordenadas.DIRECCION_CAMPO_1 + ", "
                + URIUtils.UCoordenadas.DIRECCION_CAMPO_2 + ", "
                + URIUtils.UCoordenadas.DIRECCION_CP + ", "
                + URIUtils.UCoordenadas.LOCALIDAD + ", "
                + URIUtils.UCoordenadas.PROVINCIA
                + " FROM " + URIUtils.UCoordenadas.NAME
                + " WHERE " + URIUtils.UCoordenadas.ID + " = ?;";

        String[] args = new String[] {Integer.toString(idCoordenadasBuscar)};

        try {
            Cursor c = db.rawQuery(sql, args);

            c.moveToFirst();

            Double latitud = c.getDouble(0);
            Double longitud = c.getDouble(1);
            String direccionCampo1 = c.getString(2);
            String direccionCampo2 = c.getString(3);
            Integer direccionCP = c.getInt(4);
            String localidad = c.getString(5);
            String provincia = c.getString(6);

            coordenadas = Coordenadas.newBuilder()
                    .withLatitud(latitud)
                    .withLongitud(longitud)
                    .withDireccionCampo1(direccionCampo1)
                    .withDireccionCampo2(direccionCampo2)
                    .withDireccionCP(direccionCP)
                    .withLocalidad(localidad)
                    .withProvincia(provincia)
                    .withId(idCoordenadasBuscar)
                    .build();
        } catch (SQLiteException e) {
            coordenadas = null;
            e.printStackTrace();
        }

        return coordenadas;
    }

    public ResQuery update(long conIdCoordenadas, Coordenadas coordenadas) {
        ResQuery res = new ResQuery();

        String sql = "UPDATE " + URIUtils.UCoordenadas.NAME + " SET "
                + URIUtils.UCoordenadas.LATITUD + " = ?, "
                + URIUtils.UCoordenadas.LONGITUD + " = ?, "
                + URIUtils.UCoordenadas.DIRECCION_CAMPO_1 + " = ?, "
                + URIUtils.UCoordenadas.DIRECCION_CAMPO_2 + " = ?, "
                + URIUtils.UCoordenadas.DIRECCION_CP + " = ?, "
                + URIUtils.UCoordenadas.LOCALIDAD + " = ?, "
                + URIUtils.UCoordenadas.PROVINCIA + " = ? "
                + "WHERE " + URIUtils.UCoordenadas.ID + " = ?;";

        try {
            SQLiteStatement insertStmt = db.compileStatement(sql);

            // Campos obligatorios
            insertStmt.bindDouble(1, coordenadas.getLatitud());
            insertStmt.bindDouble(2, coordenadas.getLongitud());

            // Campos opcionales
            String direccionCampo1 = coordenadas.getDireccionCampo1();
            String direccionCampo2 = coordenadas.getDireccionCampo2();
            Integer direccionCP = coordenadas.getDireccionCP();
            String localidad = coordenadas.getLocalidad();
            String provincia = coordenadas.getProvincia();

            if (direccionCampo1 == null) {
                insertStmt.bindNull(3);
            } else {
                insertStmt.bindString(3, direccionCampo1);
            }

            if (direccionCampo2 == null) {
                insertStmt.bindNull(4);
            } else {
                insertStmt.bindString(4, direccionCampo2);
            }

            if (direccionCP == null) {
                insertStmt.bindNull(5);
            } else {
                insertStmt.bindLong(5, direccionCP);
            }

            if (localidad == null) {
                insertStmt.bindNull(6);
            } else {
                insertStmt.bindString(6, localidad);
            }

            if (provincia == null) {
                insertStmt.bindNull(7);
            } else {
                insertStmt.bindString(7, provincia);
            }

            insertStmt.bindLong(8, conIdCoordenadas);

            insertStmt.execute();
        } catch (SQLiteException e) {
            res.setErrorCod(1);
            e.printStackTrace();
        }

        return res;
    }
}