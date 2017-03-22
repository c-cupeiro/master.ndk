package com.imgprocesado;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    LoginButton loginButtonOficial;
    private TextView tv_UserName;
    private TextView tv_post;
    private EditText et_textoCompartir;
    private Button btn_CompartirFB;
    private Button btn_CompartirTW;
    private Button btn_CompartirFoto;
    private Button btn_CompartirFotoShareDialog;
    private Button btn_CompartirFotoTW;

    private CallbackManager elCallbackManagerDeFacebook;
    private AccessTokenTracker accessTokenTracker;
    private final Activity THIS = this;
    private static final int RESULT_SHARE_FOTO = 101;
    private static final int RESULT_SHARE_FOTO_SHARE_DIALOG = 102;
    private static final int RESULT_SHARE_FOTO_TW = 103;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginButtonOficial = (LoginButton) findViewById(R.id.login_button);
        loginButtonOficial.setPublishPermissions("publish_actions");
        tv_UserName = (TextView) findViewById(R.id.tv_userName);
        tv_post = (TextView) findViewById(R.id.tv_post);
        et_textoCompartir = (EditText) findViewById(R.id.et_textoCompartir);
        btn_CompartirFB = (Button) findViewById(R.id.btn_compartir);
        btn_CompartirFB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Comprobar que no esta vacío
                String texto = et_textoCompartir.getText().toString();
                if (!texto.equals("")) {
                    enviarTextoAFacebook_async(texto);
                } else {
                    Toast.makeText(THIS, "Rellena el texto", Toast.LENGTH_LONG).show();
                }
            }
        });
        btn_CompartirTW = (Button) findViewById(R.id.btn_compartirTW);
        btn_CompartirTW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Comprobar que no esta vacío
                String texto = et_textoCompartir.getText().toString();
                if (!texto.equals("")) {
                    //Enviar a Twitter
                } else {
                    Toast.makeText(THIS, "Rellena el texto", Toast.LENGTH_LONG).show();
                }
            }
        });
        //Fotografías
        btn_CompartirFoto = (Button) findViewById(R.id.btn_compartirFoto);
        btn_CompartirFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compatirFoto(RESULT_SHARE_FOTO);
            }
        });
        btn_CompartirFotoShareDialog = (Button) findViewById(R.id.btn_compartirFotoShareDialog);
        btn_CompartirFotoShareDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compatirFoto(RESULT_SHARE_FOTO_SHARE_DIALOG);
            }
        });
        btn_CompartirFotoTW = (Button) findViewById(R.id.btn_compartirFotoTW);
        btn_CompartirFotoTW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compatirFoto(RESULT_SHARE_FOTO_TW);
            }
        });

        this.elCallbackManagerDeFacebook = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(this.elCallbackManagerDeFacebook,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        Toast.makeText(THIS, "Login onSuccess()", Toast.LENGTH_LONG).show();
                        actualizarVentanita();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(THIS, "Login onCancel()", Toast.LENGTH_LONG).show();
                        actualizarVentanita();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                        Toast.makeText(THIS, "Login onError(): " + exception.getMessage(),
                                Toast.LENGTH_LONG).show();
                        actualizarVentanita();
                    }
                });
        //Para quitar los botones al hacer logout
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                       AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    //write your code here what to do when user logout
                    actualizarVentanita();
                }
            }
        };

        actualizarVentanita();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        Log.d("cuandrav.onActivityResu", "llamado");

        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == RESULT_SHARE_FOTO ||
                requestCode == RESULT_SHARE_FOTO_SHARE_DIALOG || requestCode == RESULT_SHARE_FOTO_TW)
                && resultCode == RESULT_OK && data != null) {
            //Cargar imagen en un bitmap
            Bitmap bmp = null;
            String filename = data.getStringExtra(ImgProcesadoNDK.IMAGE_KEY);
            String texto = et_textoCompartir.getText().toString();
            try {
                FileInputStream is = this.openFileInput(filename);
                bmp = BitmapFactory.decodeStream(is);
                is.close();
                switch (requestCode) {
                    case RESULT_SHARE_FOTO:
                        //Enviar foto por la api de FB
                        this.enviarFotoAFacebook_async(bmp,texto);
                        break;
                    case RESULT_SHARE_FOTO_SHARE_DIALOG:
                        //Enviar foto por share dialog
                        break;
                    case RESULT_SHARE_FOTO_TW:
                        //Enviar foto a twitter
                        break;
                }
                Toast.makeText(THIS, "Imagen enviada correctamente",
                        Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.e("cuandrav.onActivityResu", "Error al cargar la imagen del fichero " + filename);
                Toast.makeText(THIS, "Fallo al Enviar la imagen: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        }

        this.elCallbackManagerDeFacebook.onActivityResult(requestCode, resultCode, data);
    }


    private void actualizarVentanita() {
        Log.d("ActualizarVentanita", "empiezo");
//
// obtengo el access token para ver si hay sesión
//
        //FACEBOOK
        AccessToken accessToken = this.obtenerAccessToken();
        if (accessToken == null) {
            Log.d("ActualizarVentanita", "no hay sesion, deshabilito");
            tv_UserName.setText("--");
            tv_post.setVisibility(View.INVISIBLE);
            et_textoCompartir.setVisibility(View.INVISIBLE);
            btn_CompartirFB.setVisibility(View.INVISIBLE);
            btn_CompartirFoto.setVisibility(View.INVISIBLE);
            btn_CompartirFotoShareDialog.setVisibility(View.INVISIBLE);
        }else{
            Log.d("ActualizarVentanita", "hay sesion");
            tv_post.setVisibility(View.VISIBLE);
            et_textoCompartir.setVisibility(View.VISIBLE);
            btn_CompartirFB.setVisibility(View.VISIBLE);
            btn_CompartirFoto.setVisibility(View.VISIBLE);
            btn_CompartirFotoShareDialog.setVisibility(View.VISIBLE);
            Profile profile = Profile.getCurrentProfile();
            if (profile != null) {
                this.tv_UserName.setText(profile.getName());
            }
        }


        //TWITTER
        btn_CompartirTW.setVisibility(View.INVISIBLE);
        btn_CompartirFotoTW.setVisibility(View.INVISIBLE);
    }

    private AccessToken obtenerAccessToken() {
        return AccessToken.getCurrentAccessToken();
    }


    private boolean sePuedePublicar() {
//
// compruebo la red
//
        if (!this.hayRed()) {
            Toast.makeText(this, "¿no hay red? No puedo publicar", Toast.LENGTH_LONG).show();
            return false;
        }
//
// compruebo permisos
//
        if (!this.tengoPermisoParaPublicar()) {
            Toast.makeText(this, "¿no tengo permisos para publicar? Los pido.", Toast.LENGTH_LONG).show();
            // pedirPermisoParaPublicar();
            LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList("publish_actions"));
            return false;
        }
        return true;
    }

    private boolean tengoPermisoParaPublicar() {
        AccessToken accessToken = this.obtenerAccessToken();
        return accessToken != null && accessToken.getPermissions().contains("publish_actions");
    }

    private boolean hayRed() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void enviarTextoAFacebook_async(final String textoQueEnviar) {
//
// si no se puede publicar no hago nada
//
        if (!sePuedePublicar()) {
            return;
        }
//
// hago la petición a través del API Graph
//
        Bundle params = new Bundle();
        params.putString("message", textoQueEnviar);
        GraphRequest request = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/feed",
                params,
                HttpMethod.POST,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        Toast.makeText(THIS, "Publicación realizada: " + textoQueEnviar, Toast.LENGTH_LONG).show();
                    }
                }
        );
        request.executeAsync();
    } // ()

    private void compatirFoto(int ID_RESULT) {
        startActivityForResult(new Intent(THIS, ImgProcesadoNDK.class), ID_RESULT);
    }

    public void enviarFotoAFacebook_async(Bitmap image, String comentario) {
        Log.d("cuandrav.envFotoFBasync", "llamado");
        if (image == null) {
            Toast.makeText(this, "Enviar foto: la imagen está vacía.", Toast.LENGTH_LONG).show();
            Log.d("cuandrav.envFotoFBasync", "acabo porque la imagen es null");
            return;
        }
//
// si no se puede publicar no hago nada
//
        if (!sePuedePublicar()) {
            return;
        }
//
// convierto el bitmap a array de bytes
//
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
//image.recycle ();
        final byte[] byteArray = stream.toByteArray();
        try {
            stream.close();
        } catch (IOException e) {
        }
//
// hago la petición a traves del Graph API
//
        Bundle params = new Bundle();
        params.putByteArray("source", byteArray); // bytes de la imagen
        params.putString("caption", comentario); // comentario
// si se quisiera publicar una imagen de internet: params.putString("url", "{image-url}");
        GraphRequest request = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/photos",
                params,
                HttpMethod.POST,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        Toast.makeText(THIS, "" + byteArray.length + " Foto enviada: " + response.toString(), Toast.LENGTH_LONG).show();
//textoConElMensaje.setText(response.toString());
                    }
                }
        );
        request.executeAsync();
    } // ()
}
