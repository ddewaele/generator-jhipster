package <%=packageName%>.web.rest.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.List;


/**
 * Utility class for handling pagination.
 *
 * <p>
 * Pagination uses the same principles as the <a href="https://developer.github.com/v3/#pagination">Github API</api>,
 * and follow <a href="http://tools.ietf.org/html/rfc5988">RFC 5988 (Link header)</a>.
 * </p>
 */
public class PaginationUtil {

    public static final int DEFAULT_OFFSET = 1;

    public static final int MIN_OFFSET = 1;

    public static final int DEFAULT_LIMIT = 20;

    public static final int MAX_LIMIT = 100;

    public static Pageable generatePageRequest(Integer offset,Integer limit,List<String> sorts) {
        
        if (offset == null || offset < MIN_OFFSET) {
            offset = DEFAULT_OFFSET;
        }
        if (limit == null || limit > MAX_LIMIT) {
            limit = DEFAULT_LIMIT;
        }
        
        if (sorts!=null && sorts.size()>0) {

            List<Sort.Order> orders = new ArrayList<Sort.Order>();
            for (int i=0 ; i<sorts.size() ; i++) {
                orders.add(new SortQueryParamConverter(sorts.get(i)).getOrder());
            }
            
            return new PageRequest(
                offset-1,limit, new Sort(
                    orders
                  )
            );
        } else {
            
            return new PageRequest(offset - 1, limit);
            
        }
    }
    
    public static Pageable generatePageRequest(Integer offset, Integer limit) {
        return generatePageRequest(offset, limit,null);
    }

    public static HttpHeaders generatePaginationHttpHeaders(Page page, String baseUrl, Integer offset, Integer limit)
        throws URISyntaxException {

        if (offset == null || offset < MIN_OFFSET) {
            offset = DEFAULT_OFFSET;
        }
        if (limit == null || limit > MAX_LIMIT) {
            limit = DEFAULT_LIMIT;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", "" + page.getTotalElements());
        String link = "";
        if (offset < page.getTotalPages()) {
            link = "<" + (new URI(baseUrl +"?page=" + (offset + 1) + "&per_page=" + limit)).toString()
                + ">; rel=\"next\",";
        }
        if (offset > 1) {
            link += "<" + (new URI(baseUrl +"?page=" + (offset - 1) + "&per_page=" + limit)).toString()
                + ">; rel=\"prev\",";
        }
        link += "<" + (new URI(baseUrl +"?page=" + page.getTotalPages() + "&per_page=" + limit)).toString()
            + ">; rel=\"last\"," +
            "<" + (new URI(baseUrl +"?page=" + 1 + "&per_page=" + limit)).toString()
            + ">; rel=\"first\"";
        headers.add(HttpHeaders.LINK, link);
        return headers;
    }

    /**
     *
     * Spring data defines a sort URL parameter that includes
     * 
     * the name of the property you want to sort the results on
     * (optional) the direction of the sort by appending a , to the the property name plus either asc or desc.
     * 
     * Examples:
     * 
     * curl -v "http://localhost:8080/people/search/nameStartsWith?name=K&sort=name,desc"
     * curl -v "http://localhost:8080/people/search/nameStartsWith?name=K&sort=name,desc&sort=age,asc"
     *
     * This helper class converts that comma-seperated string into an Order object.
     *
     */
    static class SortQueryParamConverter {
        public String value;
        
        public SortQueryParamConverter(String value) {
            this.value = value;
        }
        
        public Order getOrder() {
            try {
                String[] strings = this.value.split(",");
                return new Order(Direction.valueOf(strings[1].toUpperCase()), strings[0]);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Unable to convert sort query param " + this.value + " to sort property", ex);
            }
        }
    }

}
