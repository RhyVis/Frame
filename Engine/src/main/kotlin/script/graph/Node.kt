package rhx.frame.script.graph

import kotlinx.serialization.Serializable
import rhx.frame.core.GraphDict
import rhx.frame.core.TextDict
import rhx.frame.core.graph.Value
import rhx.frame.script.compose.Compose
import rhx.frame.script.compose.MappedParagraph

// region Node

@Serializable
sealed class Node

@Serializable
data object EmptyNode : Node()

@Serializable
data class NameNode(
    val name: String,
) : Node()

@Serializable
data class ProgramNode(
    val name: String,
    val statements: List<Statement>,
) : Node(),
    Iterable<Statement> {
    override fun iterator(): Iterator<Statement> = statements.iterator()
}

// endregion

// region Statement

@Serializable
sealed class Statement : Node()

@Serializable
data class Reference(
    val name: String,
    val refName: String? = null,
    val refId: Int? = null,
) : Statement() {
    constructor(name: String, arg: ReferenceArg) : this(name, arg.refName, arg.refId)

    val compose: Compose
        get() = TextDict[name] ?: error("TextCompose $name not found.")

    val hasRef: Boolean
        get() = refName != null || refId != null

    fun getParagraph(values: Map<String, Value>): MappedParagraph =
        if (hasRef) {
            when {
                refId != null -> compose.getParagraph(refId, values)
                refName != null -> compose.getParagraph(refName, values)
                else -> throw IllegalStateException("Reference $name has no refName or refId")
            }
        } else {
            MappedParagraph.EMPTY
        }
}

@Serializable
data class ReferenceArg(
    val refName: String? = null,
    val refId: Int? = null,
) : Node()

@Serializable
data class CallGraphStatement(
    val name: String,
) : Statement() {
    val graph: ProgramNode
        get() = GraphDict[name] ?: error("Graph $name not found.")
}

@Serializable
data class JumpMark(
    val id: String,
) : Statement()

@Serializable
data class JumpStatement(
    val targetId: String,
    val condition: Expression,
) : Statement()

@Serializable
data class VariableDeclaration(
    val name: String,
    val type: VariableType,
    val expr: Expression?,
) : Statement()

@Serializable
data class VariableCompoundAssignment(
    val name: String,
    val operator: Operator,
    val expression: Expression,
) : Statement()

@Serializable
data class VariableAssignment(
    val name: String,
    val expression: Expression,
) : Statement()

@Serializable
data class VariableSelfOperation(
    val name: String,
    val operator: SelfOperator,
) : Statement()

@Serializable
data class ObjectFieldSelfOperation(
    val target: Expression,
    val fieldName: String,
    val operator: SelfOperator,
) : Statement()

data class ObjectFieldCompoundAssignment(
    val target: Expression,
    val fieldName: String,
    val operator: Operator,
    val expression: Expression,
) : Statement()

@Serializable
data class ObjectFieldAssignment(
    val target: Expression,
    val fieldName: String,
    val expression: Expression,
) : Statement()

@Serializable
data class ObjectTypeDeclaration(
    val name: String,
    val fields: List<VariableDeclaration>,
    val methods: List<FunctionDeclaration>,
) : Statement()

@Serializable
data class ConditionalStatement(
    val conditions: List<Pair<Expression, List<Statement>>>,
) : Statement(),
    Iterable<Pair<Expression, List<Statement>>> {
    override fun iterator(): Iterator<Pair<Expression, List<Statement>>> = conditions.iterator()
}

@Serializable
data class LoopStatement(
    val condition: Expression,
    val body: List<Statement>,
) : Statement(),
    Iterable<Statement> {
    override fun iterator(): Iterator<Statement> = body.iterator()
}

@Serializable
data object LoopBreakStatement : Statement()

@Serializable
data object LoopContinueStatement : Statement()

@Serializable
data class ReturnStatement(
    val expression: Expression?,
) : Statement()

@Serializable
data class FunctionDeclaration(
    val name: String,
    val parameters: List<String>,
    val returnType: VariableType?,
    val body: List<Statement>,
) : Statement(),
    Iterable<Statement> {
    override fun iterator(): Iterator<Statement> = body.iterator()
}

@Serializable
data class GlobalFunctionDeclaration(
    val name: String,
    val parameters: List<String>,
    val returnType: VariableType?,
    val body: List<Statement>,
) : Statement(),
    Iterable<Statement> {
    override fun iterator(): Iterator<Statement> = body.iterator()

    fun toFunctionDeclaration() = FunctionDeclaration(name, parameters, returnType, body)
}

@Serializable
data class SystemCall(
    val name: String,
    val arguments: List<Expression>,
) : Statement(),
    Iterable<Expression> {
    override fun iterator(): Iterator<Expression> = arguments.iterator()
}

@Serializable
data class FunctionCall(
    val name: String,
    val arguments: List<Expression>,
) : Statement(),
    Iterable<Expression> {
    override fun iterator(): Iterator<Expression> = arguments.iterator()
}

@Serializable
data class ObjectMethodCall(
    val target: Expression,
    val methodName: String,
    val arguments: List<Expression>,
) : Statement(),
    Iterable<Expression> {
    override fun iterator(): Iterator<Expression> = arguments.iterator()
}

@Serializable
data object TerminateStatement : Statement()

// endregion

// region Expression

@Serializable
sealed class Expression : Node()

@Serializable
data class VariableReference(
    val name: String,
) : Expression()

@Serializable
data class Int32Literal(
    val value: Int,
) : Expression()

@Serializable
data class Int64Literal(
    val value: Long,
) : Expression()

@Serializable
data class FloatLiteral(
    val value: Double,
) : Expression()

@Serializable
data class StringLiteral(
    val value: String,
) : Expression() {
    companion object {
        private val EMPTY = StringLiteral("")

        fun create(value: String): StringLiteral {
            val content = value.substring(1, value.length - 1)
            return if (content.isEmpty()) {
                EMPTY
            } else {
                StringLiteral(content)
            }
        }
    }
}

@Serializable
data class BoolLiteral(
    val value: Boolean,
) : Expression() {
    companion object {
        val TRUE = BoolLiteral(true)
        val FALSE = BoolLiteral(false)
    }
}

@Serializable
data object NullLiteral : Expression()

@Serializable
data class ObjectInstantiationExpression(
    val typeName: String,
    val fields: List<Expression>,
) : Expression(),
    Iterable<Expression> {
    override fun iterator(): Iterator<Expression> = fields.iterator()
}

@Serializable
data class SystemCallExpression(
    val name: String,
    val arguments: List<Expression>,
) : Expression(),
    Iterable<Expression> {
    override fun iterator(): Iterator<Expression> = arguments.iterator()
}

@Serializable
data class FunctionCallExpression(
    val name: String,
    val arguments: List<Expression>,
) : Expression(),
    Iterable<Expression> {
    override fun iterator(): Iterator<Expression> = arguments.iterator()
}

@Serializable
data class ObjectFieldAccessExpression(
    val target: Expression,
    val fieldName: String,
) : Expression()

@Serializable
data class ObjectMethodCallExpression(
    val target: Expression,
    val methodName: String,
    val arguments: List<Expression>,
) : Expression(),
    Iterable<Expression> {
    override fun iterator(): Iterator<Expression> = arguments.iterator()
}

@Serializable
data class BinaryOperation(
    val left: Expression,
    val operator: Operator,
    val right: Expression,
) : Expression()

@Serializable
data class UnaryOperation(
    val operator: SelfOperator,
    val expression: Expression,
) : Expression()

@Serializable
data class ParenthesizedExpression(
    val expression: Expression,
) : Expression()

// endregion
