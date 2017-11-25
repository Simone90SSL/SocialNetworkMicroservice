package controller;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import repository.neo4j.HashTagNodeRepository;
import repository.neo4j.UserNodeRepository;
import sample.data.neo4j.FollowingData;
import sample.data.neo4j.UserNode;
import sample.data.neo4j.UserTagData;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/user")
public class UserController {

    private final static int PAGE_SIZE = 100;

    @Autowired
    private UserNodeRepository userNodeRepository;

    @Autowired
    private HashTagNodeRepository hashTagNodeRepository;

    @RequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE, value = "/{id}", method = GET)
    public String getUser(@PathVariable String id) throws IOException {
        UserNode userNode = userNodeRepository.findByTwitterId(Long.parseLong(id));
        JSONObject jsonObject = new JSONObject();
        if (userNode != null){

            jsonObject.put("type",userNode.getClass());
            jsonObject.put("twitterId",userNode.getTwitterId());
            jsonObject.put("GraphId",userNode.getId());
            jsonObject.put("name",userNode.getName());
            jsonObject.put("nickname",userNode.getNickname());
            jsonObject.put("nfollowing", userNode.follows!=null?userNode.follows.size():0);
            jsonObject.put("ntags",userNode.tagsRelations!=null?userNode.tagsRelations.size():0);
        } else{
            jsonObject.put("response", "NOT FOUND");
        }

        return jsonObject.toString();
    }
    @RequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE, value = "/{id}/following", method = GET)
    public String getUserFollowing(@PathVariable String id) throws IOException {
        UserNode userNode = userNodeRepository.findByTwitterId(Long.parseLong(id));
        JSONObject jsonObject = new JSONObject();
        if (userNode != null){
            if(userNode.follows != null
                    && userNode.follows.size() > 0){
                jsonObject.put("twitterId",userNode.getTwitterId());
                jsonObject.put("following", new JSONArray(userNode.follows.stream().map(f -> f.getTwitterId()).toArray()));
            } else{
                jsonObject.put("following", new JSONArray(new ArrayList()));
            }
        } else{
            jsonObject.put("response", "USER NOT FOUND");
        }
        return jsonObject.toString();
    }

    @RequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE, value = "/{id}/tags", method = GET)
    public String getUserTags(@PathVariable String id) throws IOException {
        UserNode userNode = userNodeRepository.findByTwitterId(Long.parseLong(id));
        JSONObject jsonObject = new JSONObject();
        if (userNode != null){
            if(userNode.tagsRelations != null
                    && userNode.tagsRelations.size() > 0){
                jsonObject.put("twitterId",userNode.getTwitterId());
                jsonObject.put("hashTags", new JSONArray(userNode.tagsRelations.stream()
                        .map(f -> new JSONObject().put("tag",f.getHashTagNode().getHashTag()).put("times",f.getCount()))
                        .toArray()));
            } else{
                jsonObject.put("hashTags", new JSONArray(new ArrayList()));
            }
        } else{
            jsonObject.put("response", "USER NOT FOUND");
        }
        return jsonObject.toString();
    }
}
