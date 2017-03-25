package com.imgprocesado;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
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
import com.facebook.share.Sharer;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.Media;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.MediaService;
import com.twitter.sdk.android.core.services.StatusesService;

import io.fabric.sdk.android.Fabric;
import okhttp3.MediaType;
import retrofit2.Call;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "n184furQc49RsHMwgCR0UakA6";
    private static final String TWITTER_SECRET = "J1s9FAAV04xTU3m5mAeu28KotMBhgRRvjjbKqiTLutwOhBeRrG";


    LoginButton loginButtonOficial;
    private TwitterLoginButton botonLoginTwitter;
    private Button botonLogoutTwitter;
    private TextView tv_UserName;
    private TextView tv_UserNameTW;
    private TextView tv_post;
    private EditText et_textoCompartir;
    private Button btn_CompartirFB;
    private Button btn_CompartirTW;
    private Button btn_CompartirFoto;
    private Button btn_CompartirFotoShareDialog;
    private Button btn_CompartirFotoTW;

    private CallbackManager elCallbackManagerDeFacebook;
    private AccessTokenTracker accessTokenTracker;
    private ShareDialog elShareDialog;
    private boolean loginTW = false;
    private final Activity THIS = this;
    private static final int RESULT_SHARE_FOTO = 101;
    private static final int RESULT_SHARE_FOTO_SHARE_DIALOG = 102;
    private static final int RESULT_SHARE_FOTO_TW = 103;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_main);
        //Botón FB
        loginButtonOficial = (LoginButton) findViewById(R.id.login_button);
        loginButtonOficial.setPublishPermissions("publish_actions");
        //Loguin TW
        botonLoginTwitter  = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        botonLoginTwitter.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Toast.makeText(THIS, "Autenticado en twitter: " + result.data.getUserName(), Toast.LENGTH_LONG).show();
                loginTW=true;
                //Poner el nombre del usuario
                tv_UserNameTW.setText(result.data.getUserName());
                actualizarVentanita();
            }

            @Override
            public void failure(TwitterException e) {
                Toast.makeText(THIS, "Fallo en autentificación: " + e.getMessage(), Toast.LENGTH_LONG).show();
                loginTW=true;
                actualizarVentanita();
            }
        });
        botonLogoutTwitter  = (Button) findViewById(R.id.twitter_logout_button);
        botonLogoutTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutTwitter();
                actualizarVentanita();
            }
        });

        tv_UserName = (TextView) findViewById(R.id.tv_userName);
        tv_UserNameTW = (TextView) findViewById(R.id.tv_userNameTW);
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
                    enviarTweet(texto);
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
        this.elShareDialog = new ShareDialog(this);
        this.elShareDialog.registerCallback(this.elCallbackManagerDeFacebook,
                new FacebookCallback<Sharer.Result>() {
                    @Override
                    public void onSuccess(Sharer.Result result) {
                        Toast.makeText(THIS, "Sharer onSuccess()", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(THIS, "Sharer onError(): " + error.toString(), Toast.LENGTH_LONG).show();
                    }
                });
        actualizarVentanita();
    }

    private void logoutTwitter() {
        loginTW = false;
        TwitterSession twitterSession = TwitterCore.getInstance().getSessionManager().getActiveSession();
        if (twitterSession != null) {
            ClearCookies(getApplicationContext());
            Twitter.getSessionManager().clearActiveSession();
            Twitter.logOut();
        }
    }
    public static void ClearCookies(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncMngr=CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager=CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
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
                        enviarFotoAFacebook_async(bmp, texto);
                        break;
                    case RESULT_SHARE_FOTO_SHARE_DIALOG:
                        //Enviar foto por share dialog
                        enviarFotoSharedialog(bmp);
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
        //Indicar resultados a FB y TW
        this.elCallbackManagerDeFacebook.onActivityResult(requestCode, resultCode, data);
        botonLoginTwitter.onActivityResult(requestCode, resultCode, data);
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
        } else {
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
        if(loginTW){
            btn_CompartirTW.setVisibility(View.VISIBLE);
            btn_CompartirFotoTW.setVisibility(View.VISIBLE);
            //Ocultar el botón de login y mostrar logout
            botonLoginTwitter.setVisibility(View.GONE);
            botonLogoutTwitter.setVisibility(View.VISIBLE);
        }else{
            btn_CompartirTW.setVisibility(View.INVISIBLE);
            btn_CompartirFotoTW.setVisibility(View.INVISIBLE);
            tv_UserNameTW.setText("--");
            botonLoginTwitter.setVisibility(View.VISIBLE);
            botonLogoutTwitter.setVisibility(View.GONE);
        }

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

    private boolean puedoUtilizarShareDialogParaPublicarFoto() {
        return ShareDialog.canShow(SharePhotoContent.class);
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

    private void enviarFotoSharedialog(Bitmap image) {
        if (!puedoUtilizarShareDialogParaPublicarFoto()) {
            return;
        }
        SharePhoto photo = new SharePhoto.Builder().setBitmap(image).build();
        SharePhotoContent content = new SharePhotoContent.Builder().addPhoto(photo).build();
        this.elShareDialog.show(content);
    }

    //Enviar a TWITTER

    private void enviarTweet(String texto) {
        StatusesService statusesService = Twitter.getApiClient( obtenerSesionDeTwitter() ).getStatusesService();
        Call<Tweet> call = statusesService.update(texto, null, null, null, null, null, null, null, null);
        call.enqueue(new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> result) {
                Toast.makeText(THIS, "Tweet publicado: "+ result.response.message(), Toast.LENGTH_LONG).show();
            }
            @Override
            public void failure(TwitterException e) {
                Toast.makeText(THIS, "No se pudo publicar el tweet: "+ e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private TwitterSession obtenerSesionDeTwitter() {
        return Twitter.getSessionManager().getActiveSession();
    }

    private void enviarFoto(String rutaFoto, final String texto){
        File photo = null;
        try {
            photo = new File (rutaFoto);
        } catch (Exception e) {
            Log.d("miApp", "enviarImagen : excepcion: " + e.getMessage());
            return;
        } // catch
// 3. obtenemos referencia al media service
        MediaService ms = Twitter.getApiClient( obtenerSesionDeTwitter() ).getMediaService();
// 3.1 ponemos la foto en el request body de la petición
        okhttp3.RequestBody requestBody = okhttp3.RequestBody.create(MediaType.parse ("image/png"), photo);
// 4. con el media service: enviamos la foto a Twitter
        Call<Media> call1 = ms.upload(
                requestBody, // foto que enviamos
                null,
                null);
        call1.enqueue (new Callback<Media>() {
            @Override
            public void success(Result<Media> mediaResult) {
// he tenido éxito:
                Toast.makeText(THIS, "imagen publicada: " + mediaResult.response.toString(), Toast.LENGTH_LONG);
// 5. como he tenido éxito, la foto está en twitter, pero no en el timeline (no se ve) he de escribir un tweet referenciando la foto
                // 6. obtengo referencia al status service
                StatusesService statusesService = TwitterCore.getInstance().getApiClient(obtenerSesionDeTwitter())
                        .getStatusesService();
// 7. publico un tweet
                Call<Tweet> call2 = statusesService.update(texto , // mensaje del tweet
                        null,
                        false,
                        null,
                        null,
                        null,
                        true,
                        false,
                        ""+mediaResult.data.mediaId // string con los identicadores (hasta 4, separado por coma) de las imágenes
// que quiero que aparezcan en este tweet. El mediaId referencia a la foto que acabo de subir previamente
                );
                call2.enqueue(
                        new Callback<Tweet>() {
                            @Override
                            public void success(Result<Tweet> result) {
                                Toast.makeText(THIS, "Tweet publicado: "+ result.response.message().toString(), Toast.LENGTH_LONG).show();
                            }
                            @Override
                            public void failure(TwitterException e) {
                                Toast.makeText(THIS, "No se pudo publicar el tweet: "+ e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
            @Override
            public void failure(TwitterException e) {
// failure de call1
                Toast.makeText(THIS, "No se pudo publicar el tweet: "+ e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
