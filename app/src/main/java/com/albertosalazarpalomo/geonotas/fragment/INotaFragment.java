package com.albertosalazarpalomo.geonotas.fragment;

import android.support.design.widget.FloatingActionButton;

import com.albertosalazarpalomo.geonotas.model.Coordenadas;
import com.albertosalazarpalomo.geonotas.model.CoordenadasNota;
import com.albertosalazarpalomo.geonotas.model.Nota;

/**
 * La activity que interactúe con los tres fragment-pestañas (DetallesNota., FotosNota.,
 * GeolocNotas.) debe implementar estos métodos
 */
public interface INotaFragment {
    int getEstado();
    void setEstado(int estadoNuevo);

    Nota getNota();
    void setNota(Nota nota);

    Coordenadas getCoordenadas();
    void setCoordenadas(Coordenadas coordenadas);

    CoordenadasNota getCoordenadasNota();
    void setCoordenadasNota(CoordenadasNota coordenadasNota);

    FloatingActionButton getFloatingButtonTakeFoto();
    FloatingActionButton getFloatingButtonBorrarFoto();

    void onClickBorrar();
}