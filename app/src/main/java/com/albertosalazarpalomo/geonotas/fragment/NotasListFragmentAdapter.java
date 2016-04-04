package com.albertosalazarpalomo.geonotas.fragment;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.albertosalazarpalomo.geonotas.R;
import com.albertosalazarpalomo.geonotas.dataprovider.URIUtils;

/**
 * Adaptador que suministrará información al Listview "NotasListFragment"
 */
public class NotasListFragmentAdapter extends CursorAdapter {
    private LayoutInflater mInflater;

    public NotasListFragmentAdapter(Context context) {
        super(context, null, false);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v = mInflater.inflate(R.layout.list_notas_row, parent, false);
        return v;
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView tvTitulo = (TextView) view.findViewById(R.id.titulo);
        TextView tvDescripcion = (TextView) view.findViewById(R.id.descripcion);

        // Extract properties from cursor
        final String colTitulo = URIUtils.UNota.TITULO;
        final String colDescripcion = URIUtils.UNota.DESCRIPCION;

        String titulo = cursor.getString(cursor.getColumnIndexOrThrow(colTitulo));
        String descripcion = cursor.getString(cursor.getColumnIndexOrThrow(colDescripcion));

        // Populate fields with extracted properties
        tvTitulo.setText(titulo);
        tvDescripcion.setText(String.valueOf(descripcion));
    }
}