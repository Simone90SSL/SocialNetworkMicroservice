package controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import repository.neo4j.HashTagNodeRepository;
import repository.neo4j.UserNodeRepository;
import sample.data.neo4j.HashTagNode;
import sample.data.neo4j.UserNode;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/recommendation")
public class RecommendationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecommendationController.class);

    @Autowired
    private UserNodeRepository userNodeRepository;
    @Autowired
    private HashTagNodeRepository hashTagNodeRepository;

    @RequestMapping(value = "user/{userId}", method = POST)
    ResponseEntity<String> add(@PathVariable String userId, @RequestBody String input) {
        LOGGER.info("Recommending items to '{}'", userId);
        try {
            UserNode userNode = userNodeRepository.findByTwitterId(Long.parseLong(userId));
            if (userNode == null){
                LOGGER.info("User not found '{}'", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Not Found");
            }

            HashTagNode hashTagNode;
            for(String recommendation: input.split(",")){
                hashTagNode = hashTagNodeRepository.getOrCreate(recommendation);
                userNode.addRecommendation(hashTagNode);
            }
            userNodeRepository.save(userNode);
            return ResponseEntity.status(HttpStatus.OK).build();

        } catch (NumberFormatException nfe){
            LOGGER.info("UserId is not a number '{}'", userId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User id must be a long number");
        }
    }

    @RequestMapping(value = "user/{userId}", method = GET)
    ResponseEntity<String> get(@PathVariable String userId) {
        LOGGER.info("Recommending items to '{}'", userId);
        try {
            UserNode userNode = userNodeRepository.findByTwitterId(Long.parseLong(userId));
            if (userNode == null){
                LOGGER.info("User not found '{}'", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Not Found");
            }
            StringBuilder stringBuilder = new StringBuilder();
            userNode.recommendations.forEach(stringBuilder::append);
            return ResponseEntity.status(HttpStatus.OK).body(stringBuilder.toString());

        } catch (NumberFormatException nfe){
            LOGGER.info("UserId is not a number '{}'", userId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User id must be a long number");
        }
    }


}
