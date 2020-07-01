#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_io_moneytise_demo_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "moneytiser-sdk";
    return env->NewStringUTF(hello.c_str());
}
