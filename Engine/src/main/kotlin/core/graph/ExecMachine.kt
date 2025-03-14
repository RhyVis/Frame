@file:Suppress("ktlint:standard:no-wildcard-imports")

package rhx.frame.core.graph

import io.github.oshai.kotlinlogging.KotlinLogging
import rhx.frame.core.JumpCalledException
import rhx.frame.core.JumpLimitExceededException
import rhx.frame.core.JumpTargetNotFoundException
import rhx.frame.core.ScriptException
import rhx.frame.interaction.Access
import rhx.frame.script.graph.BinaryOperation
import rhx.frame.script.graph.BoolLiteral
import rhx.frame.script.graph.CallGraphStatement
import rhx.frame.script.graph.ConditionalStatement
import rhx.frame.script.graph.Expression
import rhx.frame.script.graph.FloatLiteral
import rhx.frame.script.graph.FunctionCall
import rhx.frame.script.graph.FunctionCallExpression
import rhx.frame.script.graph.FunctionDeclaration
import rhx.frame.script.graph.GlobalFunctionDeclaration
import rhx.frame.script.graph.IntegerLiteral
import rhx.frame.script.graph.JumpMark
import rhx.frame.script.graph.JumpStatement
import rhx.frame.script.graph.LoopBreakStatement
import rhx.frame.script.graph.LoopContinueStatement
import rhx.frame.script.graph.LoopStatement
import rhx.frame.script.graph.NullLiteral
import rhx.frame.script.graph.ObjectFieldAccessExpression
import rhx.frame.script.graph.ObjectFieldAssignment
import rhx.frame.script.graph.ObjectInstantiationExpression
import rhx.frame.script.graph.ObjectMethodCall
import rhx.frame.script.graph.ObjectMethodCallExpression
import rhx.frame.script.graph.ObjectTypeDeclaration
import rhx.frame.script.graph.Operator
import rhx.frame.script.graph.ParenthesizedExpression
import rhx.frame.script.graph.Reference
import rhx.frame.script.graph.ReturnStatement
import rhx.frame.script.graph.SelfOperator
import rhx.frame.script.graph.Statement
import rhx.frame.script.graph.StringLiteral
import rhx.frame.script.graph.SystemCall
import rhx.frame.script.graph.SystemCallExpression
import rhx.frame.script.graph.TerminateStatement
import rhx.frame.script.graph.UnaryOperation
import rhx.frame.script.graph.VariableAssignment
import rhx.frame.script.graph.VariableDeclaration
import rhx.frame.script.graph.VariableReference
import rhx.frame.script.graph.VariableType

/**
 * Singleton object that executes a list of statements in a given scope.
 *
 * The object is also responsible for handling jumps, breaks, and continues.
 *
 * Use the [execScope] method to execute a list of statements in a given scope.
 *
 * And use [use] method to automatically reset the environment after the execution.
 *
 * @property env The current environment in which the statements are executed.
 *
 * @see GraphEnv
 * @see Statement
 * @see AutoCloseable
 */
object ExecMachine : AutoCloseable {
    private var env = GraphEnv.createTopLevelEnv()
    private var returnBuf: Value = Value.VOID
    private var isBreakRequested = false
    private var isContinueRequested = false
    private var jumpCounter = 0U

    /**
     * Maximum number of jumps allowed in a single [execScope] run.
     * If the limit is exceeded, a [JumpLimitExceededException] is thrown.
     *
     * Prevents infinite loops caused by jumps.
     */
    private const val MAX_JUMP_COUNT = 1000U

    /**
     * The terminal object to interaction with the user.
     */
    private val access by lazy { Access.instance }

    private val logger = KotlinLogging.logger("ExecMachine")

    init {
        registerInnerReflection()
    }

    /**
     * Execute a list of statements in a given scope.
     *
     * @param statements List of statements to execute.
     * @param scope Scope in which to execute the statements.
     *
     * @return The value returned inside the statements.
     */
    fun execScope(
        statements: List<Statement>,
        scope: GraphEnv = GraphEnv.createTopLevelEnv(),
    ): Value {
        val previousEnv = env
        val previousReturnBuf = returnBuf
        val previousJumpCounter = jumpCounter

        env = scope
        returnBuf = Value.VOID
        jumpCounter = 0U

        val jumpMarks =
            statements
                .withIndex()
                .filter { it.value is JumpMark }
                .associate { (it.value as JumpMark).id to it.index }

        try {
            var stmtIndex = 0
            while (stmtIndex < statements.size) {
                try {
                    execStatement(statements[stmtIndex])
                    if (returnBuf != Value.VOID || isBreakRequested) break
                    stmtIndex++
                } catch (e: JumpCalledException) {
                    jumpCounter++
                    if (jumpCounter > MAX_JUMP_COUNT) {
                        throw JumpLimitExceededException("Jump limit exceeded at $scope in $statements, line $stmtIndex")
                    }
                    stmtIndex = jumpMarks[e.targetId]
                        ?: throw JumpTargetNotFoundException("Jump mark ${e.targetId} not found")
                }
            }
            return returnBuf
        } catch (e: ScriptException) {
            logger.warn { "Exception was thrown by graph at $scope in $statements" }
            throw e
        } finally {
            env = previousEnv
            returnBuf = previousReturnBuf
            jumpCounter = previousJumpCounter
        }
    }

    private fun execBlock(statements: List<Statement>): Value = execScope(statements, env.createChildEnv())

    private fun execFunc(
        name: String,
        args: List<Value>,
    ): Value {
        val func = env.getFunction(name)

        if (args.size != func.parameters.size) {
            throw IllegalArgumentException("Function $name expects ${func.parameters.size} arguments, but got ${args.size}")
        }

        val funcEnv = env.createChildEnv()

        for ((param, arg) in func.parameters.zip(args)) {
            funcEnv.setVariable(param, arg)
        }

        val result = execScope(func.body, funcEnv)

        return if (result == Value.VoidValue) Value.NULL else result
    }

    private fun evalExpr(expr: Expression): Value =
        when (expr) {
            is VariableReference -> env.getVariable(expr.name)
            is IntegerLiteral -> Value.createInt64(expr.value)
            is FloatLiteral -> Value.createFloat(expr.value)
            is StringLiteral -> Value.createString(expr.value)
            is BoolLiteral -> Value.createBool(expr.value)
            is NullLiteral -> Value.NULL
            is ObjectInstantiationExpression -> objectInstantiation(expr)
            is SystemCallExpression -> SystemEnv.systemCall(expr.name, expr.map { evalExpr(it) })
            is FunctionCallExpression -> execFunc(expr.name, expr.map { evalExpr(it) })
            is BinaryOperation -> perfBinaryOpt(evalExpr(expr.left), expr.operator, evalExpr(expr.right))
            is UnaryOperation -> perfUnaryOpt(expr.operator, evalExpr(expr.expression))
            is ParenthesizedExpression -> evalExpr(expr.expression)
            is ObjectFieldAccessExpression -> objectFieldAccess(expr)
            is ObjectMethodCallExpression -> objectMethodInvoke(expr)
        }

    private fun perfBinaryOpt(
        left: Value,
        operator: Operator,
        right: Value,
    ): Value =
        when (operator) {
            Operator.ADD -> left + right
            Operator.SUBTRACT -> left - right
            Operator.MULTIPLY -> left * right
            Operator.DIVIDE -> left / right
            Operator.EQUALS -> Value.createBool(left == right)
            Operator.NOT_EQUALS -> Value.createBool(left != right)
            Operator.GREATER_THAN -> Value.createBool(left > right)
            Operator.LESS_THAN -> Value.createBool(left < right)
            Operator.GREATER_EQUALS -> Value.createBool(left >= right)
            Operator.LESS_EQUALS -> Value.createBool(left <= right)
            Operator.AND -> left.and(right)
            Operator.OR -> left.or(right)
            Operator.NOT -> left.and(!right)
        }

    private fun perfUnaryOpt(
        operator: SelfOperator,
        value: Value,
    ): Value =
        when (operator) {
            SelfOperator.NOT -> !value
            SelfOperator.NEGATE -> value.negate()
        }

    private fun execStatement(stmt: Statement) {
        when (stmt) {
            is JumpMark, is GlobalFunctionDeclaration, is ObjectTypeDeclaration -> {} // Pre handled
            is CallGraphStatement -> execScope(stmt.graph.statements, env.createChildEnv())
            is VariableDeclaration -> declareVariable(stmt)
            is VariableAssignment -> env.setVariable(stmt.name, stmt.expression.eval())
            is ObjectFieldAssignment -> objectFieldAssignment(stmt)
            is ConditionalStatement -> execConditional(stmt)
            is LoopStatement -> execLoop(stmt)
            is LoopBreakStatement -> isBreakRequested = true
            is LoopContinueStatement -> isContinueRequested = true
            is SystemCall -> SystemEnv.systemCall(stmt.name, stmt.map { it.eval() })
            is FunctionCall -> execFunc(stmt.name, stmt.map { it.eval() })
            is ObjectMethodCall -> objectMethodInvoke(stmt)
            is Reference -> callReference(stmt)
            is FunctionDeclaration -> env.declareFunction(stmt.name, stmt)
            is JumpStatement -> if (evalExpr(stmt.condition).isTrue) throw JumpCalledException(stmt.targetId)
            is ReturnStatement -> returnBuf = stmt.expression?.eval() ?: Value.NULL
            is TerminateStatement -> returnBuf = Value.NULL
        }
    }

    private fun execConditional(stmt: ConditionalStatement) {
        for ((cond, body) in stmt) {
            val result = evalExpr(cond)
            if (result.isTrue) {
                execBlock(body)
                break
            }
        }
    }

    private fun execLoop(stmt: LoopStatement) {
        isBreakRequested = false
        isContinueRequested = false

        val (cond, body) = stmt

        while (true) {
            val conditionResult = evalExpr(cond)
            if (!conditionResult.isTrue) break

            execBlock(body)

            if (returnBuf != Value.VOID) return

            if (isBreakRequested) {
                isBreakRequested = false
                break
            }

            if (isContinueRequested) {
                isContinueRequested = false
                continue
            }
        }
    }

    private fun declareVariable(stmt: VariableDeclaration) {
        if (stmt.expr != null) {
            env.declareVariable(stmt.name, stmt.type, evalExpr(stmt.expr))
        } else {
            env.declareVariable(stmt.name, stmt.type)
        }
    }

    private fun objectInstantiation(expr: ObjectInstantiationExpression): Value.ObjectValue {
        val objectType = GlobalEnv.getObjectType(expr.typeName)

        if (expr.fields.size != objectType.fields.size) {
            throw IllegalArgumentException(
                "Object type ${objectType.name} expects ${objectType.fields.size} fields, but got ${expr.fields.size}",
            )
        }

        val objectFieldValues =
            buildMap {
                objectType.fields.forEachIndexed { index, field ->
                    val fieldName = field.name
                    val fieldValue = evalExpr(expr.fields[index])
                    put(fieldName, fieldValue)
                }
            }.toMutableMap()

        return Value.ObjectValue(objectType.name, objectFieldValues)
    }

    private fun objectFieldAccess(expr: ObjectFieldAccessExpression): Value {
        val target = evalExpr(expr.target)

        if (target !is Value.ObjectValue) {
            throw IllegalArgumentException("Field access target ${expr.target} is not an object")
        }

        return target.values[expr.fieldName]
            ?: throw IllegalArgumentException("Field ${expr.fieldName} not found in object ${target.typeName}")
    }

    private fun objectFieldAssignment(stmt: ObjectFieldAssignment) {
        val target = stmt.target.eval()

        if (target !is Value.ObjectValue) {
            throw IllegalArgumentException("Field access target ${stmt.target} is not an object")
        }

        val value = stmt.expression.eval()

        target.values[stmt.fieldName] = value
    }

    private fun objectMethodInvoke(expr: ObjectMethodCallExpression): Value =
        handleObjectMethod(expr.target, expr.methodName, expr.arguments)

    private fun objectMethodInvoke(stmt: ObjectMethodCall) {
        handleObjectMethod(stmt.target, stmt.methodName, stmt.arguments)
    }

    private fun handleObjectMethod(
        tgt: Expression,
        methodName: String,
        arguments: List<Expression>,
    ): Value {
        val target = tgt.eval()

        val type = GlobalEnv.getObjectType(target.typeName)

        val method =
            type.methods.find { it.name == methodName }
                ?: throw IllegalArgumentException("Method $methodName not found in type ${target.typeName}")

        if (arguments.size != method.parameters.size) {
            throw IllegalArgumentException(
                "Method ${method.name} expects ${method.parameters.size} arguments, but got ${arguments.size}",
            )
        }

        val args = arguments.map { it.eval() }

        val methodEnv =
            env.createChildEnv().also {
                it.declareVariable("this", VariableType.OBJECT, target)
                for ((param, arg) in method.parameters.zip(args)) {
                    it.setVariable(param, arg)
                }
            }

        return execScope(method.body, methodEnv)
    }

    private fun callReference(stmt: Reference) {
        if (stmt.hasRef) {
            val p = stmt.getParagraph(env.getAllVariables())
            access.displayParagraph(p)
        } else {
            access.displayCompose(stmt.compose, env.getAllVariables())
        }
    }

    private fun registerInnerReflection() {
        SystemEnv.registerSystemCall("exec_func") { args ->
            if (args.size < 2) throw ScriptException("exec_func requires a function name and arguments")
            val name = args[0].toString()
            val arguments = args.subList(1, args.size)
            execFunc(name, arguments)
        }
    }

    private fun reset() {
        env = GraphEnv.createTopLevelEnv()
        returnBuf = Value.VOID
        isBreakRequested = false
        isContinueRequested = false
    }

    // Shortcut for evalExpr
    private fun Expression.eval(): Value = evalExpr(this)

    override fun close() = reset()
}
