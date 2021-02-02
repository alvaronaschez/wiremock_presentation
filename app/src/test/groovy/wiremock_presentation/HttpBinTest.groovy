package wiremock_presentation

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import spock.lang.Specification

class HttpBinTest extends Specification {
    String baseUrl = 'https://httpbin.org'

    // TODO remove the external server dependency using WireMock
    def "simple request"() {
        when:
        def connection = new URL("$baseUrl/get").openConnection() as HttpURLConnection

        then:
        connection.responseCode == 200
    }

    // TODO remove the external server dependency using WireMock
    def "get request"() {
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

    // TODO remove the external server dependency using WireMock
    def "post request"() {
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
