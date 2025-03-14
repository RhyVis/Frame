$test_func

@func1(arg) {arg = arg + 1;::arg}
@func2(arg) {arg = arg * 1.3;::arg}
@func3(arg) {arg = arg - 1;::arg}

:Container { *field: int32 }

*container: object = *Container(0)

~(container.field < 100000000) {
    container.field = func3(func2(func1(container.field)))
    #println(container.to_string())
}
