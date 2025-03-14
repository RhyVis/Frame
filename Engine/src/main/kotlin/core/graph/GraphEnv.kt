package rhx.frame.core.graph

import io.github.oshai.kotlinlogging.KotlinLogging
import rhx.frame.annotation.OpenClass
import rhx.frame.script.graph.FunctionDeclaration
import rhx.frame.script.graph.VariableType

/**
 * Environment for graph execution, contains variables and functions.
 * Support layered structure with parent environment.
 *
 * The top-level environment is GlobalEnv.
 */
@OpenClass
class GraphEnv(
    private val parent: GraphEnv? = null,
) {
    val localVarMap = mutableMapOf<String, Value>()
    val functionMap = mutableMapOf<String, FunctionDeclaration>()

    /**
     * Get variable from current scope or parent scope
     *
     * @param name variable name
     * @return variable value
     *
     * @throws IllegalArgumentException if variable not found
     */
    fun getVariable(name: String): Value =
        localVarMap[name]
            ?: parent?.getVariable(name)
            ?: throw IllegalArgumentException("Variable $name not found")

    /**
     * Set variable in current scope or parent scope
     *
     * @param name variable name
     * @param value variable value
     */
    fun setVariable(
        name: String,
        value: Value,
    ) {
        if (localVarMap.containsKey(name)) {
            localVarMap[name] = value
            return
        }

        if (parent?.hasVariable(name) == true) {
            parent.setVariable(name, value)
            return
        }

        localVarMap[name] = value
    }

    fun declareVariable(
        name: String,
        type: VariableType,
        initialValue: Value? = null,
    ) {
        if (localVarMap.containsKey(name)) {
            logger.warn { "$name variable shadowed by new declaration" }
        }
        localVarMap[name] = initialValue ?: type.defaultValue()
    }

    /**
     * Check if variable exists in current scope or parent scope
     *
     * @param name variable name
     * @return true if variable exists
     */
    fun hasVariable(name: String): Boolean =
        localVarMap.containsKey(name) ||
            parent?.hasVariable(name) == true

    /**
     * Returns a map of all variables in the environment chain,
     * with child variables overriding parent variables.
     *
     * @return Combined map of all variables in the environment hierarchy
     */
    fun getAllVariables(): Map<String, Value> {
        val result = mutableMapOf<String, Value>()

        parent?.getAllVariables()?.let {
            result.putAll(it)
        }

        result.putAll(localVarMap)

        return result
    }

    /**
     * Get function from current scope or parent scope
     *
     * @param name function name
     * @return function declaration
     *
     * @throws IllegalArgumentException if function not found
     */
    fun getFunction(name: String): FunctionDeclaration =
        functionMap[name]
            ?: parent?.getFunction(name)
            ?: throw IllegalArgumentException("Function $name not found")

    /**
     * Declare function in current scope
     *
     * @param name function name
     * @param function function declaration
     */
    fun declareFunction(
        name: String,
        function: FunctionDeclaration,
    ) {
        functionMap[name] = function
    }

    /**
     * Check if function exists in current scope or parent scope
     *
     * @param name function name
     * @return true if function exists
     */
    fun hasFunction(name: String): Boolean =
        functionMap.containsKey(name) ||
            parent?.hasFunction(name) == true

    /**
     * Create child environment.
     * For base environment creation, use [createChildEnv] instead.
     *
     * @return new child environment
     */
    fun createChildEnv(): GraphEnv = GraphEnv(this)

    companion object {
        /**
         * Create top-level environment with GlobalEnv as parent.
         *
         * Used in file-level scope.
         */
        fun createTopLevelEnv(): GraphEnv = GraphEnv(GlobalEnv)

        private val logger = KotlinLogging.logger("GraphEnv")
    }
}
