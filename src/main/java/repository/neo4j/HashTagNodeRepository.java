package repository.neo4j;

import org.springframework.data.neo4j.repository.GraphRepository;
import sample.data.neo4j.HashTagNode;

public interface HashTagNodeRepository extends GraphRepository<HashTagNode> {

    HashTagNode findByHashTag(String hashTag);
}
