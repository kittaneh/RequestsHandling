package requestshandling

class HomeController {

    def requestsSlicerService
    def grailsApplication

    def index() {

        //def sources = ['10.10.3.4','dc1.mv.com']
        //requestsSlicerService.simulateRequests(sources)
        println requestsSlicerService.topFreqSources(10)
        //requestsSlicerService.parseAccessLogFile("data/sample_access_log_2")
        //requestsSlicerService.sliceAccessLogFile("data/sample_access_log_2")



    }

    def bar(){
        String slicePath = grailsApplication.config.requestsSlicer.slicePath
        requestsSlicerService.parseLogSlices(slicePath)
    }

    def foo() {}
}
