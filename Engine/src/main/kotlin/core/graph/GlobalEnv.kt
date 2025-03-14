package rhx.frame.core.graph

import rhx.frame.core.graph.PredefinedType.toStringMethod
import rhx.frame.init.DataLoader
import rhx.frame.script.graph.FunctionDeclaration
import rhx.frame.script.graph.ObjectTypeDeclaration

/**
 * Global environment for graph execution, first level of environment hierarchy.
 * Used in file scope, but when a graph calls another graph, that file scope will be child of the caller's one.
 *
 * Additionally, it stores object type declarations accessible from any child scope.
 *
 * @see GraphEnv
 * @see DataLoader
 * @see ObjectTypeDeclaration
 */
object GlobalEnv : GraphEnv() {
    private val objectTypeMap =
        mutableMapOf(
            "Int32" to ObjectTypeDeclaration("Int32", emptyList(), listOf(toStringMethod)),
            "Int64" to ObjectTypeDeclaration("Int64", emptyList(), listOf(toStringMethod)),
            "Float" to ObjectTypeDeclaration("Float32", emptyList(), listOf(toStringMethod)),
            "Bool" to ObjectTypeDeclaration("Bool", emptyList(), listOf(toStringMethod)),
            "String" to ObjectTypeDeclaration("String", emptyList(), listOf(toStringMethod)),
            "Null" to ObjectTypeDeclaration("Null", emptyList(), listOf(toStringMethod)),
            "Void" to ObjectTypeDeclaration("Void", emptyList(), listOf(toStringMethod)),
        )

    private val predefinedTypes = arrayOf("Int32", "Int64", "Float", "Bool", "String", "Null", "Void")

    override fun getVariable(name: String): Value =
        localVarMap[name]
            ?: throw IllegalArgumentException("Variable $name not found")

    override fun setVariable(
        name: String,
        value: Value,
    ) {
        localVarMap[name] = value
    }

    override fun hasVariable(name: String): Boolean = localVarMap.containsKey(name)

    override fun getFunction(name: String): FunctionDeclaration =
        functionMap[name]
            ?: throw IllegalArgumentException("Function $name not found")

    override fun hasFunction(name: String): Boolean = functionMap.containsKey(name)

    override fun createChildEnv(): GraphEnv = throw IllegalStateException("Should not create child env for GlobalEnv")

    /**
     * Declare function directly in global scope
     *
     * @param name function name
     * @param function function declaration
     */
    fun declareGlobalFunction(
        name: String,
        function: FunctionDeclaration,
    ) {
        declareFunction(name, function)
    }

    /**
     * Declare object type directly in global scope
     *
     * @param name object type name
     * @param declaration object type declaration
     */
    fun declareObjectType(
        name: String,
        declaration: ObjectTypeDeclaration,
    ) {
        if (name in predefinedTypes) throw IllegalArgumentException("Cannot redefine predefined type $name")
        if (declaration.methods.isEmpty()) {
            objectTypeMap[name] =
                ObjectTypeDeclaration(
                    name = name,
                    fields = declaration.fields,
                    methods = listOf(toStringMethod),
                )
        } else {
            objectTypeMap[name] = declaration
        }
    }

    /**
     * Get object type by name
     *
     * @param name object type name
     * @return object type declaration
     */
    fun getObjectType(name: String): ObjectTypeDeclaration =
        objectTypeMap[name] ?: throw IllegalArgumentException("Object type $name not found")

    /**
     * Check if object type is declared
     *
     * @param name object type name
     * @return true if object type is declared
     */
    fun hasObjectType(name: String): Boolean = objectTypeMap.containsKey(name)

    /**
     * Delete all variables and functions
     * Used before loading new data
     *
     * @see DataLoader
     */
    fun clear() {
        localVarMap.clear()
        functionMap.clear()
        objectTypeMap.clear()
    }
}
