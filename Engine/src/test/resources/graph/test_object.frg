$test_object

:Object{*field:i}
*object:o = *Object("No type check at all")
#println(object.to_string())

:TestObject{
    *field:i
    @add_self() {
        this.field = this.field + 1
    }
    @to_string() {
        :: #to_string(this)
    }
}
*object:o = *TestObject(1)
#println(object.to_string())
object.add_self()
#println(object.to_string())
