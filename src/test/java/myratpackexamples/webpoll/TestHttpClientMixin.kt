package myratpackexamples.webpoll

import com.fasterxml.jackson.databind.ObjectMapper
import ratpack.http.client.ReceivedResponse
import ratpack.test.http.TestHttpClient
import kotlin.reflect.KClass

interface TestHttpClientMixin {

    val objectMapper: ObjectMapper

    operator fun <T : Any> TestHttpClient.get(uri: String, type: KClass<T>): T =
            objectMapper.readValue(get(uri).body.text, type.java)

    fun TestHttpClient.post(uri: String, pollJson: String): ReceivedResponse {
        return requestSpec {
            it.body {
                it.type("application/json").text(pollJson)
            }
        }.post(uri)
    }
}