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
        return  [msg:"$actionName daemon initialized"]
    }

    def parse(){
        String slicePath = grailsApplication.config.requestsSlicer.slicePath
        requestsSlicerService.parseLogSlices(slicePath)
        return  [msg:"$actionName daemon initialized"]
    }

    def simulate(){
        def sources = ['10.10.3.4','dc1.mv.com','hc.webisaba.com','127.0.0.1','ping.yahoo.com','foo.bar.net']
        requestsSimulatorService.simulate(sources)
        return  [msg:"$actionName daemon initialized"]
    }

    def top(){
        int id = params.id ? params.int('id'):-1
        def returnedDtata = [:]
        returnedDtata.put("top",(requestSourceService.topFreqSources(id)  as JSON))
        return [returnedData:returnedDtata]
    }

}
