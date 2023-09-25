package me.mrkirby153.kcutils.ktor

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Type for capturing reified type parameters
 */
typealias HttpClientCapture<Req, Resp> = suspend (Req, HttpClient, HttpRequestBuilder.() -> Unit) -> Resp

/**
 * Delegate class for an HTTP request that does not have a body
 */
class TypedCallDelegate<Req : Any, Resp : Any> @PublishedApi internal constructor(
    private val defaultInst: () -> Req,
    private val reqCapture: HttpClientCapture<Req, Resp>
) :
    ReadOnlyProperty<Any?, TypedBodilessCall<Req, Resp>> {

    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>
    ): TypedBodilessCall<Req, Resp> {
        return TypedBodilessCall(defaultInst, reqCapture)
    }
}

/**
 * Delegate class for an HTTP request that requires a body
 */
class TypedBodyApiCallDelegate<Req : Any, Resp : Any, Body : Any> @PublishedApi internal constructor(
    private val defaultInst: () -> Req,
    private val reqCapture: HttpClientCapture<Req, Resp>,
    private val bodyCapture: HttpRequestBuilder.(Body) -> Unit,
) : ReadOnlyProperty<Any?, TypedBodyCall<Req, Resp, Body>> {
    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>
    ): TypedBodyCall<Req, Resp, Body> {
        return TypedBodyCall(defaultInst, reqCapture, bodyCapture)
    }
}