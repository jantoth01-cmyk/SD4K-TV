package com.niceprice.sd4ktv;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainActivity extends Activity {
    private ExoPlayer player;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private XtreamClient xtream;
    private final List<Channel> channels = new ArrayList<>();
    private EditText server, user, pass;
    private LinearLayout loginPanel;
    private ListView list;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        setContentView(root);

        SurfaceView video = new SurfaceView(this);
        root.addView(video, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        player = new ExoPlayer.Builder(this).build();
        video.getHolder().addCallback(new android.view.SurfaceHolder.Callback() {
            public void surfaceCreated(android.view.SurfaceHolder h) { player.setVideoSurfaceHolder(h); }
            public void surfaceChanged(android.view.SurfaceHolder h,int f,int w,int he) {}
            public void surfaceDestroyed(android.view.SurfaceHolder h) { player.clearVideoSurface(); }
        });

        loginPanel = new LinearLayout(this);
        loginPanel.setOrientation(LinearLayout.VERTICAL);
        loginPanel.setPadding(28,28,28,28);
        loginPanel.setBackgroundColor(0xEE151515);
        TextView title = new TextView(this); title.setText("SD4K TV – Xtream belépés"); title.setTextColor(Color.WHITE); title.setTextSize(24); loginPanel.addView(title);
        server = field("Szerver: http://..."); user = field("Felhasználónév"); pass = field("Jelszó"); pass.setInputType(0x81);
        loginPanel.addView(server); loginPanel.addView(user); loginPanel.addView(pass);
        Button connect = new Button(this); connect.setText("Csatlakozás"); connect.setOnClickListener(v -> connect()); loginPanel.addView(connect);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(560, ViewGroup.LayoutParams.WRAP_CONTENT); lp.gravity = Gravity.CENTER; root.addView(loginPanel, lp);

        list = new ListView(this); list.setBackgroundColor(0xEE151515); list.setVisibility(ListView.GONE);
        FrameLayout.LayoutParams llp = new FrameLayout.LayoutParams(520, ViewGroup.LayoutParams.MATCH_PARENT); llp.gravity = Gravity.START; root.addView(list, llp);
    }

    private EditText field(String hint) {
        EditText e = new EditText(this); e.setHint(hint); e.setHintTextColor(0xFFAAAAAA); e.setTextColor(Color.WHITE); e.setSingleLine(true); return e;
    }

    private void connect() {
        String s=server.getText().toString().trim(), u=user.getText().toString().trim(), p=pass.getText().toString().trim();
        if(s.isEmpty()||u.isEmpty()||p.isEmpty()){Toast.makeText(this,"Töltsd ki mindhárom mezőt.",Toast.LENGTH_SHORT).show();return;}
        io.execute(() -> {
            try {
                XtreamClient x=new XtreamClient(s,u,p); x.validate(); List<Channel> result=x.getLiveStreams();
                runOnUiThread(() -> { xtream=x; channels.clear(); channels.addAll(result); showChannels(); loginPanel.setVisibility(LinearLayout.GONE); });
            } catch(Exception ex) { runOnUiThread(() -> Toast.makeText(this,ex.getMessage(),Toast.LENGTH_LONG).show()); }
        });
    }

    private void showChannels() {
        ArrayAdapter<Channel> a=new ArrayAdapter<Channel>(this,android.R.layout.simple_list_item_1,channels){
            @Override public android.view.View getView(int pos,android.view.View cv,ViewGroup parent){TextView v=(TextView)super.getView(pos,cv,parent);v.setTextColor(Color.WHITE);v.setTextSize(18);return v;}
        };
        list.setAdapter(a); list.setVisibility(ListView.VISIBLE);
        list.setOnItemClickListener((parent,v,pos,id)->{Channel c=channels.get(pos); list.setVisibility(ListView.GONE); player.setMediaItem(MediaItem.fromUri(xtream.streamUrl(c.streamId))); player.prepare(); player.play();});
    }

    @Override protected void onDestroy(){player.release();io.shutdownNow();super.onDestroy();}
}
