package requestshandling

import grails.test.mixin.TestFor
import spock.lang.Shared
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(RequestsSlicerService)
class RequestsSlicerServiceSpec extends Specification {


    // global properties to share among all specs
    @Shared requestSourceService

    /**
     *  In TEST environment you should manually inject
     *  any spring beans (even grails services)
     */
    def doWithSpring = {
        requestSourceService(RequestSourceService)
    }

    /**
     * putting all shared assignations and initializations to all specs
     */
    def setup() {
        requestSourceService = grailsApplication.mainContext.getBean('requestSourceService')
    }



    void "test slicing"() {

        given:
        String fileName = "data/localhost_access_log"

        when:
        service.sliceAccessLogFile(fileName)


        then:
        new File('web-app/temp/slice0').exists()



    }
}
