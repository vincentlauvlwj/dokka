package org.jetbrains.dokka

import org.jetbrains.dokka.DocumentationNode.*

/**
 * Implements [LanguageService] and provides rendering of symbols in Kotlin language
 */
class KotlinLanguageService : LanguageService {
    override fun render(node: DocumentationNode): ContentNode {
        return content {
            when (node.kind) {
                Kind.Package -> renderPackage(node)
                Kind.Class,
                Kind.Interface,
                Kind.Enum,
                Kind.EnumItem,
                Kind.Object -> renderClass(node)

                Kind.TypeParameter -> renderTypeParameter(node)
                Kind.Type,
                Kind.UpperBound -> renderType(node)

                Kind.Modifier -> renderModifier(node)
                Kind.Constructor,
                Kind.Function -> renderFunction(node)
                Kind.Property -> renderProperty(node)
                else -> ContentText("${node.kind}: ${node.name}")
            }
        }
    }

    override fun renderName(node: DocumentationNode): String {
        return when (node.kind) {
            Kind.Constructor -> node.owner!!.name
            else -> node.name
        }
    }

    private fun ContentNode.renderPackage(node: DocumentationNode) {
        keyword("package")
        text(" ")
        identifier(node.name)
    }

    private fun ContentNode.renderList(nodes: List<DocumentationNode>, separator: String = ", ", renderItem: (DocumentationNode) -> Unit) {
        if (nodes.none())
            return
        renderItem(nodes.first())
        nodes.drop(1).forEach {
            symbol(separator)
            renderItem(it)
        }
    }

    private fun ContentNode.renderLinked(node: DocumentationNode, body: ContentNode.(DocumentationNode)->Unit) {
        val to = node.links.firstOrNull()
        if (to == null)
            body(node)
        else
            link(to) {
                body(node)
            }
    }

    private fun ContentNode.renderType(node: DocumentationNode) {
        val typeArguments = node.details(Kind.Type)
        if (node.name == "Function${typeArguments.count() - 1}") {
            // lambda
            symbol("(")
            renderList(typeArguments.take(typeArguments.size - 1)) {
                renderType(it)
            }
            symbol(")")
            text(" ")
            symbol("->")
            text(" ")
            renderType(typeArguments.last())
            return
        }
        if (node.name == "ExtensionFunction${typeArguments.count() - 2}") {
            // extension lambda
            renderType(typeArguments.first())
            symbol(".")
            symbol("(")
            renderList(typeArguments.drop(1).take(typeArguments.size - 2)) {
                renderType(it)
            }
            symbol(")")
            text(" ")
            symbol("->")
            text(" ")
            renderType(typeArguments.last())
            return
        }
        renderLinked(node) { identifier(it.name) }
        if (typeArguments.any()) {
            symbol("<")
            renderList(typeArguments) {
                renderType(it)
            }
            symbol(">")
        }
    }

    private fun ContentNode.renderModifier(node: DocumentationNode) {
        when (node.name) {
            "final", "internal" -> {
            }
            else -> keyword(node.name)
        }
    }

    private fun ContentNode.renderTypeParameter(node: DocumentationNode) {
        val constraints = node.details(Kind.UpperBound)
        identifier(node.name)
        if (constraints.any()) {
            symbol(" : ")
            renderList(constraints) {
                renderType(it)
            }
        }
    }

    private fun ContentNode.renderParameter(node: DocumentationNode) {
        identifier(node.name)
        symbol(": ")
        val parameterType = node.detail(Kind.Type)
        renderType(parameterType)
    }

    private fun ContentNode.renderTypeParametersForNode(node: DocumentationNode) {
        val typeParameters = node.details(Kind.TypeParameter)
        if (typeParameters.any()) {
            symbol("<")
            renderList(typeParameters) {
                renderType(it)
            }
            symbol("> ")
        }
    }

    private fun ContentNode.renderSupertypesForNode(node: DocumentationNode) {
        val supertypes = node.details(Kind.Supertype)
        if (supertypes.any()) {
            symbol(" : ")
            renderList(supertypes) {
                renderType(it)
            }
        }
    }

    private fun ContentNode.renderModifiersForNode(node: DocumentationNode) {
        val modifiers = node.details(Kind.Modifier)
        for (it in modifiers) {
            if (node.kind == Kind.Interface && it.name == "abstract")
                continue
            renderModifier(it)
            text(" ")
        }
    }

    private fun ContentNode.renderClass(node: DocumentationNode) {
        renderModifiersForNode(node)
        when (node.kind) {
            Kind.Class -> keyword("class ")
            Kind.Interface -> keyword("trait ")
            Kind.Enum -> keyword("enum class ")
            Kind.EnumItem -> keyword("enum val ")
            Kind.Object -> keyword("object ")
            else -> throw IllegalArgumentException("Node $node is not a class-like object")
        }

        identifier(node.name)
        renderTypeParametersForNode(node)
        renderSupertypesForNode(node)
    }

    private fun ContentNode.renderFunction(node: DocumentationNode) {
        renderModifiersForNode(node)
        when (node.kind) {
            Kind.Constructor -> identifier(node.owner!!.name)
            Kind.Function -> keyword("fun ")
            else -> throw IllegalArgumentException("Node $node is not a function-like object")
        }
        renderTypeParametersForNode(node)
        val receiver = node.details(Kind.Receiver).singleOrNull()
        if (receiver != null) {
            renderType(receiver.detail(Kind.Type))
            symbol(".")
        }

        if (node.kind != Kind.Constructor)
            identifier(node.name)

        symbol("(")
        renderList(node.details(Kind.Parameter)) {
            renderParameter(it)
        }
        symbol(")")
        if (node.kind != Kind.Constructor) {
            symbol(": ")
            renderType(node.detail(Kind.Type))
        }
    }

    private fun ContentNode.renderProperty(node: DocumentationNode) {
        renderModifiersForNode(node)
        when (node.kind) {
            Kind.Property -> keyword("val ")
            else -> throw IllegalArgumentException("Node $node is not a property")
        }
        renderTypeParametersForNode(node)
        val receiver = node.details(Kind.Receiver).singleOrNull()
        if (receiver != null) {
            renderType(receiver.detail(Kind.Type))
            symbol(".")
        }

        identifier(node.name)
        symbol(": ")
        renderType(node.detail(Kind.Type))
    }
}