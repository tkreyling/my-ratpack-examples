package myratpackexamples.webpoll

import com.fasterxml.jackson.databind.ObjectMapper
import ratpack.http.client.ReceivedResponse
import ratpack.test.http.TestHttpClient

interface TestHttpClientMixin {

    val objectMapper: ObjectMapper

    operator fun <T> TestHttpClient.get(uri: String, type: Class<T>): T =
            objectMapper.readValue(get(uri).body.text, type)

    fun TestHttpClient.post(uri: String, pollJson: String): ReceivedResponse {
        return requestSpec {
            it.body {
                it.type("application/json").text(pollJson)
            }
        }.post(uri)
    }
}