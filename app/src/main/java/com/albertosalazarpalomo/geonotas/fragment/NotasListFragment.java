package com.albertosalazarpalomo.geonotas.fragment;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.albertosalazarpalomo.geonotas.INotasListFragment;
import com.albertosalazarpalomo.geonotas.R;
import com.albertosalazarpalomo.geonotas.dataprovider.URIUtils;

/**
 * Este fragment es el que lista realmente todas las notas disponibles, en un Listview
 */
public class NotasListFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "NotasListFrag";

    private NotasListFragmentAdapter listAdapter;
    private INotasListFragment fragmentListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            fragmentListener = (INotasListFragment) context;
        } catch (ClassCastException castException) {
            Log.e(TAG, "La actividad no implementa el método adecuado");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_notas, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupCursorAdapter();
    }

    // ¿Qué ocurre cuando pulsamos en un item-fila del ListView?
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        fragmentListener.onPulsarMostrarNota(id);
    }

    // creates a new loader after the initLoader () call
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = { URIUtils.UNota.ID, URIUtils.UNota.TITULO, URIUtils.UNota.DESCRIPCION };

        CursorLoader cursorLoader = new CursorLoader(getContext(),
                URIUtils.UNota.CONTENT_URI, projection, null, null, null);

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        listAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // data is not available anymore, delete reference
        listAdapter.swapCursor(null);
    }

    // Create simple cursor adapter to connect the cursor dataset we load with a ListView
    private void setupCursorAdapter() {
        // Create the simple cursor adapter to use for our list
        getLoaderManager().initLoader(0, null, this);

        listAdapter = new NotasListFragmentAdapter(this.getContext());

        ListView lvContacts = (ListView) getListView();
        lvContacts.setAdapter(listAdapter);
    }
}