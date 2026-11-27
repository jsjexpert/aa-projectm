package dev.poc.aaprojectm;

final class Native {
    static { System.loadLibrary("viz"); }
    static native boolean create(int w, int h, int fps);
    static native void loadPreset(byte[] milk, boolean smooth);
    static native void render();
    static native void pcm(float[] interleavedStereo, int frames);
    static native void pcmU8(byte[] monoU8, int count);
    static native void destroy();
}
