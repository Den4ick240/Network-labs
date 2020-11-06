package ru.nsu.ccfit.zhigalov.message;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageConverter {
    public interface MessageCreator {
        public Message create(String[] string);
    }

    private static final Logger log = Logger.getLogger(MessageConverter.class.getName());
    protected final Map<String, MessageCreator> messageCreatorMap;

    public MessageConverter() {
        var map = new HashMap<String, MessageCreator>();
        map.put(Disconnect.class.getName(), (String[] unused) -> new Disconnect());

        map.put(Ping.class.getName(), (String[] unused) -> new Ping());

        map.put(ConnectionRequestMessage.class.getName(), (String[] args) ->
                new ConnectionRequestMessage(args[1]));

        map.put(Confirmation.class.getName(), (String[] args) ->
                new Confirmation(args[1], args[2]));

        map.put(DataMessage.class.getName(), (String[] args) ->
                new DataMessage(args[1], args[2]));

        map.put(DeputyMessage.class.getName(), (String[] args) ->
                new DeputyMessage(args[1], args[2], args[3]));


        messageCreatorMap = map;
    }

    protected Message createMessage(String[] args) {
        var creator = messageCreatorMap.get(args[0]);
        if (creator == null) {
            log.log(Level.WARNING, "Unknown message type: " + args[0]);
        } else {
            try {
                return creator.create(args);
            } catch (IllegalArgumentException e) {
                log.log(Level.WARNING, "Couldn't decode message", e);
            }
        }
        return new Ping();
    }


    public Message bytesToMessageObject(byte[] bytes) {
        var args = new String(bytes).split(Message.separator);
        return createMessage(args);
    }

    public byte[] messageObjectToBytes(Message message) {
        return message.toString().getBytes();
    }
}
