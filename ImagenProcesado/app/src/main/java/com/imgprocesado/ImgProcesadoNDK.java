package com.imgprocesado;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ImgProcesadoNDK extends AppCompatActivity {

    private String tag = "ImgProcesadoNDK";
    private Bitmap bitmapOriginal = null;
    private Bitmap bitmapCambio = null;
    private ImageView ivDisplay = null;

    static {
        System.loadLibrary("imgprocesado");
    }

    public native void convertirGrises(Bitmap bitmapIn, Bitmap bitmapOut);
    public native void convertirSepia(Bitmap bitmapIn, Bitmap bitmapOut);

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
        Button gris = (Button) findViewById(R.id.btnGris);
        gris.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onConvertir(v,0);
            }
        });
        Button sepia = (Button) findViewById(R.id.btnSepia);
        sepia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onConvertir(v,1);
            }
        });
        Button reset = (Button) findViewById(R.id.btnReset);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onResetImagen(v);
            }
        });
    }

    public void onResetImagen(View v) {
        Log.i(tag, "Resetear Imagen");
        ivDisplay.setImageBitmap(bitmapOriginal);
    }

    public void onConvertir(View v,int option) {
        bitmapCambio = Bitmap.createBitmap(bitmapOriginal.getWidth(), bitmapOriginal.getHeight(), Bitmap.Config.ARGB_8888);
        switch (option){
            case 0://GRIS
                Log.i(tag, "Conversion a escala de grises");
                convertirGrises(bitmapOriginal, bitmapCambio);
                break;
            case 1://SEPIA
                Log.i(tag, "Conversion a escala de sepia");
                convertirSepia(bitmapOriginal, bitmapCambio);
                break;
        }

        ivDisplay.setImageBitmap(bitmapCambio);
    }
}