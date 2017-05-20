package requestshandling

import grails.converters.JSON

/**
 *  @purpose: testing controller to demonstrate the system features
 *  @author: Yahya Kittaneh
 *  @email: kittaneh@gmail.com
 */
class HomeController {

    //dependency injection using spring
    def requestsSlicerService
    def requestSourceService
    def requestsSimulatorService
    def grailsApplication

    def index() {}

    def foo() {}

    /**
     *  demonstrate the slice feature
     */
    def slice(){
        requestsSlicerService.sliceAccessLogFile("data/sample_access_log_2")
        return  [msg:"$actionName daemon initialized"]
    }

    /**
     *  demonstrate the parse feature
     */
    def parse(){
        String slicePath = grailsApplication.config.requestsSlicer.slicePath
        requestsSlicerService.parseLogSlices(slicePath)
        return  [msg:"$actionName daemon initialized"]
    }

    /**
     *  demonstrate the simulate feature
     */
    def simulate(){
        def sources = ['10.10.3.4','dc1.mv.com','hc.webisaba.com','127.0.0.1','ping.yahoo.com','foo.bar.net']
        requestsSimulatorService.simulate(sources)
        return  [msg:"$actionName daemon initialized"]
    }

    /**
     *  renders the top n sources , where n is an integer provided
     *  using the url: http://localhost:8080/RequestsHandling/home/top/n
     */
    def top(){
        int id = params.id ? params.int('id'):-1
        def returnedData = [:]
        returnedData.put("top",(requestSourceService.topFreqSources(id)  as JSON))
        return [returnedData:returnedData]
    }

}
