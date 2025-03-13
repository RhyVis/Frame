package rhx.frame.core.graph

/**
 * Supported value types in graph script
 *
 * - Int32Value: Integer value
 * - Int64Value: Long Integer value
 * - FloatValue: Actually Double
 * - BoolValue: Boolean
 * - StringValue: String value
 * - ObjectValue: Object value, with fields
 * - NullValue: Null
 * - VoidValue: Void
 *
 * Most binary operations are supported for IntValue and FloatValue, including implicit conversions, which means,
 * integer may covert to 'double' when needed.
 */
sealed class Value {
    data class Int32Value(
        val value: Int,
    ) : Value() {
        override fun equals(other: Any?): Boolean =
            when (other) {
                is Int32Value -> value == other.value
                is Int64Value -> value.toLong() == other.value
                is FloatValue -> value.toDouble() == other.value
                else -> false
            }

        override fun hashCode(): Int = value.hashCode()

        override fun toString(): String = value.toString()

        override val isNumber: Boolean = true

        override val typeName = "Int32"
    }

    data class Int64Value(
        val value: Long,
    ) : Value() {
        override fun equals(other: Any?): Boolean =
            when (other) {
                is Int32Value -> value == other.value.toLong()
                is Int64Value -> value == other.value
                is FloatValue -> value.toDouble() == other.value
                else -> false
            }

        override fun hashCode(): Int = value.hashCode()

        override fun toString(): String = value.toString()

        override val isNumber: Boolean = true

        override val typeName = "Int64"
    }

    data class FloatValue(
        val value: Double,
    ) : Value() {
        override fun equals(other: Any?): Boolean =
            when (other) {
                is Int32Value -> value == other.value.toDouble()
                is Int64Value -> value == other.value.toDouble()
                is FloatValue -> value - other.value < 0.00001
                else -> false
            }

        override fun hashCode(): Int = value.hashCode()

        override fun toString(): String = value.toString()

        override val isNumber: Boolean = true

        override val typeName: String = "Float"
    }

    data class BoolValue(
        val value: Boolean,
    ) : Value() {
        override fun equals(other: Any?): Boolean =
            when (other) {
                is BoolValue -> value == other.value
                is Boolean -> value == other
                else -> false
            }

        override fun hashCode(): Int = value.hashCode()

        override fun toString(): String = value.toString()

        override val isNumber: Boolean = false

        override val typeName: String = "Bool"
    }

    data class StringValue(
        val value: String,
    ) : Value() {
        override fun equals(other: Any?): Boolean =
            when (other) {
                is StringValue -> value == other.value
                else -> false
            }

        override fun hashCode(): Int = value.hashCode()

        override fun toString(): String = value

        override val isNumber: Boolean = false

        override val typeName: String = "String"
    }

    data class ObjectValue(
        override val typeName: String,
        val values: MutableMap<String, Value>,
    ) : Value() {
        override fun equals(other: Any?): Boolean =
            when (other) {
                is ObjectValue -> values == other.values
                else -> false
            }

        override fun hashCode(): Int = values.hashCode()

        override fun toString(): String = values.toString()

        override val isNumber: Boolean = false
    }

    data object NullValue : Value() {
        override fun toString(): String = "null"

        override val isNumber: Boolean = false

        override val typeName: String = "Null"
    }

    data object VoidValue : Value() {
        override fun toString(): String = "void"

        override val isNumber: Boolean = false

        override val typeName: String = "Void"
    }

    /**
     * I haven't impl the boolean type, so I'll use this to check if a value is true
     */
    val isTrue
        get() =
            when (this) {
                is Int32Value -> this.value != 0
                is Int64Value -> this.value != 0L
                is FloatValue -> this.value != 0.0
                is BoolValue -> this.value
                is StringValue -> this.value.isNotEmpty()
                is NullValue, is VoidValue, is ObjectValue -> false
            }

    abstract val isNumber: Boolean

    abstract val typeName: String

    operator fun plus(other: Value): Value =
        when {
            this is Int32Value && other is Int32Value -> createInt32(this.value + other.value)
            this is Int32Value && other is FloatValue -> createFloat(this.value.toDouble() + other.value)
            this is Int32Value && other is Int64Value -> createInt64(this.value.toLong() + other.value)
            this is Int64Value && other is Int32Value -> createInt64(this.value + other.value.toLong())
            this is Int64Value && other is Int64Value -> createInt64(this.value + other.value)
            this is Int64Value && other is FloatValue -> createFloat(this.value.toDouble() + other.value)
            this is FloatValue && other is Int32Value -> createFloat(this.value + other.value.toDouble())
            this is FloatValue && other is Int64Value -> createFloat(this.value + other.value.toDouble())
            this is FloatValue && other is FloatValue -> createFloat(this.value + other.value)
            this is StringValue && other is StringValue -> createString(this.value + other.value)
            else -> throw IllegalArgumentException("Unsupported operation: $this + $other")
        }

    operator fun minus(other: Value): Value =
        when {
            this is Int32Value && other is Int32Value -> createInt32(this.value - other.value)
            this is Int32Value && other is Int64Value -> createInt64(this.value.toLong() - other.value)
            this is Int32Value && other is FloatValue -> createFloat(this.value.toDouble() - other.value)
            this is Int64Value && other is Int32Value -> createInt64(this.value - other.value.toLong())
            this is Int64Value && other is Int64Value -> createInt64(this.value - other.value)
            this is Int64Value && other is FloatValue -> createFloat(this.value.toDouble() - other.value)
            this is FloatValue && other is Int32Value -> createFloat(this.value - other.value.toDouble())
            this is FloatValue && other is Int64Value -> createFloat(this.value - other.value.toDouble())
            this is FloatValue && other is FloatValue -> createFloat(this.value - other.value)
            else -> throw IllegalArgumentException("Unsupported operation: $this - $other")
        }

    operator fun times(other: Value): Value =
        when {
            this is Int32Value && other is Int32Value -> createInt32(this.value * other.value)
            this is Int32Value && other is Int64Value -> createInt64(this.value.toLong() * other.value)
            this is Int32Value && other is FloatValue -> createFloat(this.value.toDouble() * other.value)
            this is Int64Value && other is Int32Value -> createInt64(this.value * other.value.toLong())
            this is Int64Value && other is Int64Value -> createInt64(this.value * other.value)
            this is Int64Value && other is FloatValue -> createFloat(this.value.toDouble() * other.value)
            this is FloatValue && other is Int32Value -> createFloat(this.value * other.value.toDouble())
            this is FloatValue && other is Int64Value -> createFloat(this.value * other.value.toDouble())
            this is FloatValue && other is FloatValue -> createFloat(this.value * other.value)
            else -> throw IllegalArgumentException("Unsupported operation: $this * $other")
        }

    operator fun div(other: Value): Value =
        when {
            this is Int32Value && other is Int32Value -> createInt32(this.value / other.value)
            this is Int32Value && other is Int64Value -> createInt64(this.value.toLong() / other.value)
            this is Int32Value && other is FloatValue -> createFloat(this.value.toDouble() / other.value)
            this is Int64Value && other is Int32Value -> createInt64(this.value / other.value.toLong())
            this is Int64Value && other is Int64Value -> createInt64(this.value / other.value)
            this is Int64Value && other is FloatValue -> createFloat(this.value.toDouble() / other.value)
            this is FloatValue && other is Int32Value -> createFloat(this.value / other.value.toDouble())
            this is FloatValue && other is Int64Value -> createFloat(this.value / other.value.toDouble())
            this is FloatValue && other is FloatValue -> createFloat(this.value / other.value)
            else -> throw IllegalArgumentException("Unsupported operation: $this / $other")
        }

    operator fun compareTo(other: Value): Int =
        when {
            this is Int32Value && other is Int32Value -> this.value.compareTo(other.value)
            this is Int32Value && other is Int64Value -> this.value.compareTo(other.value)
            this is Int32Value && other is FloatValue -> this.value.compareTo(other.value.toInt())
            this is Int64Value && other is Int32Value -> this.value.compareTo(other.value)
            this is Int64Value && other is Int64Value -> this.value.compareTo(other.value)
            this is Int64Value && other is FloatValue -> this.value.compareTo(other.value.toLong())
            this is FloatValue && other is Int32Value -> this.value.toInt().compareTo(other.value)
            this is FloatValue && other is Int64Value -> this.value.toLong().compareTo(other.value)
            this is FloatValue && other is FloatValue -> this.value.compareTo(other.value)
            this is StringValue && other is StringValue -> this.value.compareTo(other.value)
            else -> throw IllegalArgumentException("Unsupported operation: $this == $other")
        }

    operator fun not(): Value =
        when (this) {
            is BoolValue -> createBool(!this.value)
            else -> throw IllegalArgumentException("Unsupported operation: !$this")
        }

    fun and(other: Value): Value =
        when {
            this is BoolValue && other is BoolValue -> createBool(this.value && other.value)
            else -> throw IllegalArgumentException("Unsupported operation: $this && $other")
        }

    fun or(other: Value): Value =
        when {
            this is BoolValue && other is BoolValue -> createBool(this.value || other.value)
            else -> throw IllegalArgumentException("Unsupported operation: $this || $other")
        }

    fun negate(): Value =
        when (this) {
            is Int32Value -> createInt32(-this.value)
            is Int64Value -> createInt64(-this.value)
            is FloatValue -> createFloat(-this.value)
            else -> throw IllegalArgumentException("Unsupported operation: -$this")
        }

    companion object {
        // Integer pool from -128 to 127 (common values)
        private val INT32_POOL = Array(256) { Int32Value(it - 128) }

        // Long pool for common values
        private val INT64_POOL = Array(256) { Int64Value((it - 128).toLong()) }

        // Boolean pool (just two values)
        private val TRUE = BoolValue(true)
        private val FALSE = BoolValue(false)

        // Common float values
        private val FLOAT_POOL =
            mapOf(
                0.0 to FloatValue(0.0),
                1.0 to FloatValue(1.0),
                -1.0 to FloatValue(-1.0),
            )

        // Empty string and common strings
        private val EMPTY_STRING = StringValue("")
        private val COMMON_STRINGS = mutableMapOf<String, StringValue>()

        // Singleton for null
        val NULL = NullValue

        // Singleton for void
        val VOID = VoidValue

        fun createInt32(value: Int): Int32Value =
            if (value in -128..127) {
                INT32_POOL[value + 128]
            } else {
                Int32Value(value)
            }

        fun createInt64(value: Long): Int64Value =
            if (value >= -128 && value <= 127) {
                INT64_POOL[(value + 128).toInt()]
            } else {
                Int64Value(value)
            }

        fun createFloat(value: Double): FloatValue = FLOAT_POOL[value] ?: FloatValue(value)

        fun createBool(value: Boolean): BoolValue = if (value) TRUE else FALSE

        fun createString(value: String): StringValue {
            if (value.isEmpty()) return EMPTY_STRING

            // Only pool strings under a certain length to avoid memory issues
            return if (value.length <= 16) {
                COMMON_STRINGS.getOrPut(value) { StringValue(value) }
            } else {
                StringValue(value)
            }
        }
    }
}
