package com.davidsoft.natural;

public interface ChatCommand {
    String execute(String memberName, Object extras);
}