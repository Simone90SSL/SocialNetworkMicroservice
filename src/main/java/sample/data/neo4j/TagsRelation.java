package sample.data.neo4j;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.springframework.data.annotation.Id;

@RelationshipEntity(type="TAGS")
public class TagsRelation {

    @Id
    Long id;

    @Property
    private int count;

    @StartNode
    private UserNode userNode;

    @EndNode
    private HashTagNode hashTagNode;

    public TagsRelation() {
    }

    public TagsRelation(int count, UserNode userNode, HashTagNode hashTagNode) {
        this.count = count;
        this.userNode = userNode;
        this.hashTagNode = hashTagNode;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public UserNode getUserNode() {
        return userNode;
    }

    public void setUserNode(UserNode userNode) {
        this.userNode = userNode;
    }

    public HashTagNode getHashTagNode() {
        return hashTagNode;
    }

    public void setHashTagNode(HashTagNode hashTagNode) {
        this.hashTagNode = hashTagNode;
    }

    public void incrementCount(){
        this.incrementCount(1);
    }
    public void incrementCount(int increment){
        this.count += increment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        TagsRelation that = (TagsRelation) o;

        if (!userNode.equals(that.userNode))
            return false;

        return hashTagNode.equals(that.hashTagNode);
    }

    @Override
    public int hashCode() {
        int result = userNode.hashCode();
        result = 31 * result + hashTagNode.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TagsRelation{" +
                "count=" + count +
                ", userNode=" + userNode +
                ", hashTagNode=" + hashTagNode +
                '}';
    }
}
