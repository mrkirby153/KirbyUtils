package me.mrkirby153.kcutils.ktor

import io.ktor.client.statement.HttpResponse

class DelegatedHttpRequestException(override val message: String, val repsonse: HttpResponse) :
    Exception()