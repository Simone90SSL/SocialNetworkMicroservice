package util;

import org.neo4j.ogm.exception.CypherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.ConcurrencyFailureException;
import repository.neo4j.HashTagNodeRepository;
import repository.neo4j.UserNodeRepository;
import sample.data.neo4j.HashTagNode;
import sample.data.neo4j.TagsRelation;
import sample.data.neo4j.UserNode;
import sample.data.neo4j.UserTagData;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class SocialGraphUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocialGraphUtil.class);

    private UserNodeRepository userNodeRepository;
    private HashTagNodeRepository hashTagNodeRepository;

    public SocialGraphUtil(UserNodeRepository userNodeRepository, HashTagNodeRepository hashTagNodeRepository){
        this.userNodeRepository = userNodeRepository;
        this.hashTagNodeRepository = hashTagNodeRepository;
    }

    public void getReplicatedUserNode(){
        String fileName = "/Users/simonecaldaro/LAUREA_MAGISTRALE_IN_INGEGNERIA_INFORMATICA/Tesi/SNBRS/tmp.txt";
        //read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(fileName)).parallel()) {
            stream.forEach(line -> {
                try {
                    LOGGER.info("Elaborating "+line);
                    long ida = Long.parseLong(line.split(",")[0]);
                    long idb = Long.parseLong(line.split(",")[1]);

                    UserNode a = userNodeRepository.findOne(ida);
                    UserNode b = userNodeRepository.findOne(idb);

                    UserNode c = null;
                    try{
                        c = mergeUserNode(a, b);
                    } catch (NullPointerException npe){
                        LOGGER.warn("NULL POINTER EXCEPTION --> extract again and rerun application");
                        return;
                    }

                    // Store data before remove from db
                    Set<Long> followingSet = new HashSet<>(userNodeRepository.findFollowing(c.getTwitterId()));

                    List<UserTagData> tagList = userNodeRepository.findUserTag(c.getTwitterId());
                    Map<String, Integer> tagSet = new HashMap<String, Integer>();
                    for (UserTagData userTagData: tagList){
                        if (tagSet.containsKey(userTagData.getTag())){
                            int incCount = tagSet.get(userTagData.getTag())+userTagData.getCount();
                            tagSet.put(userTagData.getTag(), incCount);
                        } else{
                            tagSet.put(userTagData.getTag(), userTagData.getCount());
                        }
                    }

                    Set<Long> followerSet = new HashSet<>(userNodeRepository.findFollower(c.getTwitterId()));

                    // Remove Old Data
                    LOGGER.info("removing old data" + c.getTwitterId());
                    try {
                        userNodeRepository.delete(c.getTwitterId());
                    } catch(CypherException ce){
                    }

                    // Restore Data
                    LOGGER.info("saving node " + c.getTwitterId());
                    userNodeRepository.save(c);

                    LOGGER.info("Adding '{}' following to '{}' ", followingSet.size(), c.getTwitterId());
                    for (long following : followingSet) {
                        c.follows.add(userNodeRepository.getOrCreate(following));
                    }
                    userNodeRepository.save(c);

                    LOGGER.info("Adding '{}' tags to '{}'", tagSet.size(), c.getTwitterId());
                    HashTagNode hashTagNode;
                    for (String tag : tagSet.keySet()) {
                        hashTagNode = hashTagNodeRepository.getOrCreate(tag);
                        c.tagsRelations.add(new TagsRelation(tagSet.get(tag), c, hashTagNode));
                    }
                    userNodeRepository.save(c);

                    LOGGER.info("Adding '{}' follower to '{}' ", followerSet.size(), c.getTwitterId());
                    UserNode follower;
                    for (Long followerId : followerSet) {
                        try {
                            follower = userNodeRepository.getOrCreate(followerId);
                        } catch (CypherException ce){
                            continue;
                        }
                        follower.follows.add(c);
                        int retry = 5;
                        do{
                            try {
                                userNodeRepository.save(follower);
                                break;
                            } catch (ConcurrencyFailureException ce){
                                retry--;
                                Thread.sleep(1000);
                            }
                        } while(retry > 0);
                        if (retry == 0){
                            throw new RuntimeException("Retry finished");
                        }
                    }
                } catch (Exception e){
                    e.printStackTrace();
                    System.exit(-1);
                }

            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static UserNode mergeUserNode(UserNode a, UserNode b){
        if (a.getTwitterId() != b.getTwitterId()){
            throw new RuntimeException("Twitter ID should be equals to merge in one node");
        }
        UserNode merge = new UserNode(a.getTwitterId());

        merge.setLocation(!a.getLocation().isEmpty()?a.getLocation():b.getLocation());
        merge.setName(!a.getName().isEmpty()?a.getName():b.getName());
        merge.setNickname(!a.getNickname().isEmpty()?a.getNickname():b.getNickname());
        merge.setUrl(!a.getUrl().isEmpty()?a.getUrl():b.getUrl());

        return merge;
    }

    public void getReplicatedHashTagNode(){
        String fileName = "/Users/simonecaldaro/LAUREA_MAGISTRALE_IN_INGEGNERIA_INFORMATICA/Tesi/SNBRS/tmp_hadhtagnode.csv";
        //read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(fileName)).parallel()) {
            stream.forEach(line -> {
                try {
                    LOGGER.info("Elaborating "+line);
                    long ida = Long.parseLong(line.split(",")[0]);
                    long idb = Long.parseLong(line.split(",")[1]);

                    HashTagNode a = hashTagNodeRepository.findOne(ida);
                    HashTagNode b = hashTagNodeRepository.findOne(idb);

                    if (a==null && b==null)
                        return;

                    HashTagNode c = new HashTagNode(a!=null?a.getHashTag():b.getHashTag());

                    // Store data before remove from db
                    List<UserTagData> userTagDataList = hashTagNodeRepository.findUserTag(c.getHashTag());
                    Map<Long, Integer> userSet = new HashMap<Long, Integer>();
                    for (UserTagData userTagData: userTagDataList){
                        if (userSet.containsKey(userTagData.getUser())){
                            int incCount = userSet.get(userTagData.getUser())+userTagData.getCount();
                            userSet.put(userTagData.getUser(), incCount);
                        } else{
                            userSet.put(userTagData.getUser(), userTagData.getCount());
                        }
                    }

                    // Remove Old Data
                    LOGGER.info("removing old data" + c.getHashTag());
                    try {
                        hashTagNodeRepository.delete(c.getHashTag());
                    } catch(CypherException ce){
                    }

                    // Restore Data
                    LOGGER.info("saving node " + c.getHashTag());
                    hashTagNodeRepository.save(c);

                    LOGGER.info("Adding '{}' tags to '{}' ", userSet.size(), c.getHashTag());
                    UserNode userNode;
                    for (Long twitterId : userSet.keySet()) {
                        userNode = userNodeRepository.getOrCreate(twitterId);
                        userNode.addTag(c, userSet.get(twitterId));
                        int retry = 5;
                        do{
                            try {
                                userNodeRepository.save(userNode);
                                break;
                            } catch(ConcurrencyFailureException ce){
                                retry --;
                                Thread.sleep(1000);
                            }
                        } while(retry>0);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                    System.exit(-1);
                }

            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
