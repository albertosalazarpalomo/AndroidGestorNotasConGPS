package com.albertosalazarpalomo.geonotas.dataprovider;

import android.net.Uri;

/**
 * Esta clase es lo que se podría llamar una clase "Contract", pues pone de acuerdo los nombres de
 * las tablas de la DB con los nombres de las URL del ContentProvider, etc.
 */
public class URIUtils {
    public static final String AUTHORITY =
            "com.albertosalazarpalomo.geonotas.dataprovider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static class UNota {
        public static final String NAME = "nota";
        public static final String ID = "_id";
        public static final String TITULO = "titulo";
        public static final String DESCRIPCION = "descripcion";
        public static final String BLOBIMAGE = "_data";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(NAME).build();

        public static Uri buildUriWith(String blockId) {
            return CONTENT_URI.buildUpon().appendPath(blockId).build();
        }
    }

    public static class UCoordenadas {
        public static final String NAME = "coordenadas";
        public static final String ID = "_id";
        public static final String LATITUD = "latitud";
        public static final String LONGITUD = "longitud";
        public static final String DIRECCION_CAMPO_1 = "direccion_campo_1";
        public static final String DIRECCION_CAMPO_2 = "direccion_campo_2";
        public static final String DIRECCION_CP = "direccion_cp";
        public static final String LOCALIDAD = "localidad";
        public static final String PROVINCIA = "provincia";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(NAME).build();

        public static Uri buildUriWith(String blockId) {
            return CONTENT_URI.buildUpon().appendPath(blockId).build();
        }

    }

    public static class UCoordenadasNota {
        public static final String NAME = "coordenadas_nota";
        public static final String ID = "_id";
        public static final String ID_COORDENADAS = "id_coordenadas";
        public static final String ID_NOTA = "id_nota";
        public static final String FECHA = "fecha";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(NAME).build();

        public static Uri buildUriWith(String blockId) {
            return CONTENT_URI.buildUpon().appendPath(blockId).build();
        }
    }

    public static class UCoordenadasNotaLast {
        public static final String NAME = "coordenadas_nota_last";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(NAME).build();

        public static Uri buildUriWith(String blockId) {
            return CONTENT_URI.buildUpon().appendPath(blockId).build();
        }
    }

    public static class UCoordenadasNotaWithIdNota {
        public static final String NAME = "coordenadas_nota_with_id_nota";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(NAME).build();

        public static Uri buildUriWith(String blockId) {
            return CONTENT_URI.buildUpon().appendPath(blockId).build();
        }
    }

    public static class WholeDatabase {
        public static final String NAME = "whole_database";
        public static final String ESTADO = "estado";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(NAME).build();

        public static Uri buildUriWith(String blockId) {
            return CONTENT_URI.buildUpon().appendPath(blockId).build();
        }
    }
}