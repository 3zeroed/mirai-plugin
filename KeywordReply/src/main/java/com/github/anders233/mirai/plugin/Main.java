package com.github.anders233.mirai.plugin;

import net.mamoe.mirai.console.plugins.PluginBase;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.GroupMessage;
import net.mamoe.mirai.console.command.*;
import net.mamoe.mirai.console.plugins.ConfigSection;
import org.jetbrains.annotations.NotNull;

import java.util.*;

class Main extends PluginBase {

    private static Main instance;

    public LinkedHashMap<Long, GroupConfig> group_config = new LinkedHashMap<>();

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        this.getEventListener().subscribeAlways(GroupMessage.class, (GroupMessage event) -> {
            String stringMessage = event.getMessage().contentToString();
            Group group = event.getGroup();
            if (group_config.containsKey(group.getId())) {
                ConfigSection vague_word_list = group_config.get(group.getId()).getVagueWords();
                ConfigSection exact_word_list = group_config.get(group.getId()).getExactWords();
                for (String word : exact_word_list.keySet()) {
                    if (stringMessage.equals(word)) {
                        group.sendMessage(Objects.requireNonNull(exact_word_list.get(word)).toString().replace("{line}", "\n").replace("{space}", " "));
                        return;
                    }
                }
                for (String word : vague_word_list.keySet()) {
                    if (stringMessage.contains(word)) {
                        group.sendMessage(Objects.requireNonNull(vague_word_list.get(word)).toString().replace("{line}", "\n").replace("{space}", " "));
                        return;
                    }
                }
            } else {
                group_config.put(group.getId(), new GroupConfig(group));
            }
        });
        JCommandManager.getInstance().register(this, new BlockingCommand("keyword", Collections.singletonList("k"), "群关键词回复设置", "/keyword help") {
            @Override
            public boolean onCommandBlocking(@NotNull CommandSender sender, @NotNull List<String> list) {
                if (list.size() < 1) {
                    return false;
                }
                switch (list.get(0)) {
                    case "a":
                    case "add":
                        if (list.size() < 5) {
                            sender.sendMessageBlocking("用法: /keyword add <type> <word> <reply> <group_id>");
                            return true;
                        }
                        String word_type = list.get(1);
                        if (!word_type.startsWith("e") || !word_type.startsWith("v")) {
                            sender.sendMessageBlocking("未知的关键字类型\n" +
                                    "<type> -> 关键词类型有:\n" +
                                    "精准词: exact, 判断成员消息为关键字就回复\n" +
                                    "模糊词: vague, 判断成员消息包含关键字就回复");
                            return true;
                        }
                        String word = list.get(2);
                        String reply = list.get(3);
                        long group_id;
                        try {
                            group_id = Long.parseLong(list.get(4));
                        } catch (NumberFormatException e) {
                            sender.sendMessageBlocking("群号填写错误");
                            return true;
                        }
                        if (!group_config.containsKey(group_id)) group_config.put(group_id, new GroupConfig(group_id));
                        GroupConfig groupConfig = group_config.get(group_id);
                        if (word_type.startsWith("e")) groupConfig.getExactWords().set(word, reply);
                        else groupConfig.getVagueWords().set(word, reply);
                        groupConfig.save();
                        sender.sendMessageBlocking("设置成功: \n" + (word_type.startsWith("e") ? "精准" : "模糊") + "问词: " + word + "\n回答: " + reply);
                        break;
                    case "d":
                    case "del":
                        if (list.size() < 3) {
                            sender.sendMessageBlocking("用法: /keyword del <word> <group_id>");
                            return true;
                        }
                        word = list.get(1);
                        try {
                            group_id = Long.parseLong(list.get(2));
                        } catch (NumberFormatException e) {
                            sender.sendMessageBlocking("群号填写错误");
                            return true;
                        }
                        if (!group_config.containsKey(group_id)) {
                            sender.sendMessageBlocking("还未创建群配置文件, 无法进行删除设置");
                            return true;
                        }
                        groupConfig = group_config.get(group_id);
                        if (groupConfig.getVagueWords().contains(word)) {
                            groupConfig.getVagueWords().remove(word);
                            sender.sendMessageBlocking("成功删除" + "精准问词: " + word);
                        } else if (groupConfig.getExactWords().contains(word)) {
                            groupConfig.getExactWords().remove(word);
                            sender.sendMessageBlocking("成功删除" + "模糊问词: " + word);
                        } else {
                            sender.sendMessageBlocking("未找到关键词: " + word);
                        }
                        groupConfig.save();
                        break;
                    case "l":
                    case "list":
                        if (list.size() < 2) {
                            sender.sendMessageBlocking("用法: /keyword list <group_id>");
                            return true;
                        }
                        try {
                            group_id = Long.parseLong(list.get(1));
                        } catch (NumberFormatException e) {
                            sender.sendMessageBlocking("群号填写错误");
                            return true;
                        }
                        if (!group_config.containsKey(group_id)) {
                            sender.sendMessageBlocking("还未创建群配置文件, 无法进行读取设置");
                            return true;
                        }
                        groupConfig = group_config.get(group_id);
                        if (sender instanceof ConsoleCommandSender) {
                            sender.sendMessageBlocking("[群" + group_id + "关键词回复列表]");
                            sender.sendMessageBlocking("[精确词]");
                            for (String key_word : groupConfig.getExactWords().keySet()) {
                                sender.sendMessageBlocking(key_word + " -> " + groupConfig.getExactWords().get(key_word));
                            }
                            sender.sendMessageBlocking("[模糊词]");
                            for (String key_word : groupConfig.getVagueWords().keySet()) {
                                sender.sendMessageBlocking(key_word + " -> " + groupConfig.getVagueWords().get(key_word));
                            }
                            sender.sendMessageBlocking("[群" + group_id + "关键词回复列表]");
                        } else {
                            StringBuilder builder = new StringBuilder();
                            builder.append("[群").append(group_id).append("关键词回复列表]\n");
                            builder.append("[精确词]\n");
                            for (String key_word : groupConfig.getExactWords().keySet()) {
                                builder.append(key_word).append(" -> ").append(groupConfig.getExactWords().get(key_word)).append("\n");
                            }
                            builder.append("[模糊词]\n");
                            for (String key_word : groupConfig.getVagueWords().keySet()) {
                                builder.append(key_word).append(" -> ").append(groupConfig.getVagueWords().get(key_word)).append("\n");
                            }
                            builder.append("[群").append(group_id).append("关键词回复列表]");
                            sender.sendMessageBlocking(builder.toString());
                        }
                        break;
                    case "help":
                        if (sender instanceof ConsoleCommandSender) {
                            sender.sendMessageBlocking("======<Help for KeywordReply>======");
                            sender.sendMessageBlocking("<type> -> 关键词类型有:");
                            sender.sendMessageBlocking("精准词: exact, 判断成员消息为关键字就回复");
                            sender.sendMessageBlocking("模糊词: vague, 判断成员消息包含关键字就回复");
                            sender.sendMessageBlocking("/keyword add <type> <word> <reply> <group_id>");
                            sender.sendMessageBlocking("添加群关键字, <reply> -> 回复词可用: ");
                            sender.sendMessageBlocking("{line}代表换行,{space}代表空格");
                            sender.sendMessageBlocking("/keyword del <word> <group_id> 删除关键字");
                            sender.sendMessageBlocking("/keyword list <group_id> 查看群关键字列表");
                            sender.sendMessageBlocking("以上所有命令单词均可缩写,Demo:/k a e Hi Hi! 123");
                        } else {
                            sender.sendMessageBlocking("" +
                                    "<Help for KeywordReply>\n" +
                                    "<type> -> 关键词类型有:\n" +
                                    "精准词: exact, 判断成员消息为关键字就回复\n" +
                                    "模糊词: vague, 判断成员消息包含关键字就回复\n" +
                                    "/keyword add <type> <word> <reply> <group_id>\n" +
                                    "添加群关键字, <reply> -> 回复词可用: \n" +
                                    "{line}代表换行,{space}代表空格" +
                                    "/keyword del <word> <group_id> 删除关键字\n" +
                                    "/keyword list <group_id> 查看群关键字列表\n" +
                                    "以上所有命令单词均可缩写,Demo:/k a e Hi Hi!" +
                                    "");
                        }
                        return true;
                    default:
                        return false;
                }
                return true;
            }
        });
    }

    @Override
    public void onDisable() {
        for (GroupConfig groupConfig : group_config.values()) groupConfig.save();
    }
}
