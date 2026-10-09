package com.medset.medsetai.util;

import com.medset.medsetai.util.ChatMarkdownFormatter.Block;
import com.medset.medsetai.util.ChatMarkdownFormatter.BlockType;
import com.medset.medsetai.util.ChatMarkdownFormatter.Span;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders safe, lightweight Markdown content using native JavaFX text nodes.
 */
public final class ChatMarkdownRenderer {

    private ChatMarkdownRenderer() {
    }

    /**
     * Replaces a message body's nodes with formatted Markdown blocks.
     *
     * @param target JavaFX container that holds the rendered content
     * @param markdown raw message text
     */
    public static void render(VBox target, String markdown) {
        List<Node> nodes = new ArrayList<>();
        for (Block block : ChatMarkdownFormatter.parse(markdown)) {
            nodes.add(renderBlock(block));
        }
        target.getChildren().setAll(nodes);
    }

    private static Node renderBlock(Block block) {
        return switch (block.type()) {
            case PARAGRAPH -> createTextFlow(
                    block.text(),
                    "message-text"
            );
            case HEADING -> createTextFlow(
                    block.text(),
                    "message-text",
                    "markdown-heading",
                    "markdown-heading-" + block.level()
            );
            case CODE -> createCodeBlock(block.text());
            case QUOTE -> createQuote(block);
            case BULLET_LIST, NUMBERED_LIST -> createListItem(block);
        };
    }

    private static Node createListItem(Block block) {
        Text marker = new Text(
                block.type() == BlockType.BULLET_LIST
                        ? "•"
                        : block.number() + "."
        );
        marker.setFont(Font.font("System", FontWeight.BOLD, 14));
        marker.getStyleClass().add("markdown-list-marker");

        TextFlow content = createTextFlow(block.text(), "message-text");
        HBox item = new HBox(8, marker, content);
        item.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        item.setMaxWidth(Double.MAX_VALUE);
        item.setPadding(new Insets(0, 0, 0, Math.max(0, block.level()) * 16));
        HBox.setHgrow(content, Priority.ALWAYS);
        return item;
    }

    private static Node createQuote(Block block) {
        Text quoteMark = new Text("│");
        quoteMark.setFont(Font.font("System", FontWeight.BOLD, 18));
        quoteMark.getStyleClass().add("markdown-quote-mark");

        TextFlow content = createTextFlow(block.text(), "message-text");
        HBox quote = new HBox(8, quoteMark, content);
        quote.setMaxWidth(Double.MAX_VALUE);
        quote.setPadding(new Insets(0, 0, 0, Math.max(0, block.level()) * 16));
        HBox.setHgrow(content, Priority.ALWAYS);
        return quote;
    }

    private static Node createCodeBlock(String content) {
        Text code = new Text(content);
        code.setFont(Font.font("Monospaced", 13));
        code.getStyleClass().addAll("markdown-run", "markdown-code");

        TextFlow flow = new TextFlow(code);
        flow.getStyleClass().add("markdown-code-flow");
        flow.setMaxWidth(Double.MAX_VALUE);

        VBox block = new VBox(flow);
        block.getStyleClass().add("markdown-code-block");
        block.setMaxWidth(Double.MAX_VALUE);
        return block;
    }

    private static TextFlow createTextFlow(
            String markdown,
            String styleClass,
            String... extraStyleClasses
    ) {
        List<Node> textNodes = new ArrayList<>();
        double fontSize = headingFontSize(extraStyleClasses);
        for (Span span : ChatMarkdownFormatter.parseInline(markdown)) {
            Text text = new Text(span.text());
            text.setFont(Font.font(
                    span.code() ? "Monospaced" : "System",
                    span.bold() ? FontWeight.BOLD : FontWeight.NORMAL,
                    span.italic() ? FontPosture.ITALIC : FontPosture.REGULAR,
                    span.code() ? 13 : fontSize
            ));
            text.setStrikethrough(span.strikethrough());
            text.getStyleClass().add("markdown-run");
            if (span.bold()) {
                text.getStyleClass().add("markdown-bold");
            }
            if (span.italic()) {
                text.getStyleClass().add("markdown-italic");
            }
            if (span.code()) {
                text.getStyleClass().add("markdown-code");
            }
            if (span.strikethrough()) {
                text.getStyleClass().add("markdown-strikethrough");
            }
            textNodes.add(text);
        }

        TextFlow flow = new TextFlow();
        flow.getStyleClass().add(styleClass);
        flow.getStyleClass().addAll(extraStyleClasses);
        flow.getChildren().setAll(textNodes);
        flow.setLineSpacing(3);
        flow.setMaxWidth(Double.MAX_VALUE);
        flow.setMinWidth(0);
        return flow;
    }

    private static double headingFontSize(String[] styleClasses) {
        for (String styleClass : styleClasses) {
            if (styleClass.startsWith("markdown-heading-")) {
                return switch (styleClass) {
                    case "markdown-heading-1" -> 24;
                    case "markdown-heading-2" -> 21;
                    case "markdown-heading-3" -> 18;
                    default -> 16;
                };
            }
        }
        return 14;
    }
}
