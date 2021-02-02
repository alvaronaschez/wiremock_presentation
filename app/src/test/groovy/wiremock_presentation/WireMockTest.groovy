package wiremock_presentation

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import spock.lang.Shared
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.client.WireMock.get
import static com.github.tomakehurst.wiremock.client.WireMock.post

class WireMockTest extends Specification {
    @Shared
    WireMockServer wireMockServer

    @Shared
    String baseUrl

    def setupSpec() {
        wireMockServer = new WireMockServer(
            new WireMockConfiguration().dynamicPort()
        )
        wireMockServer.start()
        baseUrl = wireMockServer.baseUrl()
    }

    def cleanupSpec() {
        wireMockServer.stop()
    }

    def cleanup() {
        wireMockServer.resetAll()
    }

    def "simple request"() {
        setup:
        def responseDefinitionBuilder = new ResponseDefinitionBuilder().withStatus(200)

        wireMockServer.givenThat(
            get('/get')
                .willReturn(
                        responseDefinitionBuilder
                ) as MappingBuilder
        )

        when:
        def connection = new URL("$baseUrl/get").openConnection() as HttpURLConnection

        then:
        connection.responseCode == 200
    }

    def "get request"() {
        setup:
        def responseBody = [
            args : [
                foo: 'bar',
                ham: 'jam'
            ]
        ]

        def responseDefinitionBuilder = new ResponseDefinitionBuilder()
            .withHeader('Content-Type', 'application/json')
            .withStatus(200)
            .withBody(JsonOutput.toJson(responseBody))

        wireMockServer.givenThat(
            get('/get?foo=bar&ham=jam')
                .willReturn(
                    responseDefinitionBuilder
                ) as MappingBuilder
        )

        when:
        def connection = new URL("$baseUrl/get?foo=bar&ham=jam").openConnection() as HttpURLConnection

        and:
        def text = connection.content.text as String
        def body = new JsonSlurper().parseText(text) as Map
        def args = body.args as Map

        then:
        connection.responseCode == 200
        args.foo == 'bar'
        args.ham == 'jam'
    }

    def "post request"() {
        setup:
        def responseBody = [
            json : [
                foo: 'bar',
                ham: 'jam'
            ]
        ]

        def responseDefinitionBuilder = new ResponseDefinitionBuilder()
            .withHeader('Content-Type', 'application/json')
            .withStatus(200)
            .withBody(JsonOutput.toJson(responseBody))

        wireMockServer.givenThat(
            post('/post')
                .willReturn(
                    responseDefinitionBuilder
                ) as MappingBuilder
        )

        when:
        def requestBody = JsonOutput.toJson([foo: 'bar', ham: 'jam']) as String

        and:
        def connection = new URL("$baseUrl/post").openConnection() as HttpURLConnection
        connection.with{
            requestMethod = 'POST'
            doOutput = true
            setRequestProperty('Content-Type', 'application/json')
            outputStream.write(requestBody.getBytes('UTF-8'))
        }

        and:
        def text = connection.content.text as String
        def body = new JsonSlurper().parseText(text) as Map

        then:
        connection.responseCode == 200
        body.json.foo == 'bar'
        body.json.ham == 'jam'
    }
}
