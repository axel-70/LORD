package com.flash.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Message builder: a title in a decorative header, a body where every line gets a
 * decorative prefix, and an optional footer, separated by decorative dividers.
 */
public class MessageComposer {

    static final String[] HEADERS = {
            "⊹ ֺ ָ𖥻 ִ ۫ {} ֺ ָ𖥻 ִ ۫⊹",
            "⊹ ֺ ָ𖥻 ִ ۫ {} ┆ 🕊 ┆ ֺ ָ𖥻 ִ ۫⊹",
            "⊹ ═══════ {} ═══════ ⊹",
            "━━ {} ━━",
            "•°• {} •°•",
            "꒰ {} ꒱",
    };

    static final String[] DIVIDERS = {
            "• ━━ ─ ─ ◜⋄◞ ─ ─ ━━ •",
            "━━━━━━━━━━━━━━━━━",
            "┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈",
            "•°•°•°•°•°•°•°•",
            "⊹ ═════════════ ⊹",
            "꒰ ✦ ✦ ✦ ꒱",
    };

    static final String[] PREFIXES = {
            "> ۰𖥻￤",
            "> ✦ ",
            "> ✧ ",
            "◦ ",
            "⊹ ",
            "› ",
    };

    private static final int BG = 0xFFF7F4FF;
    private static final int CARD = 0xFFFFFFFF;
    private static final int ACCENT = 0xFF7B4DFF;
    private static final int SOFT = 0xFFE9E3FF;
    private static final int TEXT = 0xFF1C1B1F;
    private static final int GRAY = 0xFF6E6A7C;

    private final Context ctx;
    private final float density;

    private EditText titleIn;
    private EditText bodyIn;
    private EditText footerIn;
    private TextView preview;
    private boolean styleBody = false;

    private int headerIdx = 0;
    private int dividerIdx = 0;
    private int prefixIdx = 0;
    private int styleIdx = 0;

    private ChipBar headerBar;
    private ChipBar dividerBar;
    private ChipBar prefixBar;
    private ChipBar styleBar;

    public MessageComposer(Context ctx) {
        this.ctx = ctx;
        this.density = ctx.getResources().getDisplayMetrics().density;
    }

    private int dp(int v) {
        return Math.round(v * density);
    }

    public ScrollView build() {
        ScrollView sv = new ScrollView(ctx);
        LinearLayout box = new LinearLayout(ctx);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(0, 0, 0, dp(24));
        sv.addView(box);

        box.addView(label("العنوان (يتزخرف بالنمط اللي تختاره)"), wrapBottom(4));
        titleIn = field("مثلاً: LORD ┆ 𖤐 ┆ AXEL", false);
        box.addView(titleIn, matchBottom(12));

        box.addView(label("المحتوى (كل سطر بياخد البادئة اللي تختارها)"), wrapBottom(4));
        bodyIn = field("اكتب كل سطر في سطر جديد", true);
        box.addView(bodyIn, matchBottom(8));

        LinearLayout styleRow = new LinearLayout(ctx);
        styleRow.setOrientation(LinearLayout.HORIZONTAL);
        styleRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView sl = text("زخرفة المحتوى أيضاً", 14, TEXT, false);
        styleRow.addView(sl, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        Switch sw = new Switch(ctx);
        sw.setOnCheckedChangeListener((CompoundButton b, boolean on) -> {
            styleBody = on;
            update();
        });
        styleRow.addView(sw, wrap());
        box.addView(styleRow, matchBottom(12));

        box.addView(label("الفوتر (الرابط أو أي سطر أخير)"), wrapBottom(4));
        footerIn = field("https://wa.me/...", true);
        box.addView(footerIn, matchBottom(14));

        box.addView(label("شكل العنوان"), wrapBottom(4));
        headerBar = new ChipBar(HEADERS, i -> { headerIdx = i; update(); }, true);
        box.addView(headerBar.view, matchBottom(10));

        box.addView(label("شكل الفاصل"), wrapBottom(4));
        dividerBar = new ChipBar(DIVIDERS, i -> { dividerIdx = i; update(); }, false);
        box.addView(dividerBar.view, matchBottom(10));

        box.addView(label("بادئة الأسطر"), wrapBottom(4));
        prefixBar = new ChipBar(PREFIXES, i -> { prefixIdx = i; update(); }, false);
        box.addView(prefixBar.view, matchBottom(10));

        String[] styleLabels = new String[FancyStyles.STYLES.size()];
        for (int i = 0; i < styleLabels.length; i++) styleLabels[i] = FancyStyles.STYLES.get(i).label;
        box.addView(label("نمط الحروف"), wrapBottom(4));
        styleBar = new ChipBar(styleLabels, i -> { styleIdx = i; update(); }, false);
        box.addView(styleBar.view, matchBottom(14));

        box.addView(label("المعاينة"), wrapBottom(4));
        LinearLayout card = new LinearLayout(ctx);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackground(rounded(CARD, 14));
        preview = text("", 16, TEXT, false);
        card.addView(preview, matchWidth());
        box.addView(card, matchBottom(10));

        TextView copyBtn = text("نسخ الرسالة", 16, 0xFFFFFFFF, true);
        copyBtn.setGravity(Gravity.CENTER);
        copyBtn.setPadding(0, dp(12), 0, dp(12));
        copyBtn.setBackground(rounded(ACCENT, 12));
        copyBtn.setOnClickListener(v -> {
            String msg = compose();
            if (msg.isEmpty()) {
                Toast.makeText(ctx, "اكتب حاجة الأول", Toast.LENGTH_SHORT).show();
                return;
            }
            ClipboardManager cm = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("flash", msg));
            Toast.makeText(ctx, "تم نسخ الرسالة ✓", Toast.LENGTH_SHORT).show();
        });
        box.addView(copyBtn, matchBottom(8));

        titleIn.addTextChangedListener(new SimpleWatcher(this::update));
        bodyIn.addTextChangedListener(new SimpleWatcher(this::update));
        footerIn.addTextChangedListener(new SimpleWatcher(this::update));

        update();
        return sv;
    }

    // ---------- composing ----------

    private String applyStyle(String text) {
        return FancyStyles.STYLES.get(styleIdx).apply(text);
    }

    String compose() {
        List<List<String>> blocks = new ArrayList<>();

        String title = titleIn.getText().toString().trim();
        if (!title.isEmpty()) {
            List<String> header = new ArrayList<>();
            header.add(HEADERS[headerIdx].replace("{}", applyStyle(title)));
            blocks.add(header);
        }

        List<String> body = new ArrayList<>();
        for (String raw : bodyIn.getText().toString().split("\n", -1)) {
            String line = raw.trim();
            if (line.isEmpty()) {
                body.add("");
            } else {
                String content = styleBody ? applyStyle(line) : line;
                body.add(PREFIXES[prefixIdx] + content);
            }
        }
        while (!body.isEmpty() && body.get(body.size() - 1).isEmpty()) {
            body.remove(body.size() - 1);
        }
        if (!body.isEmpty()) blocks.add(body);

        List<String> footer = new ArrayList<>();
        for (String raw : footerIn.getText().toString().split("\n")) {
            String line = raw.trim();
            if (!line.isEmpty()) footer.add(line);
        }
        if (!footer.isEmpty()) blocks.add(footer);

        List<String> out = new ArrayList<>();
        for (int i = 0; i < blocks.size(); i++) {
            if (i > 0) out.add(DIVIDERS[dividerIdx]);
            out.addAll(blocks.get(i));
        }
        return String.join("\n", out);
    }

    private void update() {
        String msg = compose();
        if (msg.isEmpty()) {
            preview.setText("اكتب العنوان أو المحتوى وهتشوف الرسالة هنا");
            preview.setTextColor(GRAY);
        } else {
            preview.setText(msg);
            preview.setTextColor(TEXT);
        }
        if (headerBar != null) headerBar.render();
        if (dividerBar != null) dividerBar.render();
        if (prefixBar != null) prefixBar.render();
        if (styleBar != null) styleBar.render();
    }

    // ---------- chips ----------

    private final class ChipBar {
        final HorizontalScrollView view;
        final LinearLayout row;
        final String[] labels;
        final java.util.function.IntConsumer pick;
        final boolean tall;
        int selected = 0;

        ChipBar(String[] labels, java.util.function.IntConsumer pick, boolean tall) {
            this.labels = labels;
            this.pick = pick;
            this.tall = tall;
            this.view = new HorizontalScrollView(ctx);
            this.view.setHorizontalScrollBarEnabled(false);
            this.row = new LinearLayout(ctx);
            this.row.setOrientation(LinearLayout.HORIZONTAL);
            this.view.addView(row);
        }

        void render() {
            selected = currentIndex();
            row.removeAllViews();
            for (int i = 0; i < labels.length; i++) {
                final int idx = i;
                String shown = labels[i].replace("{}", "…");
                TextView chip = text(shown, 14, selected == i ? 0xFFFFFFFF : ACCENT, selected == i);
                chip.setGravity(Gravity.CENTER);
                chip.setPadding(dp(12), dp(8), dp(12), dp(8));
                chip.setBackground(rounded(selected == i ? ACCENT : SOFT, 10));
                chip.setOnClickListener(v -> pick.accept(idx));
                LinearLayout.LayoutParams lp = wrap();
                lp.setMargins(0, 0, dp(8), 0);
                row.addView(chip, lp);
            }
        }

        private int currentIndex() {
            if (this == headerBar) return headerIdx;
            if (this == dividerBar) return dividerIdx;
            if (this == prefixBar) return prefixIdx;
            return styleIdx;
        }
    }

    // ---------- helpers ----------

    private EditText field(String hint, boolean multi) {
        EditText e = new EditText(ctx);
        e.setHint(hint);
        e.setTextColor(TEXT);
        e.setHintTextColor(GRAY);
        e.setTextSize(16);
        e.setPadding(dp(14), dp(10), dp(14), dp(10));
        e.setBackground(rounded(CARD, 14));
        if (multi) {
            e.setMinLines(3);
            e.setGravity(Gravity.TOP);
        } else {
            e.setSingleLine(true);
        }
        return e;
    }

    private TextView label(String s) {
        TextView t = text(s, 13, GRAY, false);
        t.setPadding(dp(4), 0, 0, 0);
        return t;
    }

    private TextView text(String s, int sp, int color, boolean bold) {
        TextView t = new TextView(ctx);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(t.getTypeface(), Typeface.BOLD);
        return t;
    }

    private GradientDrawable rounded(int color, int radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radiusDp));
        return g;
    }

    private LinearLayout.LayoutParams matchWidth() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams matchBottom(int bottomDp) {
        LinearLayout.LayoutParams lp = matchWidth();
        lp.setMargins(0, 0, 0, dp(bottomDp));
        return lp;
    }

    private LinearLayout.LayoutParams wrapBottom(int bottomDp) {
        LinearLayout.LayoutParams lp = wrap();
        lp.setMargins(0, 0, 0, dp(bottomDp));
        return lp;
    }

    private LinearLayout.LayoutParams wrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private static final class SimpleWatcher implements TextWatcher {
        private final Runnable action;

        SimpleWatcher(Runnable action) {
            this.action = action;
        }

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { action.run(); }
        @Override public void afterTextChanged(Editable s) {}
    }
}
