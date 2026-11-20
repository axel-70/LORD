package com.flash.app;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
    private final FrameLayout[] navPills = new FrameLayout[3];
    private final ImageView[] navIcons = new ImageView[3];
    private final TextView[] navLabels = new TextView[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Theme.init(this);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);

        // Large title, Apple style
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(Theme.dp(20), Theme.dp(34), Theme.dp(20), Theme.dp(8));
        TextView title = Theme.text(this, "Flash", 34, Theme.TEXT, true);
        header.addView(title, Theme.wrap());
        header.addView(Theme.text(this, "حروف ورموز ورسايل مزخرفة، اضغط على أي شيء وهيتنسخ",
                13, Theme.GRAY, false), Theme.wrap());
        root.addView(header, Theme.matchWidth());

        FrameLayout content = new FrameLayout(this);
        content.setPadding(Theme.dp(16), Theme.dp(4), Theme.dp(16), 0);
        pages[0] = buildLettersPage();
        pages[1] = buildSymbolsPage();
        pages[2] = new MessageComposer(this).build();
        for (View p : pages) {
            content.addView(p, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        }
        root.addView(content, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        TextView footer = Theme.text(this, Theme.COPYRIGHT, 11, Theme.GRAY, false);
        footer.setGravity(Gravity.CENTER_HORIZONTAL);
        footer.setPadding(0, Theme.dp(8), 0, Theme.dp(8));
        root.addView(footer, Theme.matchWidth());

        // Bottom navigation, like WhatsApp
        View line = new View(this);
        line.setBackgroundColor(Theme.HAIRLINE);
        root.addView(line, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, Math.max(1, Theme.dp(0.5f))));
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setBackgroundColor(Color.BLACK);
        String[] names = {"الحروف", "الرموز", "الرسايل"};
        int[] icons = {R.drawable.ic_nav_letters, R.drawable.ic_nav_symbols, R.drawable.ic_nav_messages};
        for (int i = 0; i < names.length; i++) {
            nav.addView(navItem(i, names[i], icons[i]),
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        }
        root.addView(nav, Theme.matchWidth());

        View screen = root;
        setContentView(screen);
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
        inputText.setTextSize(17);
        inputText.setMinLines(2);
        inputText.setGravity(Gravity.TOP);
        inputText.setPadding(Theme.dp(14), Theme.dp(12), Theme.dp(14), Theme.dp(12));
        Theme.input(inputText);
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
        symbolSearch.setTextSize(16);
        symbolSearch.setSingleLine(true);
        symbolSearch.setPadding(Theme.dp(14), Theme.dp(10), Theme.dp(14), Theme.dp(10));
        Theme.input(symbolSearch);
        symbolSearch.addTextChangedListener(new SimpleWatcher(this::refreshSymbols));
        box.addView(symbolSearch, Theme.matchBottom(12));

        symbolsBox = new LinearLayout(this);
        symbolsBox.setOrientation(LinearLayout.VERTICAL);
        box.addView(symbolsBox, Theme.matchWidth());
        return sv;
    }

    private View navItem(int idx, String label, int iconRes) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER_HORIZONTAL);
        item.setPadding(0, Theme.dp(10), 0, Theme.dp(10));
        item.setOnClickListener(v -> showPage(idx));

        navPills[idx] = new FrameLayout(this);
        navIcons[idx] = new ImageView(this);
        navIcons[idx].setImageResource(iconRes);
        FrameLayout.LayoutParams ilp = new FrameLayout.LayoutParams(Theme.dp(24), Theme.dp(24));
        ilp.gravity = Gravity.CENTER;
        navPills[idx].addView(navIcons[idx], ilp);
        item.addView(navPills[idx], new LinearLayout.LayoutParams(Theme.dp(64), Theme.dp(32)));

        navLabels[idx] = Theme.text(this, label, 12, Theme.GRAY, false);
        navLabels[idx].setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams llp = Theme.wrap();
        llp.setMargins(0, Theme.dp(4), 0, 0);
        item.addView(navLabels[idx], llp);
        return item;
    }

    private void showPage(int index) {
        for (int i = 0; i < pages.length; i++) {
            boolean active = i == index;
            pages[i].setVisibility(active ? View.VISIBLE : View.GONE);
            navPills[i].setBackground(active ? Theme.rounded(Theme.ACTIVE_PILL, 16) : null);
            navIcons[i].setImageTintList(ColorStateList.valueOf(active ? Theme.TEXT : Theme.GRAY));
            navLabels[i].setTextColor(active ? Theme.TEXT : Theme.GRAY);
            navLabels[i].setTypeface(active ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
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
        t.setBackground(Theme.card(14));
        t.setOnClickListener(v -> Theme.copy(this, sym));
        Theme.press(t);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(Theme.dp(3), Theme.dp(3), Theme.dp(3), Theme.dp(3));
        t.setLayoutParams(lp);
        return t;
    }

    private LinearLayout labelCard(String label, String value) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(Theme.dp(16), Theme.dp(12), Theme.dp(16), Theme.dp(12));
        card.setBackground(Theme.card(18));
        card.addView(Theme.text(this, label, 12, Theme.GRAY, false), Theme.wrap());
        TextView v = Theme.text(this, value, 18, Theme.TEXT, false);
        card.addView(v, Theme.matchWidth());
        card.setOnClickListener(x -> Theme.copy(this, value));
        Theme.press(card);
        return card;
    }

    private TextView sectionTitle(String s) {
        return Theme.section(this, s);
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
