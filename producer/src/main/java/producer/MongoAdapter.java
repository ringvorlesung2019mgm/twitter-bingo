package producer;


import com.mongodb.MongoClient;
import com.mongodb.client.ChangeStreamIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import org.bson.Document;

import java.util.Iterator;

import static com.mongodb.client.model.Sorts.*;


/**
 * Streams tweets for a given query from mongodb.
 */
public class MongoAdapter {

    // TODO move to config
    public static final String DEFAULT_DB = "twitter";
    public static final String DEFAULT_COLLECTION = "tweets";

    //MongoClient is static so all instances of MongoAdapter can share the same connection pool
    static MongoClient client;

    public MongoAdapter(String uri){
        if (client == null){
            client = new MongoClient(uri);
        }
    }

    /**
     * Starts a stream from mongodb.
     *
     * @param q query
     * @param db mongodb database string
     * @param collection mongodb collection string
     * @return Cursor pointing to results
     */
    public ResultCursor stream(Query q,String db, String collection){
        MongoCollection coll = client.getDatabase(db).getCollection(collection);
        FindIterable existing = coll.find(q.getMongodbQuery());
        existing = existing.sort(orderBy(ascending("createdAt")));

        ChangeStreamIterable incoming = coll.watch(q.getMongodbChangestreamFilter());

        return new ResultCursor(existing.iterator(),incoming.iterator());
    }

    /**
     * Starts a stream from mongodb
     * @param q query
     * @return Cursor pointing to results
     */
    public ResultCursor stream(Query q){
        return stream(q,DEFAULT_DB,DEFAULT_COLLECTION);
    }

    public class ResultCursor implements Iterator<Document> {
        MongoCursor<Document> existing;
        MongoCursor<ChangeStreamDocument> incoming;

        ResultCursor(MongoCursor existing, MongoCursor incoming){
            this.existing = existing;
            this.incoming = incoming;
        }

        @Override
        public boolean hasNext() {
            return existing.hasNext() || incoming.hasNext();
        }

        public Document tryNext(){
            Document d = existing.tryNext();
            if (d != null){
                return d;
            }
            ChangeStreamDocument csd = incoming.tryNext();
            if (csd != null){
                return (Document) csd.getFullDocument();
            }
            return null;
        }

        @Override
        public Document next() {
            if (existing.hasNext()) {
                return existing.next();
            }
            return (Document)incoming.next().getFullDocument();
        }
        }

}
