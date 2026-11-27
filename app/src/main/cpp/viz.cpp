#include <jni.h>
#include <mutex>
#include <string>
#include <android/log.h>
#include <projectM-4/projectM.h>

// ponytail: one global instance + one lock; fine for a single car surface.
static std::mutex g_lock;
static projectm_handle g_pm = nullptr;

extern "C" {

JNIEXPORT jboolean JNICALL Java_dev_poc_aaprojectm_Native_create(JNIEnv*, jclass, jint w, jint h, jint fps) {
    std::lock_guard<std::mutex> l(g_lock);
    g_pm = projectm_create();  // needs a current GLES3 context on this thread
    if (!g_pm) { __android_log_print(ANDROID_LOG_ERROR, "viz", "projectm_create failed"); return JNI_FALSE; }
    projectm_set_window_size(g_pm, w, h);
    projectm_set_mesh_size(g_pm, 48, 36);       // calibration knob: lower if head unit drops frames
    projectm_set_fps(g_pm, fps);
    projectm_set_preset_duration(g_pm, 1e9);    // Java side decides when to switch
    projectm_set_hard_cut_enabled(g_pm, false);
    projectm_set_soft_cut_duration(g_pm, 3);
    return JNI_TRUE;
}

JNIEXPORT void JNICALL Java_dev_poc_aaprojectm_Native_loadPreset(JNIEnv* env, jclass, jbyteArray data, jboolean smooth) {
    jsize n = env->GetArrayLength(data);
    std::string s(n, '\0');
    env->GetByteArrayRegion(data, 0, n, reinterpret_cast<jbyte*>(&s[0]));
    std::lock_guard<std::mutex> l(g_lock);
    if (g_pm) projectm_load_preset_data(g_pm, s.c_str(), smooth);
}

JNIEXPORT void JNICALL Java_dev_poc_aaprojectm_Native_render(JNIEnv*, jclass) {
    std::lock_guard<std::mutex> l(g_lock);
    if (g_pm) projectm_opengl_render_frame(g_pm);
}

// Interleaved stereo floats; frames = samples per channel.
JNIEXPORT void JNICALL Java_dev_poc_aaprojectm_Native_pcm(JNIEnv* env, jclass, jfloatArray buf, jint frames) {
    std::lock_guard<std::mutex> l(g_lock);
    if (!g_pm) return;  // no surface yet: drop audio
    auto* p = static_cast<float*>(env->GetPrimitiveArrayCritical(buf, nullptr));
    projectm_pcm_add_float(g_pm, p, frames, PROJECTM_STEREO);
    env->ReleasePrimitiveArrayCritical(buf, p, JNI_ABORT);
}

// Visualizer API waveform: mono unsigned 8-bit, centered on 128.
JNIEXPORT void JNICALL Java_dev_poc_aaprojectm_Native_pcmU8(JNIEnv* env, jclass, jbyteArray buf, jint count) {
    std::lock_guard<std::mutex> l(g_lock);
    if (!g_pm) return;
    auto* p = static_cast<uint8_t*>(env->GetPrimitiveArrayCritical(buf, nullptr));
    projectm_pcm_add_uint8(g_pm, p, count, PROJECTM_MONO);
    env->ReleasePrimitiveArrayCritical(buf, p, JNI_ABORT);
}

JNIEXPORT void JNICALL Java_dev_poc_aaprojectm_Native_destroy(JNIEnv*, jclass) {
    std::lock_guard<std::mutex> l(g_lock);
    if (g_pm) projectm_destroy(g_pm);
    g_pm = nullptr;
}

}
