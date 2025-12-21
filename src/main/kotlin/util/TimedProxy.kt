package org.example.util

import org.example.log.Log
import java.lang.reflect.InvocationHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Proxy

object TimedProxy {
    inline fun <reified T : Any> create(original: T, log: Log): T {
        return create(T::class.java, original, log)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> create(interfaceClass: Class<T>, original: T, log: Log): T {
        return Proxy.newProxyInstance(
            interfaceClass.classLoader,
            arrayOf(interfaceClass),
            TimedInvocationHandler(original, log)
        ) as T
    }

    private class TimedInvocationHandler<T>(private val original: T, private val log: Log) : InvocationHandler {
        override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any? {
            val className = (original as Any).javaClass.simpleName
            val methodName = method.name
            val arguments = args?.joinToString(", ") ?: ""
            return time("$className.$methodName($arguments)", log) {
                try {
                    method.invoke(original, *(args ?: emptyArray()))
                } catch (e: InvocationTargetException) {
                    throw e.cause ?: e
                }
            }
        }
    }
}
