package requestshandling

import grails.async.Promise

import static grails.async.Promises.task

/**
 *  @purpose: slices, and parses apache tomcat large access_log file (sample used : 75 MB)
 *  @author: Yahya Kittaneh
 *  @email: kittaneh@gmail.com
 */
class RequestsSlicerService {

    //dependency injection using spring
    def grailsApplication
    def requestSourceService

    /**
     *  slices access_log file (apache tomcat access_log) to web-app/temp
     *  each slice file is on pattern sliceN where N is the number of slice index
     *  for every 1000 line in the sample access_log file
     *  uses asynchronous programming using grails Promises
     *  see path: conf/data/sample_access_log_2
     */
    void sliceAccessLogFile(String logFileName) {

        //get global config variables
        String slicePath = grailsApplication.config.requestsSlicer.slicePath
        String filePrefix = grailsApplication.config.requestsSlicer.filePrefix

        //get sample access_log as stream
        def resource = this.class.classLoader.getResource(logFileName)
        def dataFile = new File(resource.path)

        int i
        int fileNumber

        if (!dataFile.exists()) {
            log.debug "File does not exist"
        } else {  //begin slicing process
            i = 0
            fileNumber = 0
            new File(slicePath).mkdir()
            File fileToWrite = new File(slicePath + filePrefix + fileNumber)

            //async programming to tune performance and deal with large task
            //see http://docs.grails.org/2.5.6/guide/async.html#promises
            Promise p = task {
                // slice every 1000 line to a slice
                dataFile.eachLine { line ->
                    if (i > 1000) {
                        i = 0
                        fileNumber += 1
                        fileToWrite = new File(slicePath + filePrefix + fileNumber)
                    }
                    i = i + 1

                    //append line to slice
                    fileToWrite << ("$line\r\n")
                }
            }

            //notify main thread with completion
            p.onComplete { result ->
                println "Promise returned $result"
            }

        }
    }

    /**
     * parses slices inside folder web-app/temp
     * uses asynchronous programming using grails Promises
     */
    void parseLogSlices(String slicesPath) {

        def slicesFolder = new File(slicesPath)

        if (!slicesFolder.exists()) {
            String msg = " ${slicesFolder} does not exist"
            log.error msg
            throw new RuntimeException(msg)
        } else {  //begins parsing process

            //async programming to tune performance and deal with large task
            //see http://docs.grails.org/2.5.6/guide/async.html#promises
            Promise p = task {
                slicesFolder.eachFile { file ->
                    parseLogSlice(file.path)
                }
            }

            //notify main thread with completion
            p.onComplete { result ->
                println "Promise returned $result"
            }
        }


    }

    /**
     * parses single slice using its relative path
     * example : /web-app/temp/slice5
     */
    void parseLogSlice(String logSliceFilePath) {

        File sliceFile = new File(logSliceFilePath)

        if (!sliceFile.exists()) {
            String msg = "${logSliceFilePath} does not exist"
            log.error msg
            throw new RuntimeException(msg)
        } else {  //begin parse process

            sliceFile.eachLine { line ->

                //extract source, requestId, and userAgent from line
                String source = line.split("\t-")[0]
                String requestId = UUID.randomUUID().toString()
                String userAgent = "TEST/1.1" //todo: define user-agent pattern inside sliceFile

                //persist requestSource
                RequestSource requestSource = new RequestSource(source: source, requestId: requestId, userAgent: userAgent)
                requestSourceService.handleRequest(requestSource)
            }
        }


    }


}
