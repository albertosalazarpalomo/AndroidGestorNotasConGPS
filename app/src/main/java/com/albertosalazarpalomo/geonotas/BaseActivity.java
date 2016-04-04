package com.albertosalazarpalomo.geonotas;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.albertosalazarpalomo.geonotas.nube.Nube;

/**
 * Utilizaremos esta clase base, para extender todas las demás y dar acceso al Drawer desde todas
 * las Activities que lo necesiten
 */
public class BaseActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "BaseAct";

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view nota clicks here.
        int id = item.getItemId();

        switch (id) {
            // Apartado principal (listado de notas)
            case R.id.nav_notas:
                Intent intent = new Intent();
                intent.setClass(BaseActivity.this, MainActivity.class);
                startActivity(intent);

                this.finish();
                break;

            // Guardar en la nube
            case R.id.nav_salvar_nube:
                    Nube nube = new Nube(this);
                    nube.showLogin(Nube.TipoLogin.SAVE);
                break;

            // Restaurar de la nube
            case R.id.nav_restaurar_nube:
                Nube nube2 = new Nube(this);
                nube2.showLogin(Nube.TipoLogin.LOAD);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}