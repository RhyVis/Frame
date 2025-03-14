@file:Suppress("ktlint:standard:no-wildcard-imports")

package rhx.frame.script.graph

import rhx.frame.antlr.graphBaseVisitor
import rhx.frame.antlr.graphParser.*

class GraphNodeBuilder : graphBaseVisitor<Node>() {
    override fun defaultResult(): Node = EmptyNode

    override fun visitProgram(ctx: ProgramContext): Node {
        val name =
            ctx
                .name()
                .ID()
                .text
                .trim()
        val statements = ctx.statement().mapNotNull { visit(it) as? Statement }
        return ProgramNode(name, statements)
    }

    override fun visitName(ctx: NameContext): Node = NameNode(ctx.ID().text)

    override fun visitReference(ctx: ReferenceContext): Node =
        if (ctx.referenceArg() != null) {
            Reference(ctx.ID().text, visit(ctx.referenceArg()!!) as ReferenceArg)
        } else {
            Reference(ctx.ID().text)
        }

    override fun visitReferenceArg(ctx: ReferenceArgContext): Node =
        when {
            ctx.INT32() != null -> ReferenceArg(refId = ctx.INT32()!!.text.toInt())
            ctx.STRING() != null -> {
                val textWrap = ctx.STRING()!!.text
                ReferenceArg(refName = textWrap.substring(1, textWrap.length - 1))
            }
            else -> throw IllegalArgumentException("Unknown reference argument: ${ctx.text}")
        }

    override fun visitCallGraph(ctx: CallGraphContext): Node = CallGraphStatement(ctx.ID().text)

    override fun visitJumpMark(ctx: JumpMarkContext): Node = JumpMark(ctx.ID().text)

    override fun visitJumpStatement(ctx: JumpStatementContext): Node = JumpStatement(ctx.ID().text, visit(ctx.expression()) as Expression)

    override fun visitObjectDeclaration(ctx: ObjectDeclarationContext): Node {
        val name = ctx.ID().text
        val fields = ctx.variableDeclaration().mapNotNull { visit(it) as? VariableDeclaration }
        val methods = ctx.functionDeclaration().mapNotNull { visit(it) as? FunctionDeclaration }
        return ObjectTypeDeclaration(name, fields, methods)
    }

    override fun visitConditionalStatement(ctx: ConditionalStatementContext): Node {
        val conditions =
            ctx.expression().mapIndexed { index, exprCtx ->
                val expr = visit(exprCtx) as Expression
                val statements = ctx.codeBlock(index)?.statement()?.mapNotNull { visit(it) as? Statement } ?: emptyList()
                expr to statements
            }
        return ConditionalStatement(conditions)
    }

    override fun visitLoopStatement(ctx: LoopStatementContext): Node {
        val condition = visit(ctx.expression()) as Expression
        val statements = ctx.codeBlock().statement().mapNotNull { visit(it) as? Statement }
        return LoopStatement(condition, statements)
    }

    override fun visitLoopBreakStatement(ctx: LoopBreakStatementContext): Node = LoopBreakStatement

    override fun visitLoopContinueStatement(ctx: LoopContinueStatementContext): Node = LoopContinueStatement

    override fun visitReturnStatement(ctx: ReturnStatementContext): Node {
        val exp = ctx.expression()?.let { visit(it) as Expression }
        return ReturnStatement(exp)
    }

    override fun visitVariableDeclaration(ctx: VariableDeclarationContext): Node {
        val varName = ctx.ID().text
        val type = mapType(ctx.TYPE().text) ?: throw IllegalArgumentException("Unknown type: ${ctx.TYPE().text}")
        val initialValExpr = ctx.expression()?.let { visit(it) as Expression }
        return VariableDeclaration(varName, type, initialValExpr)
    }

    override fun visitVariableAssignment(ctx: VariableAssignmentContext): Node {
        val fieldAccess = visit(ctx.fieldAccess())

        if (ctx.INCREMENT() != null || ctx.DECREMENT() != null) {
            val operator = if (ctx.INCREMENT() != null) SelfOperator.INCREMENT else SelfOperator.DECREMENT

            return when (fieldAccess) {
                is VariableReference -> VariableSelfOperation(fieldAccess.name, operator)
                is ObjectFieldAccessExpression -> ObjectFieldSelfOperation(fieldAccess.target, fieldAccess.fieldName, operator)
                else -> throw IllegalArgumentException("Unknown field access type $fieldAccess")
            }
        }

        val expression = visit(ctx.expression()!!) as Expression
        val operator =
            when {
                ctx.PLUS_EQ() != null -> Operator.PLUS_ASSIGN
                ctx.MINUS_EQ() != null -> Operator.MINUS_ASSIGN
                ctx.MULT_EQ() != null -> Operator.MULTIPLY_ASSIGN
                ctx.DIV_EQ() != null -> Operator.DIVIDE_ASSIGN
                ctx.MOD_EQ() != null -> Operator.MODULO_ASSIGN
                else -> null
            }

        return when (fieldAccess) {
            is VariableReference -> {
                if (operator != null) {
                    VariableCompoundAssignment(fieldAccess.name, operator, expression)
                } else {
                    VariableAssignment(fieldAccess.name, expression)
                }
            }
            is ObjectFieldAccessExpression -> {
                if (operator != null) {
                    ObjectFieldCompoundAssignment(fieldAccess.target, fieldAccess.fieldName, operator, expression)
                } else {
                    ObjectFieldAssignment(fieldAccess.target, fieldAccess.fieldName, expression)
                }
            }
            else -> throw IllegalArgumentException("Unknown field access type $fieldAccess")
        }
    }

    override fun visitFieldAccess(ctx: FieldAccessContext): Node {
        val ids = ctx.ID().map { it.text }
        return if (ids.size == 1) {
            VariableReference(ids[0])
        } else {
            ObjectFieldAccessExpression(VariableReference(ids[0]), ids[1])
        }
    }

    override fun visitFunctionDeclaration(ctx: FunctionDeclarationContext): Node {
        val name = ctx.ID().text
        val params = ctx.paramList()?.ID()?.map { it.text } ?: emptyList()
        val returnType = mapType(ctx.TYPE()?.text)
        val body = ctx.codeBlock().statement().map { visit(it) as Statement }
        return FunctionDeclaration(name, params, returnType, body)
    }

    override fun visitGlobalFunctionDeclaration(ctx: GlobalFunctionDeclarationContext): Node {
        val name = ctx.ID().text
        val params = ctx.paramList()?.ID()?.map { it.text } ?: emptyList()
        val returnType = mapType(ctx.TYPE()?.text)
        val body = ctx.codeBlock().statement().map { visit(it) as Statement }
        return GlobalFunctionDeclaration(name, params, returnType, body)
    }

    private fun mapType(type: String?) =
        when (type) {
            "int32" -> VariableType.INT32
            "int64" -> VariableType.INT64
            "float" -> VariableType.FLOAT
            "bool" -> VariableType.BOOL
            "string" -> VariableType.STRING
            "object" -> VariableType.OBJECT
            "any" -> VariableType.ANY
            else -> null
        }

    override fun visitSystemCall(ctx: SystemCallContext): Node {
        val name = ctx.ID().text
        val args = ctx.argumentList()?.expression()?.map { visit(it) as Expression } ?: emptyList()
        return SystemCall(name, args)
    }

    override fun visitFunctionCall(ctx: FunctionCallContext): Node {
        val name = ctx.ID().text
        val exp = ctx.argumentList()?.expression()?.map { visit(it) as Expression } ?: emptyList()
        return FunctionCall(name, exp)
    }

    override fun visitObjectMethodCall(ctx: ObjectMethodCallContext): Node {
        val target = VariableReference(ctx.ID(0)!!.text)
        val methodName = ctx.ID(1)!!.text
        val args = ctx.argumentList()?.expression()?.map { visit(it) as Expression } ?: emptyList()
        return ObjectMethodCall(target, methodName, args)
    }

    override fun visitExpression(ctx: ExpressionContext): Node {
        return when {
            ctx.literal() != null -> visit(ctx.literal()!!)
            ctx.ID() != null -> VariableReference(ctx.ID()!!.text)
            ctx.objectAccessExpr() != null -> visit(ctx.objectAccessExpr()!!)
            ctx.objectInstantiationExpr() != null -> visit(ctx.objectInstantiationExpr()!!)
            ctx.systemCallExpr() != null -> visit(ctx.systemCallExpr()!!)
            ctx.functionCallExpr() != null -> visit(ctx.functionCallExpr()!!)
            ctx.childCount == 3 &&
                ctx
                    .getChild(
                        0,
                    )?.text == "(" &&
                ctx.getChild(2)?.text == ")" -> ParenthesizedExpression(visit(ctx.expression(0)!!) as Expression)
            ctx.childCount == 2 -> {
                val expr = visit(ctx.expression(0)!!) as Expression
                when (ctx.getChild(0)?.text) {
                    "!" -> UnaryOperation(SelfOperator.NOT, expr)
                    "-" -> UnaryOperation(SelfOperator.NEGATE, expr)
                    else -> throw IllegalArgumentException("Unknown unary operator: ${ctx.getChild(0)?.text}")
                }
            }
            ctx.childCount == 3 && ctx.expression().size == 2 -> {
                val left = visit(ctx.expression(0)!!) as Expression
                val right = visit(ctx.expression(1)!!) as Expression
                val operator =
                    when (ctx.getChild(1)?.text) {
                        "+" -> Operator.ADD
                        "-" -> Operator.SUBTRACT
                        "*" -> Operator.MULTIPLY
                        "/" -> Operator.DIVIDE
                        "%" -> Operator.MODULO
                        "==" -> Operator.EQUALS
                        "!=" -> Operator.NOT_EQUALS
                        ">" -> Operator.GREATER_THAN
                        "<" -> Operator.LESS_THAN
                        ">=" -> Operator.GREATER_EQUALS
                        "<=" -> Operator.LESS_EQUALS
                        "&&" -> Operator.AND
                        "||" -> Operator.OR
                        else -> throw IllegalArgumentException("Unknown binary operator: ${ctx.getChild(1)?.text}")
                    }
                return BinaryOperation(left, operator, right)
            }

            else -> throw IllegalArgumentException("Unknown expression: ${ctx.text}")
        }
    }

    override fun visitLiteral(ctx: LiteralContext): Node =
        when {
            ctx.INT32() != null -> Int32Literal(ctx.INT32()!!.text.toInt())
            ctx.INT64() != null -> Int64Literal(ctx.INT64()!!.text.toLong())
            ctx.FLOAT() != null -> FloatLiteral(ctx.FLOAT()!!.text.toDouble())
            ctx.STRING() != null -> StringLiteral.create(ctx.STRING()!!.text)
            ctx.text == "null" -> NullLiteral
            ctx.text == "true" -> BoolLiteral.TRUE
            ctx.text == "false" -> BoolLiteral.FALSE
            else -> throw IllegalArgumentException("Unknown literal: ${ctx.text}")
        }

    override fun visitObjectAccessExpr(ctx: ObjectAccessExprContext): Node {
        val target = VariableReference(ctx.ID(0)!!.text)
        val name = ctx.ID(1)!!.text
        if (ctx.childCount == 3) {
            return ObjectFieldAccessExpression(target, name)
        } else {
            val args = ctx.argumentList()?.expression()?.map { visit(it) as Expression } ?: emptyList()
            return ObjectMethodCallExpression(target, name, args)
        }
    }

    override fun visitObjectInstantiationExpr(ctx: ObjectInstantiationExprContext): Node {
        val typeName = ctx.ID().text
        val args = ctx.argumentList()?.expression()?.map { visit(it) as Expression } ?: emptyList()
        return ObjectInstantiationExpression(typeName, args)
    }

    override fun visitSystemCallExpr(ctx: SystemCallExprContext): Node {
        val name = ctx.ID().text
        val args = ctx.argumentList()?.expression()?.map { visit(it) as Expression } ?: emptyList()
        return SystemCallExpression(name, args)
    }

    override fun visitFunctionCallExpr(ctx: FunctionCallExprContext): Node {
        val name = ctx.ID().text
        val args = ctx.argumentList()?.expression()?.map { visit(it) as Expression } ?: emptyList()
        return FunctionCallExpression(name, args)
    }

    override fun visitTerminateStatement(ctx: TerminateStatementContext): Node = TerminateStatement
}
