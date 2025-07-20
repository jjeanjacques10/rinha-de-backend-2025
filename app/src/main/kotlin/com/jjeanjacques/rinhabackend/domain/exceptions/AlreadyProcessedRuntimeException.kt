package com.jjeanjacques.rinhabackend.domain.exceptions

class AlreadyProcessedRuntimeException : RuntimeException {
    constructor(message: String) : super(message)
}