package repository.neo4j;

import sample.data.neo4j.TweetNode;
import org.springframework.data.neo4j.repository.GraphRepository;

public interface TweetNodeRepository extends GraphRepository<TweetNode> {

    TweetNode findByTwitterId(long twitterId);


}
