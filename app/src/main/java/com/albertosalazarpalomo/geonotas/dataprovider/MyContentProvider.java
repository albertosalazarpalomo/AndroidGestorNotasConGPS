package com.albertosalazarpalomo.geonotas.dataprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.albertosalazarpalomo.geonotas.model.Coordenadas;
import com.albertosalazarpalomo.geonotas.model.CoordenadasNota;
import com.albertosalazarpalomo.geonotas.model.Nota;

/**
 * ContentProvider que envuelve las consultas de la DB, y permite una capa extra de abstracción,
 * aparte de los beneficios propios del uso de un ContentProvider en Android
 */
public class MyContentProvider extends ContentProvider {
    // Constantes usadas por el UriMacher
    private static final int NOTA = 10;
    private static final int NOTA_ID = 20;
    private static final int COORDENADAS = 30;
    private static final int COORDENADAS_ID = 40;
    private static final int COORDENADAS_NOTA = 50;
    private static final int COORDENADAS_NOTA_ID = 60;
    private static final int COORDENADAS_NOTA_LAST = 70;
    private static final int COORDENADAS_NOTA_LAST_ID = 80;
    private static final int COORDENADAS_NOTA_WITH_ID_NOTA = 90;

    private static final int RESTART_WHOLE_DATABASE = 160;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        // Significado de modificadores: # = número, * = texto

        sURIMatcher.addURI(URIUtils.AUTHORITY, URIUtils.UNota.NAME
                , NOTA);
        sURIMatcher.addURI(URIUtils.AUTHORITY, URIUtils.UNota.NAME + "/#"
                , NOTA_ID);

        sURIMatcher.addURI(URIUtils.AUTHORITY, URIUtils.UCoordenadas.NAME
                , COORDENADAS);
        sURIMatcher.addURI(URIUtils.AUTHORITY, URIUtils.UCoordenadas.NAME + "/#"
                , COORDENADAS_ID);

        sURIMatcher.addURI(URIUtils.AUTHORITY, URIUtils.UCoordenadasNota.NAME
                , COORDENADAS_NOTA);
        sURIMatcher.addURI(URIUtils.AUTHORITY, URIUtils.UCoordenadasNota.NAME + "/#"
                , COORDENADAS_NOTA_ID);

        sURIMatcher.addURI(URIUtils.AUTHORITY, URIUtils.UCoordenadasNotaLast.NAME
                , COORDENADAS_NOTA_LAST);
        sURIMatcher.addURI(URIUtils.AUTHORITY, URIUtils.UCoordenadasNotaLast.NAME + "/#"
                , COORDENADAS_NOTA_LAST_ID);

        sURIMatcher.addURI(URIUtils.AUTHORITY, URIUtils.UCoordenadasNotaWithIdNota.NAME + "/#"
                , COORDENADAS_NOTA_WITH_ID_NOTA);

        sURIMatcher.addURI(URIUtils.AUTHORITY, URIUtils.WholeDatabase.NAME
                , RESTART_WHOLE_DATABASE);
    }

    // Adaptadores que nos permiten acceder a la base de datos
    DBAdapter myDBAdapter;

    @Override
    public boolean onCreate() {
        myDBAdapter = new DBAdapter(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = myDBAdapter.open();

        Cursor cursor = null;

        int uriType = sURIMatcher.match(uri);

        // Como manejaremos de forma distinta dos grandes grupos, hacemos un if-else y dos switch
        if (uriType == COORDENADAS_NOTA_LAST || uriType == COORDENADAS_NOTA_LAST_ID) {
            CoordenadasNotaDBAdapter coordenadasNotaDBAdapter = new CoordenadasNotaDBAdapter(db);

            switch (uriType) {
                case COORDENADAS_NOTA_LAST:
                    cursor = coordenadasNotaDBAdapter.getAllActualCoordenadas();
                    break;

                case COORDENADAS_NOTA_LAST_ID:
                    int idNotaBuscar = Integer.parseInt(uri.getLastPathSegment());

                    cursor = coordenadasNotaDBAdapter
                            .getLastActualCoordenadas(idNotaBuscar);
                    break;
            }
        } else {
            // Using SQLiteQueryBuilder instead of query() method
            SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

            // Configuramos el limit
            String limit = null;

            switch (uriType) {
                case NOTA:
                    queryBuilder.setTables(URIUtils.UNota.NAME);
                    break;

                case NOTA_ID:
                    queryBuilder.setTables(URIUtils.UNota.NAME);

                    // adding the ID to the original query
                    queryBuilder.appendWhere(URIUtils.UNota.ID + "="
                            + uri.getLastPathSegment());
                    break;

                case COORDENADAS:
                    queryBuilder.setTables(URIUtils.UCoordenadas.NAME);
                    break;

                case COORDENADAS_NOTA:
                    queryBuilder.setTables(URIUtils.UCoordenadasNota.NAME);
                    break;

                default:
                    throw new IllegalArgumentException("Unknown URI: " + uri);
            }

            cursor = queryBuilder.query(db, projection, selection,
                    selectionArgs, null, null, sortOrder, limit);
        }

        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    /*
      Devolverá null, porque no será relevante información extra sobre el tipo de datos
     */
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = myDBAdapter.open();

        int uriType = sURIMatcher.match(uri);

        // Comprobamos qué se nos pide insertar
        switch (uriType) {
            case RESTART_WHOLE_DATABASE:
                final NubeDBAdapter nubeDBAdapter = new NubeDBAdapter(db);

                String jsonEstado = values.getAsString(URIUtils.WholeDatabase.ESTADO);

                if (nubeDBAdapter.restart(jsonEstado)) {
                    return uri;
                } else {
                    throw new IllegalArgumentException("Unknown URI: " + uri);
                }

            case NOTA:
                // Creamos un nuevo UNota
                String titulo = values.getAsString(URIUtils.UNota.TITULO);
                String descripcion = values.getAsString(URIUtils.UNota.DESCRIPCION);
                byte[] blobImage = values.getAsByteArray(URIUtils.UNota.BLOBIMAGE);

                Nota nota = Nota.newBuilder()
                        .withTitulo(titulo)
                        .withDescripcion(descripcion)
                        .withBlobImage(blobImage)
                        .build();

                // Y lo introducimos en la DB
                final NotaDBAdapter notaDBAdapter = new NotaDBAdapter(db);
                ResQuery resQuery = notaDBAdapter.create(nota);

                long id = resQuery.getIdRowInsertada();

                // ¿Se ha realizado con éxito la operación, o lanzamos una excepción?
                if (id == -1) {
                    throw new IllegalArgumentException("Unknown URI: " + uri);
                } else {
                    return notifyAndGetUriWithId(uri, id);
                }

            case COORDENADAS:
                // Creamos un objeto "Coordenadas"
                Double latitud = values.getAsDouble(URIUtils.UCoordenadas.LATITUD);
                Double longitud = values.getAsDouble(URIUtils.UCoordenadas.LONGITUD);

                Coordenadas coordenadas = Coordenadas.newBuilder()
                        .withLatitud(latitud)
                        .withLongitud(longitud)
                        .build();

                // Y lo introducimos en la DB
                final CoordenadasDBAdapter coorDBAdapter = new CoordenadasDBAdapter(db);
                ResQuery resQueryCoor = coorDBAdapter.create(coordenadas);

                long idCoor = resQueryCoor.getIdRowInsertada();

                // ¿Se ha realizado con éxito la operación, o lanzamos una excepción?
                if (idCoor == -1) {
                    throw new IllegalArgumentException("Unknown URI: " + uri);
                } else {
                    // Devolvemos el id generado
                    return notifyAndGetUriWithId(uri, idCoor);
                }

            case COORDENADAS_NOTA:
                // Creamos un objeto "Coordenadas"
                Integer idNota = values.getAsInteger(URIUtils.UCoordenadasNota.ID_NOTA);
                Integer idCoordenadas = values.getAsInteger(URIUtils.UCoordenadasNota.ID_COORDENADAS);
                Long fechaActual = values.getAsLong(URIUtils.UCoordenadasNota.FECHA);

                CoordenadasNota coordenadasNotaPorCrear = CoordenadasNota.newBuilder()
                        .withIdNota(idNota)
                        .withIdCoordenadas(idCoordenadas)
                        .withFecha(fechaActual)
                        .build();

                // Y lo introducimos en la DB
                final CoordenadasNotaDBAdapter coorNotaDBAdapter = new CoordenadasNotaDBAdapter(db);
                ResQuery resQueryCoorNota = coorNotaDBAdapter.create(coordenadasNotaPorCrear);

                long idCoorNota = resQueryCoorNota.getIdRowInsertada();

                // ¿Se ha realizado con éxito la operación, o lanzamos una excepción?
                if (idCoorNota == -1) {
                    throw new IllegalArgumentException("Unknown URI: " + uri);
                } else {
                    // Devolvemos el id generado
                    return notifyAndGetUriWithId(uri, idCoorNota);
                }

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted = 0;
        final SQLiteDatabase db = myDBAdapter.open();

        int id = Integer.parseInt(uri.getLastPathSegment());

        int uriType = sURIMatcher.match(uri);

        // Comprobamos qué se nos pide insertar
        switch (uriType) {
            case NOTA_ID:
                // Y lo borramos de la DB
                final NotaDBAdapter notaDBAdapter = new NotaDBAdapter(db);

                if (notaDBAdapter.delete(id)) {
                    rowsDeleted = 1;
                }
                break;
            
            case COORDENADAS_ID:
                final CoordenadasDBAdapter coorDBAdapter = new CoordenadasDBAdapter(db);

                if (coorDBAdapter.delete(id)) {
                    rowsDeleted = 1;
                }
                break;

            case COORDENADAS_NOTA_WITH_ID_NOTA:
                final CoordenadasNotaDBAdapter coorNotaDBAdapter = new CoordenadasNotaDBAdapter(db);

                //if (coorNotaDBAdapter.deleteAllPorIdNota(id)) {
                if (coorNotaDBAdapter.deleteAllWithIdNota(id)) {
                    rowsDeleted = 1;
                }
                break;

            case COORDENADAS_NOTA_ID:
                final CoordenadasNotaDBAdapter coorNotaDBAdapter2 = new CoordenadasNotaDBAdapter(db);

                if (coorNotaDBAdapter2.delete(id)) {
                    rowsDeleted = 1;
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int rowsUpdated = 0;
        final SQLiteDatabase db = myDBAdapter.open();

        int uriType = sURIMatcher.match(uri);

        // Comprobamos qué se nos pide insertar
        switch (uriType) {
            case NOTA_ID:
                int id = Integer.parseInt(uri.getLastPathSegment());

                // Creamos un nuevo UNota
                String titulo = values.getAsString(URIUtils.UNota.TITULO);
                String descripcion = values.getAsString(URIUtils.UNota.DESCRIPCION);
                byte[] imageBlob = values.getAsByteArray(URIUtils.UNota.BLOBIMAGE);

                Nota item = Nota.newBuilder()
                        .withTitulo(titulo)
                        .withDescripcion(descripcion)
                        .withBlobImage(imageBlob)
                        .build();

                // Y lo introducimos en la DB
                final NotaDBAdapter notaDBAdapter = new NotaDBAdapter(db);
                ResQuery resQuery = notaDBAdapter.update((int) id, item);

                // ¿Se ha realizado con éxito la operación?
                if (resQuery.isError()) {
                    rowsUpdated = 0;
                } else {
                    rowsUpdated = 1;
                }
                break;
            
            case COORDENADAS_ID:
                int idCoor = Integer.parseInt(uri.getLastPathSegment());

                // Creamos un objeto "Coordenadas"
                Double latitud = values.getAsDouble(URIUtils.UCoordenadas.LATITUD);
                Double longitud = values.getAsDouble(URIUtils.UCoordenadas.LONGITUD);

                Coordenadas coordenadas = Coordenadas.newBuilder()
                        .withLatitud(latitud)
                        .withLongitud(longitud)
                        .build();

                // Y lo introducimos en la DB
                final CoordenadasDBAdapter coorDBAdapter = new CoordenadasDBAdapter(db);
                ResQuery resQuery2 = coorDBAdapter.update((int) idCoor, coordenadas);

                // ¿Se ha realizado con éxito la operación?
                if (resQuery2.isError()) {
                    rowsUpdated = 0;
                } else {
                    rowsUpdated = 1;
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver()
                .notifyChange(uri, null);

        return rowsUpdated;
    }

    // Para notificar los cambios a View, etc.
    private Uri notifyAndGetUriWithId(Uri uri, long id) {
        //if (id > 0) {
            Uri notaUri = ContentUris.withAppendedId(uri, id);
            //if (!isInBatchMode()) {
                // notify all listeners of changes:
                getContext().getContentResolver()
                        .notifyChange(notaUri, null);
            //}
            return notaUri;
        //}

        // s.th. went wrong:
        //throw new SQLException("Problem while inserting into uri: " + uri);
    }
}