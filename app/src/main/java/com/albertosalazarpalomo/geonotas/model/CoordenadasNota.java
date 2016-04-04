package com.albertosalazarpalomo.geonotas.model;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.albertosalazarpalomo.geonotas.dataprovider.URIUtils;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * El modelo "CoordenadasNota" ha sido creado como un Parcelable, para poder almacenarlo entre ciclos de
 * destrucción-recreación de las activities que lo usen. Integra más campos de los usados actualmente
 * en la app, por motivos de escalabilidad: lo ideal sería que en un futuro pudieran usarse todos.
 */
public class CoordenadasNota implements Parcelable {

    // Estas líneas sirven para "exponer" ante la librería GSON, cada campo del modelo (para cuando
    // sean transformados a formato JSON)
    @Expose
    @SerializedName("_id")
    private Integer id;

    @Expose
    @SerializedName(URIUtils.UCoordenadasNota.ID_COORDENADAS)
    private Integer idCoordenadas;

    @Expose
    @SerializedName(URIUtils.UCoordenadasNota.ID_NOTA)
    private Integer idNota;

    @Expose
    private Long fecha;

    // El constructor es privado porque uso el patrón builder
    private CoordenadasNota(Builder builder) {
        id = builder.idCoordenadasNota;
        idCoordenadas = builder.idCoordenadas;
        idNota = builder.idNota;
        fecha = builder.fecha;
    }

    // Para transferirlos al ContentProvider, he creado este método especial, que lo simplifica
    public ContentValues toContentValues (){
        ContentValues values = new ContentValues();

        values.put(URIUtils.UCoordenadasNota.ID, this.getId());
        values.put(URIUtils.UCoordenadasNota.ID_COORDENADAS, this.getIdCoordenadas());
        values.put(URIUtils.UCoordenadasNota.ID_NOTA, this.getIdNota());
        values.put(URIUtils.UCoordenadasNota.FECHA, this.getFecha());

        return values;
    }

    // He creado las siguientes líneas con un plugin generador de código, para tener una API más
    // "fluent", a la hora de construir el objeto
    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(CoordenadasNota copy) {
        Builder builder = new Builder();

        try {
            builder.idCoordenadasNota = copy.id;
        } catch (NullPointerException e) {
            builder.idCoordenadasNota = null;
        }
        try {
            builder.idCoordenadas = copy.idCoordenadas;
        } catch (NullPointerException e) {
            builder.idCoordenadas = null;
        }
        try {
            builder.idNota = copy.idNota;
        } catch (NullPointerException e) {
            builder.idNota = null;
        }
        try {
            builder.fecha = copy.fecha;
        } catch (NullPointerException e) {
            builder.fecha = null;
        }

        return builder;
    }

    // Builder
    public static final class Builder {
        private Integer idCoordenadasNota;
        private Integer idCoordenadas;
        private Integer idNota;
        private Long fecha;

        private Builder() {
        }

        public Builder withId(Integer val) {
            idCoordenadasNota = val;
            return this;
        }

        public Builder withIdCoordenadas(Integer val) {
            idCoordenadas = val;
            return this;
        }

        public Builder withIdNota(Integer val) {
            idNota = val;
            return this;
        }

        public Builder withFecha(Long val) {
            fecha = val;
            return this;
        }

        public CoordenadasNota build() {
            assert idCoordenadas != null;
            assert idNota != null;
            assert fecha != null;

            return new CoordenadasNota(this);
        }
    }

    // Getters

    public Integer getId() {
        return id;
    }

    public Integer getIdCoordenadas() {
        return idCoordenadas;
    }

    public Integer getIdNota() {
        return idNota;
    }

    public Long getFecha() {
        return fecha;
    }

    // Campos creados por otro plugin de Android Studio, para que la clase sea parcelable

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeValue(this.idCoordenadas);
        dest.writeValue(this.idNota);
        dest.writeValue(this.fecha);
    }

    protected CoordenadasNota(Parcel in) {
        this.id = (Integer) in.readValue(Integer.class.getClassLoader());
        this.idCoordenadas = (Integer) in.readValue(Integer.class.getClassLoader());
        this.idNota = (Integer) in.readValue(Integer.class.getClassLoader());
        this.fecha = (Long) in.readValue(Long.class.getClassLoader());
    }

    public static final Parcelable.Creator<CoordenadasNota> CREATOR = new Parcelable.Creator<CoordenadasNota>() {
        public CoordenadasNota createFromParcel(Parcel source) {
            return new CoordenadasNota(source);
        }

        public CoordenadasNota[] newArray(int size) {
            return new CoordenadasNota[size];
        }
    };
}