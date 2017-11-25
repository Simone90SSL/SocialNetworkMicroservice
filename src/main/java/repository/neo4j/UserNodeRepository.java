package repository.neo4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.annotation.QueryResult;
import sample.data.neo4j.FollowingData;
import sample.data.neo4j.UserNode;
import org.springframework.data.neo4j.repository.GraphRepository;
import sample.data.neo4j.UserTagData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public interface UserNodeRepository extends GraphRepository<UserNode> {

    UserNode findByTwitterId(long twitterId);

    @Query("MATCH (a:UserNode{twitterId:{0}}) WITH a MATCH (b:UserNode{twitterId:{1}}) CREATE (a)-[r:FOLLOW]->(b)")
    void addFollow(long twitterId1, long twitterId2);

    @Query("MATCH (a:UserNode) return a.twitterId")
    Page<Long> findAllTwitterId(Pageable pageable);

    @Query("MATCH (u:UserNode)-[:FOLLOW]->(v:UserNode) return u.twitterId as a, v.twitterId as b")
    Page<FollowingData> findFollowing(Pageable pageable);

    @Query("MATCH (u:UserNode)-[t:TAGS]->(v:HashTagNode) return u.twitterId as user, v.hashTag as tag, t.count as count")
    Page<UserTagData> findUserTag(Pageable pageable);
}
