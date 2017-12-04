package loader;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.neo4j.UserNodeRepository;
import sample.data.neo4j.UserNode;
import transaction.TransactionProducer;

public class UserLoader extends Loader {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserLoader.class);

    public UserLoader(UserNodeRepository userNodeRepository, UserNode userNode) {
        super(userNodeRepository, userNode);
    }

    @Override
    public void startLoad(String dataToLoad) {
        final JSONObject userJsonObj = new JSONObject(dataToLoad);
        String url = getValueOrDefault(userJsonObj, "URL");
        String location = getValueOrDefault(userJsonObj, "LOCATION");
        String screenname = getValueOrDefault(userJsonObj, "SCREENNAME");
        String name = getValueOrDefault(userJsonObj, "NAME");

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
    }

    @Override
    public void sendTransactionResult(TransactionProducer transactionProducer) {
        transactionProducer.sendUser(userNode, loadStatus);
    }

    private String getValueOrDefault(JSONObject jsonObj, String key) {
        try {
            return jsonObj.getString(key);
        } catch (JSONException je) {
            return "";
        }
    }
}

