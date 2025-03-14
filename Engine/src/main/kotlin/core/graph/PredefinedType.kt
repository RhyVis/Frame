package rhx.frame.core.graph

import rhx.frame.script.graph.FunctionDeclaration
import rhx.frame.script.graph.ReturnStatement
import rhx.frame.script.graph.SystemCallExpression
import rhx.frame.script.graph.VariableReference
import rhx.frame.script.graph.VariableType

object PredefinedType {
    val toStringMethod =
        FunctionDeclaration(
            name = "to_string",
            parameters = emptyList(),
            returnType = VariableType.STRING,
            body =
                listOf(
                    ReturnStatement(
                        expression =
                            SystemCallExpression(
                                name = "to_string",
                                arguments =
                                    listOf(
                                        VariableReference("this"),
                                    ),
                            ),
                    ),
                ),
        )
}
