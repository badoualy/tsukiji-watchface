package com.badoualy.tsukiji.watchface;

public class Kanji {

    public static final Kanji DEFAULT_KANJI = new Kanji("月", "", "", "");

    public final String kanji;
    public final String onYomi;
    public final String kunYomi;
    public final String translation;

    public Kanji(String kanji, String onYomi, String kunYomi, String translation) {
        this.kanji = kanji;
        this.onYomi = onYomi;
        this.kunYomi = kunYomi;
        this.translation = translation.substring(0, Math.min(32, translation.length()));
    }
}
