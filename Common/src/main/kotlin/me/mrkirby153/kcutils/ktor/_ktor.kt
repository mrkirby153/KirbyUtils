package me.mrkirby153.kcutils.ktor

import io.ktor.client.call.body
import io.ktor.client.plugins.resources.delete
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.head
import io.ktor.client.plugins.resources.options
import io.ktor.client.plugins.resources.patch
import io.ktor.client.plugins.resources.post
import io.ktor.client.plugins.resources.put
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

@PublishedApi
internal inline fun <reified Req : Any, reified Resp : Any> getRequestCapture(
    method: RequestMethod,
    expectedCode: HttpStatusCode
): HttpClientCapture<Req, Resp> =
    { req, httpClient, builder ->
        val response: HttpResponse = when (method) {
            RequestMethod.DELETE -> httpClient.delete(req) { this.builder() }
            RequestMethod.GET -> httpClient.get(req) { this.builder() }
            RequestMethod.HEAD -> httpClient.head(req) { this.builder() }
            RequestMethod.OPTIONS -> httpClient.options(req) { this.builder() }
            RequestMethod.PATCH -> httpClient.patch(req) { this.builder() }
            RequestMethod.POST -> httpClient.post(req) { this.builder() }
            RequestMethod.PUT -> httpClient.put(req) { this.builder() }
        }
        if (response.status != expectedCode) {
            throw DelegatedHttpRequestException(
                "Wrong response code. Expected $expectedCode, got ${response.status}",
                response
            )
        }
        response.body()
    }

/**
 * Creates a request with the given [method].
 *
 * If [expectedCode] does not match the response [DelegatedHttpRequestException] is thrown
 *
 * The request is determined from [Req] and the type of the response is
 * automatically deserialized to [Resp].
 *
 *
 * This method returns a delegate that can be invoked to execute the request.
 *
 * Example usage:
 * ```kt
 * @Resource("/echo")
 * class EchoRequest()
 *
 * @Serializable
 * data class EchoResponse(val message: String)
 *
 * fun makeRequest() {
 *     val echoRequest by request<EchoRequest, EchoResponse>()
 *     val resp : EchoResponse = echoRequest(httpClient)
 * }
 * ```
 */
inline fun <reified Req : Any, reified Resp : Any> request(
    method: RequestMethod,
    expectedCode: HttpStatusCode = HttpStatusCode.OK,
): TypedCallDelegate<Req, Resp> {
    val reqCapture: HttpClientCapture<Req, Resp> = getRequestCapture(method, expectedCode)
    val defaultInst = { Req::class.java.getConstructor().newInstance() }
    return TypedCallDelegate(defaultInst, reqCapture)
}

/**
 * Creates a request that requires a body with the given [method].
 *
 * @see request
 */
inline fun <reified Req : Any, reified Resp : Any, reified Body : Any> requestWithBody(
    method: RequestMethod,
    expectedCode: HttpStatusCode = HttpStatusCode.OK
): TypedBodyApiCallDelegate<Req, Resp, Body> {
    val reqCapture = getRequestCapture<Req, Resp>(method, expectedCode)
    val defaultInst = { Req::class.java.getConstructor().newInstance() }
    val bodyCapture: HttpRequestBuilder.(Body) -> Unit = {
        setBody<Body>(it)
    }
    return TypedBodyApiCallDelegate(defaultInst, reqCapture, bodyCapture)
}

/**
 * Creates a GET request that expects [expectedCode]
 *
 * @see request
 */
inline fun <reified Req : Any, reified Resp : Any> get(
    expectedCode: HttpStatusCode = HttpStatusCode.OK,
) = request<Req, Resp>(RequestMethod.GET, expectedCode)

/**
 * Creates a POST request that expects [expectedCode]
 *
 * @see request
 */
inline fun <reified Req : Any, reified Resp : Any, reified Body : Any> post(
    expectedCode: HttpStatusCode = HttpStatusCode.OK,
) = requestWithBody<Req, Resp, Body>(RequestMethod.POST, expectedCode)

/**
 * Creates a PATCH request that expects [expectedCode]
 *
 * @see request
 */
inline fun <reified Req : Any, reified Resp : Any, reified Body : Any> patch(
    expectedCode: HttpStatusCode = HttpStatusCode.OK,
) = requestWithBody<Req, Resp, Body>(RequestMethod.PATCH, expectedCode)

/**
 * Creates a HEAD request that expects [expectedCode]
 *
 * @see request
 */
inline fun <reified Req : Any, reified Resp : Any> head(
    expectedCode: HttpStatusCode = HttpStatusCode.OK,
) = request<Req, Resp>(RequestMethod.HEAD, expectedCode)

/**
 * Creates a DELETE request that expects [expectedCode]
 *
 * @see request
 */
inline fun <reified Req : Any, reified Resp : Any> delete(
    expectedCode: HttpStatusCode = HttpStatusCode.OK,
) = request<Req, Resp>(RequestMethod.DELETE, expectedCode)

/**
 * Creates an OPTIONS request that expects [expectedCode]
 *
 * @see request
 */
inline fun <reified Req : Any, reified Resp : Any> options(
    expectedCode: HttpStatusCode = HttpStatusCode.OK,
) = request<Req, Resp>(RequestMethod.OPTIONS, expectedCode)

/**
 * Creates a PUT request that expects [expectedCode]
 *
 * @see request
 */
inline fun <reified Req : Any, reified Resp : Any, reified Body : Any> put(
    expectedCode: HttpStatusCode = HttpStatusCode.OK,
) = requestWithBody<Req, Resp, Body>(RequestMethod.PUT, expectedCode)