package ru.nsu.ccfit.zhiglaov.networklabs.networklab2;


import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.*;

import java.util.Locale;

public class ArgumentHandler {
    final static String DEFAULT_IP_ADDR = "localhost";
    final static Integer DEFAULT_PORT = 6789;

    final static String
            DESCRIPTION = "Start server or client",
            SUBPARSERS_HELP = "Sub-command help",
            SERVER_HELP = "launch app in server mode",
            CLIENT_HELP = "launch app in client mode",
            PORT_ARG_HELP_SERVER = "port which server will listen to",
            PORT_ARG_HELP_CLIENT = "port which client will send messages to",
            FILE_ARG_HELP = "file to send",
            IP_ADDR_ARG_HELP = "ip",
            SERVER_ARG_NAME = "server",
            CLIENT_ARG_NAME = "client",
            PORT_ARG_NAME = "port",
            IP_ADDR_ARG_NAME = "ip_addr",
            FILE_ARG_NAME = "file",
            APP_MODE_ARG_NAME = "mode";

    enum Mode {
        SERVER, CLIENT
    }

    private final ArgumentParser parser;

    public ArgumentHandler(String className, Locale locale) {
        parser = ArgumentParsers.newFor(className).locale(locale)
                .build()
                .defaultHelp(true)
                .description(DESCRIPTION);
        Subparsers subparsers = parser.addSubparsers().help(SUBPARSERS_HELP);

        Subparser serverSubparser = subparsers.addParser(SERVER_ARG_NAME)
                .setDefault(APP_MODE_ARG_NAME, Mode.SERVER)
                .help(SERVER_HELP);
        serverSubparser.addArgument("-p", "--" + PORT_ARG_NAME)
                .type(Integer.class)
                .help(PORT_ARG_HELP_SERVER)
                .setDefault(DEFAULT_PORT);

        Subparser clientSubparser = subparsers.addParser(CLIENT_ARG_NAME)
                .setDefault(APP_MODE_ARG_NAME, Mode.CLIENT)
                .help(CLIENT_HELP);
        clientSubparser.addArgument("-i", "--" + IP_ADDR_ARG_NAME)
                .type(String.class)
                .help(IP_ADDR_ARG_HELP)
                .setDefault(DEFAULT_IP_ADDR);
        clientSubparser.addArgument("-p", "--" + PORT_ARG_NAME)
                .type(Integer.class)
                .help(PORT_ARG_HELP_CLIENT)
                .setDefault(DEFAULT_PORT);
        clientSubparser.addArgument("-f", "--" + FILE_ARG_NAME)
                .type(Arguments.fileType().acceptSystemIn().verifyCanRead())
                .help(FILE_ARG_HELP)
                .required(true);
    }

    public IArguments parse(String[] args) throws Exception {
        Namespace ns;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            throw e;
        }
        return switch ((Mode) ns.get(APP_MODE_ARG_NAME)) {
            case CLIENT -> new ClientArguments(ns.getString(IP_ADDR_ARG_NAME), ns.getInt(PORT_ARG_NAME), ns.getString(FILE_ARG_NAME));
            case SERVER -> new ServerArguments(ns.getInt(PORT_ARG_NAME));
        };
    }
}
