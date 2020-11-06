package ru.nsu.ccfit.zhigalov.arguments;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class ArgumentHandler {
    static final String USAGE = "USAGE: nickName port lossPercent [remoteIp] [remotePort]";

    interface Parser {
        Arguments parse(String args[]);
    }

    final Map<Integer, Parser> parsers;

    public ArgumentHandler() {
        parsers = new HashMap<>();
        parsers.put(3, (String[] args) ->
                new Arguments(
                        args[0],
                        Integer.parseInt(args[1]),
                        Integer.parseInt(args[2])
                ));
        parsers.put(5, (String[] args) ->
                new Arguments(
                        args[0],
                        Integer.parseInt(args[1]),
                        Integer.parseInt(args[2]),
                        new InetSocketAddress(args[3], Integer.parseInt(args[4]))
                ));
    }

    public Arguments parse(String[] args) throws ArgumentException {
        var parser = parsers.get(args.length);
        if (parser == null)
            throw new ArgumentException(USAGE);

        try {
            return parser.parse(args);
        } catch (IllegalArgumentException e) {
            throw new ArgumentException(e, USAGE);
        }
    }


}
