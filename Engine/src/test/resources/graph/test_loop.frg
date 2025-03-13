$test_loop
*var:i;var=1;
@add(ref){::ref+1}
~(var<10){#println(var);var=add(var)}
?(var==10){}?(true){#except("Fail")}
*lg:i;lg=1;
~(lg<100){lg=add(lg)}
#println("Success")
