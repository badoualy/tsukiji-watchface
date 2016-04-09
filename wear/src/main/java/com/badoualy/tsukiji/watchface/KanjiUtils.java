package com.badoualy.tsukiji.watchface;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class KanjiUtils {

    private static final int LINE_COUNT = 979;
    private static final int CACHE_SIZE = 60;

    private KanjiUtils() {

    }

    private static Random random = new Random();
    private static List<Kanji> cache = new ArrayList<>();

    public static String convertNumber(int number) {
        if (number > 60)
            return "";
        return NUMBERS[number];
    }

    public static Kanji getRandomKanji(Context context) {
        if (!cache.isEmpty())
            cache.remove(0);
        if (cache.isEmpty())
            fillCache(context);
        if (cache.isEmpty())
            return Kanji.DEFAULT_KANJI;

        return cache.get(0);
    }

    private static void fillCache(Context context) {
        try {
            final InputStream is = context.getResources().openRawResource(R.raw.kanji);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            int[] indexes = new int[CACHE_SIZE];
            for (int i = 0; i < CACHE_SIZE; i++)
                indexes[i] = random.nextInt(LINE_COUNT);
            Arrays.sort(indexes);

            int line = 0;
            for (int index : indexes) {
                if (index < line)
                    continue;

                while (line != index) {
                    reader.readLine();
                    line++;
                }

                String[] fields = reader.readLine().split(";", -1);
                line++;
                cache.add(new Kanji(fields[0], fields[1], fields[2], fields[3]));
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final String[] NUMBERS = {
            "零",
            "一",
            "二",
            "三",
            "四",
            "五",
            "六",
            "七",
            "八",
            "九",

            "十",
            "十一",
            "十二",
            "十三 ",
            "十四",
            "十五",
            "十六",
            "十七",
            "十八",
            "十九",

            "二十",
            "二十一",
            "二十二",
            "二十三 ",
            "二十四",
            "二十五",
            "二十六",
            "二十七",
            "二十八",
            "二十九",

            "三十",
            "三十一",
            "三十二",
            "三十三 ",
            "三十四",
            "三十五",
            "三十六",
            "三十七",
            "三十八",
            "三十九",

            "四十",
            "四十一",
            "四十二",
            "四十三 ",
            "四十四",
            "四十五",
            "四十六",
            "四十七",
            "四十八",
            "四十九",

            "五十",
            "五十一",
            "五十二",
            "五十三 ",
            "五十四",
            "五十五",
            "五十六",
            "五十七",
            "五十八",
            "五十九",

            "六十",
            };
}
