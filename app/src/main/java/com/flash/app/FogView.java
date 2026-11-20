package com.flash.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Pure black background with slowly drifting golden fog.
 * Drawn with a few soft radial gradients, so it is cheap and needs no image assets.
 */
final class FogView extends View {

    private static final class Blob {
        final float cx, cy, radius, driftX, driftY, speed, phase;
        final int alpha;

        Blob(float cx, float cy, float radius, float driftX, float driftY,
             float speed, float phase, int alpha) {
            this.cx = cx;
            this.cy = cy;
            this.radius = radius;
            this.driftX = driftX;
            this.driftY = driftY;
            this.speed = speed;
            this.phase = phase;
            this.alpha = alpha;
        }
    }

    private static final Blob[] BLOBS = {
            new Blob(0.22f, 0.06f, 0.62f, 0.10f, 0.04f, 1f, 0.0f, 150),
            new Blob(0.95f, 0.30f, 0.52f, 0.08f, 0.07f, 2f, 1.7f, 95),
            new Blob(0.05f, 0.62f, 0.60f, 0.09f, 0.06f, 1f, 3.1f, 80),
            new Blob(0.80f, 0.98f, 0.58f, 0.10f, 0.05f, 2f, 4.4f, 130),
            new Blob(0.50f, 0.45f, 0.75f, 0.12f, 0.08f, 1f, 5.6f, 35),
    };

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shade = new Paint();
    private final RadialGradient unit = new RadialGradient(0f, 0f, 1f,
            new int[]{0xCCF0C050, 0x66B8892B, 0x00B8892B},
            new float[]{0f, 0.45f, 1f}, Shader.TileMode.CLAMP);
    private ValueAnimator animator;
    private float t;

    FogView(Context ctx) {
        super(ctx);
        paint.setShader(unit);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Darken the lower part a little so the footer and cards stay readable.
        shade.setShader(new LinearGradient(0, h * 0.55f, 0, h,
                0x00000000, 0x99000000, Shader.TileMode.CLAMP));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(70000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(a -> {
            t = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        canvas.drawColor(Theme.BLACK);

        float base = Math.max(w, h);
        double turn = t * 2.0 * Math.PI;
        for (Blob b : BLOBS) {
            double a = turn * b.speed + b.phase;
            float x = w * (b.cx + b.driftX * (float) Math.sin(a));
            float y = h * (b.cy + b.driftY * (float) Math.cos(a));
            float r = base * b.radius * (1f + 0.07f * (float) Math.sin(a * 2));
            paint.setAlpha(b.alpha);
            canvas.save();
            canvas.translate(x, y);
            canvas.scale(r, r);
            canvas.drawCircle(0f, 0f, 1f, paint);
            canvas.restore();
        }
        canvas.drawRect(0, h * 0.55f, w, h, shade);
    }
}
