$test_loop
*var:int32;var=1;
@add(ref){::ref+1}
~(var<10){#println(var);var=add(var)}
?(var==10){}?(true){#except("Fail")}
*lg:int32;lg=1;
~(lg<100){lg=add(lg)}
#println("Success")
