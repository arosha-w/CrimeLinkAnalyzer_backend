package com.crimeLink.analyzer.util;

/**
 * Utility class for sanitizing user-controlled input before logging.
 * <p>
 * Prevents <strong>Log Injection</strong> (CWE-117) by stripping or escaping
 * carriage-return, line-feed, and other ASCII control characters that an
 * attacker could use to forge log entries.
 * <p>
 * Usage:
 * <pre>
 *   log.info("Processing file: {}", LogSanitizer.sanitize(file.getOriginalFilename()));
 * </pre>
 */
public final class LogSanitizer {

    /** Maximum length of a sanitized value written to a log line. */
    private static final int MAX_LENGTH = 200;

    private LogSanitizer() {
        // utility class – no instances
    }

    /**
     * Sanitize a {@code String} value so it is safe to include in a log message.
     * <ul>
     *   <li>{@code null} → the literal string {@code "null"}</li>
     *   <li>CR ({@code \r}) → the two-character escape {@code \r}</li>
     *   <li>LF ({@code \n}) → the two-character escape {@code \n}</li>
     *   <li>TAB ({@code \t}) → the two-character escape {@code \t}</li>
     *   <li>Any other ASCII control character ({@code U+0000–U+001F}, {@code U+007F})
     *       except space → {@code ?}</li>
     *   <li>Values longer than {@value #MAX_LENGTH} characters (after escaping)
     *       are truncated with a {@code …(truncated)} suffix</li>
     * </ul>
     *
     * @param input the raw, potentially attacker-controlled string
     * @return a sanitized string safe for logging; never {@code null}
     */
    public static String sanitize(String input) {
        if (input == null) {
            return "null";
        }

        StringBuilder sb = new StringBuilder(Math.min(input.length(), MAX_LENGTH + 20));
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (ch == '\r') {
                sb.append("\\r");
            } else if (ch == '\n') {
                sb.append("\\n");
            } else if (ch == '\t') {
                sb.append("\\t");
            } else if ((ch >= '\u0000' && ch <= '\u001F') || ch == '\u007F') {
                sb.append('?');
            } else {
                sb.append(ch);
            }

            // Early exit when we already exceed the cap
            if (sb.length() > MAX_LENGTH) {
                break;
            }
        }

        if (sb.length() > MAX_LENGTH) {
            sb.setLength(MAX_LENGTH);
            sb.append("…(truncated)");
        }

        return sb.toString();
    }

    /**
     * Convenience overload that accepts any {@link Object}.
     * Calls {@link Object#toString()} before sanitizing.
     *
     * @param input the object whose string representation should be sanitized
     * @return a sanitized string safe for logging; never {@code null}
     */
    public static String sanitize(Object input) {
        if (input == null) {
            return "null";
        }
        return sanitize(input.toString());
    }
}
