package com.flash.app;

import android.app.Activity;
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

public class MainActivity extends Activity {

    private static final String SAMPLE = "Flash";

    private EditText inputText;
    private EditText symbolSearch;
    private LinearLayout lettersBox;
    private LinearLayout framesBox;
    private LinearLayout symbolsBox;
    private final View[] pages = new View[3];
    private final TextView[] tabs = new TextView[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Theme.init(this);
        getWindow().setStatusBarColor(Theme.RED);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackground(Theme.background());
        root.setPadding(Theme.dp(16), Theme.dp(36), Theme.dp(16), Theme.dp(8));

        TextView title = Theme.text(this, "⚡ Flash", 30, Theme.YELLOW, true);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        root.addView(title, Theme.wrap());

        TextView subtitle = Theme.text(this, "حروف ورموز ورسايل مزخرفة، اضغط على أي شيء وهيتنسخ", 13, Theme.ON_RED_SOFT, false);
        subtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        root.addView(subtitle, Theme.wrap());

        LinearLayout tabRow = new LinearLayout(this);
        tabRow.setOrientation(LinearLayout.HORIZONTAL);
        String[] names = {"الحروف", "الرموز", "الرسايل"};
        for (int i = 0; i < names.length; i++) {
            final int idx = i;
            tabs[i] = Theme.text(this, names[i], 15, Theme.ON_RED, true);
            tabs[i].setGravity(Gravity.CENTER);
            tabs[i].setOnClickListener(v -> showPage(idx));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Theme.dp(44), 1f);
            lp.setMargins(Theme.dp(4), Theme.dp(12), Theme.dp(4), Theme.dp(8));
            tabRow.addView(tabs[i], lp);
        }
        root.addView(tabRow, Theme.matchWidth());

        FrameLayout content = new FrameLayout(this);
        pages[0] = buildLettersPage();
        pages[1] = buildSymbolsPage();
        pages[2] = new MessageComposer(this).build();
        for (View p : pages) {
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
        inputText.setTextColor(Theme.TEXT);
        inputText.setHintTextColor(Theme.GRAY);
        inputText.setTextSize(17);
        inputText.setMinLines(2);
        inputText.setGravity(Gravity.TOP);
        inputText.setPadding(Theme.dp(14), Theme.dp(12), Theme.dp(14), Theme.dp(12));
        inputText.setBackground(Theme.rounded(Theme.CARD, 14));
        inputText.addTextChangedListener(new SimpleWatcher(this::refreshLetters));
        box.addView(inputText, Theme.matchBottom(12));

        lettersBox = new LinearLayout(this);
        lettersBox.setOrientation(LinearLayout.VERTICAL);
        box.addView(lettersBox, Theme.matchWidth());

        box.addView(sectionTitle("الأطر"), Theme.matchBottom(6));
        framesBox = new LinearLayout(this);
        framesBox.setOrientation(LinearLayout.VERTICAL);
        box.addView(framesBox, Theme.matchWidth());
        return sv;
    }

    private ScrollView buildSymbolsPage() {
        ScrollView sv = new ScrollView(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        sv.addView(box);

        symbolSearch = new EditText(this);
        symbolSearch.setHint("ابحث عن تصنيف، مثلاً: قلوب");
        symbolSearch.setTextColor(Theme.TEXT);
        symbolSearch.setHintTextColor(Theme.GRAY);
        symbolSearch.setTextSize(16);
        symbolSearch.setSingleLine(true);
        symbolSearch.setPadding(Theme.dp(14), Theme.dp(10), Theme.dp(14), Theme.dp(10));
        symbolSearch.setBackground(Theme.rounded(Theme.CARD, 14));
        symbolSearch.addTextChangedListener(new SimpleWatcher(this::refreshSymbols));
        box.addView(symbolSearch, Theme.matchBottom(12));

        symbolsBox = new LinearLayout(this);
        symbolsBox.setOrientation(LinearLayout.VERTICAL);
        box.addView(symbolsBox, Theme.matchWidth());
        return sv;
    }

    private void showPage(int index) {
        for (int i = 0; i < pages.length; i++) {
            pages[i].setVisibility(i == index ? View.VISIBLE : View.GONE);
            boolean active = i == index;
            tabs[i].setBackground(Theme.rounded(active ? Theme.CARD : Theme.PILL, 12));
            tabs[i].setTextColor(active ? Theme.RED : Theme.ON_RED);
        }
    }

    // ---------- content ----------

    private void refreshLetters() {
        String src = inputText.getText().toString().trim();
        if (src.isEmpty()) src = SAMPLE;

        lettersBox.removeAllViews();
        for (FancyStyles.Style style : FancyStyles.STYLES) {
            lettersBox.addView(labelCard(style.label, style.apply(src)), Theme.matchBottom(8));
        }

        framesBox.removeAllViews();
        for (String frame : SymbolLibrary.FRAMES) {
            framesBox.addView(Theme.copyCard(this, frame.replace("{}", src)), Theme.matchBottom(8));
        }
    }

    private void refreshSymbols() {
        String q = symbolSearch.getText().toString().trim();
        symbolsBox.removeAllViews();

        for (SymbolLibrary.Category cat : SymbolLibrary.CATEGORIES) {
            if (!q.isEmpty() && !cat.name.contains(q)) continue;

            symbolsBox.addView(sectionTitle(cat.name), Theme.matchBottom(6));

            LinearLayout row = null;
            int col = 0;
            for (String sym : cat.symbols) {
                if (col == 0) {
                    row = new LinearLayout(this);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    symbolsBox.addView(row, Theme.matchWidth());
                }
                row.addView(symbolCell(sym));
                col++;
                if (col == 6) col = 0;
            }
            while (row != null && col != 0 && col < 6) {
                row.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1f));
                col++;
            }
            symbolsBox.addView(new View(this), new LinearLayout.LayoutParams(1, Theme.dp(14)));
        }
    }

    private TextView symbolCell(String sym) {
        TextView t = Theme.text(this, sym, 22, Theme.TEXT, false);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, Theme.dp(10), 0, Theme.dp(10));
        t.setBackground(Theme.rounded(Theme.CARD, 12));
        t.setOnClickListener(v -> Theme.copy(this, sym));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(Theme.dp(3), Theme.dp(3), Theme.dp(3), Theme.dp(3));
        t.setLayoutParams(lp);
        return t;
    }

    private LinearLayout labelCard(String label, String value) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(Theme.dp(14), Theme.dp(10), Theme.dp(14), Theme.dp(10));
        card.setBackground(Theme.rounded(Theme.CARD, 14));
        card.addView(Theme.text(this, label, 12, Theme.GRAY, false), Theme.wrap());
        TextView v = Theme.text(this, value, 18, Theme.TEXT, false);
        card.addView(v, Theme.matchWidth());
        card.setOnClickListener(x -> Theme.copy(this, value));
        return card;
    }

    private TextView sectionTitle(String s) {
        TextView t = Theme.text(this, s, 15, Theme.ON_RED, true);
        t.setPadding(Theme.dp(4), Theme.dp(8), 0, Theme.dp(6));
        return t;
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
