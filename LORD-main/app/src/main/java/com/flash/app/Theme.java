package com.flash.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/** Shared colors and small view helpers. The app theme is black with a golden fog, Apple style. */
final class Theme {

    private Theme() {}

    // Palette
    static final int BLACK = 0xFF000000;
    static final int GOLD = 0xFFE8C468;
    static final int GOLD_LIGHT = 0xFFF8E7A8;
    static final int GOLD_DEEP = 0xFFB8892B;
    static final int GOLD_SOFT = 0xFFC9B27A;
    static final int TEXT = 0xFFF5F5F7;      // Apple label white
    static final int GRAY = 0xFF8E8E93;      // Apple secondary gray
    static final int ON_GOLD = 0xFF1A1405;
    static final int GLASS_TOP = 0x26FFFFFF;
    static final int GLASS_BOTTOM = 0x0DFFFFFF;
    static final int GLASS_STROKE = 0x38E8C468;
    static final int CHIP = 0x1FE8C468;
    static final int PILL = 0x1AFFFFFF;

    static final String COPYRIGHT = "© 2026 FLASH Team. All Rights Reserved";

    private static float density = 1f;

    static void init(Context ctx) {
        density = ctx.getResources().getDisplayMetrics().density;
    }

    static int dp(float v) {
        return Math.round(v * density);
    }

    // ---------- drawables ----------

    static GradientDrawable rounded(int color, int radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radiusDp));
        return g;
    }

    /** Frosted dark glass with a fine golden hairline. */
    static GradientDrawable glass(int radiusDp) {
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{GLASS_TOP, GLASS_BOTTOM});
        g.setCornerRadius(dp(radiusDp));
        g.setStroke(Math.max(1, dp(0.8f)), GLASS_STROKE);
        return g;
    }

    /** Solid gold gradient, used for the active tab. */
    static GradientDrawable goldFill(int radiusDp) {
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{GOLD_LIGHT, GOLD, GOLD_DEEP});
        g.setCornerRadius(dp(radiusDp));
        return g;
    }

    static GradientDrawable chipBg(int radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(CHIP);
        g.setCornerRadius(dp(radiusDp));
        g.setStroke(Math.max(1, dp(0.8f)), GLASS_STROKE);
        return g;
    }

    // ---------- text ----------

    static TextView text(Context ctx, String s, int sp, int color, boolean bold) {
        TextView t = new TextView(ctx);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        return t;
    }

    /** Paints a text view with a gold gradient. Call after the text size is final. */
    static void goldText(TextView t) {
        float w = t.getPaint().measureText(t.getText().toString());
        t.getPaint().setShader(new LinearGradient(0, 0, Math.max(w, 1f), 0,
                new int[]{GOLD_LIGHT, GOLD, GOLD_DEEP, GOLD_LIGHT}, null, Shader.TileMode.CLAMP));
        t.setTextColor(GOLD);
    }

    static TextView section(Context ctx, String s) {
        TextView t = text(ctx, "✦  " + s, 15, GOLD, true);
        t.setPadding(dp(4), dp(10), 0, dp(6));
        return t;
    }

    static void input(EditText e) {
        e.setTextColor(TEXT);
        e.setHintTextColor(GRAY);
        e.setHighlightColor(0x66E8C468);
        e.setBackground(glass(16));
    }

    // ---------- layout ----------

    static LinearLayout.LayoutParams matchWidth() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    static LinearLayout.LayoutParams matchBottom(int bottomDp) {
        LinearLayout.LayoutParams lp = matchWidth();
        lp.setMargins(0, 0, 0, dp(bottomDp));
        return lp;
    }

    static LinearLayout.LayoutParams wrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    // ---------- behavior ----------

    /** Soft Apple-like press feedback. Does not consume the touch, so clicks still work. */
    static void press(View v) {
        v.setOnTouchListener((x, e) -> {
            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    x.animate().scaleX(0.97f).scaleY(0.97f).alpha(0.8f).setDuration(90).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    x.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(180).start();
                    break;
                default:
                    break;
            }
            return false;
        });
    }

    static void copy(Context ctx, String value) {
        ClipboardManager cm = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("flash", value));
        Toast.makeText(ctx, "تم النسخ ✓", Toast.LENGTH_SHORT).show();
    }

    /** A glass card with one line of text that copies itself on tap. */
    static TextView copyCard(Context ctx, String value) {
        TextView t = text(ctx, value, 18, TEXT, false);
        t.setPadding(dp(16), dp(12), dp(16), dp(12));
        t.setBackground(glass(18));
        t.setOnClickListener(v -> copy(ctx, value));
        press(t);
        return t;
    }

    /** A small chip that copies itself on tap. */
    static TextView copyChip(Context ctx, String value) {
        TextView t = text(ctx, value, 18, GOLD, false);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(14), dp(9), dp(14), dp(9));
        t.setBackground(chipBg(12));
        t.setOnClickListener(v -> copy(ctx, value));
        press(t);
        LinearLayout.LayoutParams lp = wrap();
        lp.setMargins(0, 0, dp(8), 0);
        t.setLayoutParams(lp);
        return t;
    }

    static int withAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }
}
