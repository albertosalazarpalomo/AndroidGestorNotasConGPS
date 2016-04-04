package com.albertosalazarpalomo.geonotas;

/**
 * Interfaz que debo implementar en cualquier actividad que muestre algún tipo de listado de
 * las notas (ya sea en un mapa, o en verdadero listado de texto)
 */
public interface INotasListFragment {
    void onPulsarMostrarNota(long id);
}