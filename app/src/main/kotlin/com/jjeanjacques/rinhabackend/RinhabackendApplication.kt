package com.jjeanjacques.rinhabackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class RinhabackendApplication

fun main(args: Array<String>) {
	runApplication<RinhabackendApplication>(*args)
}
