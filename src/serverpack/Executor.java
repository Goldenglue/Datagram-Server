package serverpack;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

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
    private Map<String, NoParameterMethods> stringNoParameterMethodMap;
    private Map<String, ParameterMethod> stringParameterMethodMap;
    private String message;
    private Vector<Object> someVector = new Vector<>();

    @FunctionalInterface
    interface NoParameterMethods {
        void execute();
    }

    private NoParameterMethods[] noParameterMethods = new NoParameterMethods[]{
            this::sendSerializedObject,
            this::clearVectorOnServer,
            this::clearVectorOnClient,
            this::sizeOnServer,
            this::requestObject,
            this::sendObject
    };

    @FunctionalInterface
    interface ParameterMethod {
        void execute(String something);
    }

    private ParameterMethod[] parameterMethods = new ParameterMethod[]{
            this::receiveSerializedObject,
            this::sizeOnClient,
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
     * -clrvc - cannot be executed directly from server,
     * identifies that client requested to clear storage of objects on server
     * -clrvs - clears storage of objects on client
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
        String[] possibleNoParameterCommands = new String[]{"-sobjs", "-clrvc","-clrvs", "-vecsc", "-gobjc"
                , "-gobjs"};
        String[] possibleParameterCommands = new String[]{"-sobjc", "-vecss", "-rcobj"};
        stringNoParameterMethodMap = new HashMap<>();
        for (int i = 0; i < possibleNoParameterCommands.length; i++) {
            stringNoParameterMethodMap.put(possibleNoParameterCommands[i], noParameterMethods[i]);
        }

        stringParameterMethodMap = new HashMap<>();
        for (int i = 0; i < possibleParameterCommands.length; i++) {
            stringParameterMethodMap.put(possibleParameterCommands[i], parameterMethods[i]);
        }
    }



    void executeMessageFromClient(String[] message) {
        this.message = message[0];
        message[0] = message[0].replaceAll("\\d", "");
        System.out.println("executing command " + message[0]);
        for (Map.Entry<String, ParameterMethod> temp : stringParameterMethodMap.entrySet()) {
            if (temp.getKey().equals(message[0])) {
                temp.getValue().execute(message[1]);
            }
        }
        for (Map.Entry<String, NoParameterMethods> temp : stringNoParameterMethodMap.entrySet()) {
            if (temp.getKey().equals(message[0])) {
                temp.getValue().execute();
            }
        }
    }

    void executeMessageFromClient(String message) {
        this.message = message;
        message = message.replaceAll("\\d", "");
        for (Map.Entry<String, NoParameterMethods> temp : stringNoParameterMethodMap.entrySet()) {
            if (temp.getKey().equals(message)) {
                temp.getValue().execute();
            }
        }
    }

    private void sendSerializedObject() {
        Gson gson = new Gson();
        String temp = gson.toJson(someVector);
        Connection.sendPacketOfData(message,temp);
    }

    //i didn't really know what type incoming objects should be so i choose Object...
    private void receiveSerializedObject(String data) {
        Gson gson = new Gson();
        JsonParser jsonParser = new JsonParser();
        data = data.split("\0")[0];
        System.out.println(data + "my json");
        JsonArray jsonArray = jsonParser.parse(data).getAsJsonArray();
        Type heh = new TypeToken<Object>() {
        }.getType();
        for (int i = 0; i < jsonArray.size(); i++) {
            System.out.println(jsonArray.get(i));
            someVector.add(gson.fromJson(jsonArray.get(i), heh));
        }
        System.out.println(someVector.size());
    }

    private void clearVectorOnServer() {
        someVector.removeAllElements();
        System.out.println("Vector cleared");
    }

    private void clearVectorOnClient() {
        Connection.sendPacketOfData(message);
    }

    private void sizeOnServer() {
        System.out.println("size on server is  " + String.valueOf(someVector.size()));
        Connection.sendPacketOfData(message,String.valueOf(someVector.size()));
    }

    private void sizeOnClient(String data) {
        System.out.println("size of client: " + data);
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
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Type",type);
        jsonObject.addProperty("Object",object);
        Connection.sendPacketOfData("-robj");
        Connection.sendPacketOfData("-rcobj",gson.toJson(jsonObject));

    }

    private void getObject(String data) {
        Gson gson = new Gson();
        System.out.println(data);
        Type heh = new TypeToken<Object>() {}.getType();
        someVector.add(gson.fromJson(data, heh));

    }
}
