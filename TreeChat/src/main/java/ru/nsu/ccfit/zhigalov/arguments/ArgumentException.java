package ru.nsu.ccfit.zhigalov.arguments;

public class ArgumentException extends Exception {
    String message;

    public ArgumentException(String usage) {
        message = usage;
    }

    public ArgumentException(Exception e, String usage) {
        message = e.getMessage() + "\n" + usage;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
