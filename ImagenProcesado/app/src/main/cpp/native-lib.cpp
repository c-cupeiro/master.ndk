#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_imgprocesado_ImgProcesadoNDK_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
