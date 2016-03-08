package com.kidnapp.org.webviewimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Ajay on 12/7/2015.
 */
public class UrlExtractor {
        public static List<String> extractUrls(String input) {
            List<String> result = new ArrayList<String>();

            String regex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&amp;@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&amp;@#/%=~_()|]";
            Pattern p = Pattern.compile(regex);
            Matcher matcher = p.matcher(input);
            while (matcher.find()) {
                result.add(matcher.group());
            }

            return result;
        }
}
