package com.davidsoft.natural.chinese.commands;

import com.davidsoft.natural.ChatCommand;

public class Self implements ChatCommand {

    @Override
    public String execute(String memberName, Object addition) {
        switch (memberName) {
            case "name":
                return "...抱歉，丁奕宁还没给我起名字";
        }
        return null;
    }
}