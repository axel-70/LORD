package com.zakhrafa.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Offline library of decorative symbols and frames. Every entry is a plain Unicode character. */
public final class SymbolLibrary {

    private SymbolLibrary() {}

    public static final class Category {
        public final String name;
        public final List<String> symbols;

        Category(String name, String... symbols) {
            this.name = name;
            this.symbols = Collections.unmodifiableList(Arrays.asList(symbols));
        }
    }

    /** Frame patterns. "{}" is replaced by the user's text. */
    public static final List<String> FRAMES = Collections.unmodifiableList(Arrays.asList(
            "꧁ {} ꧂",
            "✦ {} ✦",
            "༺ {} ༻",
            "⊰ {} ⊱",
            "𓆩 {} 𓆪",
            "•°•°• {} •°•°•",
            "『 {} 』",
            "❝ {} ❞",
            "ᯓ ✧ {} ✧ ᯓ",
            "꒰ {} ꒱",
            "⋆ {} ⋆",
            "˚ ༘ {} ⋆｡˚",
            "⸙ {} ⸙",
            "◦•●◉✿ {} ✿◉●•◦",
            "« {} »",
            "⚜ {} ⚜"
    ));

    public static final List<Category> CATEGORIES = Collections.unmodifiableList(Arrays.asList(
            new Category("نجوم ونقاط",
                    "✦", "✧", "★", "☆", "✶", "✷", "✩", "✪", "⋆", "✬", "✭", "✮", "✯", "✰", "⊹", "⁂", "⁕", "✣", "✤", "❉", "❋", "✺", "✹", "❂"),
            new Category("زهور",
                    "❀", "✿", "❁", "❃", "✾", "✽", "❦", "❧", "✻", "✼", "❋", "⚘", "❄", "❅", "❆", "✵"),
            new Category("قلوب",
                    "♡", "♥", "❤", "❣", "❥", "♢", "♦", "♧", "♤", "♠", "♣", "♧", "❦", "༄"),
            new Category("أقواس",
                    "꧁", "꧂", "༺", "༻", "⊰", "⊱", "⸨", "⸩", "〘", "〙", "⟬", "⟭", "❮", "❯", "⟦", "⟧", "⦅", "⦆", "『", "』", "꒰", "꒱", "⸙", "𓆩", "𓆪", "ᯓ", "۩", "۞", "۝"),
            new Category("فواصل",
                    "─", "━", "═", "≡", "∘", "°", "˚", "・", "·", "•", "☙", "⁜", "⁘", "⁖", "࿐", "∴", "∵", "ꕥ", "꙳", "⋮", "࿇", "｡", "˙"),
            new Category("أسهم وعلامات",
                    "➳", "➤", "➢", "➔", "➜", "➝", "➞", "➟", "⇨", "⇒", "➽", "➼", "↯", "☞", "☛", "✔", "✓", "✗", "✘", "✎", "✐", "✑", "✒", "✍"),
            new Category("موسيقى",
                    "♪", "♫", "♬", "♩", "♭", "♯"),
            new Category("أشكال",
                    "◉", "◎", "●", "○", "◦", "◈", "◇", "◆", "◊", "⬡", "⬢", "⬥", "⧫", "⧈", "⌘", "⏣", "⎈"),
            new Category("متنوعة",
                    "☾", "☽", "⚝", "⚜", "✞", "✟", "☯", "♾", "∞", "⊙", "⊚", "⊛", "⊜", "⊝", "꩜", "ꙮ", "ꔷ")
    ));
}
