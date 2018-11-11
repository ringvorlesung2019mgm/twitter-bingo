import twitter4j.FilterQuery;

/** Represetns a user-query for tweets.
 *
 */
public class Query {
    private String hashtag;

    public Query(String hashtag){
        this.hashtag = hashtag;
    }

    /** Convert this query to a Twitter4J-Query
     *
     */
    public twitter4j.Query getT4JQuery(){
        return new twitter4j.Query("#"+hashtag);
    }

    /** Convert this query to a Twitter4J-FilterQuery
     *
     */
    public FilterQuery getT4JFilterQuery(){
        return new FilterQuery("#"+hashtag);
    }
}
