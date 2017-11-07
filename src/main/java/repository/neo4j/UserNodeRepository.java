package repository.neo4j;

import sample.data.neo4j.UserNode;
import org.springframework.data.neo4j.repository.GraphRepository;

public interface UserNodeRepository extends GraphRepository<UserNode> {

    UserNode findByTwitterId(long twitterId);
}
