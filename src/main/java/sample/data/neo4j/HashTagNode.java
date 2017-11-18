package sample.data.neo4j;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class HashTagNode {

    @GraphId
    Long id;

    @Index(unique=true)
    private String hashTag;

    private HashTagNode(){ }

    public HashTagNode(String hashTag) {
        this.hashTag = hashTag;
    }

    public String getHashTag() {
        return hashTag;
    }

    public void setHashTag(String hashTag) {
        this.hashTag = hashTag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HashTagNode that = (HashTagNode) o;

        return hashTag.equals(that.hashTag);
    }

    @Override
    public int hashCode() {
        return hashTag.hashCode();
    }

    @Override
    public String toString() {
        return "HashTagNode{" +
                "hashTag='" + hashTag + '\'' +
                '}';
    }
}
