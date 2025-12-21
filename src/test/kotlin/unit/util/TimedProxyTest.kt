package unit.util

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.kotest.matchers.shouldNotBe
import fixtures.FakeLog
import org.example.util.TimedProxy
import org.junit.jupiter.api.Test

class TimedProxyTest {

    interface Calculator {
        fun add(a: Int, b: Int): Int
        fun subtract(a: Int, b: Int): Int
    }

    class RealCalculator : Calculator {
        override fun add(a: Int, b: Int): Int = a + b

        override fun subtract(a: Int, b: Int): Int = a - b
    }

    @Test
    fun `should proxy method calls and log execution time`() {
        val fakeLog = FakeLog()
        val realCalculator = RealCalculator()
        val proxyCalculator = TimedProxy.create<Calculator>(realCalculator, fakeLog)

        val sum = proxyCalculator.add(5, 3)
        sum shouldBe 8

        fakeLog.messages shouldContain "INFO: starting RealCalculator.add(5, 3)"
        val finishedLog = fakeLog.messages.find { it.startsWith("INFO: finished RealCalculator.add(5, 3)") }
        finishedLog shouldNotBe null
        finishedLog shouldStartWith "INFO: finished RealCalculator.add(5, 3) in"
        
        val difference = proxyCalculator.subtract(10, 4)
        difference shouldBe 6

        fakeLog.messages shouldContain "INFO: starting RealCalculator.subtract(10, 4)"
        val finishedSubtractLog = fakeLog.messages.find { it.startsWith("INFO: finished RealCalculator.subtract(10, 4)") }
        finishedSubtractLog shouldNotBe null
        finishedSubtractLog shouldStartWith "INFO: finished RealCalculator.subtract(10, 4) in"
    }
}
