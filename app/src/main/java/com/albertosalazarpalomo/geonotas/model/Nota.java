package com.albertosalazarpalomo.geonotas.model;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.albertosalazarpalomo.geonotas.dataprovider.URIUtils;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * El modelo "Nota" ha sido creado como un Parcelable, para poder almacenarlo entre ciclos de
 * destrucción-recreación de las activities que lo usen. Integra más campos de los usados actualmente
 * en la app, por motivos de escalabilidad: lo ideal sería que en un futuro pudieran usarse todos.
 */
public class Nota implements Parcelable {

    // Estas líneas sirven para "exponer" ante la librería GSON, cada campo del modelo (para cuando
    // sean transformados a formato JSON)
    @Expose
    @SerializedName("_id")
    private Integer id;

    @Expose
    private String titulo;

    @Expose
    private String descripcion;

    @Expose
    @SerializedName(URIUtils.UNota.BLOBIMAGE)
    private byte[] blobImage;

    // El constructor es privado porque uso el patrón builder
    private Nota(Builder builder) {
        id = builder._id;
        titulo = builder.titulo;
        descripcion = builder.descripcion;
        blobImage = builder.blobImage;
    }

    // Para transferirlos al ContentProvider, he creado este método especial, que lo simplifica
    public ContentValues toContentValues (){
        ContentValues values = new ContentValues();

        values.put(URIUtils.UNota.ID, this.getId());
        values.put(URIUtils.UNota.TITULO, this.getTitulo());
        values.put(URIUtils.UNota.DESCRIPCION, this.getDescripcion());
        values.put(URIUtils.UNota.BLOBIMAGE, this.getBlobImage());

        return values;
    }

    // He creado las siguientes líneas con un plugin generador de código, para tener una API más
    // "fluent", a la hora de construir el objeto
    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(Nota copy) {
        Builder builder = new Builder();
        builder._id = copy.id;
        builder.titulo = copy.titulo;
        builder.descripcion = copy.descripcion;
        builder.blobImage = copy.blobImage;
        return builder;
    }

    public static final class Builder {
        private Integer _id;
        private String titulo;
        private String descripcion;
        private byte[] blobImage;

        private Builder() {
        }

        public Builder withId(Integer val) {
            _id = val;
            return this;
        }

        public Builder withTitulo(String val) {
            titulo = val;
            return this;
        }

        public Builder withDescripcion(String val) {
            descripcion = val;
            return this;
        }

        public Builder withBlobImage(byte[] val) {
            blobImage = val;
            return this;
        }

        public Nota build() {
            // Campos obligatorios
            assert titulo != null;
            assert descripcion != null;

            return new Nota(this);
        }
    }

    // Getters

    public Integer getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public byte[] getBlobImage() {
        return blobImage;
    }

    // Campos creados por otro plugin de Android Studio, para que la clase sea parcelable

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.titulo);
        dest.writeString(this.descripcion);
        dest.writeByteArray(this.blobImage);
    }

    protected Nota(Parcel in) {
        this.id = (Integer) in.readValue(Integer.class.getClassLoader());
        this.titulo = in.readString();
        this.descripcion = in.readString();
        this.blobImage = in.createByteArray();
    }

    public static final Parcelable.Creator<Nota> CREATOR = new Parcelable.Creator<Nota>() {
        public Nota createFromParcel(Parcel source) {
            return new Nota(source);
        }

        public Nota[] newArray(int size) {
            return new Nota[size];
        }
    };
}