package com.chineselearning.progressservice.evaluation;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


final class TextNormalizer {

    private TextNormalizer()
    {
    }

    static List<String> tokens(String raw)
    {
        String text = Normalizer.normalize(raw, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
        List<String> tokens = new ArrayList<>();
        StringBuilder word = new StringBuilder();

        int i = 0;
        while (i < text.length())
        {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);

            if (Character.UnicodeScript.of(cp) == Character.UnicodeScript.HAN)
            {
                flush(word, tokens);
                tokens.add(new String(Character.toChars(cp)));

            }
            else if (Character.isLetterOrDigit(cp))
            {
                word.appendCodePoint(cp);

            }
            else
            {
                flush(word, tokens);
            }
        }
        flush(word, tokens);
        return tokens;
    }

    static boolean sameText(String a, String b)
    {
        return tokens(a).equals(tokens(b));
    }

    private static void flush(StringBuilder word, List<String> tokens)
    {
        if (!word.isEmpty())
        {
            tokens.add(word.toString());
            word.setLength(0);
        }
    }
}