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
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * Writing screen. The user types the message freely; decoration options live in a
 * side panel. A blank line in the text starts a new section (separated by a divider).
 * Every decoration can be cancelled by tapping its selected option again or "بدون".
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

    private static final String NONE = "بدون";
    private static final int PANEL_DP = 290;

    private static final int BG = 0xFFF7F4FF;
    private static final int CARD = 0xFFFFFFFF;
    private static final int ACCENT = 0xFF7B4DFF;
    private static final int SOFT = 0xFFE9E3FF;
    private static final int TEXT = 0xFF1C1B1F;
    private static final int GRAY = 0xFF6E6A7C;

    private final Context ctx;
    private final float density;

    private FrameLayout root;
    private View scrim;
    private LinearLayout panel;
    private boolean panelOpen = false;

    private EditText editor;
    private TextView preview;
    private Switch styleSwitch;

    // selections: 0 = none, otherwise index+1 into the matching array
    private int headerSel = 0;
    private int dividerSel = 0;
    private int prefixSel = 0;
    private int styleSel = 0;
    private boolean styleBody = false;

    private final List<ChipBar> bars = new ArrayList<>();

    public MessageComposer(Context ctx) {
        this.ctx = ctx;
        this.density = ctx.getResources().getDisplayMetrics().density;
    }

    private int dp(int v) {
        return Math.round(v * density);
    }

    public View build() {
        root = new FrameLayout(ctx);

        // ----- main writing column -----
        ScrollView main = new ScrollView(ctx);
        LinearLayout box = new LinearLayout(ctx);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(0, 0, 0, dp(24));
        main.addView(box);

        LinearLayout topRow = new LinearLayout(ctx);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView head = text("اكتب رسالتك", 15, TEXT, true);
        topRow.addView(head, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        TextView openBtn = text("🎨 الزخرفة", 14, ACCENT, true);
        openBtn.setPadding(dp(12), dp(8), dp(12), dp(8));
        openBtn.setBackground(rounded(SOFT, 10));
        openBtn.setOnClickListener(v -> openPanel(true));
        topRow.addView(openBtn, wrap());
        box.addView(topRow, matchBottom(10));

        editor = new EditText(ctx);
        editor.setHint("اكتب هنا... السطر الأول هو العنوان، والسطر الفاضي بيفصل بين الأقسام");
        editor.setTextColor(TEXT);
        editor.setHintTextColor(GRAY);
        editor.setTextSize(16);
        editor.setMinLines(10);
        editor.setGravity(Gravity.TOP);
        editor.setPadding(dp(14), dp(12), dp(14), dp(12));
        editor.setBackground(rounded(CARD, 14));
        editor.addTextChangedListener(new SimpleWatcher(this::update));
        box.addView(editor, matchBottom(14));

        box.addView(label("المعاينة"), wrapBottom(4));
        LinearLayout card = new LinearLayout(ctx);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackground(rounded(CARD, 14));
        preview = text("", 16, TEXT, false);
        card.addView(preview, matchWidth());
        box.addView(card, matchBottom(12));

        TextView copyBtn = text("نسخ الرسالة", 16, 0xFFFFFFFF, true);
        copyBtn.setGravity(Gravity.CENTER);
        copyBtn.setPadding(0, dp(12), 0, dp(12));
        copyBtn.setBackground(rounded(ACCENT, 12));
        copyBtn.setOnClickListener(v -> copyMessage());
        box.addView(copyBtn, matchBottom(8));

        root.addView(main, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        // ----- dim background, closes the panel on tap -----
        scrim = new View(ctx);
        scrim.setBackgroundColor(0x66000000);
        scrim.setVisibility(View.GONE);
        scrim.setOnClickListener(v -> openPanel(false));
        root.addView(scrim, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        // ----- side panel -----
        panel = new LinearLayout(ctx);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setBackgroundColor(CARD);
        panel.setElevation(dp(12));
        panel.setPadding(dp(14), dp(40), dp(14), dp(14));

        LinearLayout panelTop = new LinearLayout(ctx);
        panelTop.setOrientation(LinearLayout.HORIZONTAL);
        panelTop.setGravity(Gravity.CENTER_VERTICAL);
        panelTop.addView(text("الزخرفة", 17, TEXT, true),
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        TextView close = text("إغلاق ✕", 14, ACCENT, true);
        close.setOnClickListener(v -> openPanel(false));
        panelTop.addView(close, wrap());
        panel.addView(panelTop, matchBottom(12));

        ScrollView options = new ScrollView(ctx);
        LinearLayout optBox = new LinearLayout(ctx);
        optBox.setOrientation(LinearLayout.VERTICAL);
        options.addView(optBox);

        LinearLayout switchRow = new LinearLayout(ctx);
        switchRow.setOrientation(LinearLayout.HORIZONTAL);
        switchRow.setGravity(Gravity.CENTER_VERTICAL);
        switchRow.addView(text("زخرفة المحتوى كله", 14, TEXT, false),
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        styleSwitch = new Switch(ctx);
        styleSwitch.setOnCheckedChangeListener((CompoundButton b, boolean on) -> {
            styleBody = on;
            update();
        });
        switchRow.addView(styleSwitch, wrap());
        optBox.addView(switchRow, matchBottom(14));

        optBox.addView(label("شكل العنوان"), wrapBottom(4));
        optBox.addView(bar(HEADERS, () -> headerSel, v -> headerSel = v), matchBottom(12));

        optBox.addView(label("شكل الفاصل"), wrapBottom(4));
        optBox.addView(bar(DIVIDERS, () -> dividerSel, v -> dividerSel = v), matchBottom(12));

        optBox.addView(label("بادئة الأسطر"), wrapBottom(4));
        optBox.addView(bar(PREFIXES, () -> prefixSel, v -> prefixSel = v), matchBottom(12));

        String[] styleLabels = new String[FancyStyles.STYLES.size()];
        for (int i = 0; i < styleLabels.length; i++) styleLabels[i] = FancyStyles.STYLES.get(i).label;
        optBox.addView(label("نمط الحروف"), wrapBottom(4));
        optBox.addView(bar(styleLabels, () -> styleSel, v -> styleSel = v), matchBottom(14));

        TextView clear = text("مسح كل الزخرفة", 14, 0xFFFF3B30, true);
        clear.setGravity(Gravity.CENTER);
        clear.setPadding(0, dp(10), 0, dp(10));
        clear.setBackground(rounded(0xFFFFEBEA, 10));
        clear.setOnClickListener(v -> {
            headerSel = 0;
            dividerSel = 0;
            prefixSel = 0;
            styleSel = 0;
            styleSwitch.setChecked(false);
            update();
        });
        optBox.addView(clear, matchBottom(8));

        panel.addView(options, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        FrameLayout.LayoutParams panelLp = new FrameLayout.LayoutParams(dp(PANEL_DP), FrameLayout.LayoutParams.MATCH_PARENT);
        panelLp.gravity = Gravity.RIGHT;
        root.addView(panel, panelLp);
        panel.setTranslationX(dp(PANEL_DP));

        update();
        return root;
    }

    /** Closes the side panel if it is open. Returns true if something was closed. */
    public boolean closeIfOpen() {
        if (panelOpen) {
            openPanel(false);
            return true;
        }
        return false;
    }

    private void openPanel(boolean open) {
        panelOpen = open;
        scrim.setVisibility(open ? View.VISIBLE : View.GONE);
        panel.animate().translationX(open ? 0 : dp(PANEL_DP)).setDuration(200).start();
    }

    // ---------- composing ----------

    private String styled(String s) {
        if (styleSel == 0) return s;
        return FancyStyles.STYLES.get(styleSel - 1).apply(s);
    }

    /** Builds the final message text from the editor and the chosen decorations. */
    String compose() {
        List<List<String>> blocks = new ArrayList<>();
        List<String> current = new ArrayList<>();

        for (String raw : editor.getText().toString().split("\n", -1)) {
            String line = raw.trim();
            if (line.isEmpty()) {
                if (!current.isEmpty()) {
                    blocks.add(current);
                    current = new ArrayList<>();
                }
            } else {
                current.add(line);
            }
        }
        if (!current.isEmpty()) blocks.add(current);
        if (blocks.isEmpty()) return "";

        List<String> out = new ArrayList<>();
        for (int b = 0; b < blocks.size(); b++) {
            if (b > 0) {
                if (dividerSel > 0) out.add(DIVIDERS[dividerSel - 1]);
                else out.add("");
            }
            List<String> block = blocks.get(b);
            for (int i = 0; i < block.size(); i++) {
                String line = block.get(i);
                boolean isTitle = b == 0 && i == 0;
                if (isTitle) {
                    String t = styled(line);
                    out.add(headerSel > 0 ? HEADERS[headerSel - 1].replace("{}", t) : t);
                } else {
                    String content = styleBody ? styled(line) : line;
                    out.add(prefixSel > 0 ? PREFIXES[prefixSel - 1] + content : content);
                }
            }
        }
        return String.join("\n", out);
    }

    private void update() {
        if (preview == null) return;
        String msg = compose();
        if (msg.isEmpty()) {
            preview.setText("المعاينة هتظهر هنا وانت بتكتب");
            preview.setTextColor(GRAY);
        } else {
            preview.setText(msg);
            preview.setTextColor(TEXT);
        }
        for (ChipBar bar : bars) bar.render();
    }

    private void copyMessage() {
        String msg = compose();
        if (msg.isEmpty()) {
            Toast.makeText(ctx, "اكتب حاجة الأول", Toast.LENGTH_SHORT).show();
            return;
        }
        ClipboardManager cm = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("flash", msg));
        Toast.makeText(ctx, "تم نسخ الرسالة ✓", Toast.LENGTH_SHORT).show();
    }

    // ---------- chips ----------

    /**
     * A row of options. The first chip is "بدون" (no decoration). Tapping the selected
     * option again cancels it and goes back to "بدون".
     */
    private View bar(String[] templates, IntSupplier get, IntConsumer set) {
        String[] labels = new String[templates.length + 1];
        labels[0] = NONE;
        for (int i = 0; i < templates.length; i++) labels[i + 1] = templates[i].replace("{}", "…");

        HorizontalScrollView scroll = new HorizontalScrollView(ctx);
        scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        scroll.addView(row);

        ChipBar bar = new ChipBar(row, labels, get, set);
        bars.add(bar);
        return scroll;
    }

    private final class ChipBar {
        final LinearLayout row;
        final String[] labels;
        final IntSupplier get;
        final IntConsumer set;

        ChipBar(LinearLayout row, String[] labels, IntSupplier get, IntConsumer set) {
            this.row = row;
            this.labels = labels;
            this.get = get;
            this.set = set;
        }

        void render() {
            int selected = get.getAsInt();
            row.removeAllViews();
            for (int i = 0; i < labels.length; i++) {
                final int option = i;
                boolean on = (selected == i);
                TextView chip = text(labels[i], 14, on ? 0xFFFFFFFF : ACCENT, on);
                chip.setGravity(Gravity.CENTER);
                chip.setPadding(dp(12), dp(8), dp(12), dp(8));
                chip.setBackground(rounded(on ? ACCENT : SOFT, 10));
                chip.setOnClickListener(v -> {
                    // tapping the selected option again cancels it
                    int next = (option == get.getAsInt()) ? 0 : option;
                    set.accept(next);
                    update();
                });
                LinearLayout.LayoutParams lp = wrap();
                lp.setMargins(0, 0, dp(8), 0);
                row.addView(chip, lp);
            }
        }
    }

    // ---------- helpers ----------

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
