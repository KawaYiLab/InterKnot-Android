package dev.kawayilab.interknot.presentation.components

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = LocalTextStyle.current,
    onLinkClick: ((String) -> Unit)? = null
) {
    val uriHandler = LocalUriHandler.current
    val annotated = remember(text) { parseMarkdown(text) }

    ClickableText(
        text = annotated,
        style = style,
        modifier = modifier,
        onClick = { offset ->
            annotated.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()
                ?.let { annotation ->
                    val url = annotation.item
                    onLinkClick?.invoke(url) ?: runCatching { uriHandler.openUri(url) }
                }
        }
    )
}

private fun parseMarkdown(source: String): AnnotatedString = buildAnnotatedString {
    val lines = source.replace("\r\n", "\n").split("\n")
    lines.forEachIndexed { index, rawLine ->
        val line = rawLine.trimEnd()
        when {
            line.startsWith("# ") -> {
                parseInline(line.substring(2), SpanStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp))
            }
            line.startsWith("## ") -> {
                parseInline(line.substring(3), SpanStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp))
            }
            line.startsWith("### ") -> {
                parseInline(line.substring(4), SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp))
            }
            line.startsWith("#### ") -> {
                parseInline(line.substring(5), SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp))
            }
            line.startsWith("##### ") -> {
                parseInline(line.substring(6), SpanStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp))
            }
            line.startsWith("###### ") -> {
                parseInline(line.substring(7), SpanStyle(fontWeight = FontWeight.Bold, fontSize = 13.sp))
            }
            else -> parseInline(line)
        }
        if (index < lines.lastIndex) append('\n')
    }
}

private fun AnnotatedString.Builder.parseInline(line: String, baseStyle: SpanStyle? = null) {
    baseStyle?.let { pushStyle(it) }
    val pending = mutableListOf<Pair<Int, SpanStyle>>()
    var i = 0
    while (i < line.length) {
        val c = line[i]
        when {
            c == '\\' && i + 1 < line.length -> {
                append(line[i + 1])
                i += 2
            }
            c == '[' -> {
                val parsed = parseLink(line, i)
                if (parsed != null) {
                    val (linkEnd, label, url) = parsed
                    val start = length
                    parseInline(label)
                    addStyle(
                        SpanStyle(
                            color = androidx.compose.ui.graphics.Color(0xFF2196F3),
                            textDecoration = TextDecoration.Underline
                        ),
                        start,
                        length
                    )
                    addStringAnnotation(tag = "URL", annotation = url, start = start, end = length)
                    i = linkEnd
                } else {
                    append(c)
                    i++
                }
            }
            c == '*' || c == '_' -> {
                val isDouble = i + 1 < line.length && line[i + 1] == c
                val marker = if (isDouble) "**" else "*"
                val style = if (isDouble) {
                    SpanStyle(fontWeight = FontWeight.Bold)
                } else {
                    SpanStyle(fontStyle = FontStyle.Italic)
                }
                if (pending.isNotEmpty() && pending.last().second == style) {
                    pending.removeAt(pending.lastIndex)
                    pop()
                } else {
                    pending.add(length to style)
                    pushStyle(style)
                }
                i += marker.length
            }
            else -> {
                append(c)
                i++
            }
        }
    }
    // Unclosed styles are popped implicitly when builder ends.
    repeat(pending.size) { pop() }
    baseStyle?.let { pop() }
}

private fun parseLink(line: String, start: Int): Triple<Int, String, String>? {
    var i = start + 1
    val labelBuilder = StringBuilder()
    while (i < line.length && line[i] != ']') {
        labelBuilder.append(line[i])
        i++
    }
    if (i >= line.length || line[i] != ']') return null
    i++
    if (i >= line.length || line[i] != '(') return null
    i++
    val urlBuilder = StringBuilder()
    while (i < line.length && line[i] != ')') {
        urlBuilder.append(line[i])
        i++
    }
    if (i >= line.length || line[i] != ')') return null
    return Triple(i + 1, labelBuilder.toString(), urlBuilder.toString().trim())
}
