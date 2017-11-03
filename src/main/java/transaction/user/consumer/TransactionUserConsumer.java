package transaction.user.consumer;

import cache.Cache;
import domain.User;
import org.neo4j.ogm.drivers.http.request.HttpRequestException;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONException;
import org.neo4j.ogm.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import repository.neo4j.UserRepository;
import transaction.user.producer.TransactionUserProducer;

import java.util.HashMap;

@Component
public class TransactionUserConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionUserConsumer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionUserProducer transactionUserProducer;

    @KafkaListener(topics = "${kafka.topic.usertransactiona}")
    public void receive(String userInfoJson) {

        long sysTime = System.currentTimeMillis();
        LOGGER.info("Received User Transaction '{}'", sysTime);
        Long TwitterID = Long.parseLong(userInfoJson.split(":")[0]);
        try {
            User user = Cache.getUser(TwitterID);
            if (user == null){
                user = userRepository.findByTwitterId(TwitterID);
                if (user == null) {
                    LOGGER.debug("'{}' not found --> CREATE", TwitterID);
                    user = new User(TwitterID);
                    this.userRepository.save(user);
                    user = this.userRepository.findByTwitterId(TwitterID);
                }
                Cache.addUser(user);
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

            String email = "";
            try{
                email = userJsonObj.getString("EMAIL");
            } catch (JSONException je){}

            user.setUrl(url);
            user.setLocation(location);
            user.setNickname(screenname);
            user.setName(name);
            user.setEmail(email);
            userRepository.save(user);
            transactionUserProducer.send(TwitterID+",OK");
            return;
        } catch (JSONException e) {
            LOGGER.error("JSON Error");
            e.printStackTrace();
        }
        transactionUserProducer.send(TwitterID+",KO");
    }
}