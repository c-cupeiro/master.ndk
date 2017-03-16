package com.imgprocesado;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class ImgProcesadoNDK extends AppCompatActivity {

    private static final String[] PERMS_ALL={
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
    };
    private boolean isInPermission=false;
    private static final int RESULT_PERMS_ALL=1;

    private String tag = "ImgProcesadoNDK";
    private Bitmap bitmapOriginal = null;
    private Bitmap bitmapCambio = null;
    private ImageView ivDisplay = null;

    static {
        System.loadLibrary("imgprocesado");
    }

    public native void convertirGrises(Bitmap bitmapIn, Bitmap bitmapOut);
    public native void convertirSepia(Bitmap bitmapIn, Bitmap bitmapOut);
    public native void ponerMarco1(Bitmap bitmapIn, Bitmap bitmapOut);
    public native void ponerMarco2(Bitmap bitmapIn, Bitmap bitmapOut);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.i(tag, "Imagen antes de modificar");
        ivDisplay = (ImageView) findViewById(R.id.ivDisplay);
        BitmapFactory.Options options = new BitmapFactory.Options();
        // Asegurar que la imagen tiene 24 bits de color
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bitmapOriginal = BitmapFactory.decodeResource(this.getResources(), R.drawable.sampleimage, options);
        if (bitmapOriginal != null) ivDisplay.setImageBitmap(bitmapOriginal);

        ActivityCompat.requestPermissions(this,
                netPermissions(PERMS_ALL), RESULT_PERMS_ALL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.opt_resetImg:
                onResetImagen();
                return true;
            case R.id.opt_gris:
                onConvertir(0);
                return true;
            case R.id.opt_sepia:
                onConvertir(1);
                return true;
            case R.id.opt_marco1:
                onConvertir(2);
                return true;
            case R.id.opt_marco2:
                onConvertir(3);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onResetImagen() {
        Log.i(tag, "Resetear Imagen");
        ivDisplay.setImageBitmap(bitmapOriginal);
    }

    public void onConvertir(int option) {
        bitmapCambio = Bitmap.createBitmap(bitmapOriginal.getWidth(), bitmapOriginal.getHeight(), Bitmap.Config.ARGB_8888);
        switch (option){
            case 0://GRIS
                Log.i(tag, "Conversion a escala de grises");
                convertirGrises(bitmapOriginal, bitmapCambio);
                break;
            case 1://SEPIA
                Log.i(tag, "Conversion a sepia");
                convertirSepia(bitmapOriginal, bitmapCambio);
                break;
            case 2://MARCO 1
                Log.i(tag, "Conversion a Marco 1");
                ponerMarco1(bitmapOriginal, bitmapCambio);
                break;
            case 3://MARCO 2
                Log.i(tag, "Conversion a Marco 2");
                ponerMarco2(bitmapOriginal, bitmapCambio);
                break;
        }

        ivDisplay.setImageBitmap(bitmapCambio);
    }

    //Permisos
    private String[] netPermissions(String[] wanted) {
        ArrayList<String> result=new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return(result.toArray(new String[result.size()]));
    }

    private boolean hasPermission(String perm) {
        return(ContextCompat.checkSelfPermission(this, perm)==
                PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        boolean noPermission=false;

        isInPermission=false;

        if (requestCode==RESULT_PERMS_ALL) {
            String[] notpermission = netPermissions(PERMS_ALL);
            if (notpermission.length>0) {
                noPermission = true;
            }
        }
        if (noPermission) {
            Toast.makeText(this, R.string.msg_no_perm,
                    Toast.LENGTH_LONG).show();
            this.finish();
        }
    }
}