package requestshandling

import groovy.transform.ToString

/**
 *  @purpose: ORM RequestSource domain class
 *  @author: Yahya Kittaneh
 *  @email: kittaneh@gmail.com
 */
@ToString
class RequestSource {

    String requestId  //UUID request id
    String userAgent
    String source  // source domainName or IP

    static constraints = {
        requestId nullable: true
        userAgent nullable: true
    }


}
