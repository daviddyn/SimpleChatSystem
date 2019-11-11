package com.davidsoft.natural.chinese.commands.system;

import com.davidsoft.natural.ChatCommand;

import java.util.Calendar;

public class DateTime implements ChatCommand {

    private static Calendar calendar = Calendar.getInstance();

    private static String getWeek() {
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case 2:
                return "星期一";
            case 3:
                return "星期二";
            case 4:
                return "星期三";
            case 5:
                return "星期四";
            case 6:
                return "星期五";
            case 7:
                return "星期六";
            default:
                return "星期日";
        }
    }

    private static String getNoon() {
        int second = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
        if (second < 300) {
            return "凌晨";
        }
        else if (second < 481) {
            return "早上";
        }
        else if (second < 690) {
            return "上午";
        }
        else if (second < 780) {
            return "中午";
        }
        else if (second < 1080) {
            return "下午";
        }
        else {
            return "晚上";
        }
    }

    private static String getTime() {
        return getNoon() + calendar.get(Calendar.HOUR) + "点" + calendar.get(Calendar.MINUTE) + "分";
    }

    private static String toChinese(int month) {
        switch (month) {
            case 1:
                return "一";
            case 2:
                return "二";
            case 3:
                return "三";
            case 4:
                return "四";
            case 5:
                return "五";
            case 6:
                return "六";
            case 7:
                return "七";
            case 8:
                return "八";
            case 9:
                return "九";
            case 10:
                return "十";
            case 11:
                return "十一";
            case 12:
                return "十二";
        }
        return null;
    }

    @Override
    public String execute(String memberName, Object addition) {
        calendar.setTimeInMillis(System.currentTimeMillis());
        switch (memberName) {
            case "month":
                return toChinese(calendar.get(Calendar.MONTH) + 1) + "月";
            case "day":
                return calendar.get(Calendar.DATE) + "日";
            case "week":
                return getWeek();
            case "time":
                return getTime();
            case "noon":
                return getNoon();
        }
        return null;
    }
}