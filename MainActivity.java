package com.example.runnergame;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.MotionEvent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.content.Context;
import android.os.Handler;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(new RunnerView(this));
    }

    static class Obstacle {
        float x, w, h;
        Obstacle(float x, float w, float h) { this.x=x; this.w=w; this.h=h; }
    }

    class RunnerView extends View {
        Paint p = new Paint(3);
        Handler handler = new Handler();
        Random random = new Random();
        ArrayList<Obstacle> obstacles = new ArrayList<>();
        float ground, playerY, velocity, speed, score, best;
        float playerX = 90, playerSize = 48;
        boolean jumping=false, gameOver=false, started=false;
        long lastSpawn=0;
        int sky = Color.rgb(130, 205, 245);

        Runnable loop = new Runnable() {
            @Override public void run() {
                updateGame();
                invalidate();
                handler.postDelayed(this, 16);
            }
        };

        RunnerView(Context c) {
            super(c);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            handler.post(loop);
        }

        void updateGame() {
            if (!started || gameOver) return;
            float floor = getHeight() * .78f;
            ground = floor;
            if (jumping) {
                playerY += velocity;
                velocity += 1.05f;
                if (playerY >= floor-playerSize) {
                    playerY=floor-playerSize; jumping=false; velocity=0;
                }
            }
            speed = Math.min(15f, 7f + score/500f);
            score += .18f;
            long now=System.currentTimeMillis();
            if (now-lastSpawn > Math.max(650, 1450-(int)(speed*35))) {
                float h=35+random.nextInt(48);
                obstacles.add(new Obstacle(getWidth()+20, 28+random.nextInt(18), h));
                lastSpawn=now;
            }
            for (int i=obstacles.size()-1;i>=0;i--) {
                Obstacle o=obstacles.get(i); o.x-=speed;
                if (o.x+o.w<0) obstacles.remove(i);
                else if (playerX+playerSize-8>o.x && playerX+8<o.x+o.w &&
                         playerY+playerSize-7>floor-o.h) {
                    gameOver=true; best=Math.max(best,score);
                }
            }
        }

        void jumpOrStart() {
            if (gameOver) {
                obstacles.clear(); score=0; speed=7; gameOver=false; started=true;
                jumping=false; playerY=getHeight()*.78f-playerSize; lastSpawn=0;
            } else if (!started) {
                started=true; playerY=getHeight()*.78f-playerSize;
            } else if (!jumping) {
                jumping=true; velocity=-17.5f;
            }
            invalidate();
        }

        @Override public boolean onTouchEvent(MotionEvent e) {
            if (e.getAction()==MotionEvent.ACTION_DOWN) { jumpOrStart(); return true; }
            return true;
        }

        void text(Canvas c, String s, float x, float y, float size, int color) {
            p.setColor(color); p.setTextSize(size); p.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));
            c.drawText(s,x,y,p);
        }

        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            float w=getWidth(), h=getHeight(), floor=h*.78f;
            p.setColor(sky); c.drawColor(sky);
            // sun and clouds
            p.setColor(Color.rgb(255,224,110)); c.drawCircle(w*.82f,h*.15f,38,p);
            p.setColor(Color.WHITE);
            c.drawCircle(w*.18f,h*.20f,22,p); c.drawCircle(w*.21f,h*.18f,28,p); c.drawCircle(w*.25f,h*.20f,20,p);
            c.drawCircle(w*.58f,h*.29f,18,p); c.drawCircle(w*.61f,h*.27f,24,p); c.drawCircle(w*.65f,h*.29f,17,p);
            // distant hills
            p.setColor(Color.rgb(105,190,145));
            c.drawOval(-60,floor-85, w*.48f,floor+35,p);
            c.drawOval(w*.35f,floor-110,w+70,floor+30,p);
            p.setColor(Color.rgb(67,165,95)); c.drawRect(0,floor,w,h,p);
            p.setColor(Color.rgb(45,125,70)); c.drawRect(0,floor,w,floor+8,p);
            // ground marks
            p.setColor(Color.rgb(220,245,190));
            for(int i=0;i<8;i++){ float x=(i*110-(started?(score*speed*.7f)%110:0)); c.drawRect(x,floor+35,x+42,floor+40,p); }

            float py = started ? playerY : floor-playerSize;
            // runner shadow
            p.setColor(0x33000000); c.drawOval(playerX,floor-5,playerX+playerSize+5,floor+7,p);
            // character body
            p.setColor(Color.rgb(255,125,45)); c.drawRoundRect(new RectF(playerX,py+12,playerX+playerSize,py+playerSize),14,14,p);
            p.setColor(Color.rgb(255,220,170)); c.drawCircle(playerX+playerSize-8,py+12,13,p);
            p.setColor(Color.BLACK); c.drawCircle(playerX+playerSize-4,py+10,2.8f,p);
            p.setColor(Color.rgb(35,70,110)); c.drawRect(playerX+8,py+playerSize-2,playerX+18,py+playerSize+8,p);
            c.drawRect(playerX+30,py+playerSize-2,playerX+40,py+playerSize+8,p);

            // obstacles
            for(Obstacle o:obstacles){
                p.setColor(Color.rgb(30,115,55));
                c.drawRoundRect(new RectF(o.x,floor-o.h,o.x+o.w,floor),7,7,p);
                p.setColor(Color.rgb(70,180,85));
                c.drawCircle(o.x+o.w*.5f,floor-o.h, o.w*.52f,p);
            }

            text(c,"RUNNER",28,55,26,Color.WHITE);
            text(c,"SCORE: "+(int)score,28,95,20,Color.WHITE);
            text(c,"BEST: "+(int)best,28,125,16,Color.WHITE);
            if(!started){
                text(c,"TAP TO START",w*.5f-100,h*.48f,25,Color.WHITE);
                text(c,"Tap anywhere to jump",w*.5f-112,h*.48f+36,16,Color.WHITE);
            }
            if(gameOver){
                p.setColor(0x99000000); c.drawRect(0,0,w,h,p);
                text(c,"GAME OVER",w*.5f-100,h*.43f,30,Color.WHITE);
                text(c,"SCORE: "+(int)score,w*.5f-65,h*.49f,21,Color.WHITE);
                text(c,"TAP TO RESTART",w*.5f-112,h*.57f,19,Color.WHITE);
            }
        }
    }
}
