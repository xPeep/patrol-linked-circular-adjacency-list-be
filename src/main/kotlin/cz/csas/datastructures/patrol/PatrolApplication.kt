package cz.csas.datastructures.patrol

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PatrolApplication

fun main(args: Array<String>) {
	runApplication<PatrolApplication>(*args)
}
