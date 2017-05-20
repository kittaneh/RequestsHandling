package requestshandling

import org.apache.http.ConnectionReuseStrategy
import org.apache.http.HttpHost
import org.apache.http.HttpResponse
import org.apache.http.impl.DefaultBHttpClientConnection
import org.apache.http.impl.DefaultConnectionReuseStrategy
import org.apache.http.message.BasicHttpRequest
import org.apache.http.protocol.HttpCoreContext
import org.apache.http.protocol.HttpProcessor
import org.apache.http.protocol.HttpProcessorBuilder
import org.apache.http.protocol.HttpRequestExecutor
import org.apache.http.protocol.RequestConnControl
import org.apache.http.protocol.RequestContent
import org.apache.http.protocol.RequestExpectContinue
import org.apache.http.protocol.RequestTargetHost
import org.apache.http.protocol.RequestUserAgent


class RequestsSimulatorService {

    //dependency injection using spring
    def requestSourceService

    /**
     * simulates http requests using Apache HttpCore
     * customizes request header values
     */
    void simulate(ArrayList<String> sources) {

        //null argument is not welcomed :)
        if (!sources)
            throw new RuntimeException("sources cannot be null")

        //here we go with apache HTTPCore to simulate http requests
        //see http://hc.apache.org/httpcomponents-core-4.4.x/index.html
        HttpProcessor httpproc = HttpProcessorBuilder.create()
                .add(new RequestContent())
                .add(new RequestTargetHost())
                .add(new RequestConnControl())
                .add(new RequestUserAgent("Test/1.1"))
                .add(new RequestExpectContinue(true)).build()


        HttpRequestExecutor httpexecutor = new HttpRequestExecutor()

        HttpCoreContext coreContext = HttpCoreContext.create()
        HttpHost host = new HttpHost("localhost", 8080)
        coreContext.setTargetHost(host)

        DefaultBHttpClientConnection conn = new DefaultBHttpClientConnection(8 * 1024)
        ConnectionReuseStrategy connStrategy = DefaultConnectionReuseStrategy.INSTANCE

        //create requests for each source
        sources.each { String source ->

            //randomize number of requests for each source between [10,100]
            Random random = new Random()
            int numOfSourceRequests = random.nextInt(100 - 10) + 10 //10 inclusive

            //groovier 'for' loop :)
            (1..numOfSourceRequests).each {

                try {

                    //bind an active socket to the current connection
                    if (!conn.isOpen()) {
                        Socket socket = new Socket(host.getHostName(), host.getPort())
                        conn.bind(socket)
                    }

                    //apply XFF 'X-FORWARDED-FOR' to bind each request with its source
                    //see https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Forwarded-For
                    BasicHttpRequest request = new BasicHttpRequest("GET", "/RequestsHandling/home/foo")
                    request.setHeader("X-Forwarded-For", source)

                    //generate v4 UUID for a request id header and bind it to the request
                    //see https://en.wikipedia.org/wiki/List_of_HTTP_header_fields (search for X-Request-ID within page)
                    String requestId = UUID.randomUUID().toString()
                    request.setHeader("X-Request-ID", requestId)

                    //verify each request with its source
                    request.allHeaders.each { log.info(it) }

                    httpexecutor.preProcess(request, httpproc, coreContext)
                    HttpResponse response = httpexecutor.execute(request, conn, coreContext)
                    httpexecutor.postProcess(response, httpproc, coreContext)

                    //handle the request data
                    RequestSource requestSource = new RequestSource()
                    requestSource.requestId = request.getAllHeaders().find { it.name == "X-Request-ID" }.value
                    requestSource.source = request.getAllHeaders().find { it.name == "X-Forwarded-For" }.value
                    requestSource.userAgent = request.getAllHeaders().find { it.name == "User-Agent" }.value
                    requestSourceService.handleRequest(requestSource)


                    if (!connStrategy.keepAlive(response, coreContext)) {
                        conn.close()
                    } else {
                        log.info "Connection kept alive..."
                    }

                } finally {
                    conn.close()
                }

            }

        }

    }

}
