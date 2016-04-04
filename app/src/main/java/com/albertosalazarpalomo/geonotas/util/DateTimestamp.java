package com.albertosalazarpalomo.geonotas.util;

import android.text.format.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Clase para generar fecha-hora en formato timestamp (números de segundos desde 1970), para guardar
 * la fecha de creación en la DB de sqlite (no admite otro formato)
 */
public class DateTimestamp {
    private static final String FORMATO_FECHA = "dd/MM/yyyy";

    // Generamos la hora actual
    public static long generarTimestamp() {
        return System.currentTimeMillis() / 1000;
    }

    // Transformamos la hora-fecha en una string
    public static String timestampToStr(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time * 1000);
        //cal.setTimeInMillis(time);
        String date = DateFormat.format(FORMATO_FECHA, cal).toString();
        return date;
    }

    // Transformamos una string en una hora-fecha timestamp
    public static Long dateToTimestamp(String fecha) {
        Long resultado;

        SimpleDateFormat format = new SimpleDateFormat(FORMATO_FECHA);

        try {
            Date newDate = format.parse(fecha);
            resultado = newDate.getTime() / 1000;
        } catch (ParseException e) {
            resultado = null;
        }

        return resultado;
    }
}