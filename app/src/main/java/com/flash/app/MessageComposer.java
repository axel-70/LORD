package com.flash.app;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Writing screen. The user types the message in the big box. Below it is a palette:
 * tap any decoration and it is copied, then pasted into the message.
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
            "ᯓ ✧ ━━━━━━━━━━ ✧ ᯓ",
            "༺ ─────────── ༻",
            "˚ ༘ ⋆｡˚ ───── ˚｡⋆ ༘ ˚",
            "◦•●◉✿ ━━━━━━━━ ✿◉●•◦",
    };

    static final String[] PREFIXES = {
            "> ۰𖥻￤",
            "> ✦ ",
            "> ✧ ",
            "◦ ",
            "⊹ ",
            "› ",
            "✿ ",
            "❥ ",
            "☆ ",
            "➳ ",
            "꒰ ",
            "• ",
    };

    private final Context ctx;

    private EditText editor;
    private EditText wordIn;
    private LinearLayout paletteBox;

    public MessageComposer(Context ctx) {
        this.ctx = ctx;
    }

    public View build() {
        ScrollView sv = new ScrollView(ctx);
        LinearLayout box = new LinearLayout(ctx);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(0, 0, 0, Theme.dp(24));
        sv.addView(box);

        box.addView(sectionTitle("اكتب رسالتك"), Theme.matchBottom(4));
        editor = new EditText(ctx);
        editor.setHint("اكتب هنا، وانسخ من الزخرفة تحت وألصقها");
        editor.setTextColor(Theme.TEXT);
        editor.setHintTextColor(Theme.GRAY);
        editor.setTextSize(16);
        editor.setMinLines(10);
        editor.setGravity(Gravity.TOP);
        editor.setPadding(Theme.dp(14), Theme.dp(12), Theme.dp(14), Theme.dp(12));
        editor.setBackground(Theme.rounded(Theme.CARD, 14));
        box.addView(editor, Theme.matchBottom(10));

        LinearLayout actions = new LinearLayout(ctx);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        TextView copyAll = Theme.text(ctx, "نسخ الرسالة كلها", 15, Theme.RED, true);
        copyAll.setGravity(Gravity.CENTER);
        copyAll.setPadding(0, Theme.dp(12), 0, Theme.dp(12));
        copyAll.setBackground(Theme.rounded(Theme.CARD, 12));
        copyAll.setOnClickListener(v -> {
            String msg = editor.getText().toString();
            if (msg.trim().isEmpty()) {
                Toast.makeText(ctx, "اكتب حاجة الأول", Toast.LENGTH_SHORT).show();
                return;
            }
            Theme.copy(ctx, msg);
        });
        LinearLayout.LayoutParams lpCopy = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lpCopy.setMargins(0, 0, Theme.dp(6), 0);
        actions.addView(copyAll, lpCopy);

        TextView clear = Theme.text(ctx, "مسح", 15, Theme.ON_RED, true);
        clear.setGravity(Gravity.CENTER);
        clear.setPadding(Theme.dp(20), Theme.dp(12), Theme.dp(20), Theme.dp(12));
        clear.setBackground(Theme.rounded(Theme.PILL, 12));
        clear.setOnClickListener(v -> editor.setText(""));
        actions.addView(clear, Theme.wrap());
        box.addView(actions, Theme.matchBottom(18));

        box.addView(sectionTitle("الزخرفة (اضغط أي شكل يتنسخ)"), Theme.matchBottom(4));
        wordIn = new EditText(ctx);
        wordIn.setHint("اكتب كلمة عشان تتزخرف بيها (اختياري)");
        wordIn.setTextColor(Theme.TEXT);
        wordIn.setHintTextColor(Theme.GRAY);
        wordIn.setTextSize(16);
        wordIn.setSingleLine(true);
        wordIn.setPadding(Theme.dp(14), Theme.dp(10), Theme.dp(14), Theme.dp(10));
        wordIn.setBackground(Theme.rounded(Theme.CARD, 14));
        wordIn.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { refreshPalette(); }
            @Override public void afterTextChanged(Editable s) {}
        });
        box.addView(wordIn, Theme.matchBottom(12));

        paletteBox = new LinearLayout(ctx);
        paletteBox.setOrientation(LinearLayout.VERTICAL);
        box.addView(paletteBox, Theme.matchWidth());

        refreshPalette();
        return sv;
    }

    private void refreshPalette() {
        String word = wordIn.getText().toString().trim();
        if (word.isEmpty()) word = "Flash";

        paletteBox.removeAllViews();

        paletteBox.addView(sectionTitle("عناوين"), Theme.matchBottom(4));
        for (String h : HEADERS) {
            paletteBox.addView(Theme.copyCard(ctx, h.replace("{}", word)), Theme.matchBottom(8));
        }

        paletteBox.addView(sectionTitle("أحرف زخرفية"), Theme.matchBottom(4));
        for (FancyStyles.Style s : FancyStyles.STYLES) {
            paletteBox.addView(Theme.copyCard(ctx, s.apply(word)), Theme.matchBottom(8));
        }

        paletteBox.addView(sectionTitle("أطر"), Theme.matchBottom(4));
        for (String f : SymbolLibrary.FRAMES) {
            paletteBox.addView(Theme.copyCard(ctx, f.replace("{}", word)), Theme.matchBottom(8));
        }

        paletteBox.addView(sectionTitle("فواصل"), Theme.matchBottom(4));
        paletteBox.addView(chipRow(DIVIDERS), Theme.matchBottom(12));

        paletteBox.addView(sectionTitle("بادئات الأسطر"), Theme.matchBottom(4));
        paletteBox.addView(chipRow(PREFIXES), Theme.matchBottom(12));
    }

    private HorizontalScrollView chipRow(String[] items) {
        HorizontalScrollView scroll = new HorizontalScrollView(ctx);
        scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        for (String item : items) {
            row.addView(Theme.copyChip(ctx, item));
        }
        scroll.addView(row);
        return scroll;
    }

    private TextView sectionTitle(String s) {
        TextView t = Theme.text(ctx, s, 15, Theme.ON_RED, true);
        t.setPadding(Theme.dp(4), Theme.dp(8), 0, Theme.dp(6));
        return t;
    }
}
