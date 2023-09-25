package me.mrkirby153.kcutils.ktor

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder

/**
 * Generic top-level class for all requests
 */
open class TypedCall<Req : Any, Resp : Any> internal constructor(
    private val defaultInst: () -> Req,
    private val reqCapture: HttpClientCapture<Req, Resp>
) {
    protected suspend fun doInvoke(
        client: HttpClient,
        req: Req? = null,
        builder: HttpRequestBuilder.() -> Unit = {}
    ): Resp {
        return reqCapture.invoke(req ?: defaultInst.invoke(), client, builder)
    }
}

/**
 * Class representing a request that does not have a body
 */
class TypedBodilessCall<Req : Any, Resp : Any>(
    defaultInst: () -> Req,
    reqCapture: HttpClientCapture<Req, Resp>
) : TypedCall<Req, Resp>(defaultInst, reqCapture) {

    /**
     * Makes the request using the given [client]
     *
     * If [req] is provided, it is used to make the request, otherwise the default no-args
     * constructor for [Req] is used.
     */
    suspend operator fun invoke(
        client: HttpClient,
        req: Req? = null,
        builder: HttpRequestBuilder.() -> Unit = {}
    ): Resp = super.doInvoke(client, req, builder)
}

/**
 * Class representing a request that has a body
 */
class TypedBodyCall<Req : Any, Resp : Any, Body : Any>(
    defaultInst: () -> Req,
    reqCapture: HttpClientCapture<Req, Resp>,
    private val bodyCapture: HttpRequestBuilder.(Body) -> Unit,
) : TypedCall<Req, Resp>(defaultInst, reqCapture) {

    /**
     * Makes the request using the given [client] and [body]
     *
     * If [req] is provided, it is used to make the request, otherwise the default no-args
     * constructor for [Req] is used.
     */
    suspend operator fun invoke(
        client: HttpClient,
        req: Req? = null,
        body: Body,
        builder: HttpRequestBuilder.() -> Unit = {}
    ): Resp {
        val wrappedBuilder: HttpRequestBuilder.() -> Unit = {
            this.bodyCapture(body)
            builder()
        }
        return super.doInvoke(client, req, wrappedBuilder)
    }
}

/**
 * Represents a request method
 */
enum class RequestMethod {
    DELETE,
    GET,
    HEAD,
    OPTIONS,
    PATCH,
    POST,
    PUT
}