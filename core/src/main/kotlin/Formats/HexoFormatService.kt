package org.jetbrains.dokka.Formats

import com.google.inject.Inject
import com.google.inject.name.Named
import org.jetbrains.dokka.*
import org.jetbrains.dokka.Utilities.impliedPlatformsName

open class HexoOutputBuilder(
    to: StringBuilder,
    location: Location,
    generator: NodeLocationAwareGenerator,
    languageService: LanguageService,
    extension: String,
    impliedPlatforms: List<String>
) : StructuredOutputBuilder(to, location, generator, languageService, extension, impliedPlatforms) {
    private var needHardLineBreaks = false

    override fun appendNodes(nodes: Iterable<DocumentationNode>) {
        to.appendln("---")
        to.appendln("title: ${getPageTitle(nodes)}")
        to.appendln("layout: api")
        to.appendln("---")
        to.appendln()
        super.appendNodes(nodes)
    }

    protected fun div(to: StringBuilder, cssClass: String, block: () -> Unit) {
        to.append("<div class=\"$cssClass\">")
        block()
        to.append("</div>")
    }

    override fun appendAsSignature(node: ContentNode, block: () -> Unit) {
        val contentLength = node.textLength
        if (contentLength == 0) return
        div(to, "signature") {
            needHardLineBreaks = contentLength >= 62
            try {
                block()
            } finally {
                needHardLineBreaks = false
            }
        }
    }

    override fun appendSoftLineBreak() {
        if (needHardLineBreaks) {
            to.append("<br/>")
        }
    }

    override fun appendIndentedSoftLineBreak() {
        if (needHardLineBreaks) {
            to.append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;")
        }
    }

    override fun appendText(text: String) {
        to.append(text.htmlEscape())
    }

    override fun appendSymbol(text: String) {
        to.append("<span class=\"symbol\">${text.htmlEscape()}</span>")
    }

    override fun appendKeyword(text: String) {
        to.append("<span class=\"keyword\">${text.htmlEscape()}</span>")
    }

    override fun appendIdentifier(text: String, kind: IdentifierKind, signature: String?) {
        val id = signature?.let { " id=\"$it\"" }.orEmpty()
        to.append("<span class=\"identifier\"$id>${text.htmlEscape()}</span>")
    }

    override fun appendBlockCode(language: String, body: () -> Unit) {
        val openTags = if (language.isNotBlank())
            "<pre><code class=\"lang-$language\">"
        else
            "<pre><code>"
        wrap(openTags, "</code></pre>", body)
    }

    override fun appendHeader(level: Int, body: () -> Unit) {
        wrapInTag("h$level", body)
    }

    override fun appendParagraph(body: () -> Unit) {
        wrapInTag("p", body)
    }

    override fun appendSoftParagraph(body: () -> Unit) {
        appendParagraph(body)
    }

    override fun appendLine() {
        to.append("<br/>")
    }

    override fun appendAnchor(anchor: String) {
        to.append("<a name=\"${anchor.htmlEscape()}\"></a>")
    }

    override fun appendTable(vararg columns: String, body: () -> Unit) {
        to.append("<table class=\"api-docs-table\">")
        to.append("<thead><tr>")
        columns.forEach { to.append("<th>$it</th>") }
        to.append("</tr></thead>")
        body()
        to.append("</table>")
    }

    override fun appendTableBody(body: () -> Unit) {
        wrapInTag("tbody", body)
    }

    override fun appendTableRow(body: () -> Unit) {
        wrapInTag("tr", body)
    }

    override fun appendTableCell(body: () -> Unit) {
        wrapInTag("td", body)
    }

    override fun appendLink(href: String, body: () -> Unit) {
        wrap("<a href=\"$href\">", "</a>", body)
    }

    override fun appendStrong(body: () -> Unit) {
        wrapInTag("strong", body)
    }

    override fun appendEmphasis(body: () -> Unit) {
        wrapInTag("em", body)
    }

    override fun appendStrikethrough(body: () -> Unit) {
        wrapInTag("s", body)
    }

    override fun appendCode(body: () -> Unit) {
        wrapInTag("code", body)
    }

    override fun appendUnorderedList(body: () -> Unit) {
        wrapInTag("ul", body)
    }

    override fun appendOrderedList(body: () -> Unit) {
        wrapInTag("ol", body)
    }

    override fun appendListItem(body: () -> Unit) {
        wrapInTag("li", body)
    }

    override fun appendBreadcrumbSeparator() {
        to.append("&nbsp;/&nbsp;")
    }

    override fun appendNonBreakingSpace() {
        to.append("&nbsp;")
    }

    override fun ensureParagraph() {

    }
}

open class HexoFormatService(
    generator: NodeLocationAwareGenerator,
    signatureGenerator: LanguageService,
    linkExtension: String,
    impliedPlatforms: List<String>
) : GFMFormatService(generator, signatureGenerator, linkExtension, impliedPlatforms) {

    @Inject
    constructor(
        generator: NodeLocationAwareGenerator,
        signatureGenerator: LanguageService,
        @Named(impliedPlatformsName) impliedPlatforms: List<String>
    ) : this(generator, signatureGenerator, "html", impliedPlatforms)

    override fun createOutputBuilder(to: StringBuilder, location: Location): FormattedOutputBuilder =
        HexoOutputBuilder(to, location, generator, languageService, extension, impliedPlatforms)
}