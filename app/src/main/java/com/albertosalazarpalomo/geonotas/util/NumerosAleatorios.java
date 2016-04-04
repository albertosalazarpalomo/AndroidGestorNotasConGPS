package com.albertosalazarpalomo.geonotas.util;

import java.util.Random;

/**
 * Pequeña clase para generar números aleatorios en un rango
 */
public class NumerosAleatorios {
    public static int getRandomNumberInRange(int min, int max) {
        // Solo permitiremos esta excepción a la hora de generar un rango, si los dos números son 0
        if (max == 0) {
            return 0;
        }

        if (min >= max) {
            throw new IllegalArgumentException("max debe ser mayor que min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }
}