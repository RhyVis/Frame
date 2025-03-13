package rhx.frame.core.graph

import rhx.frame.script.graph.node.FunctionDeclaration
import rhx.frame.script.graph.node.ReturnStatement
import rhx.frame.script.graph.node.SystemCallExpression
import rhx.frame.script.graph.node.VariableReference
import rhx.frame.script.graph.node.VariableType

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
