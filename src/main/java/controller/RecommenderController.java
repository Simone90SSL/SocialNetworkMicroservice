package controller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import repository.mongodb.TweetDocumentRepository;
import repository.neo4j.HashTagNodeRepository;
import repository.neo4j.UserNodeRepository;
import sample.data.mongodb.TweetDocument;
import sample.data.neo4j.FollowingData;
import sample.data.neo4j.UserTagData;

import java.io.IOException;
import java.io.OutputStream;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/stream")
public class RecommenderController {

    private final static int PAGE_SIZE = 100;

    @Autowired
    private UserNodeRepository userNodeRepository;
    @Autowired
    private HashTagNodeRepository hashTagNodeRepository;
    @Autowired
    private TweetDocumentRepository tweetDocumentRepository;

    @RequestMapping(value="user", method = GET)
    void sendUserByteArray(OutputStream os) throws IOException {
        int page = 0;
        Page<Long> twitterIdPage = null;
        do{
            twitterIdPage = userNodeRepository.findAllTwitterId(new PageRequest(page++, PAGE_SIZE));
            for(long twitterId: twitterIdPage){
                os.write((twitterId+"\n").getBytes());
            }
            os.flush();
        } while(twitterIdPage.hasNext());
    }

    @RequestMapping(value="hashTag", method = GET)
    void sendHashTagByteArray(OutputStream os) throws IOException {
        int page = 0;
        Page<String> hashTagPage = null;
        do{
            hashTagPage = hashTagNodeRepository.findAllHashTags(new PageRequest(page++, PAGE_SIZE));
            for(String hashTag: hashTagPage){
                os.write((hashTag+"\n").getBytes());
            }
            os.flush();
        } while(hashTagPage.hasNext());
    }

    @RequestMapping(value="following", method = GET)
    void sendFollowingByteArray(OutputStream os) throws IOException {
        int page = 0;
        Page<FollowingData> followingPage = null;
        do{
            followingPage = userNodeRepository.findFollowing(new PageRequest(page++, PAGE_SIZE));
            for(FollowingData followingData: followingPage){
                os.write((followingData.toString()+"\n").getBytes());
            }
            os.flush();
        } while(followingPage.hasNext());
    }

    @RequestMapping(value="userTag", method = GET)
    void sendUserTagByteArray(OutputStream os) throws IOException {
        int page = 0;
        Page<UserTagData> userTagPage = null;
        do{
            userTagPage = userNodeRepository.findUserTag(new PageRequest(page++, PAGE_SIZE));
            for(UserTagData usertagData: userTagPage){
                os.write((usertagData.toString()+"\n").getBytes());
            }
            os.flush();
        } while(userTagPage.hasNext());
    }

    @RequestMapping(value="tweets", method = GET)
    void sendTweetsByteArray(OutputStream os) throws IOException {
        int page = 0;
        Page<TweetDocument> tweetDocuments = null;
        JSONObject jsonObjectDocument;
        do{
            tweetDocuments = tweetDocumentRepository.findAll(new PageRequest(page++, PAGE_SIZE));
            for(TweetDocument tweetDocument: tweetDocuments){
                jsonObjectDocument = new JSONObject()
                        .put("creator", tweetDocument.getCreator())
                        .put("twitterId", tweetDocument.getTwitterId())
                        .put("content", tweetDocument.getText());
                os.write((jsonObjectDocument.toString()+"\n").getBytes());
            }
            os.flush();
        } while(tweetDocuments.hasNext());
    }
}
