package server.websocket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;

public class CommandSerializer {

    public static Gson createSerializer() {
        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapter(UserGameCommand.class,
                (JsonDeserializer<UserGameCommand>) (el, type, ctx) -> {
                    if (el.isJsonObject()) {
                        String commandString =
                                el.getAsJsonObject().get("serverMessageType").getAsString();
                        UserGameCommand.CommandType commandType =
                                UserGameCommand.CommandType.valueOf(commandString);
                        return switch (commandType) {
                            case CONNECT, LEAVE, RESIGN -> ctx.deserialize(el, UserGameCommand.class);
                            case MAKE_MOVE -> ctx.deserialize(el, MakeMoveCommand.class);
                            default -> null;
                        };
                    } else {
                        return null;
                    }
                });

        return gsonBuilder.create();
    }


}
