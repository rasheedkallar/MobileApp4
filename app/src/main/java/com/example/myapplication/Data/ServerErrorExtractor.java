package com.example.myapplication.Data;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ServerErrorExtractor {
    private ServerErrorExtractor() {}
    public static String extractError(byte[] responseBody) {
        if (responseBody == null || responseBody.length == 0) {
            return "No response body";
        }
        String body = new String(responseBody, StandardCharsets.UTF_8).trim();
        return extractError(body);
    }

    /** From String body to best-effort error message */
    public static String extractError(String body) {
        if (body == null || body.isEmpty()) return "Empty response";
        String lower = body.toLowerCase(Locale.US);

        // 1) If it looks like JSON, don't try to parse HTML—let your JSON error handler deal with it
        if (lower.startsWith("{") || lower.startsWith("[")) {
            // You can enhance here: pick "message" field if present, etc.
            return compact(body, 300);
        }

        // 2) Try XML-style Web API error blocks embedded or returned directly
        String x;
        x = innerTag(body, "ExceptionMessage");
        if (x != null && !x.isEmpty()) return cleanText(x);

        x = innerTag(body, "Message");
        if (x != null && !x.isEmpty()) return cleanText(x);

        x = innerTag(body, "StackTrace");
        if (x != null && !x.isEmpty()) return "StackTrace: " + cleanText(x);

        // 3) Try typical HTML fields: <title>, <h1>, <h2>
        x = innerTag(body, "title");
        if (isMeaningful(x)) return cleanText(x);

        x = innerTag(body, "h1");
        if (isMeaningful(x)) return cleanText(x);

        x = innerTag(body, "h2");
        if (isMeaningful(x)) return cleanText(x);

        // 4) Strip HTML to plain text, search first line containing "error"/"exception"
        String plain = stripHtmlToText(body);
        String bestLine = pickInterestingLine(plain);
        if (isMeaningful(bestLine)) return bestLine;

        // 5) Fallback: compact the stripped plain text
        String compacted = compact(plain, 300);
        if (!compacted.isEmpty()) return compacted;

        // 6) Last fallback: compact raw body
        return compact(body, 300);
    }
    private static String innerTag(String html, String tag) {
        // Case-insensitive, dotall
        String regex = "(?is)<\\s*" + tag + "\\b[^>]*>(.*?)<\\s*/\\s*" + tag + "\\s*>";
        Matcher m = Pattern.compile(regex).matcher(html);
        if (m.find()) {
            return m.group(1).trim();
        }
        return null;
    }

    private static String stripHtmlToText(String html) {
        Spanned spanned;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            spanned = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            spanned = Html.fromHtml(html);
        }
        return spanned == null ? "" : spanned.toString().trim();
    }

    private static String pickInterestingLine(String text) {
        if (text == null || text.isEmpty()) return null;
        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            String l = line.trim();
            if (l.isEmpty()) continue;
            String ll = l.toLowerCase(Locale.US);
            if (ll.contains("exception") || ll.contains("error") || ll.contains("failed") || ll.contains("stack trace")) {
                return l;
            }
        }
        // If nothing matched, return the first non-empty line
        for (String line : lines) {
            String l = line.trim();
            if (!l.isEmpty()) return l;
        }
        return null;
    }

    private static String cleanText(String s) {
        if (s == null) return null;
        return stripHtmlToText(s).replaceAll("\\s+", " ").trim();
    }

    private static boolean isMeaningful(String s) {
        return s != null && !s.trim().isEmpty() && !"home page".equalsIgnoreCase(s.trim());
    }

    private static String compact(String s, int maxLen) {
        if (s == null) return "";
        String cleaned = s.replaceAll("\\s+", " ").trim();
        if (cleaned.length() <= maxLen) return cleaned;
        return cleaned.substring(0, maxLen) + "…";
    }
}