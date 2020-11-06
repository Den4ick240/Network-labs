package ru.nsu.ccfit.zhigalov.connection.saferebuilding;

import ru.nsu.ccfit.zhigalov.message.DeputyMessage;

public class MessageConverter extends ru.nsu.ccfit.zhigalov.message.MessageConverter {
    public MessageConverter() {
        messageCreatorMap.put(UnconfirmedMessagesRequest.class.getName(), (String[] args) ->
                new UnconfirmedMessagesRequest(args[1], args[2], args[3]));
    }
}
