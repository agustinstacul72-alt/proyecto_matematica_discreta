package com.medset.medsetai.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatMarkdownFormatterTest {

    @Test
    void parsesBoldItalicAndInlineCodeWithoutShowingMarkers() {
        List<ChatMarkdownFormatter.Span> spans =
                ChatMarkdownFormatter.parseInline(
                        "A **bold** and *italic* word with `code`."
                );

        assertEquals(
                List.of(
                        new ChatMarkdownFormatter.Span("A ", false, false, false, false),
                        new ChatMarkdownFormatter.Span("bold", true, false, false, false),
                        new ChatMarkdownFormatter.Span(" and ", false, false, false, false),
                        new ChatMarkdownFormatter.Span("italic", false, true, false, false),
                        new ChatMarkdownFormatter.Span(" word with ", false, false, false, false),
                        new ChatMarkdownFormatter.Span("code", false, false, true, false),
                        new ChatMarkdownFormatter.Span(".", false, false, false, false)
                ),
                spans
        );
    }

    @Test
    void leavesUnclosedMarkdownMarkersVisible() {
        List<ChatMarkdownFormatter.Span> spans =
                ChatMarkdownFormatter.parseInline("unfinished **bold");

        assertEquals("unfinished **bold", spans.getFirst().text());
        assertFalse(spans.getFirst().bold());
    }

    @Test
    void recognizesHeadingsListsParagraphsAndFencedCode() {
        List<ChatMarkdownFormatter.Block> blocks = ChatMarkdownFormatter.parse("""
                ### Operaciones

                1. **Unión** de conjuntos
                - Elemento en A

                ```text
                A ∪ B
                ```
                """);

        assertEquals(4, blocks.size());
        assertEquals(ChatMarkdownFormatter.BlockType.HEADING, blocks.get(0).type());
        assertEquals(3, blocks.get(0).level());
        assertEquals("Operaciones", blocks.get(0).text());
        assertEquals(ChatMarkdownFormatter.BlockType.NUMBERED_LIST, blocks.get(1).type());
        assertEquals(1, blocks.get(1).number());
        assertEquals("**Unión** de conjuntos", blocks.get(1).text());
        assertEquals(ChatMarkdownFormatter.BlockType.BULLET_LIST, blocks.get(2).type());
        assertEquals(ChatMarkdownFormatter.BlockType.CODE, blocks.get(3).type());
        assertEquals("A ∪ B", blocks.get(3).text());
    }

    @Test
    void convertsLatexSetSymbolsAndPowersToReadableUnicode() {
        assertEquals(
                "A ∪ B ⊆ ℝ, x² ∈ ℕ, ∅",
                ChatMarkdownFormatter.normalizeMath(
                        "$A \\cup B \\subseteq \\mathbb{R}$, x^{2} \\in \\mathbb{N}, \\emptyset"
                )
        );
    }

    @Test
    void convertsFractionsAndSquareRoots() {
        assertEquals(
                "(a+b)/(c) + √(x)",
                ChatMarkdownFormatter.normalizeMath(
                        "\\frac{a+b}{c} + \\sqrt{x}"
                )
        );
    }

    @Test
    void preservesUnknownLatexAndUnsupportedScriptCharacters() {
        assertEquals(
                "\\unknown{x} x^{abc}",
                ChatMarkdownFormatter.normalizeMath(
                        "\\unknown{x} x^{abc}"
                )
        );
    }
}
