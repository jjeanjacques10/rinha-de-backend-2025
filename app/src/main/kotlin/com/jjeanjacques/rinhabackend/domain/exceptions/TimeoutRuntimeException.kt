package com.jjeanjacques.rinhabackend.domain.exceptions

class TimeoutRuntimeException : RuntimeException {
    constructor(message: String) : super(message)
}