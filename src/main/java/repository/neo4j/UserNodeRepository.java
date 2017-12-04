package repository.neo4j;

import cache.Cache;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.annotation.QueryResult;
import sample.data.neo4j.FollowingData;
import sample.data.neo4j.UserNode;
import org.springframework.data.neo4j.repository.GraphRepository;
import sample.data.neo4j.UserTagData;

import java.util.*;
import java.util.stream.Stream;

public interface UserNodeRepository extends GraphRepository<UserNode> {

    UserNode findByTwitterId(long twitterId);

    @Query("MATCH (a:UserNode{twitterId:{0}}) WITH a " +
           "MATCH (b:UserNode{twitterId:{1}}) CREATE (a)-[r:FOLLOW]->(b)")
    void addFollow(long twitterId1, long twitterId2);

    @Query("MATCH (a:UserNode{twitterId:{0}})-[r:FOLLOW]->(b:UserNode{twitterId:{1}}) DELETE r")
    void deleteFollow(long twitterId1, long twitterId2);

    @Query("MATCH (a:UserNode{twitterId:{0}}) WITH a " +
           "MATCH (b:HashTagNode{hashTag:{1}}) CREATE (a)-[r:TAGS{count:{2}}]->(b)")
    void addTags(long twitterId1, String hashTag, int count);

    @Query("MATCH (a:UserNode{twitterId:{0}})-[r:TAGS]->(b:HashTagNode{hashTag:{1}}) DELETE r")
    void deleteTags(long twitterId1, String hashTag);

    @Query("MATCH (a:UserNode) return a.twitterId")
    Page<Long> findAllTwitterId(Pageable pageable);

    @Query("MATCH (u:UserNode)-[:FOLLOW]->(v:UserNode) return u.twitterId as a, v.twitterId as b")
    Page<FollowingData> findFollowing(Pageable pageable);

    @Query("MATCH (u:UserNode)-[t:TAGS]->(v:HashTagNode) " +
            "return u.twitterId as user, v.hashTag as tag, t.count as count")
    Page<UserTagData> findUserTag(Pageable pageable);

    @Query("MATCH (u:UserNode{twitterId:{0}})-[t:TAGS]->(v:HashTagNode) " +
            "return u.twitterId as user, v.hashTag as tag, t.count as count")
    List<UserTagData> findUserTag(long twitterId);

    @Query("MATCH (u:UserNode{twitterId:{0}})-[:FOLLOW]->(v:UserNode) " +
            "return v.twitterId")
    List<Long> findFollowing(long twitterId);

    @Query("MATCH (u:UserNode)-[:FOLLOW]->(v:UserNode{twitterId:{0}}) " +
            "return u.twitterId")
    List<Long> findFollower(long twitterId);

    @Query("MATCH (n:UserNode{twitterId:{0}}) OPTIONAL MATCH (n)-[r]-() DELETE r,n")
    void delete(long twitterId);

    default UserNode getOrCreate(long twitterId){
        return Optional
                .ofNullable(Cache.getUser(twitterId))
                .orElse(Optional
                        .ofNullable(findByTwitterId(twitterId))
                        .orElseGet(() ->
                        {
                            UserNode x = new UserNode(twitterId);
                            save(x);
                            return x;
                        })
                );
    }
}

