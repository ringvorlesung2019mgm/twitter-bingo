package producer;

import org.bson.Document;
import org.bson.conversions.Bson;
import twitter4j.FilterQuery;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Represetns a user-query for tweets.
 *
 * @author db
 */
public class Query {

    private String queryString;

    /** This regex matches all characters we do not want to have in a query-string
     *
     */
    private static final Pattern unwantedCharacters = Pattern.compile("[^a-zA-Z0-9]+");

    public Query(String queryString) {
        this.queryString = normalizeQuerystring(queryString);
    }


    /** Cleans and normalizes a user-supplied query and returns a proper query-string
     *
     * @param input some query-string
     * @return a normalized and cleaned query-string
     */
    private static String normalizeQuerystring(String input){
        return unwantedCharacters.matcher(input).replaceAll("").toLowerCase();
    }


    /**
     * Convert this query to a Twitter4J-producer.Query
     */
    public twitter4j.Query getT4JQuery() {
        return new twitter4j.Query("#" + queryString);
    }

    /**
     * Convert this query to a Twitter4J-FilterQuery
     */
    public FilterQuery getT4JFilterQuery() {
        return new FilterQuery("#" + queryString);
    }


    /**
     * Returns a mongodb filter that matches this query
     */
    public Bson getMongodbQuery() {
        return new Document("hashtags", queryString);
    }

    /** Returns a mongodb changestream filter matching this query
     * ChangeStream filters are a little bit different from "regular" mongodb filters.
     * @return
     */
    public List<Bson> getMongodbChangestreamFilter() {
        return Arrays.asList(new Document("$match", new Document("fullDocument.hashtags", queryString)));
    }

    @Override
    public String toString() {
        return queryString;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Query) {
            if (((Query) o).queryString.equals(queryString)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return queryString.hashCode();
    }
}
