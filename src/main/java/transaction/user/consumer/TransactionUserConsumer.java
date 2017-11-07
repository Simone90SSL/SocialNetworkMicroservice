package transaction.user.consumer;

import cache.Cache;
import sample.data.neo4j.UserNode;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import repository.neo4j.UserNodeRepository;
import transaction.user.producer.TransactionUserProducer;

@Component
public class TransactionUserConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionUserConsumer.class);

    @Autowired
    private UserNodeRepository userNodeRepository;

    @Autowired
    private TransactionUserProducer transactionUserProducer;

    @KafkaListener(topics = "${kafka.topic.usertransactiona}")
    public void receive(String userInfoJson) {

        long sysTime = System.currentTimeMillis();
        LOGGER.info("Received User Transaction '{}'", sysTime);
        Long TwitterID = Long.parseLong(userInfoJson.split(":")[0]);
        try {
            UserNode userNode = Cache.getUser(TwitterID);
            if (userNode == null){
                userNode = userNodeRepository.findByTwitterId(TwitterID);
                if (userNode == null) {
                    LOGGER.debug("'{}' not found --> CREATE", TwitterID);
                    userNode = new UserNode(TwitterID);
                }
                Cache.addUser(userNode);
            }

            String userJson = userInfoJson.substring(userInfoJson.indexOf(':')+1);

            final JSONObject userJsonObj = new JSONObject(userJson);

            String url = "";
            try {
                url = userJsonObj.getString("URL");
            } catch (JSONException je){}

            String location = "";
            try {
                location = userJsonObj.getString("LOCATION");
            } catch (JSONException je){}

            String screenname = "";
            try{
                screenname = userJsonObj.getString("SCREENNAME");
            } catch (JSONException je){}

            String name = "";
            try{
                name = userJsonObj.getString("NAME");
            } catch (JSONException je){}

            if (!url.isEmpty()){
                userNode.setUrl(url);
            }
            if (!location.isEmpty()) {
                userNode.setLocation(location);
            }
            if (!screenname.isEmpty()) {
                userNode.setNickname(screenname);
            }
            if (!name.isEmpty()) {
                userNode.setName(name);
            }

            userNodeRepository.save(userNode);
            transactionUserProducer.send(TwitterID+",OK");
            return;
        } catch (JSONException je) {
            LOGGER.error("JSON Error for transaction time {} and input {} ", sysTime, userInfoJson);
            LOGGER.error(je.getMessage(), je);
            je.printStackTrace();
        } catch (Exception e){
            LOGGER.error("Exception executing user transaction consumer with input '{}'", userInfoJson);
            LOGGER.error(e.getMessage(), e);
            e.printStackTrace();
        }
        transactionUserProducer.send(TwitterID+",KO");
    }
}