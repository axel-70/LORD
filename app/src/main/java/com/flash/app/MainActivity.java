package com.flash.app;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Typeface;
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
    private static final String SAMPLE = "Flash";

    private EditText inputText;
    private EditText symbolSearch;
    private LinearLayout lettersBox;
    private LinearLayout framesBox;
    private LinearLayout symbolsBox;
    private ScrollView[] pages = new ScrollView[3];
    private TextView[] tabs = new TextView[3];

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

        TextView title = text("Flash", 30, TEXT, true);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        root.addView(title, wrap());

        TextView subtitle = text("حروف ورموز ورسايل مزخرفة، اضغط على أي شيء وهيتنسخ", 13, GRAY, false);
        subtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        root.addView(subtitle, wrap());

        LinearLayout tabRow = new LinearLayout(this);
        tabRow.setOrientation(LinearLayout.HORIZONTAL);
        String[] names = {"الحروف", "الرموز", "الرسايل"};
        for (int i = 0; i < names.length; i++) {
            final int idx = i;
            tabs[i] = text(names[i], 15, ACCENT, true);
            tabs[i].setGravity(Gravity.CENTER);
            tabs[i].setOnClickListener(v -> showPage(idx));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(44), 1f);
            lp.setMargins(dp(4), dp(12), dp(4), dp(8));
            tabRow.addView(tabs[i], lp);
        }
        root.addView(tabRow, matchWidth());

        FrameLayout content = new FrameLayout(this);
        pages[0] = buildLettersPage();
        pages[1] = buildSymbolsPage();
        pages[2] = new MessageComposer(this).build();
        for (ScrollView p : pages) {
            content.addView(p, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        }
        root.addView(content, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        setContentView(root);
        showPage(0);
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
        inputText.addTextChangedListener(new SimpleWatcher(this::refreshLetters));
        box.addView(inputText, matchBottom(12));

        lettersBox = new LinearLayout(this);
        lettersBox.setOrientation(LinearLayout.VERTICAL);
        box.addView(lettersBox, matchWidth());

        box.addView(sectionTitle("الأطر"), matchBottom(6));
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
        symbolSearch.addTextChangedListener(new SimpleWatcher(this::refreshSymbols));
        box.addView(symbolSearch, matchBottom(12));

        symbolsBox = new LinearLayout(this);
        symbolsBox.setOrientation(LinearLayout.VERTICAL);
        box.addView(symbolsBox, matchWidth());
        return sv;
    }

    private void showPage(int index) {
        for (int i = 0; i < pages.length; i++) {
            pages[i].setVisibility(i == index ? View.VISIBLE : View.GONE);
            boolean active = i == index;
            tabs[i].setBackground(rounded(active ? ACCENT : SOFT, 12));
            tabs[i].setTextColor(active ? 0xFFFFFFFF : ACCENT);
        }
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

            symbolsBox.addView(text(cat.name, 15, ACCENT, true), matchBottom(6));

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
            while (row != null && col != 0 && col < 6) {
                row.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1f));
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
            card.addView(text(label, 12, GRAY, false), wrap());
        }
        card.addView(text(value, 18, TEXT, false), matchWidth());
        card.setOnClickListener(x -> copy(value));
        parent.addView(card, matchBottom(8));
    }

    // ---------- helpers ----------

    private void copy(String value) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("flash", value));
        Toast.makeText(this, "تم النسخ ✓", Toast.LENGTH_SHORT).show();
    }

    private TextView text(String s, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(t.getTypeface(), Typeface.BOLD);
        return t;
    }

    private TextView sectionTitle(String s) {
        TextView t = text(s, 15, ACCENT, true);
        t.setPadding(dp(4), dp(8), 0, dp(6));
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
