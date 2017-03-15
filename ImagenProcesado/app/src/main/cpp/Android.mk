LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := imgprocesado
LOCAL_SRC_FILES := com_imgprocesado_ImgProcesadoNDK.c
LOCAL_LDLIBS := -llog
LOCAL_CFLAGS := -Werror
include $(BUILD_SHARED_LIBRARY)