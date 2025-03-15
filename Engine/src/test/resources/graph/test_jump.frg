$test_jump
*flag:int32=0
@loop() {
    %[return]
    flag = flag + 1
    ?(flag > 10) {#println("Should exit")}
    >[return](flag <= 10)
}
loop()
#println("Success")
