package repositories.interfaces

import entities.Test

interface ITestRepo {
//    fun findById(id: Long): Test?

    fun helloWorld(): String
    fun findAllNames(): List<String>
//    fun save(test: Test)
}