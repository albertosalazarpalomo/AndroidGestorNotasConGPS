package com.albertosalazarpalomo.geonotas.dataprovider;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;

import com.albertosalazarpalomo.geonotas.model.Nota;

/**
 * Métodos para obtener información sobre las Notas, de la DB: se explican por sí mismos
 */
public class NotaDBAdapter {
    private SQLiteDatabase db;

    public NotaDBAdapter(SQLiteDatabase db) {
        this.db = db;
    }

    public ResQuery create(Nota nota) {
        ResQuery res = new ResQuery();

        String sql = "INSERT INTO " + URIUtils.UNota.NAME + " ("
                + URIUtils.UNota.TITULO + ", "
                + URIUtils.UNota.DESCRIPCION + ", "
                + URIUtils.UNota.BLOBIMAGE
                + ") VALUES(?,?,?); ";

        try {
            SQLiteStatement insertStmt = db.compileStatement(sql);

            insertStmt.bindString(1, nota.getTitulo());
            insertStmt.bindString(2, nota.getDescripcion());

            byte[] blobImage = nota.getBlobImage();
            if (blobImage == null) {
                insertStmt.bindNull(3);
            } else {
                insertStmt.bindBlob(3, blobImage);
            }

            long idGenerado = insertStmt.executeInsert();
            res.setIdRowInsertada(idGenerado);
        } catch (SQLiteException e) {
            res.setErrorCod(1);
            e.printStackTrace();
        }

        return res;
    }

    public ResQuery createWithId(Nota nota) {
        ResQuery res = new ResQuery();

        String sql = "INSERT INTO " + URIUtils.UNota.NAME + " ("
                + URIUtils.UNota.TITULO + ", "
                + URIUtils.UNota.DESCRIPCION + ", "
                + URIUtils.UNota.BLOBIMAGE + ", "
                + URIUtils.UNota.ID
                + ") VALUES(?,?,?,?); ";

        try {
            SQLiteStatement insertStmt = db.compileStatement(sql);

            insertStmt.bindString(1, nota.getTitulo());
            insertStmt.bindString(2, nota.getDescripcion());
            insertStmt.bindLong(4, nota.getId());

            byte[] blobImage = nota.getBlobImage();
            if (blobImage == null) {
                insertStmt.bindNull(3);
            } else {
                insertStmt.bindBlob(3, blobImage);
            }

            insertStmt.executeInsert();
        } catch (SQLiteException e) {
            res.setErrorCod(1);
            e.printStackTrace();
        }

        return res;
    }

    public Nota get(int idNotaBuscar) {
        Nota nota = null;

        String sql = "SELECT "
                + URIUtils.UNota.TITULO + ", "
                + URIUtils.UNota.DESCRIPCION
                + " FROM " + URIUtils.UNota.NAME
                + " WHERE " + URIUtils.UNota.ID + " = ?;";

        String[] args = new String[] {Integer.toString(idNotaBuscar)};

        try {
            Cursor c = db.rawQuery(sql, args);

            c.moveToFirst();
            String titulo = c.getString(0);
            String descripcion = c.getString(1);

            nota = Nota.newBuilder()
                    .withTitulo(titulo)
                    .withDescripcion(descripcion)
                    .withId(idNotaBuscar)
                    .build();
        } catch (SQLiteException e) {
            nota = null;
            e.printStackTrace();
        }

        return nota;
    }

    public ResQuery update(long conIdNota, Nota nota) {
        ResQuery res = new ResQuery();

        String sql = "UPDATE " + URIUtils.UNota.NAME + " SET "
                + URIUtils.UNota.TITULO + " = ?, "
                + URIUtils.UNota.DESCRIPCION + " = ?, "
                + URIUtils.UNota.BLOBIMAGE + " = ?"
                + " WHERE " + URIUtils.UNota.ID + " = ?;";

        try {
            SQLiteStatement insertStmt = db.compileStatement(sql);

            insertStmt.bindString(1, nota.getTitulo());
            insertStmt.bindString(2, nota.getDescripcion());
            insertStmt.bindLong(4, conIdNota);

            byte[] blobImage = nota.getBlobImage();
            if (blobImage == null) {
                insertStmt.bindNull(3);
            } else {
                insertStmt.bindBlob(3, blobImage);
            }

            insertStmt.execute();
        } catch (SQLiteException e) {
            res.setErrorCod(1);
            e.printStackTrace();
        }

        return res;
    }

    public boolean delete(int idNotaBuscar){
        boolean ok = true;

        String sql = "DELETE FROM " + URIUtils.UNota.NAME
                + " WHERE " + URIUtils.UNota.ID + " = ?;";

        try {
            SQLiteStatement stmt = db.compileStatement(sql);
            stmt.bindLong(1, idNotaBuscar);
            stmt.execute();
        } catch (SQLiteException e) {
            ok = false;
            e.printStackTrace();
        }

        return ok;
    }
}