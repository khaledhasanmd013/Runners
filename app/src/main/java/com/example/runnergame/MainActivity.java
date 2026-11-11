
package com.example.runnergame;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.MotionEvent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.RectF;
import android.content.Context;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().getDecorView().setSystemUiVisibility(
            5894 | 1024 | 512);
        setContentView(new GameView(this));
    }

    class GameView extends View {
        Paint p = new Paint(3);
        Random random = new Random();
        ArrayList<RectF> blocks = new ArrayList<>();
        float x = 100, y, vy = 0, score = 0;
        float ground, speed = 8;
        boolean started = false, over = false;
        long last = 0;

        GameView(Context c) {
            super(c);
        }

        void drawText(Canvas c, String s, float x, float y, int size) {
            p.setColor(Color.WHITE);
            p.setTextSize(size);
            p.setFakeBoldText(true);
            c.drawText(s, x, y, p);
        }

        void jump() {
            if (over) {
                blocks.clear();
                score = 0;
                speed = 8;
                over = false;
                started = true;
                y = ground - 55;
            } else if (!started) {
                started = true;
                y = ground - 55;
            } else if (y >= ground - 56) {
                vy = -18;
            }
            invalidate();
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                jump();
                return true;
            }
            return true;
        }

        @Override
        protected void onDraw(Canvas c) {
            super.onDraw(c);
            float w = getWidth(), h = getHeight();
            ground = h * .80f;

            c.drawColor(Color.rgb(100, 190, 245));

            p.setColor(Color.YELLOW);
            c.drawCircle(w - 65, 90, 38, p);

            p.setColor(Color.rgb(50, 160, 80));
            c.drawRect(0, ground, w, h, p);

            p.setColor(Color.WHITE);
            for (int i = 0; i < 6; i++) {
                c.drawCircle(i * 150 + 40, 180, 25, p);
            }

            if (started && !over) {
                if (y == 0) y = ground - 55;
                y += vy;
                vy += 0.9f;

                if (y > ground - 55) {
                    y = ground - 55;
                    vy = 0;
                }

                score += 0.2f;
                speed = Math.min(15, 8 + score / 300);

                long now = System.currentTimeMillis();
                if (now - last > 1200) {
                    float bh = 35 + random.nextInt(45);
                    blocks.add(new RectF(w, ground-bh, w+35, ground));
                    last = now;
                }

                for (int i = blocks.size()-1; i >= 0; i--) {
                    RectF b = blocks.get(i);
                    b.offset(-speed, 0);

                    if (b.right < 0) {
                        blocks.remove(i);
                    } else if (x+48 > b.left && x < b.right
                            && y+55 > b.top) {
                        over = true;
                    }
                }
            }

            float py = started ? y : ground - 55;

            p.setColor(Color.rgb(255, 110, 35));
            c.drawRoundRect(
                new RectF(x, py, x+48, py+55), 12, 12, p);

            p.setColor(Color.WHITE);
            c.drawCircle(x+35, py+13, 10, p);

            p.setColor(Color.BLACK);
            c.drawCircle(x+38, py+12, 3, p);

            p.setColor(Color.rgb(30, 60, 110));
            c.drawRect(x+8, py+48, x+18, py+62, p);
            c.drawRect(x+30, py+48, x+40, py+62, p);

            p.setColor(Color.rgb(30, 120, 55));
            for (RectF b : blocks) {
                c.drawRoundRect(b, 7, 7, p);
            }

            drawText(c, "RUNNER", 25, 50, 25);
            drawText(c, "SCORE: " + (int)score, 25, 90, 20);

            if (!started) {
                drawText(c, "TAP TO START", w/2-100, h/2, 25);
            }

            if (over) {
                drawText(c, "GAME OVER", w/2-100, h/2, 30);
                drawText(c, "TAP TO RESTART", w/2-115, h/2+45, 20);
            } else if (started) {
                postInvalidateDelayed(16);
            }
        }
    }
}
