package com.albertosalazarpalomo.geonotas.model;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.albertosalazarpalomo.geonotas.dataprovider.URIUtils;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * El modelo "Coordenadas" ha sido creado como un Parcelable, para poder almacenarlo entre ciclos de
 * destrucción-recreación de las activities que lo usen. Integra más campos de los usados actualmente
 * en la app, por motivos de escalabilidad: lo ideal sería que en un futuro pudieran usarse todos.
 */
public class Coordenadas implements Parcelable {

    // Estas líneas sirven para "exponer" ante la librería GSON, cada campo del modelo (para cuando
    // sean transformados a formato JSON)
    @Expose
    @SerializedName("_id")
    private Integer id;

    @Expose
    private Double latitud; // UCoordenadas

    @Expose
    private Double longitud;

    @Expose
    @SerializedName(URIUtils.UCoordenadas.DIRECCION_CAMPO_1)
    private String direccionCampo1; // Campos relativos a dirección

    @Expose
    @SerializedName(URIUtils.UCoordenadas.DIRECCION_CAMPO_2)
    private String direccionCampo2;

    @Expose
    private String localidad;

    @Expose
    private String provincia;

    @Expose
    @SerializedName(URIUtils.UCoordenadas.DIRECCION_CP)
    private Integer direccionCP;

    // El constructor es privado porque uso el patrón builder
    private Coordenadas(Builder builder) {
        id = builder.id;
        latitud = builder.latitud;
        longitud = builder.longitud;
        direccionCampo1 = builder.direccionCampo1;
        direccionCampo2 = builder.direccionCampo2;
        localidad = builder.localidad;
        provincia = builder.provincia;
        direccionCP = builder.direccionCP;
    }

    // Para transferirlos al ContentProvider, he creado este método especial, que lo simplifica
    public ContentValues toContentValues (){
        ContentValues values = new ContentValues();

        values.put(URIUtils.UCoordenadas.ID, this.getId());
        values.put(URIUtils.UCoordenadas.LATITUD, this.getLatitud());
        values.put(URIUtils.UCoordenadas.LONGITUD, this.getLongitud());
        values.put(URIUtils.UCoordenadas.DIRECCION_CAMPO_1, this.getDireccionCampo1());
        values.put(URIUtils.UCoordenadas.DIRECCION_CAMPO_2, this.getDireccionCampo2());
        values.put(URIUtils.UCoordenadas.DIRECCION_CP, this.getDireccionCP());
        values.put(URIUtils.UCoordenadas.LOCALIDAD, this.getLocalidad());
        values.put(URIUtils.UCoordenadas.PROVINCIA, this.getProvincia());

        return values;
    }

    // He creado las siguientes líneas con un plugin generador de código, para tener una API más
    // "fluent", a la hora de construir el objeto
    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(Coordenadas copy) {
        Builder builder = new Builder();

        try {
            builder.id = copy.id;
        } catch (NullPointerException e) {
            builder.id = null;
        }
        try {
            builder.latitud = copy.latitud;
        } catch (NullPointerException e) {
            builder.latitud = null;
        }
        try {
            builder.longitud = copy.longitud;
        } catch (NullPointerException e) {
            builder.longitud = null;
        }
        try {
            builder.direccionCampo1 = copy.direccionCampo1;
        } catch (NullPointerException e) {
            builder.direccionCampo1 = null;
        }
        try {
            builder.direccionCampo2 = copy.direccionCampo2;
        } catch (NullPointerException e) {
            builder.direccionCampo2 = null;
        }
        try {
            builder.localidad = copy.localidad;
        } catch (NullPointerException e) {
            builder.localidad = null;
        }
        try {
            builder.provincia = copy.provincia;
        } catch (NullPointerException e) {
            builder.provincia = null;
        }
        try {
            builder.direccionCP = copy.direccionCP;
        } catch (NullPointerException e) {
            builder.direccionCP = null;
        }

        return builder;
    }

    // Builder
    public static final class Builder {
        private Integer id;
        private Double latitud;
        private Double longitud;
        private String direccionCampo1;
        private String direccionCampo2;
        private String localidad;
        private String provincia;
        private Integer direccionCP;

        private Builder() {
        }

        public Builder withId(Integer val) {
            id = val;
            return this;
        }

        public Builder withLatitud(Double val) {
            latitud = val;
            return this;
        }

        public Builder withLongitud(Double val) {
            longitud = val;
            return this;
        }

        public Builder withDireccionCampo1(String val) {
            direccionCampo1 = val;
            return this;
        }

        public Builder withDireccionCampo2(String val) {
            direccionCampo2 = val;
            return this;
        }

        public Builder withLocalidad(String val) {
            localidad = val;
            return this;
        }

        public Builder withProvincia(String val) {
            provincia = val;
            return this;
        }

        public Builder withDireccionCP(Integer val) {
            direccionCP = val;
            return this;
        }

        public Coordenadas build() {
            //assert latitud != null;
            //assert longitud != null;

            return new Coordenadas(this);
        }
    }

    // Getters

    public Integer getId() {
        return id;
    }

    public Double getLatitud() {
        return latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public String getDireccionCampo1() {
        return direccionCampo1;
    }

    public String getDireccionCampo2() {
        return direccionCampo2;
    }

    public String getLocalidad() {
        return localidad;
    }

    public String getProvincia() {
        return provincia;
    }

    public Integer getDireccionCP() {
        return direccionCP;
    }

    // Campos creados por otro plugin de Android Studio, para que la clase sea parcelable

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeValue(this.latitud);
        dest.writeValue(this.longitud);
        dest.writeString(this.direccionCampo1);
        dest.writeString(this.direccionCampo2);
        dest.writeString(this.localidad);
        dest.writeString(this.provincia);
        dest.writeValue(this.direccionCP);
    }

    protected Coordenadas(Parcel in) {
        this.id = (Integer) in.readValue(Integer.class.getClassLoader());
        this.latitud = (Double) in.readValue(Double.class.getClassLoader());
        this.longitud = (Double) in.readValue(Double.class.getClassLoader());
        this.direccionCampo1 = in.readString();
        this.direccionCampo2 = in.readString();
        this.localidad = in.readString();
        this.provincia = in.readString();
        this.direccionCP = (Integer) in.readValue(Integer.class.getClassLoader());
    }

    public static final Parcelable.Creator<Coordenadas> CREATOR = new Parcelable.Creator<Coordenadas>() {
        public Coordenadas createFromParcel(Parcel source) {
            return new Coordenadas(source);
        }

        public Coordenadas[] newArray(int size) {
            return new Coordenadas[size];
        }
    };
}