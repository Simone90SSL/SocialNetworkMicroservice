package repository.neo4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import sample.data.neo4j.HashTagNode;


public interface HashTagNodeRepository extends GraphRepository<HashTagNode> {

    HashTagNode findByHashTag(String hashTag);

    @Query("MATCH (a:HashTagNode) return a.hashTag")
    Page<String> findAllHashTags(Pageable pageable);
}
