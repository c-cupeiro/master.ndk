#include "org_jordi_pruebafacebook2017sdk1_ImgProcesadoNDK.h"
#include <android/log.h>
#include <android/bitmap.h>


#define LOG_TAG "libimgprocesadondk"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

typedef struct {
    uint8_t red;
    uint8_t green;
    uint8_t blue;
    uint8_t alpha;
} rgba;

/*Conversion a grises por pixel*/
JNIEXPORT void JNICALL Java_org_jordi_pruebafacebook2017sdk1_ImgProcesadoNDK_convertirGrises
    (JNIEnv *env, jobject obj, jobject bitmapcolor, jobject bitmapgris) {

    AndroidBitmapInfo infocolor;
    void *pixelscolor;
    AndroidBitmapInfo infogris;
    void *pixelsgris;
    int ret;
    int y;
    int x;
    LOGI("convertirGrises");

    if ((ret = AndroidBitmap_getInfo(env, bitmapcolor, &infocolor)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return;
    }

    if ((ret = AndroidBitmap_getInfo(env, bitmapgris, &infogris)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return;
    }

    LOGI("imagen color :: ancho %d;alto %d;avance %d;formato %d;flags %d", infocolor.width,
        infocolor.height, infocolor.stride,
        infocolor.format, infocolor.flags);
    if (infocolor.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap no es formato RGBA_8888 !");
        return;
    }

    LOGI("imagen gris :: ancho %d;alto %d;avance %d;formato %d;flags %d", infogris.width,
    infogris.height, infogris.stride,
    infogris.format, infogris.flags);
    if (infogris.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap no es formato RGBA_8888 !");
        return;
    }

    if ((ret = AndroidBitmap_lockPixels(env, bitmapcolor, &pixelscolor)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
    }

    if ((ret = AndroidBitmap_lockPixels(env, bitmapgris, &pixelsgris)) < 0) {
        LOGE("AndroidBitmap_lockPixels() fallo ! error=%d", ret);
    }

    // modificacion pixeles en el algoritmo de escala grises
    for (y = 0; y < infocolor.height; y++) {
        rgba *line = (rgba *) pixelscolor;
        rgba *grisline = (rgba *) pixelsgris;
        for (x = 0; x < infocolor.width; x++) {
            float output = (line[x].red + line[x].green + line[x].blue) / 3;
            if (output > 255) output = 255;
            grisline[x].red = grisline[x].green = grisline[x].blue = (uint8_t) output;
            grisline[x].alpha = line[x].alpha;
        }
        pixelscolor = (char *) pixelscolor + infocolor.stride;
        pixelsgris = (char *) pixelsgris + infogris.stride;
    }

    LOGI("unlocking pixels");
    AndroidBitmap_unlockPixels(env, bitmapcolor);
    AndroidBitmap_unlockPixels(env, bitmapgris);

}

/*Conversion a sepia por pixel*/
JNIEXPORT void JNICALL Java_org_jordi_pruebafacebook2017sdk1_ImgProcesadoNDK_convertirSepia
        (JNIEnv *env, jobject obj, jobject bitmapcolor, jobject bitmapsepia) {

    AndroidBitmapInfo infocolor;
    void *pixelscolor;
    AndroidBitmapInfo infosepia;
    void *pixelssepia;
    int ret;
    int y;
    int x;
    LOGI("convertirSepia");

    if ((ret = AndroidBitmap_getInfo(env, bitmapcolor, &infocolor)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return;
    }

    if ((ret = AndroidBitmap_getInfo(env, bitmapsepia, &infosepia)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return;
    }

    LOGI("imagen color :: ancho %d;alto %d;avance %d;formato %d;flags %d", infocolor.width,
         infocolor.height, infocolor.stride,
         infocolor.format, infocolor.flags);
    if (infocolor.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap no es formato RGBA_8888 !");
        return;
    }

    LOGI("imagen sepia :: ancho %d;alto %d;avance %d;formato %d;flags %d", infosepia.width,
         infosepia.height, infosepia.stride,
         infosepia.format, infosepia.flags);
    if (infosepia.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap no es formato RGBA_8888 !");
        return;
    }

    if ((ret = AndroidBitmap_lockPixels(env, bitmapcolor, &pixelscolor)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
    }

    if ((ret = AndroidBitmap_lockPixels(env, bitmapsepia, &pixelssepia)) < 0) {
        LOGE("AndroidBitmap_lockPixels() fallo ! error=%d", ret);
    }

    // modificacion pixeles en el algoritmo de sepia
    for (y = 0; y < infocolor.height; y++) {
        rgba *line = (rgba *) pixelscolor;
        rgba *sepialine = (rgba *) pixelssepia;
        for (x = 0; x < infocolor.width; x++) {
            float outputRed = (line[x].red * .393)+(line[x].green * .769)+(line[x].blue * .189);
            if (outputRed > 255) outputRed = 255;
            float outputGreen = (line[x].red * .349)+(line[x].green * .686)+(line[x].blue * .168);
            if (outputGreen > 255) outputGreen = 255;
            float outputBlue = (line[x].red * .272)+(line[x].green * .534)+(line[x].blue * .131);
            if (outputBlue > 255) outputBlue = 255;
            sepialine[x].red = (uint8_t) outputRed;
            sepialine[x].green = (uint8_t) outputGreen;
            sepialine[x].blue = (uint8_t) outputBlue;
            sepialine[x].alpha = line[x].alpha;
        }
        pixelscolor = (char *) pixelscolor + infocolor.stride;
        pixelssepia = (char *) pixelssepia + infosepia.stride;
    }

    LOGI("unlocking pixels");
    AndroidBitmap_unlockPixels(env, bitmapcolor);
    AndroidBitmap_unlockPixels(env, bitmapsepia);

}

/*Conversion a Marco1 por pixel*/
JNIEXPORT void JNICALL Java_org_jordi_pruebafacebook2017sdk1_ImgProcesadoNDK_ponerMarco1
        (JNIEnv *env, jobject obj, jobject bitmapcolor, jobject bitmapmarco1) {

    AndroidBitmapInfo infocolor;
    void *pixelscolor;
    AndroidBitmapInfo infoMarco1;
    void *pixelsmarco1;
    int ret;
    int y;
    int x;
    LOGI("convertirSepia");

    if ((ret = AndroidBitmap_getInfo(env, bitmapcolor, &infocolor)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return;
    }

    if ((ret = AndroidBitmap_getInfo(env, bitmapmarco1, &infoMarco1)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return;
    }

    LOGI("imagen color :: ancho %d;alto %d;avance %d;formato %d;flags %d", infocolor.width,
         infocolor.height, infocolor.stride,
         infocolor.format, infocolor.flags);
    if (infocolor.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap no es formato RGBA_8888 !");
        return;
    }

    LOGI("imagen sepia :: ancho %d;alto %d;avance %d;formato %d;flags %d", infoMarco1.width,
         infoMarco1.height, infoMarco1.stride,
         infoMarco1.format, infoMarco1.flags);
    if (infoMarco1.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap no es formato RGBA_8888 !");
        return;
    }

    if ((ret = AndroidBitmap_lockPixels(env, bitmapcolor, &pixelscolor)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
    }

    if ((ret = AndroidBitmap_lockPixels(env, bitmapmarco1, &pixelsmarco1)) < 0) {
        LOGE("AndroidBitmap_lockPixels() fallo ! error=%d", ret);
    }

    int borde = 10;
    // modificacion pixeles en el algoritmo de escala grises
    for (y = 0; y < infocolor.height; y++) {
        rgba *line = (rgba *) pixelscolor;
        rgba *marco1line = (rgba *) pixelsmarco1;
        for (x = 0; x < infocolor.width; x++) {
            if(y<=borde || x<=borde || y>=(infocolor.height-borde) || x>=(infocolor.width-borde)){
                //Se pone la marca
                marco1line[x].red = (uint8_t) 0;
                marco1line[x].green = (uint8_t) 0;
                marco1line[x].blue = (uint8_t) 0;
                marco1line[x].alpha = (uint8_t) 255;
            }else{
                marco1line[x].red = line[x].red;
                marco1line[x].green = line[x].green;
                marco1line[x].blue = line[x].blue;
                marco1line[x].alpha = line[x].alpha;
            }
        }
        pixelscolor = (char *) pixelscolor + infocolor.stride;
        pixelsmarco1 = (char *) pixelsmarco1 + infoMarco1.stride;
    }

    LOGI("unlocking pixels");
    AndroidBitmap_unlockPixels(env, bitmapcolor);
    AndroidBitmap_unlockPixels(env, bitmapmarco1);

}

/*Conversion a Marco2 por pixel*/
JNIEXPORT void JNICALL Java_org_jordi_pruebafacebook2017sdk1_ImgProcesadoNDK_ponerMarco2
        (JNIEnv *env, jobject obj, jobject bitmapcolor, jobject bitmapmarco2) {

    AndroidBitmapInfo infocolor;
    void *pixelscolor;
    AndroidBitmapInfo infomarco2;
    void *pixelsmarco2;
    int ret;
    int y;
    int x;
    LOGI("convertirSepia");

    if ((ret = AndroidBitmap_getInfo(env, bitmapcolor, &infocolor)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return;
    }

    if ((ret = AndroidBitmap_getInfo(env, bitmapmarco2, &infomarco2)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return;
    }

    LOGI("imagen color :: ancho %d;alto %d;avance %d;formato %d;flags %d", infocolor.width,
         infocolor.height, infocolor.stride,
         infocolor.format, infocolor.flags);
    if (infocolor.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap no es formato RGBA_8888 !");
        return;
    }

    LOGI("imagen sepia :: ancho %d;alto %d;avance %d;formato %d;flags %d", infomarco2.width,
         infomarco2.height, infomarco2.stride,
         infomarco2.format, infomarco2.flags);
    if (infomarco2.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap no es formato RGBA_8888 !");
        return;
    }

    if ((ret = AndroidBitmap_lockPixels(env, bitmapcolor, &pixelscolor)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
    }

    if ((ret = AndroidBitmap_lockPixels(env, bitmapmarco2, &pixelsmarco2)) < 0) {
        LOGE("AndroidBitmap_lockPixels() fallo ! error=%d", ret);
    }

    int borde = 10;
    jclass clazz = (*env)->GetObjectClass(env, obj);
    LOGI("Class: %d",clazz);
    if (!clazz) {
        LOGE("callback_handler: FALLO object Class");
    }
    jmethodID method = (*env)->GetStaticMethodID(env, clazz, "hayPixel", "(II)Z");
    LOGI("METODO: %d",method);
    if (!method) {
        LOGE("callback_hand ler: FALLO metodo ID");
    }
    // modificacion pixeles en el algoritmo de escala grises
    for (y = 0; y < infocolor.height; y++) {
        rgba *line = (rgba *) pixelscolor;
        rgba *marco2line = (rgba *) pixelsmarco2;
        for (x = 0; x < infocolor.width; x++) {
            if(y<=borde || x<=borde || y>=(infocolor.height-borde) || x>=(infocolor.width-borde)){
                //Se pone la marca
                jboolean result = (*env)->CallStaticBooleanMethod(env, clazz, method,x,y);
                //Seleccionar el color
                int color;
                if(result){
                    color = 0;
                }else{
                    color = 255;
                }

                marco2line[x].red = (uint8_t) color;
                marco2line[x].green = (uint8_t) color;
                marco2line[x].blue = (uint8_t) color;
                marco2line[x].alpha = (uint8_t) 255;
            }else{
                marco2line[x].red = line[x].red;
                marco2line[x].green = line[x].green;
                marco2line[x].blue = line[x].blue;
                marco2line[x].alpha = line[x].alpha;
            }
        }
        pixelscolor = (char *) pixelscolor + infocolor.stride;
        pixelsmarco2 = (char *) pixelsmarco2 + infomarco2.stride;
    }

    LOGI("unlocking pixels");
    AndroidBitmap_unlockPixels(env, bitmapcolor);
    AndroidBitmap_unlockPixels(env, bitmapmarco2);

}