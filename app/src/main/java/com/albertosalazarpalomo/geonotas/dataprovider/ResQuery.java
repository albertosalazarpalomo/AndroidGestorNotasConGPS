package com.albertosalazarpalomo.geonotas.dataprovider;

/**
 * Esta clase nos dará más información en los distintos adaptadores de la DB, a la hora de devolver
 * el resultado de una consulta (¿se ha producido un error?, ¿cuál?, ¿qué id se ha generado?, etc.)
 */
public class ResQuery {
    int errorCod = 0;
    long idRowInsertada = -1;

    public ResQuery() {}

    public boolean isError() {
        if (getErrorCod() == 0) {
            return false;
        } else {
            return true;
        }
    }

    public int getErrorCod() {
        return errorCod;
    }

    public void setErrorCod(int errorCod) {
        this.errorCod = errorCod;
    }

    public long getIdRowInsertada() {
        return idRowInsertada;
    }

    public void setIdRowInsertada(long idRowInsertada) {
        this.idRowInsertada = idRowInsertada;
    }
}