package requestshandling

import grails.async.Promise
import grails.transaction.Transactional
import org.apache.http.ConnectionReuseStrategy
import org.apache.http.HttpHost
import org.apache.http.HttpResponse
import org.apache.http.impl.DefaultBHttpClientConnection
import org.apache.http.impl.DefaultConnectionReuseStrategy
import org.apache.http.message.BasicHttpRequest
import org.apache.http.protocol.*

import static grails.async.Promises.task

/**
 *  assumptions:
 * @author: Yahya Kittaneh
 */
class RequestsSlicerService {


    def grailsApplication

    /**
     *
     * @param sources
     */
    void simulateRequests(ArrayList<String> sources) {

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
                    handleRequest(requestSource)


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

    /**
     *
     * @param logFileName
     */
    void sliceAccessLogFile(String logFileName) {

        String slicePath = grailsApplication.config.requestsSlicer.slicePath
        String filePrefix = grailsApplication.config.requestsSlicer.filePrefix

        def resource = this.class.classLoader.getResource(logFileName)
        def dataFile = new File(resource.path)

        int i
        int fileNumber

        if (!dataFile.exists()) {
            log.debug "File does not exist"
        } else {
            i = 0
            fileNumber = 0
            new File(slicePath).mkdir()
            File fileToWrite = new File(slicePath + filePrefix + fileNumber)

            Promise p = task {
                dataFile.eachLine { line ->
                    if (i > 1000) {
                        i = 0
                        fileNumber += 1
                        fileToWrite = new File(slicePath + filePrefix + fileNumber)
                    }
                    i = i + 1
                    fileToWrite << ("$line\r\n")
                }
            }

            p.onComplete { result ->
                println "Promise returned $result"
            }

        }
    }

    /**
     *
     * @param slicesPath
     */
    void parseLogSlices(String slicesPath) {

        def slicesFolder = new File(slicesPath)

        if (!slicesFolder.exists()) {
            String msg = " ${slicesFolder} does not exist"
            log.error msg
            throw new RuntimeException(msg)
        } else {
            Promise p = task {
                slicesFolder.eachFile { file ->
                    parseLogSlice(file.path)
                }
            }
            p.onComplete { result ->
                println "Promise returned $result"
            }
        }


    }

    /**
     *
     * @param logSliceFilePath
     */
    void parseLogSlice(String logSliceFilePath) {

        File sliceFile = new File(logSliceFilePath)

        if (!sliceFile.exists()) {
            String msg = "${logSliceFilePath} does not exist"
            log.error msg
            throw new RuntimeException(msg)
        } else {

            sliceFile.eachLine { line ->

                String source = line.split("\t-")[0]
                String requestId = UUID.randomUUID().toString()
                String userAgent = "TEST/1.1" //todo: define user-agent pattern inside slicefile

                RequestSource requestSource = new RequestSource(source: source, requestId: requestId, userAgent: userAgent)
                handleRequest(requestSource)
            }
        }


    }

    /**
     *
     * @param requestSource
     * @return
     */
    @Transactional
    RequestSource handleRequest(RequestSource requestSource) {

        //again null argument is not welcomed !
        if (!requestSource)
            throw new RuntimeException("requestSource cannot be null")

        //store request to some db
        if (!requestSource.hasErrors() && requestSource.save())
            return requestSource

        //something wrong happened ? ok log it
        else {
            requestSource.errors.allErrors.each {
                log.error("validation error happened: " + it)
            }
        }

        return null

    }

    /**
     *
     * @param numberOfTops
     * @return
     */
    List topFreqSources(int numberOfTops) {

        //negatives mean get all sources without limit
        if (numberOfTops <= 0)
            numberOfTops = -1

        return RequestSource.executeQuery(""" select new map(rs.source as source, count(rs) as numberOfRequests)
                                                     from RequestSource rs
                                                     group by  rs.source
                                                     order by  count(rs) desc
                                                     """, [max: numberOfTops, offset: 0])

    }

}
