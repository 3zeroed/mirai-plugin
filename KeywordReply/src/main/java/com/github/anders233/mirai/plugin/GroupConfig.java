package com.github.anders233.mirai.plugin;

import net.mamoe.mirai.console.plugins.Config;
import net.mamoe.mirai.console.plugins.ConfigSection;
import net.mamoe.mirai.console.plugins.ConfigSectionFactory;
import net.mamoe.mirai.contact.Group;

public class GroupConfig {

    private Config config;
    private ConfigSection vague_word_list;
    private ConfigSection exact_word_list;

    public GroupConfig(Group group) {
        this.config = Main.getInstance().loadConfig(group.getId() + ".yml");
        this.config.set("group_name", group.getName());
        this.config.setIfAbsent("vague_word_list", ConfigSectionFactory.create());
        this.config.setIfAbsent("exact_word_list", ConfigSectionFactory.create());
        this.vague_word_list = this.config.getConfigSection("vague_word_list");
        this.exact_word_list = this.config.getConfigSection("exact_word_list");
        save();
    }

    public GroupConfig(long group_id) {
        this.config = Main.getInstance().loadConfig(group_id + ".yml");
        this.config.set("group_name", group_id);
        this.config.setIfAbsent("vague_word_list", ConfigSectionFactory.create());
        this.config.setIfAbsent("exact_word_list", ConfigSectionFactory.create());
        this.vague_word_list = this.config.getConfigSection("vague_word_list");
        this.exact_word_list = this.config.getConfigSection("exact_word_list");
        save();
    }

    public Config getConfig() {
        return config;
    }

    public ConfigSection getExactWords() {
        return exact_word_list;
    }

    public ConfigSection getVagueWords() {
        return vague_word_list;
    }

    public void save() {
        getConfig().set("vague_word_list", vague_word_list);
        getConfig().set("exact_word_list", exact_word_list);
        getConfig().save();
    }
}
