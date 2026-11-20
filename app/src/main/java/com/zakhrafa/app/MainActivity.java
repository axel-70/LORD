package com.zakhrafa.app;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final int BG = 0xFFF7F4FF;
    private static final int CARD = 0xFFFFFFFF;
    private static final int ACCENT = 0xFF7B4DFF;
    private static final int SOFT = 0xFFE9E3FF;
    private static final int TEXT = 0xFF1C1B1F;
    private static final int GRAY = 0xFF6E6A7C;
    private static final String SAMPLE = "Zakhrafa";

    private EditText inputText;
    private EditText symbolSearch;
    private LinearLayout lettersBox;
    private LinearLayout framesBox;
    private LinearLayout symbolsBox;
    private ScrollView lettersPage;
    private ScrollView symbolsPage;
    private TextView tabLetters;
    private TextView tabSymbols;

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(dp(16), dp(36), dp(16), dp(8));

        TextView title = text("زخرفة", 30, TEXT, true);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        root.addView(title, wrapWidth());

        TextView subtitle = text("حروف ورموز وأطر زخرفية، اضغط على أي شيء وهو يتنسخ", 13, GRAY, false);
        subtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        root.addView(subtitle, wrapWidth());

        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabLetters = tabButton("الحروف والأطر");
        tabSymbols = tabButton("الرموز");
        tabLetters.setOnClickListener(v -> showLetters(true));
        tabSymbols.setOnClickListener(v -> showLetters(false));
        LinearLayout.LayoutParams tabLp = new LinearLayout.LayoutParams(0, dp(44), 1f);
        tabLp.setMargins(dp(4), dp(12), dp(4), dp(8));
        tabs.addView(tabLetters, tabLp);
        LinearLayout.LayoutParams tabLp2 = new LinearLayout.LayoutParams(0, dp(44), 1f);
        tabLp2.setMargins(dp(4), dp(12), dp(4), dp(8));
        tabs.addView(tabSymbols, tabLp2);
        root.addView(tabs, matchWidth());

        FrameLayout content = new FrameLayout(this);
        lettersPage = buildLettersPage();
        symbolsPage = buildSymbolsPage();
        content.addView(lettersPage, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        content.addView(symbolsPage, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        root.addView(content, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        setContentView(root);
        showLetters(true);
        refreshLetters();
        refreshSymbols();
    }

    // ---------- pages ----------

    private ScrollView buildLettersPage() {
        ScrollView sv = new ScrollView(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        sv.addView(box);

        inputText = new EditText(this);
        inputText.setHint("اكتب نصك هنا (حروف إنجليزية وأرقام)");
        inputText.setTextColor(TEXT);
        inputText.setHintTextColor(GRAY);
        inputText.setTextSize(17);
        inputText.setMinLines(2);
        inputText.setGravity(Gravity.TOP);
        inputText.setPadding(dp(14), dp(12), dp(14), dp(12));
        inputText.setBackground(rounded(CARD, 14));
        inputText.addTextChangedListener(new SimpleWatcher(() -> refreshLetters()));
        box.addView(inputText, matchWidthWithBottom(12));

        lettersBox = new LinearLayout(this);
        lettersBox.setOrientation(LinearLayout.VERTICAL);
        box.addView(lettersBox, matchWidth());

        box.addView(sectionTitle("الأطر"), matchWidthWithBottom(6));
        framesBox = new LinearLayout(this);
        framesBox.setOrientation(LinearLayout.VERTICAL);
        box.addView(framesBox, matchWidth());
        return sv;
    }

    private ScrollView buildSymbolsPage() {
        ScrollView sv = new ScrollView(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        sv.addView(box);

        symbolSearch = new EditText(this);
        symbolSearch.setHint("ابحث عن تصنيف، مثلاً: قلوب");
        symbolSearch.setTextColor(TEXT);
        symbolSearch.setHintTextColor(GRAY);
        symbolSearch.setTextSize(16);
        symbolSearch.setSingleLine(true);
        symbolSearch.setPadding(dp(14), dp(10), dp(14), dp(10));
        symbolSearch.setBackground(rounded(CARD, 14));
        symbolSearch.addTextChangedListener(new SimpleWatcher(() -> refreshSymbols()));
        box.addView(symbolSearch, matchWidthWithBottom(12));

        symbolsBox = new LinearLayout(this);
        symbolsBox.setOrientation(LinearLayout.VERTICAL);
        box.addView(symbolsBox, matchWidth());
        return sv;
    }

    private void showLetters(boolean letters) {
        lettersPage.setVisibility(letters ? View.VISIBLE : View.GONE);
        symbolsPage.setVisibility(letters ? View.GONE : View.VISIBLE);
        styleTab(tabLetters, letters);
        styleTab(tabSymbols, !letters);
    }

    // ---------- content ----------

    private void refreshLetters() {
        String src = inputText.getText().toString().trim();
        if (src.isEmpty()) src = SAMPLE;

        lettersBox.removeAllViews();
        for (FancyStyles.Style style : FancyStyles.STYLES) {
            addCopyCard(lettersBox, style.label, style.apply(src));
        }

        framesBox.removeAllViews();
        for (String frame : SymbolLibrary.FRAMES) {
            addCopyCard(framesBox, null, frame.replace("{}", src));
        }
    }

    private void refreshSymbols() {
        String q = symbolSearch.getText().toString().trim();
        symbolsBox.removeAllViews();

        for (SymbolLibrary.Category cat : SymbolLibrary.CATEGORIES) {
            if (!q.isEmpty() && !cat.name.contains(q)) continue;

            TextView head = text(cat.name, 15, ACCENT, true);
            symbolsBox.addView(head, matchWidthWithBottom(6));

            LinearLayout row = null;
            int col = 0;
            for (String sym : cat.symbols) {
                if (col == 0) {
                    row = new LinearLayout(this);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    symbolsBox.addView(row, matchWidth());
                }
                row.addView(symbolCell(sym));
                col++;
                if (col == 6) col = 0;
            }
            // pad the last row so cells keep the same width
            while (row != null && col != 0 && col < 6) {
                View filler = new View(this);
                row.addView(filler, new LinearLayout.LayoutParams(0, 1, 1f));
                col++;
            }
            symbolsBox.addView(new View(this), new LinearLayout.LayoutParams(1, dp(14)));
        }
    }

    private TextView symbolCell(String sym) {
        TextView t = text(sym, 22, TEXT, false);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, dp(10), 0, dp(10));
        t.setBackground(rounded(CARD, 12));
        t.setOnClickListener(v -> copy(sym));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(dp(3), dp(3), dp(3), dp(3));
        t.setLayoutParams(lp);
        return t;
    }

    private void addCopyCard(LinearLayout parent, String label, String value) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(10), dp(14), dp(10));
        card.setBackground(rounded(CARD, 14));
        if (label != null) {
            card.addView(text(label, 12, GRAY, false), wrapWidth());
        }
        TextView v = text(value, 18, TEXT, false);
        card.addView(v, matchWidth());
        card.setOnClickListener(x -> copy(value));
        parent.addView(card, matchWidthWithBottom(8));
    }

    // ---------- helpers ----------

    private void copy(String value) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("zakhrafa", value));
        Toast.makeText(this, "تم النسخ ✓", Toast.LENGTH_SHORT).show();
    }

    private TextView text(String s, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(t.getTypeface(), android.graphics.Typeface.BOLD);
        return t;
    }

    private TextView sectionTitle(String s) {
        TextView t = text(s, 15, ACCENT, true);
        t.setPadding(dp(4), dp(8), 0, dp(6));
        return t;
    }

    private TextView tabButton(String s) {
        TextView t = text(s, 15, ACCENT, true);
        t.setGravity(Gravity.CENTER);
        return t;
    }

    private void styleTab(TextView t, boolean active) {
        t.setBackground(rounded(active ? ACCENT : SOFT, 12));
        t.setTextColor(active ? 0xFFFFFFFF : ACCENT);
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

    private LinearLayout.LayoutParams matchWidthWithBottom(int bottomDp) {
        LinearLayout.LayoutParams lp = matchWidth();
        lp.setMargins(0, 0, 0, dp(bottomDp));
        return lp;
    }

    private LinearLayout.LayoutParams wrapWidth() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    /** Minimal TextWatcher that only cares about changes. */
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
