package com.imgprocesado;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    LoginButton loginButtonOficial;
    private TextView tv_UserName;

    private CallbackManager elCallbackManagerDeFacebook;
    private final Activity THIS = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginButtonOficial = (LoginButton) findViewById(R.id.login_button);
        loginButtonOficial.setPublishPermissions("publish_actions");
        tv_UserName = (TextView) findViewById(R.id.tv_userName);

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
        this.actualizarVentanita();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
// se llama cuando otra actividad que hemos arrancado termina y nos devuelve el control
// tal vez, devolviéndonos algun resultado (resultCode, data)
        Log.d("cuandrav.onActivityResu", "llamado");
//
// avisar a super
//
        super.onActivityResult(requestCode, resultCode, data);
//
// avisar a Facebook (a su callback manager) por si le afecta
//
        this.elCallbackManagerDeFacebook.onActivityResult(requestCode, resultCode, data);
    }


    private void actualizarVentanita() {
        Log.d("ActualizarVentanita", "empiezo");
//
// obtengo el access token para ver si hay sesión
//
        AccessToken accessToken = this.obtenerAccessToken();
        if (accessToken == null) {
            Log.d("ActualizarVentanita", "no hay sesion, deshabilito");
//
// sesion con facebook cerrada
//
            /*this.botonHacerLogin.setEnabled(true);
            this.botonLogOut.setEnabled(false);
            this.textoConElMensaje.setEnabled(false);
            this.botonCompartir.setEnabled(false);
            this.botonEnviarFoto.setEnabled(false);
            this.elTextoDeBienvenida.setText("haz login");*/
            return;
        }
        Log.d("ActualizarVentanita", "hay sesion");
//
// sí hay sesión
//
        /*Log.d("cuandrav.actualizarVent", "hay sesion habilito");
        this.botonHacerLogin.setEnabled(false);
        this.botonLogOut.setEnabled(true);
        this.textoConElMensaje.setEnabled(true);
        this.botonCompartir.setEnabled(true);
        this.botonEnviarFoto.setEnabled(true);*/
//
// averiguo los datos básicos del usuario acreditado
//
        Profile profile = Profile.getCurrentProfile();
        if (profile != null) {
            this.tv_UserName.setText(profile.getName());
        }
    } // ()

    private AccessToken obtenerAccessToken() {
        return AccessToken.getCurrentAccessToken();
    }

    private boolean hayRed() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
// http://stackoverflow.com/questions/15091591/post-on-facebook-wall-without-showing-dialog-on-android
// comprobar que estamos conetactos a internet, antes de hacer el login con
// facebook. Si no: da problemas.
    } // ()

}
