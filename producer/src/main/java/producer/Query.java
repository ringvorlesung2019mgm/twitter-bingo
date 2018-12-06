package producer;

import org.bson.Document;
import org.bson.conversions.Bson;
import twitter4j.FilterQuery;

import java.util.Arrays;
import java.util.List;

/** Represetns a user-query for tweets.
 *
 */
public class Query {
    private String hashtag;

    public Query(String hashtag){
        this.hashtag = hashtag;
    }

    /** Convert this query to a Twitter4J-producer.Query
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


    /** Returns a mongodb filter that matches this query
     *
     */
    public Bson getMongodbQuery(){
        return new Document("hashtags",hashtag);
    }

    public List<Bson> getMongodbChangestreamFilter(){
        return Arrays.asList(new Document("$match",new Document("fullDocument.hashtags",hashtag)));
    }

    @Override
    public String toString(){
        return hashtag;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Query){
            if(((Query)o).hashtag == hashtag){
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hashtag.hashCode();
    }
}
