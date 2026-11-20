package com.zakhrafa.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Converts Latin letters and digits into Unicode decorative alphabets.
 * Pure Java with no Android dependencies, so it can be tested on any JVM.
 *
 * Note: these are regular Unicode characters, not fonts. They copy and paste
 * anywhere, but screen readers and search cannot read them as normal letters.
 */
public final class FancyStyles {

    private FancyStyles() {}

    public static final class Style {
        public final String label;   // Arabic label shown in the UI
        private final Mapper mapper;

        Style(String label, Mapper mapper) {
            this.label = label;
            this.mapper = mapper;
        }

        public String apply(String text) {
            return text == null ? "" : mapper.map(text);
        }
    }

    private interface Mapper {
        String map(String text);
    }

    // ---------- contiguous block mapping ----------

    /**
     * Shifts A-Z, a-z and 0-9 to a contiguous block of code points.
     * Pass 0 for a block that does not exist for that character class.
     * holesFrom/holesTo replace characters that were already encoded elsewhere
     * (for example the Script "B" lives at U+212C, not inside the math block).
     */
    private static Mapper block(int upper, int lower, int digit, int[] holesFrom, int[] holesTo) {
        return text -> {
            StringBuilder sb = new StringBuilder();
            text.codePoints().forEach(cp -> {
                int out = cp;
                boolean hole = false;
                if (holesFrom != null) {
                    for (int i = 0; i < holesFrom.length; i++) {
                        if (cp == holesFrom[i]) { out = holesTo[i]; hole = true; break; }
                    }
                }
                if (!hole) {
                    if (cp >= 'A' && cp <= 'Z' && upper != 0) out = upper + (cp - 'A');
                    else if (cp >= 'a' && cp <= 'z' && lower != 0) out = lower + (cp - 'a');
                    else if (cp >= '0' && cp <= '9' && digit != 0) out = digit + (cp - '0');
                }
                sb.appendCodePoint(out);
            });
            return sb.toString();
        };
    }

    // ---------- table mapping ----------

    private static int[] codePoints(String s) {
        return s.codePoints().toArray();
    }

    /** Maps letters (case-insensitive) through a 26-entry table of code points. */
    private static Mapper letterTable(String table) {
        int[] map = codePoints(table);
        if (map.length != 26) throw new IllegalArgumentException("table must have 26 entries");
        return text -> {
            StringBuilder sb = new StringBuilder();
            text.codePoints().forEach(cp -> {
                int lower = Character.toLowerCase(cp);
                if (lower >= 'a' && lower <= 'z') sb.appendCodePoint(map[lower - 'a']);
                else sb.appendCodePoint(cp);
            });
            return sb.toString();
        };
    }

    /** Upside-down lowercase letters, read in reverse order. */
    private static Mapper upsideDown() {
        int[] to = codePoints("ɐqɔpǝɟƃɥᴉɾʞlɯuodbɹsʇnʌʍxʎz");
        return text -> {
            List<Integer> out = new ArrayList<>();
            text.toLowerCase().codePoints().forEach(cp -> {
                if (cp >= 'a' && cp <= 'z') out.add(to[cp - 'a']);
                else out.add(cp);
            });
            Collections.reverse(out);
            StringBuilder sb = new StringBuilder();
            for (int cp : out) sb.appendCodePoint(cp);
            return sb.toString();
        };
    }

    /** Full-width ASCII (Ｆｕｌｌ). */
    private static Mapper fullWidth() {
        return text -> {
            StringBuilder sb = new StringBuilder();
            text.codePoints().forEach(cp -> {
                if (cp == ' ') sb.append('\u3000');
                else if (cp >= 0x21 && cp <= 0x7E) sb.appendCodePoint(cp + 0xFEE0);
                else sb.appendCodePoint(cp);
            });
            return sb.toString();
        };
    }

    // ---------- style list ----------

    public static final List<Style> STYLES;

    static {
        List<Style> s = new ArrayList<>();

        s.add(new Style("عريض", block(0x1D400, 0x1D41A, 0x1D7CE, null, null)));
        s.add(new Style("مائل", block(0x1D434, 0x1D44E, 0, new int[]{'h'}, new int[]{0x210E})));
        s.add(new Style("عريض مائل", block(0x1D468, 0x1D482, 0, null, null)));
        s.add(new Style("خط كتابة", block(0x1D49C, 0x1D4B6, 0,
                new int[]{'B', 'E', 'F', 'H', 'I', 'L', 'M', 'R', 'e', 'g', 'o'},
                new int[]{0x212C, 0x2130, 0x2131, 0x210B, 0x2110, 0x2112, 0x2133, 0x211B, 0x212F, 0x210A, 0x2134})));
        s.add(new Style("كتابة عريضة", block(0x1D4D0, 0x1D4EA, 0, null, null)));
        s.add(new Style("قوطي", block(0x1D504, 0x1D51E, 0,
                new int[]{'C', 'H', 'I', 'R', 'Z'},
                new int[]{0x212D, 0x210C, 0x2111, 0x211C, 0x2128})));
        s.add(new Style("قوطي عريض", block(0x1D56C, 0x1D586, 0, null, null)));
        s.add(new Style("مزدوج", block(0x1D538, 0x1D552, 0x1D7D8,
                new int[]{'C', 'H', 'N', 'P', 'Q', 'R', 'Z'},
                new int[]{0x2102, 0x210D, 0x2115, 0x2119, 0x211A, 0x211D, 0x2124})));
        s.add(new Style("سانس", block(0x1D5A0, 0x1D5BA, 0x1D7E2, null, null)));
        s.add(new Style("سانس عريض", block(0x1D5D4, 0x1D5EE, 0x1D7EC, null, null)));
        s.add(new Style("سانس مائل", block(0x1D608, 0x1D622, 0, null, null)));
        s.add(new Style("سانس عريض مائل", block(0x1D63C, 0x1D656, 0, null, null)));
        s.add(new Style("مونو", block(0x1D670, 0x1D68A, 0x1D7F6, null, null)));

        s.add(new Style("دائري", block(0x24B6, 0x24D0, 0, null, null)));
        s.add(new Style("دائري أسود", block(0x1F150, 0x1F150, 0, null, null)));
        s.add(new Style("مربع", block(0x1F130, 0x1F130, 0, null, null)));
        s.add(new Style("مربع أسود", block(0x1F170, 0x1F170, 0, null, null)));
        s.add(new Style("عرض كامل", fullWidth()));

        s.add(new Style("حروف صغيرة", letterTable(
                "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘꞯʀꜱᴛᴜᴠᴡxʏᴢ")));
        s.add(new Style("مقلوب", upsideDown()));

        STYLES = Collections.unmodifiableList(s);
    }
}
