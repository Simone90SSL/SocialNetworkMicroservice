package sample.data.neo4j;

import org.springframework.data.neo4j.annotation.QueryResult;

@QueryResult
public class UserTagData {

    long user;
    String tag;
    int count;

    public UserTagData(){}

    @Override
    public String toString() {
        return user + "," + tag + "," + count;
    }
}
