package org.jordi.pruebafacebook2017sdk1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.jordi.pruebafacebook2017sdk1.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class ImgProcesadoNDK extends AppCompatActivity {

    public static final String IMAGE_KEY = "imagen";
    private static Uri uriFichero;

    private static String tag = "ImgProcesadoNDK";
    private Bitmap bitmapOriginal = null;
    private Bitmap bitmapCambio = null;
    private ImageView ivDisplay = null;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;

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
        if(getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Log.i(tag, "Imagen antes de modificar");
        ivDisplay = (ImageView) findViewById(R.id.ivDisplay);
        setDefaultImage();
        if (bitmapOriginal != null) ivDisplay.setImageBitmap(bitmapOriginal);

        Button btn_foto = (Button) findViewById(R.id.btnFoto);
        btn_foto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hacerFoto(v);
            }
        });
        Button btn_gallery = (Button) findViewById(R.id.btnGaleria);
        btn_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seleccionarGaleria(v);
            }
        });
        Button btn_enviar = (Button) findViewById(R.id.btnEnviar);
        btn_enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarFoto();
            }
        });
    }

    private void setDefaultImage() {
        if(bitmapOriginal== null){
            BitmapFactory.Options options = new BitmapFactory.Options();
            // Asegurar que la imagen tiene 24 bits de color
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmapOriginal = BitmapFactory.decodeResource(this.getResources(), R.drawable.sampleimage, options);
        }
    }

    private void enviarFoto(){
        try {
            //Write file
            String filename = "bitmap.png";
            String baseFolder;
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                baseFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            }
// revert to using internal storage (not sure if there's an equivalent to the above)
            else {
                baseFolder = this.getFilesDir().getAbsolutePath();
            }
            File file = new File(baseFolder + File.separator + filename);
            file.getParentFile().mkdirs();
            //FileOutputStream stream = this.openFileOutput(filename, Context.MODE_PRIVATE);
            FileOutputStream stream = new FileOutputStream(file);
            Bitmap image = ((BitmapDrawable)ivDisplay.getDrawable()).getBitmap();
            image.compress(Bitmap.CompressFormat.PNG, 100, stream);

            //Cleanup
            stream.close();
            image.recycle();

            Intent intent_resul = new Intent();
            intent_resul.putExtra(IMAGE_KEY,file.getAbsolutePath());
            setResult(RESULT_OK,intent_resul);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al enviar la foto al MainActivity", Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
        }

    }

    private void hacerFoto(View v) {

        String mediaStorageDir =
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES).getPath();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.ENGLISH).format(new Date());
        uriFichero = Uri.fromFile(new java.io.File(mediaStorageDir +
                java.io.File.separator + "IMG_" + timeStamp + ".jpg"));
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriFichero);
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);

        /*Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }*/

    }

    private void seleccionarGaleria(View v) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(galleryIntent,
                "Seleccionar fotografía"), REQUEST_IMAGE_GALLERY);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
            case android.R.id.home:
                finish();
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
        switch (option) {
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

    public static boolean hayPixel(int x, int y) {
        return x%10 == y%10;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri uri = null;
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            uri = uriFichero;
        }
        if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK) {
            uri = data.getData();
        }
        if(uri!=null){
            try {
                bitmapOriginal = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                onResetImagen();
            } catch (IOException e) {
                bitmapOriginal = null;
                setDefaultImage();
                Toast.makeText(this, R.string.error_gallery, Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, R.string.error_gallery, Toast.LENGTH_SHORT).show();
        }
    }
}