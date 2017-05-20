package requestshandling

import grails.transaction.Transactional

/**
 *  @purpose: persist, and query data stored in the RequestSource domain class
 *  @author: Yahya Kittaneh
 *  @email: kittaneh@gmail.com
 */
@Transactional
class RequestSourceService {

    /**
     *  persists RequestSource domain class
     */
    @Transactional
    RequestSource handleRequest(RequestSource requestSource) {

        //null argument is not welcomed !
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
     *  query the top N sources, when provided the numberOfTops integer
     *  when numberOfTops is provided with negative or zero value the
     *  method will assume returning all data
     */
    List topFreqSources(int numberOfTops) {

        //negatives mean get all sources without limit
        if (numberOfTops <= 0)
            numberOfTops = -1

        //use hql to return data as list of maps
        return RequestSource.executeQuery(""" select new map(rs.source as source, count(rs) as numberOfRequests)
                                                     from RequestSource rs
                                                     group by  rs.source
                                                     order by  count(rs) desc
                                                     """, [max: numberOfTops, offset: 0])

    }
}
