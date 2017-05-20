package requestshandling

import grails.transaction.Transactional

@Transactional
class RequestSourceService {


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
