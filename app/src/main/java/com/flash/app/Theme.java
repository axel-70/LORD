package com.flash.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/** Shared colors and view helpers. Pure black, transparent controls, Apple style. */
final class Theme {

    private Theme() {}

    static final int BLACK = 0xFF000000;
    static final int TEXT = 0xFFFFFFFF;
    static final int GRAY = 0xFF8E8E93;        // Apple secondary label
    static final int FILL = 0x14FFFFFF;        // faint translucent card
    static final int FIELD = 0x1FFFFFFF;       // search / input field
    static final int HAIRLINE = 0x24FFFFFF;
    static final int OUTLINE = 0x4DFFFFFF;
    static final int ACTIVE_PILL = 0x33FFFFFF;

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

    /** Translucent card with a hairline border. */
    static GradientDrawable card(int radiusDp) {
        GradientDrawable g = rounded(FILL, radiusDp);
        g.setStroke(Math.max(1, dp(0.5f)), HAIRLINE);
        return g;
    }

    /** Fully transparent button with a thin outline. */
    static GradientDrawable outline(int radiusDp) {
        GradientDrawable g = rounded(0x00000000, radiusDp);
        g.setStroke(Math.max(1, dp(1f)), OUTLINE);
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

    static TextView section(Context ctx, String s) {
        TextView t = text(ctx, s, 13, GRAY, true);
        t.setPadding(dp(6), dp(12), 0, dp(6));
        return t;
    }

    static void input(EditText e) {
        e.setTextColor(TEXT);
        e.setHintTextColor(GRAY);
        e.setHighlightColor(0x55FFFFFF);
        e.setBackground(rounded(FIELD, 16));
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

    /** Soft press feedback. Does not consume the touch, so clicks still work. */
    static void press(View v) {
        v.setOnTouchListener((x, e) -> {
            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    x.animate().scaleX(0.97f).scaleY(0.97f).alpha(0.7f).setDuration(90).start();
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

    static TextView copyCard(Context ctx, String value) {
        TextView t = text(ctx, value, 18, TEXT, false);
        t.setPadding(dp(16), dp(12), dp(16), dp(12));
        t.setBackground(card(18));
        t.setOnClickListener(v -> copy(ctx, value));
        press(t);
        return t;
    }

    static TextView copyChip(Context ctx, String value) {
        TextView t = text(ctx, value, 18, TEXT, false);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(14), dp(9), dp(14), dp(9));
        t.setBackground(outline(14));
        t.setOnClickListener(v -> copy(ctx, value));
        press(t);
        LinearLayout.LayoutParams lp = wrap();
        lp.setMargins(0, 0, dp(8), 0);
        t.setLayoutParams(lp);
        return t;
    }
}
