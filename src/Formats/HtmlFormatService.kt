package org.jetbrains.dokka

public open class HtmlFormatService(locationService: LocationService,
                                    signatureGenerator: LanguageService,
                                    val templateService: HtmlTemplateService = HtmlTemplateService.default())
: StructuredFormatService(locationService, signatureGenerator) {
    override val extension: String = "html"

    override public fun formatText(text: String): String {
        return text.htmlEscape()
    }
    override fun formatSymbol(text: String): String {
        return "<span class=\"symbol\">${formatText(text)}</span>"
    }
    override fun formatKeyword(text: String): String {
        return "<span class=\"keyword\">${formatText(text)}</span>"
    }
    override fun formatIdentifier(text: String): String {
        return "<span class=\"identifier\">${formatText(text)}</span>"
    }

    override fun appendBlockCode(to: StringBuilder, line: String) {
        to.appendln("<code>")
        to.appendln(line)
        to.appendln("</code>")
    }

    override fun appendBlockCode(to: StringBuilder, lines: Iterable<String>) {
        to.appendln("<code>")
        to.appendln(lines.join("\n"))
        to.appendln("</code>")
    }

    override fun appendHeader(to: StringBuilder, text: String, level: Int) {
        to.appendln("<h$level>${text}</h$level>")
    }

    override fun appendText(to: StringBuilder, text: String) {
        to.appendln("<p>${text}</p>")
    }

    override fun appendLine(to: StringBuilder, text: String) {
        to.appendln("${text}<br/>")
    }

    override fun appendLine(to: StringBuilder) {
        to.appendln("<br/>")
    }

    override fun appendTable(to: StringBuilder, body: () -> Unit) {
        to.appendln("<table>")
        body()
        to.appendln("</table>")
    }

    override fun appendTableHeader(to: StringBuilder, body: () -> Unit) {
        to.appendln("<thead>")
        body()
        to.appendln("</thead>")
    }

    override fun appendTableBody(to: StringBuilder, body: () -> Unit) {
        to.appendln("<tbody>")
        body()
        to.appendln("</tbody>")
    }

    override fun appendTableRow(to: StringBuilder, body: () -> Unit) {
        to.appendln("<tr>")
        body()
        to.appendln("</tr>")
    }

    override fun appendTableCell(to: StringBuilder, body: () -> Unit) {
        to.appendln("<td>")
        body()
        to.appendln("</td>")
    }

    override fun formatLink(text: String, location: Location): String {
        return "<a href=\"${location.path}\">${text}</a>"
    }

    override fun formatLink(text: String, href: String): String {
        return "<a href=\"${href}\">${text}</a>"
    }

    override fun formatBold(text: String): String {
        return "<b>${text}</b>"
    }

    override fun formatCode(code: String): String {
        return "<code>${code.htmlEscape()}</code>"
    }

    override fun formatBreadcrumbs(items: Iterable<FormatLink>): String {
        return items.map { formatLink(it) }.joinToString("&nbsp;/&nbsp;")
    }


    override fun appendNodes(location: Location, to: StringBuilder, nodes: Iterable<DocumentationNode>) {
        templateService.appendHeader(to)
        super<StructuredFormatService>.appendNodes(location, to, nodes)
        templateService.appendFooter(to)
    }

    override fun appendOutlineChildren(to: StringBuilder, nodes: Iterable<DocumentationNode>) {
    }
    override fun appendOutlineHeader(to: StringBuilder, node: DocumentationNode) {
    }
}