package com.medset.medsetai.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the Markdown subset commonly returned by the local chat models.
 */
public final class ChatMarkdownFormatter {

    private static final Pattern HEADING = Pattern.compile("^(#{1,6})\\s+(.+)$");
    private static final Pattern LIST_ITEM = Pattern.compile("^([*+-]|\\d+[.)])\\s+(.+)$");
    private static final Pattern LATEX_GROUP = Pattern.compile("\\\\(frac|sqrt)\\s*\\{([^{}]*)}\\s*(?:\\{([^{}]*)})?");
    private static final Pattern LATEX_SET = Pattern.compile("\\\\mathbb\\{([RNCQZ])}");
    private static final Pattern LATEX_COMMAND = Pattern.compile("\\\\([A-Za-z]+)");
    private static final Pattern MATH_WRAPPER = Pattern.compile(
            "\\\\\\((.*?)\\\\\\)|\\\\\\[(.*?)\\\\\\]|\\$\\$(.*?)\\$\\$|\\$(.+?)\\$"
    );

    private static final String SUPERSCRIPT_SOURCE =
            "0123456789+-=()in";
    private static final String SUPERSCRIPT_TARGET =
            "⁰¹²³⁴⁵⁶⁷⁸⁹⁺⁻⁼⁽⁾ⁱⁿ";
    private static final String SUBSCRIPT_SOURCE =
            "0123456789+-=()aehijklmnoprstuvx";
    private static final String SUBSCRIPT_TARGET =
            "₀₁₂₃₄₅₆₇₈₉₊₋₌₍₎ₐₑₕᵢⱼₖₗₘₙₒₚᵣₛₜᵤᵥₓ";

    private ChatMarkdownFormatter() {
    }

    /**
     * Converts Markdown text into displayable blocks.
     *
     * @param markdown raw response from the assistant
     * @return formatted blocks in display order
     */
    public static List<Block> parse(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return List.of();
        }

        List<Block> blocks = new ArrayList<>();
        StringBuilder paragraph = new StringBuilder();
        StringBuilder code = new StringBuilder();
        boolean inCodeBlock = false;

        for (String rawLine : markdown.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1)) {
            String line = rawLine.stripTrailing();

            if (line.stripLeading().startsWith("```")) {
                appendParagraph(blocks, paragraph);
                if (inCodeBlock) {
                    blocks.add(new Block(BlockType.CODE, code.toString(), 0, 0));
                    code.setLength(0);
                    inCodeBlock = false;
                } else {
                    inCodeBlock = true;
                }
                continue;
            }

            if (inCodeBlock) {
                if (!code.isEmpty()) {
                    code.append('\n');
                }
                code.append(line);
                continue;
            }

            if (line.isBlank()) {
                appendParagraph(blocks, paragraph);
                continue;
            }

            String content = line.stripLeading();
            int indentation = line.length() - content.length();
            Matcher heading = HEADING.matcher(content);
            Matcher listItem = LIST_ITEM.matcher(content);

            if (heading.matches()) {
                appendParagraph(blocks, paragraph);
                blocks.add(new Block(
                        BlockType.HEADING,
                        normalizeMath(heading.group(2)),
                        heading.group(1).length(),
                        0
                ));
            } else if (content.startsWith(">")) {
                appendParagraph(blocks, paragraph);
                blocks.add(new Block(
                        BlockType.QUOTE,
                        normalizeMath(content.substring(1).stripLeading()),
                        indentation / 2,
                        0
                ));
            } else if (listItem.matches()) {
                appendParagraph(blocks, paragraph);
                String marker = listItem.group(1);
                blocks.add(new Block(
                        Character.isDigit(marker.charAt(0))
                                ? BlockType.NUMBERED_LIST
                                : BlockType.BULLET_LIST,
                        normalizeMath(listItem.group(2)),
                        indentation / 2,
                        Character.isDigit(marker.charAt(0))
                                ? Integer.parseInt(marker.replaceAll("[^0-9]", ""))
                                : 0
                ));
            } else if (paragraph.isEmpty()) {
                paragraph.append(normalizeMath(content));
            } else {
                paragraph.append(' ').append(normalizeMath(content));
            }
        }

        if (inCodeBlock) {
            blocks.add(new Block(BlockType.CODE, code.toString(), 0, 0));
        }
        appendParagraph(blocks, paragraph);
        return List.copyOf(blocks);
    }

    /**
     * Parses inline emphasis, code spans, and links.
     *
     * @param markdown inline Markdown text
     * @return styled text spans
     */
    public static List<Span> parseInline(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return List.of();
        }

        List<Span> spans = new ArrayList<>();
        parseInline(markdown, false, false, false, false, spans);
        return List.copyOf(spans);
    }

    /**
     * Replaces common LaTeX set-theory and arithmetic notation with Unicode.
     *
     * @param text text that may contain LaTeX notation
     * @return readable mathematical text
     */
    public static String normalizeMath(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        Matcher wrapper = MATH_WRAPPER.matcher(text);
        StringBuffer unwrapped = new StringBuffer();
        while (wrapper.find()) {
            String expression = firstNonNull(
                    wrapper.group(1),
                    wrapper.group(2),
                    wrapper.group(3),
                    wrapper.group(4)
            );
            wrapper.appendReplacement(
                    unwrapped,
                    Matcher.quoteReplacement(expression)
            );
        }
        wrapper.appendTail(unwrapped);

        String normalized = replaceLatexGroups(unwrapped.toString());
        normalized = replaceLatexCommands(normalized);
        normalized = normalizeScripts(normalized, '^', SUPERSCRIPT_SOURCE, SUPERSCRIPT_TARGET);
        normalized = normalizeScripts(normalized, '_', SUBSCRIPT_SOURCE, SUBSCRIPT_TARGET);
        return normalized
                .replace("\\{", "{")
                .replace("\\}", "}")
                .replace("\\$", "$");
    }

    private static void parseInline(
            String text,
            boolean bold,
            boolean italic,
            boolean code,
            boolean strikethrough,
            List<Span> spans
    ) {
        StringBuilder plain = new StringBuilder();
        int index = 0;

        while (index < text.length()) {
            if (text.charAt(index) == '\\' && index + 1 < text.length()
                    && isMarkdownPunctuation(text.charAt(index + 1))) {
                plain.append(text.charAt(index + 1));
                index += 2;
                continue;
            }

            if (text.charAt(index) == '`') {
                int end = text.indexOf('`', index + 1);
                if (end > index + 1) {
                    flushPlain(spans, plain, bold, italic, code, strikethrough);
                    addSpan(spans, text.substring(index + 1, end), bold, italic, true, strikethrough);
                    index = end + 1;
                    continue;
                }
            }

            String marker = markerAt(text, index);
            if (marker != null) {
                int end = text.indexOf(marker, index + marker.length());
                if (end > index + marker.length()) {
                    flushPlain(spans, plain, bold, italic, code, strikethrough);
                    boolean nextBold = bold;
                    boolean nextItalic = italic;
                    boolean nextStrike = strikethrough;
                    if ("**".equals(marker) || "__".equals(marker)) {
                        nextBold = !bold;
                    } else if ("***".equals(marker) || "___".equals(marker)) {
                        nextBold = !bold;
                        nextItalic = !italic;
                    } else if ("~~".equals(marker)) {
                        nextStrike = !strikethrough;
                    } else {
                        nextItalic = !italic;
                    }
                    parseInline(
                            text.substring(index + marker.length(), end),
                            nextBold,
                            nextItalic,
                            code,
                            nextStrike,
                            spans
                    );
                    index = end + marker.length();
                    continue;
                }
            }

            plain.append(text.charAt(index));
            index++;
        }

        flushPlain(spans, plain, bold, italic, code, strikethrough);
    }

    private static String markerAt(String text, int index) {
        if (text.startsWith("***", index)) {
            return "***";
        }
        if (text.startsWith("___", index)) {
            return "___";
        }
        if (text.startsWith("**", index)) {
            return "**";
        }
        if (text.startsWith("__", index)) {
            return "__";
        }
        if (text.startsWith("~~", index)) {
            return "~~";
        }
        char character = text.charAt(index);
        if (character == '*' || character == '_') {
            return String.valueOf(character);
        }
        return null;
    }

    private static boolean isMarkdownPunctuation(char character) {
        return "\\`*_{}[]()#+-.!~$".indexOf(character) >= 0;
    }

    private static void flushPlain(
            List<Span> spans,
            StringBuilder plain,
            boolean bold,
            boolean italic,
            boolean code,
            boolean strikethrough
    ) {
        if (!plain.isEmpty()) {
            addSpan(spans, plain.toString(), bold, italic, code, strikethrough);
            plain.setLength(0);
        }
    }

    private static void addSpan(
            List<Span> spans,
            String text,
            boolean bold,
            boolean italic,
            boolean code,
            boolean strikethrough
    ) {
        if (text.isEmpty()) {
            return;
        }

        if (!spans.isEmpty()) {
            Span previous = spans.get(spans.size() - 1);
            if (previous.bold() == bold
                    && previous.italic() == italic
                    && previous.code() == code
                    && previous.strikethrough() == strikethrough) {
                spans.set(spans.size() - 1, new Span(
                        previous.text() + text,
                        bold,
                        italic,
                        code,
                        strikethrough
                ));
                return;
            }
        }

        spans.add(new Span(text, bold, italic, code, strikethrough));
    }

    private static void appendParagraph(List<Block> blocks, StringBuilder paragraph) {
        if (!paragraph.isEmpty()) {
            blocks.add(new Block(BlockType.PARAGRAPH, paragraph.toString(), 0, 0));
            paragraph.setLength(0);
        }
    }

    private static String replaceLatexGroups(String text) {
        Matcher matcher = LATEX_GROUP.matcher(text);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String replacement;
            if ("frac".equals(matcher.group(1))) {
                replacement = "(" + matcher.group(2) + ")/(" + matcher.group(3) + ")";
            } else {
                replacement = "√(" + matcher.group(2) + ")";
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        Matcher setMatcher = LATEX_SET.matcher(result.toString());
        StringBuffer sets = new StringBuffer();
        while (setMatcher.find()) {
            String symbol = switch (setMatcher.group(1)) {
                case "N" -> "ℕ";
                case "Z" -> "ℤ";
                case "Q" -> "ℚ";
                case "R" -> "ℝ";
                case "C" -> "ℂ";
                default -> setMatcher.group();
            };
            setMatcher.appendReplacement(sets, Matcher.quoteReplacement(symbol));
        }
        setMatcher.appendTail(sets);
        return sets.toString().replaceAll("\\\\mathbb\\{([A-Za-z])}", "$1");
    }

    private static String replaceLatexCommands(String text) {
        Matcher matcher = LATEX_COMMAND.matcher(text);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String symbol = switch (matcher.group(1)) {
                case "bigcup" -> "⋃";
                case "bigcap" -> "⋂";
                case "cup" -> "∪";
                case "cap" -> "∩";
                case "setminus" -> "∖";
                case "subseteq" -> "⊆";
                case "subsetneq" -> "⊊";
                case "subset" -> "⊂";
                case "supseteq" -> "⊇";
                case "supset" -> "⊃";
                case "notin" -> "∉";
                case "in" -> "∈";
                case "emptyset", "varnothing" -> "∅";
                case "forall" -> "∀";
                case "exists" -> "∃";
                case "land" -> "∧";
                case "lor" -> "∨";
                case "neg" -> "¬";
                case "Rightarrow", "Longrightarrow" -> "⇒";
                case "rightarrow", "to" -> "→";
                case "leftarrow" -> "←";
                case "leftrightarrow" -> "↔";
                case "times" -> "×";
                case "cdot" -> "·";
                case "leq", "le" -> "≤";
                case "geq", "ge" -> "≥";
                case "neq", "ne" -> "≠";
                case "approx" -> "≈";
                case "infty" -> "∞";
                case "therefore" -> "∴";
                case "because" -> "∵";
                case "pm" -> "±";
                case "mp" -> "∓";
                case "pi" -> "π";
                case "theta" -> "θ";
                case "lambda" -> "λ";
                case "mu" -> "μ";
                case "sigma" -> "σ";
                case "Delta" -> "Δ";
                case "Omega" -> "Ω";
                case "alpha" -> "α";
                case "beta" -> "β";
                case "gamma" -> "γ";
                case "empty" -> "∅";
                case "left", "right", "displaystyle", "text", "mathrm", "quad", "qquad" -> "";
                default -> matcher.group();
            };
            matcher.appendReplacement(result, Matcher.quoteReplacement(symbol));
        }
        matcher.appendTail(result);
        return result.toString()
                .replace("\\,", " ")
                .replace("\\;", " ")
                .replace("\\!", "")
                .replace("\\:", " ");
    }

    private static String normalizeScripts(
            String text,
            char marker,
            String source,
            String target
    ) {
        StringBuilder result = new StringBuilder(text.length());
        for (int index = 0; index < text.length(); index++) {
            if (text.charAt(index) != marker || index + 1 >= text.length()) {
                result.append(text.charAt(index));
                continue;
            }

            int start;
            int end;
            if (text.charAt(index + 1) == '{') {
                start = index + 2;
                end = text.indexOf('}', start);
                if (end < 0) {
                    result.append(marker);
                    continue;
                }
            } else {
                start = index + 1;
                end = start + 1;
            }

            String value = text.substring(start, end);
            String converted = convertScript(value, source, target);
            if (converted == null) {
                result.append(marker);
                if (text.charAt(index + 1) == '{') {
                    result.append('{').append(value).append('}');
                } else {
                    result.append(value);
                }
            } else {
                result.append(converted);
            }
            index = text.charAt(index + 1) == '{' ? end : end - 1;
        }
        return result.toString();
    }

    private static String convertScript(String value, String source, String target) {
        StringBuilder converted = new StringBuilder(value.length());
        for (int index = 0; index < value.length(); index++) {
            int position = source.indexOf(value.charAt(index));
            if (position < 0) {
                return null;
            }
            converted.append(target.charAt(position));
        }
        return converted.toString();
    }

    private static String firstNonNull(String... values) {
        for (String value : values) {
            if (value != null) {
                return value;
            }
        }
        return "";
    }

    public enum BlockType {
        PARAGRAPH,
        HEADING,
        BULLET_LIST,
        NUMBERED_LIST,
        QUOTE,
        CODE
    }

    public record Block(BlockType type, String text, int level, int number) {
    }

    public record Span(
            String text,
            boolean bold,
            boolean italic,
            boolean code,
            boolean strikethrough
    ) {
    }
}
