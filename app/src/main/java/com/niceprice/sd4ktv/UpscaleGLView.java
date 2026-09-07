package com.niceprice.sd4ktv;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.view.Surface;

public final class UpscaleGLView extends GLSurfaceView {
    public interface SurfaceReadyListener { void onSurfaceReady(Surface surface); }
    private final RendererImpl renderer;

    public UpscaleGLView(Context context, SurfaceReadyListener listener) {
        super(context);
        setEGLContextClientVersion(3);
        renderer = new RendererImpl(listener);
        setRenderer(renderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    public void setVideoSize(int width, int height) { renderer.w = Math.max(1,width); renderer.h = Math.max(1,height); }
    public void cycleMode() { renderer.mode = (renderer.mode + 1) % 4; requestRender(); }
    public String getModeName() {
        return renderer.mode == 1 ? "SD javítás" : renderer.mode == 2 ? "Erős SD" : renderer.mode == 3 ? "Sport" : "Normál";
    }

    private final class RendererImpl implements Renderer, SurfaceTexture.OnFrameAvailableListener {
        private final SurfaceReadyListener listener;
        private SurfaceTexture st;
        private int tex;
        volatile int w=720,h=576,mode=1;
        RendererImpl(SurfaceReadyListener l){listener=l;}

        @Override public void onSurfaceCreated(javax.microedition.khronos.opengles.GL10 gl, javax.microedition.khronos.egl.EGLConfig cfg) {
            int[] t=new int[1]; GLES30.glGenTextures(1,t,0); tex=t[0];
            GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,tex);
            GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GLES30.GL_TEXTURE_MIN_FILTER,GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GLES30.GL_TEXTURE_MAG_FILTER,GLES30.GL_LINEAR);
            st=new SurfaceTexture(tex); st.setOnFrameAvailableListener(this);
            post(() -> listener.onSurfaceReady(new Surface(st)));
        }
        @Override public void onSurfaceChanged(javax.microedition.khronos.opengles.GL10 gl,int width,int height){GLES30.glViewport(0,0,width,height);}
        @Override public void onDrawFrame(javax.microedition.khronos.opengles.GL10 gl){ if(st!=null) st.updateTexImage(); }
        @Override public void onFrameAvailable(SurfaceTexture surfaceTexture){ requestRender(); }
    }
}
