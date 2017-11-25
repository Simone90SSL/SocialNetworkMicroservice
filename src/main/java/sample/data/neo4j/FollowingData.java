package sample.data.neo4j;

import org.springframework.data.neo4j.annotation.QueryResult;

@QueryResult
public class FollowingData {
    long a;
    long b;

    public FollowingData(){}

    @Override
    public String toString() {
        return a + "," + b;
    }
}
