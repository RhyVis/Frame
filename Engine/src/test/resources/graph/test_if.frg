$test_if
*var:int32
var=1
~(var<0){
    #println(var)
}
?(var>-1){
    #println("Var greater than -1")
}

*let:int32
let=999

?(let == 0) {
    #println("Should not appear")
    #except("Fail")
} ?(let == 999) {
    #println("Should appear")
}

#println("Success")
