package requestshandling

import groovy.transform.ToString


@ToString
class RequestSource {

    String requestId
    String userAgent
    String source

    static constraints = {
        requestId nullable: true
        userAgent nullable: true
    }


}
