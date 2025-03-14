$test_object

:Object{*field:int32}
*let: object = *Object("No type check at all")
#println(let.to_string())

:TestObject{
    *field:int32
    @add_self() {
        this.field = this.field + 1
    }
    @to_string() {
        :: #to_string(this)
    }
}
*let: object = *TestObject(1)
#println(let.to_string())
let.add_self()
#println(let.to_string())
