$test_self_opt
*var:int32 = 1
~(var < 10) {
    var++;
    #println(var);
}
*let:int32 = 999
let++
#assert(1000 == let);
