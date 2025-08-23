package nz.adjmunro.knomadic.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ThrowableExtTest {

    inline fun <T> isSuspended(block: () -> T): Boolean {
        return CoroutineScope(Dispatchers.Default).let {
            println("1 Coroutine context: ${it.coroutineContext.job.parent}")
            it.coroutineContext.job.parent != null
        }.also {
            print(it)
            if(it) block()
        }
    }

    inline fun <T> isSuspended2(block: () -> T): Boolean {
        return runBlocking {
            println("2 Coroutine context: ${coroutineContext.job.parent}")
            coroutineContext.job.parent != null
        }.also {
            print(it)
            if(it) block()
        }
    }

    @Test
    fun testIsSuspended() {
        runTest {
            launch {
                async() {
                    println("Nested launch coroutine context: ${coroutineContext.job.parent}")
                }
            }
            val result = async {
                isSuspended {
                    // This block is executed in a coroutine context
                    println("testIsSuspended: Hello, World!")
                }
            }

            assertTrue(result.await())
        }
    }

    @Test
    fun testIsNotSuspended() {
        val result = isSuspended {
            // This block is executed in a non-suspended context
            println("testIsNotSuspended: Hello, World!")
        }

        assertFalse(result)
    }

    @Test
    fun testIsSuspended2() {
        runBlocking {
            val result = isSuspended2 {
                // This block is executed in a coroutine context
                println("testIsSuspended2: Hello, World!")
            }

            assertTrue(result)
        }
    }

    @Test
    fun testIsNotSuspended2() {
        val result = isSuspended2 {
            // This block is executed in a non-suspended context
            println("testIsNotSuspended2: Hello, World!")
        }

        assertFalse(result)
    }
}
