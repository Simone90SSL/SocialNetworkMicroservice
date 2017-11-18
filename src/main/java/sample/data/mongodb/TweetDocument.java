package sample.data.mongodb;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class TweetDocument {

    @Id
    private long twitterId;

    private String text;
    private String createdAt;
    private String geoLocation;
    private String lang;

    public TweetDocument(){

    }

    public TweetDocument(long twitterId, String text, String createdAt, String geoLocation, String lang) {
        this.twitterId = twitterId;
        this.text = text;
        this.createdAt = createdAt;
        this.geoLocation = geoLocation;
        this.lang = lang;
    }

    public TweetDocument(long twitterId) {
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
        return "TweetDocument{" +
                "twitterId=" + twitterId +
                ", text='" + text + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", geoLocation='" + geoLocation + '\'' +
                ", lang='" + lang + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o){
        if (o!=null && o.getClass().equals(this.getClass())){
            TweetDocument tn = (TweetDocument)o;
            return tn.twitterId == this.twitterId;
        } else{
            return false;
        }
    }

    @Override
    public int hashCode(){
        return Long.hashCode(this.twitterId);
    }

}
