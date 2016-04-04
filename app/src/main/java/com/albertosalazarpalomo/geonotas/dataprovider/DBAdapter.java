package com.albertosalazarpalomo.geonotas.dataprovider;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Adaptador de la DB que es el encargado de crear toda la estructura de las tablas, así como de
 * recrearla si la versión de la DB es nueva
 */
public class DBAdapter {
    protected static final String TAG = "DBAdapter";

    public static final String DATABASE_NAME = "notas_geoloc_1.db";
    public static final int DATABASE_VERSION = 103;

    protected final Context context;
    protected DatabaseHelper mDBHelper;

    public DBAdapter(Context ctx)
    {
        context = ctx;
        mDBHelper = new DatabaseHelper(context);
    }

    public SQLiteDatabase open() throws SQLException {
        if (mDBHelper == null) {
            mDBHelper = new DatabaseHelper(context);
        }

        return mDBHelper.getWritableDatabase();
    }

    protected static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            final String tableUNota =
                    "CREATE TABLE "
                    + URIUtils.UNota.NAME + "("
                    + URIUtils.UNota.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + URIUtils.UNota.TITULO + " TEXT, "
                    + URIUtils.UNota.DESCRIPCION + " TEXT, "
                    + URIUtils.UNota.BLOBIMAGE + " BLOB"
                    + ");";
            final String tableUCoordenadas =
                    "CREATE TABLE "
                    + URIUtils.UCoordenadas.NAME + "("
                    + URIUtils.UCoordenadas.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + URIUtils.UCoordenadas.LATITUD + " FLOAT, "
                    + URIUtils.UCoordenadas.LONGITUD + " FLOAT, "
                    + URIUtils.UCoordenadas.DIRECCION_CAMPO_1 + " TEXT, "
                    + URIUtils.UCoordenadas.DIRECCION_CAMPO_2 + " TEXT, "
                    + URIUtils.UCoordenadas.DIRECCION_CP + " INTEGER, "
                    + URIUtils.UCoordenadas.LOCALIDAD + " TEXT, "
                    + URIUtils.UCoordenadas.PROVINCIA + " TEXT"
                    + ");";
            final String tableUCoordenadasNota =
                    "CREATE TABLE "
                    + URIUtils.UCoordenadasNota.NAME + "("
                    + URIUtils.UCoordenadasNota.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + URIUtils.UCoordenadasNota.ID_COORDENADAS + " INTEGER, "
                    + URIUtils.UCoordenadasNota.ID_NOTA +" INTEGER, "
                    + URIUtils.UCoordenadasNota.FECHA +" INTEGER, "
                    + "FOREIGN KEY(" + URIUtils.UCoordenadasNota.ID_COORDENADAS + ") REFERENCES " + URIUtils.UCoordenadas.NAME + "(" + URIUtils.UCoordenadas.ID + ") ON DELETE CASCADE, "
                    + "FOREIGN KEY(" + URIUtils.UCoordenadasNota.ID_NOTA + ") REFERENCES " + URIUtils.UNota.NAME + "(" + URIUtils.UNota.ID + ") ON DELETE CASCADE"
                    + ");";

            db.execSQL(tableUNota);
            db.execSQL(tableUCoordenadas);
            db.execSQL(tableUCoordenadasNota);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion) {
            Log.w(TAG, "Actualizando base de datos desde la versión " + oldVersion + " a "
                    + newVersion + ", con lo que se destruirán todos los datos!");

            db.execSQL("DROP TABLE IF EXISTS " + URIUtils.UCoordenadasNota.NAME + ";");
            db.execSQL("DROP TABLE IF EXISTS " + URIUtils.UCoordenadas.NAME + ";");
            db.execSQL("DROP TABLE IF EXISTS " + URIUtils.UNota.NAME + ";");

            onCreate(db);
        }

        @Override
        public void onConfigure(SQLiteDatabase db){
            db.execSQL("PRAGMA foreign_keys = ON;"); // Activamos claves foráneas
        }
    }
}