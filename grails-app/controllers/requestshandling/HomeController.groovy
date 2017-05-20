package requestshandling

import grails.converters.JSON

class HomeController {

    def requestsSlicerService
    def requestSourceService
    def requestsSimulatorService
    def grailsApplication

    def index() {}

    def foo() {}

    def slice(){
        requestsSlicerService.sliceAccessLogFile("data/sample_access_log_2")
    }

    def parse(){
        String slicePath = grailsApplication.config.requestsSlicer.slicePath
        requestsSlicerService.parseLogSlices(slicePath)
    }

    def simulate(){
        def sources = ['10.10.3.4','dc1.mv.com']
        requestsSimulatorService.simulate(sources)
    }

    def top(){
        int id = params.id ? params.int('id'):-1
        def returnedDtata = [:]
        returnedDtata.put("top",(requestSourceService.topFreqSources(id)  as JSON))
       // println requestSourceService.topFreqSources(id) as JSON
        return [returnedData:returnedDtata]
    }

}
