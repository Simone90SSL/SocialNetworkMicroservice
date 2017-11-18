package repository.mongodb;

import org.springframework.data.mongodb.repository.MongoRepository;
import sample.data.mongodb.TweetDocument;

public interface TweetDocumentRepository extends MongoRepository<TweetDocument, Long> {

}
