package com.flash.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/** Shared colors and small view helpers. The app theme is a red gradient. */
final class Theme {

    private Theme() {}

    static final int RED = 0xFFE01B24;
    static final int DEEP_RED = 0xFF6E0008;
    static final int YELLOW = 0xFFFFD60A;
    static final int CARD = 0xFFFFFFFF;
    static final int TEXT = 0xFF1C1B1F;
    static final int GRAY = 0xFF6E6A7C;
    static final int ON_RED = 0xFFFFFFFF;
    static final int ON_RED_SOFT = 0xFFFFD3D6;
    static final int PILL = 0x33FFFFFF;
    static final int CHIP = 0xFFFDE7E9;

    private static float density = 1f;

    static void init(Context ctx) {
        density = ctx.getResources().getDisplayMetrics().density;
    }

    static int dp(float v) {
        return Math.round(v * density);
    }

    static GradientDrawable background() {
        return new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{RED, DEEP_RED});
    }

    static GradientDrawable rounded(int color, int radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radiusDp));
        return g;
    }

    static TextView text(Context ctx, String s, int sp, int color, boolean bold) {
        TextView t = new TextView(ctx);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(t.getTypeface(), Typeface.BOLD);
        return t;
    }

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

    static void copy(Context ctx, String value) {
        ClipboardManager cm = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("flash", value));
        Toast.makeText(ctx, "تم النسخ ✓", Toast.LENGTH_SHORT).show();
    }

    /** A white card with one line of text that copies itself on tap. */
    static TextView copyCard(Context ctx, String value) {
        TextView t = text(ctx, value, 18, TEXT, false);
        t.setPadding(dp(14), dp(10), dp(14), dp(10));
        t.setBackground(rounded(CARD, 14));
        t.setOnClickListener(v -> copy(ctx, value));
        return t;
    }

    /** A small chip that copies itself on tap. */
    static TextView copyChip(Context ctx, String value) {
        TextView t = text(ctx, value, 18, RED, false);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(12), dp(8), dp(12), dp(8));
        t.setBackground(rounded(CHIP, 10));
        t.setOnClickListener(v -> copy(ctx, value));
        LinearLayout.LayoutParams lp = wrap();
        lp.setMargins(0, 0, dp(8), 0);
        t.setLayoutParams(lp);
        return t;
    }
}
