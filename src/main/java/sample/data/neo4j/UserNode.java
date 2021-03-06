package sample.data.neo4j;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.*;

@NodeEntity
public class UserNode {

    @GraphId
    Long id;


    @Index(unique=true)
    private long twitterId;

    private String name;
    private String nickname;
    private String location;
    private String url;

    @Relationship(type = "FOLLOW")
    public Set<UserNode> follows;

    @Relationship(type = "TAGS")
    public List<TagsRelation> tagsRelations;

    @Relationship(type = "MAY_LIKE")
    public List<HashTagNode> recommendations;

    public UserNode() {
        // Empty constructor required as of Neo4j API 2.0.5
    };

    public UserNode(long twitterId, String name, String nickname, String location, String url) {
        this.twitterId = twitterId;
        this.name = name;
        this.nickname = nickname;
        this.location = location;
        this.url = url;

        this.follows = new HashSet<>();
        this.tagsRelations = new ArrayList<>();
        this.recommendations = new ArrayList<>();
    }

    public UserNode(long twitterId) {
        this(twitterId, "", "", "", "");
    }

    public long getTwitterId() {
        return twitterId;
    }

    public String getName() {
        return name;
    }

    public String getNickname() {
        return nickname;
    }

    public String getLocation() {
        return location;
    }

    public String getUrl() {
        return url;
    }


    public Set<UserNode> getFollows() {
        return follows;
    }

    public void setTwitterId(long twitterId) {
        this.twitterId = twitterId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "UserNode{" +
                "id=" + id +
                ", twitterId=" + twitterId +
                ", name='" + name + '\'' +
                ", nickname='" + nickname + '\'' +
                ", location='" + location + '\'' +
                ", url='" + url + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o){
        if (o!=null && o.getClass().equals(this.getClass())){
            UserNode un = (UserNode)o;
            return un.twitterId == this.twitterId;
        } else{
            return false;
        }
    }

    @Override
    public int hashCode(){
        return Long.hashCode(this.twitterId);
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFollows(Set<UserNode> follows) {
        this.follows = follows;
    }

    public void addTag(HashTagNode hashTagNode, int count){
        if (tagsRelations == null)
            tagsRelations = new ArrayList<>();

        int i = tagsRelations.indexOf(new TagsRelation(count, this, hashTagNode));
        if (i == -1){
            tagsRelations.add(new TagsRelation(count, this, hashTagNode));
        } else{
            tagsRelations.get(i).incrementCount(count);
        }
    }

    public void addRecommendation(HashTagNode hashTagNode) {
        if (recommendations == null)
            recommendations = new ArrayList<>();

        if (!recommendations.contains(hashTagNode))
            recommendations.add(hashTagNode);
    }
}
