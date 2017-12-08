package repository.neo4j;

import cache.Cache;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import sample.data.neo4j.HashTagNode;
import sample.data.neo4j.UserTagData;

import java.util.List;
import java.util.Optional;


public interface HashTagNodeRepository extends GraphRepository<HashTagNode> {

    HashTagNode findByHashTag(String hashTag);

    @Query("MATCH (a:HashTagNode) return a.hashTag")
    Page<String> findAllHashTags(Pageable pageable);

    @Query("MATCH (u:UserNode)-[t:TAGS]->(v:HashTagNode{hashTag:{0}}) " +
            "return u.twitterId as user, v.hashTag as tag, t.count as count")
    List<UserTagData> findUserTag(String hashTag);

    @Query("MATCH (n:HashTagNode{hashTag:{0}}) OPTIONAL MATCH (n)-[r]-() DELETE r,n")
    void delete(String hashTag);

    default HashTagNode getOrCreate(String hashTag) {
        return Optional
                .ofNullable(Cache.getHashTag(hashTag))
                .orElse(
                        Optional
                                .ofNullable(findByHashTag(hashTag))
                                .orElseGet(
                                        () -> {
                                            HashTagNode h = new HashTagNode(hashTag);
                                            save(h);
                                            return h;
                                        })
                );
    }
}

