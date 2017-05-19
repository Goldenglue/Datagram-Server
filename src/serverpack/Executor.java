package serverpack;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Process commands that come from server or to server. Dedicated own class for formatting reasons, hope this way it's
 * easier to add new commands and new methods for them.
 * Created by IvanOP on 04.05.2017.
 */
public class Executor {
    private Map<String, Call> stringMethodMap;
    private String[] possibleCommands;
    private String message;
    private Vector<Object> someVector = new Vector<>();

    interface Call {
        void execute();
    }

    private Call[] calls = new Call[]{
            this::receiveSerializedObject,
            this::sendSerializedObject,
            this::clearVectorOnServer,
            this::clearVectorOnClient,
            this::sizeOnServer,
            this::sizeOnClient,
            this::requestObject,
            this::sendObject,
            this::getObject
    };

    Executor() {
        try {
            setStringMethodMap();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets command for every function that works with server/client
     * -dc - disconnect from server
     * -sobjc - cannot be executed directly from server, identifies that client sent object
     * -sobjs - send someVector to server in JSON format
     * -clrc - cannot be executed directly from server,
     * identifies that client requested to clear storage of objects on server
     * -clrs - clears storage of objects on client
     * -vecsc - cannot be executed directly from server,
     * identifies that client requested size of objects storage on server
     * -vecss -  requests size of objects storage on client
     * -gobjc - cannot be executed directly from server,
     * identifies that client requested object with given number ex:-gobjs3
     * -gobjs - request of object with given number ex: -gobjc3
     * -robj - cannot be executed directly from server,
     * identifies that client sent requested object by command -gobjc
     *
     * @throws NoSuchMethodException
     */
    private void setStringMethodMap() throws NoSuchMethodException {
        possibleCommands = new String[]{"-sobjc", "-sobjs", "-clrc", "-clrs", "-vecsc", "-vecss", "-gobjs"
                , "-gobjc", "-robj"};
        stringMethodMap = new HashMap<>();
        for (int i = 0; i < possibleCommands.length; i++) {
            stringMethodMap.put(possibleCommands[i], calls[i]);
        }
    }

    void executeMessageFromClient(String message) {
        this.message = message;
        message = message.replaceAll("\\d", "");
        if (stringMethodMap.containsKey(message)) {
            for (Map.Entry<String, Call> temp : stringMethodMap.entrySet()) {
                if (temp.getKey().equals(message)) {
                    temp.getValue().execute();
                }
            }
        }
    }

    private void sendSerializedObject() {
        Connection.sendPacketOfData("-sobjs");
        Gson gson = new Gson();
        String temp = gson.toJson(someVector);
        Connection.sendPacketOfData(temp);
    }

    //i didn't really know what type incoming objects should be so i choose Object...
    private void receiveSerializedObject() {
        Gson gson = new Gson();
        String string = Connection.receivePacketOfData();
        JsonParser jsonParser = new JsonParser();
        System.out.println(string + " hehehehehe");
        JsonArray jsonArray = jsonParser.parse(string).getAsJsonArray();
        Type heh = new TypeToken<Object>() {
        }.getType();
        for (int i = 0; i < jsonArray.size(); i++) {
            System.out.println(jsonArray.get(i));
            someVector.add(gson.fromJson(jsonArray.get(i), heh));
        }
    }

    private void clearVectorOnServer() {
        someVector.removeAllElements();
        System.out.println("Vector cleared");
    }

    private void clearVectorOnClient() {
        Connection.sendPacketOfData(message);
    }

    private void sizeOnServer() {
        Connection.sendPacketOfData(String.valueOf(someVector.size()));
    }

    private void sizeOnClient() {
        Connection.sendPacketOfData(message);
        System.out.println("size on client: " + Connection.receivePacketOfData());
    }

    private void requestObject() {
        Connection.sendPacketOfData(message);
    }

    //type is used to give idea of what type object is going to be so client knows what constructor to use
    // in this situation. might as well get a better checking procedure.
    private void sendObject() {
        Gson gson = new Gson();
        message = message.replaceAll("[^0-9]", "");
        String object = gson.toJson(someVector.get(Integer.valueOf(message)));
        String type;
        if (object.contains("png")) {
            type = "Images";
        } else {
            type = "Strings";
        }
        Connection.sendPacketOfData("-robj");
        System.out.println(type);
        Connection.sendPacketOfData(type);
        System.out.println(object);
        Connection.sendPacketOfData(object);

    }

    private void getObject() {
        Gson gson = new Gson();

        String object = Connection.receivePacketOfData();
        System.out.println(object);
        Type heh = new TypeToken<Object>() {
        }.getType();
        someVector.add(gson.fromJson(object, heh));

    }
}
