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
    def requestSourceService

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
                requestSourceService.handleRequest(requestSource)
            }
        }


    }


}
