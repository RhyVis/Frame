package rhx.frame.script.graph.node

import rhx.frame.init.GraphDict
import rhx.frame.init.TextDict
import rhx.frame.script.compose.TextCompose

// region Node

sealed class Node

data object EmptyNode : Node()

data class NameNode(
    val name: String,
) : Node()

data class ProgramNode(
    val name: String,
    val statements: List<Statement>,
) : Node(),
    Iterable<Statement> {
    override fun iterator(): Iterator<Statement> = statements.iterator()
}

// endregion

// region Statement

sealed class Statement : Node()

data class Reference(
    val name: String,
) : Statement() {
    val compose: TextCompose
        get() = TextDict[name] ?: error("TextCompose $name not found.")
}

data class CallGraphStatement(
    val name: String,
) : Statement() {
    val graph: ProgramNode
        get() = GraphDict[name] ?: error("Graph $name not found.")
}

data class JumpMark(
    val id: String,
) : Statement()

data class JumpStatement(
    val targetId: String,
    val condition: Expression,
) : Statement()

data class VariableDeclaration(
    val name: String,
    val type: VariableType,
    val expr: Expression?,
) : Statement()

data class VariableAssignment(
    val name: String,
    val expression: Expression,
) : Statement()

data class ObjectFieldAssignment(
    val target: Expression,
    val fieldName: String,
    val expression: Expression,
) : Statement()

data class ObjectTypeDeclaration(
    val name: String,
    val fields: List<VariableDeclaration>,
    val methods: List<FunctionDeclaration>,
) : Statement()

data class ConditionalStatement(
    val conditions: List<Pair<Expression, List<Statement>>>,
) : Statement(),
    Iterable<Pair<Expression, List<Statement>>> {
    override fun iterator(): Iterator<Pair<Expression, List<Statement>>> = conditions.iterator()
}

data class LoopStatement(
    val condition: Expression,
    val body: List<Statement>,
) : Statement(),
    Iterable<Statement> {
    override fun iterator(): Iterator<Statement> = body.iterator()
}

data object LoopBreakStatement : Statement()

data object LoopContinueStatement : Statement()

data class ReturnStatement(
    val expression: Expression?,
) : Statement()

data class FunctionDeclaration(
    val name: String,
    val parameters: List<String>,
    val returnType: VariableType?,
    val body: List<Statement>,
) : Statement(),
    Iterable<Statement> {
    override fun iterator(): Iterator<Statement> = body.iterator()
}

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

data class SystemCall(
    val name: String,
    val arguments: List<Expression>,
) : Statement(),
    Iterable<Expression> {
    override fun iterator(): Iterator<Expression> = arguments.iterator()
}

data class FunctionCall(
    val name: String,
    val arguments: List<Expression>,
) : Statement(),
    Iterable<Expression> {
    override fun iterator(): Iterator<Expression> = arguments.iterator()
}

data class ObjectMethodCall(
    val target: Expression,
    val methodName: String,
    val arguments: List<Expression>,
) : Statement(),
    Iterable<Expression> {
    override fun iterator(): Iterator<Expression> = arguments.iterator()
}

data object TerminateStatement : Statement()

// endregion

// region Expression

sealed class Expression : Node()

data class VariableReference(
    val name: String,
) : Expression()

data class IntegerLiteral(
    val value: Long,
) : Expression()

data class FloatLiteral(
    val value: Double,
) : Expression()

data class StringLiteral(
    val value: String,
) : Expression()

data class BoolLiteral(
    val value: Boolean,
) : Expression() {
    companion object {
        val TRUE = BoolLiteral(true)
        val FALSE = BoolLiteral(false)
    }
}

data object NullLiteral : Expression()

data class ObjectInstantiationExpression(
    val typeName: String,
    val fields: List<Expression>,
) : Expression(),
    Iterable<Expression> {
    override fun iterator(): Iterator<Expression> = fields.iterator()
}

data class SystemCallExpression(
    val name: String,
    val arguments: List<Expression>,
) : Expression(),
    Iterable<Expression> {
    override fun iterator(): Iterator<Expression> = arguments.iterator()
}

data class FunctionCallExpression(
    val name: String,
    val arguments: List<Expression>,
) : Expression(),
    Iterable<Expression> {
    override fun iterator(): Iterator<Expression> = arguments.iterator()
}

data class ObjectFieldAccessExpression(
    val target: Expression,
    val fieldName: String,
) : Expression()

data class ObjectMethodCallExpression(
    val target: Expression,
    val methodName: String,
    val arguments: List<Expression>,
) : Expression(),
    Iterable<Expression> {
    override fun iterator(): Iterator<Expression> = arguments.iterator()
}

data class BinaryOperation(
    val left: Expression,
    val operator: Operator,
    val right: Expression,
) : Expression()

data class UnaryOperation(
    val operator: SelfOperator,
    val expression: Expression,
) : Expression()

data class ParenthesizedExpression(
    val expression: Expression,
) : Expression()

// endregion
