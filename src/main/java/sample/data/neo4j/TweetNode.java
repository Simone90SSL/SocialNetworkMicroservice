package sample.data.neo4j;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class TweetNode {

    @GraphId
    Long id;

    @Index(unique=true)
    private long twitterId;

    private String text;
    private String createdAt;
    private String geoLocation;
    private String lang;

    public TweetNode(){

    }

    public TweetNode(long twitterId, String text, String createdAt, String geoLocation, String lang) {
        this.twitterId = twitterId;
        this.text = text;
        this.createdAt = createdAt;
        this.geoLocation = geoLocation;
        this.lang = lang;
    }

    public TweetNode(long twitterId) {
        this(twitterId, "", "", "", "");
    }

    public long getTwitterId() {
        return twitterId;
    }

    public String getText() {
        return text;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getGeoLocation() {
        return geoLocation;
    }

    public String getLang() {
        return lang;
    }

    public void setTwitterId(long twitterId) {
        this.twitterId = twitterId;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setGeoLocation(String geoLocation) {
        this.geoLocation = geoLocation;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    @Override
    public String toString() {
        return "TweetNode{" +
                "id=" + id +
                ", twitterId=" + twitterId +
                ", text='" + text + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", geoLocation='" + geoLocation + '\'' +
                ", lang='" + lang + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o){
        if (o!=null && o.getClass().equals(this.getClass())){
            TweetNode tn = (TweetNode)o;
            return tn.twitterId == this.twitterId;
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
}
