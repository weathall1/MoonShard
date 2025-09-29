package com.yourpackage;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import java.io.File;
import org.bukkit.entity.Player;

public class MoonShard extends JavaPlugin {
    private Database database;
    private FileConfiguration langConfig;
    private int startingBalance;

    @Override
    public void onEnable() {
        // 保存預設配置文件
        saveDefaultConfig();
        loadLanguage(getConfig().getString("language", "en_US"));

        // 初始餘額
        startingBalance = getConfig().getInt("starting-balance", 0);

        // 初始化資料庫
        database = new Database(this);
        if (!database.connect()) {
            getLogger().severe("Failed to connect to database. Disabling plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        database.createTable();

        // 註冊命令
        getCommand("moonshard").setExecutor(new MoonShardCommand(this));

        // 註冊 PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MoonShardPlaceholder(this).register();
        }
    }

    @Override
    public void onDisable() {
        if (database != null) {
            database.disconnect();
        }
    }

    private void loadLanguage(String lang) {
        File langFile = new File(getDataFolder(), "lang/" + lang + ".yml");
        if (!langFile.exists()) {
            saveResource("lang/" + lang + ".yml", false);
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    public String getMessage(String key) {
        String message = langConfig.getString(key, "Message not found: " + key);
        return langConfig.getString("prefix", "") + message.replace("&", "§");
    }

    public String getMessage(String key, String player, int amount, int balance) {
        return getMessage(key)
                .replace("%player%", player)
                .replace("%amount%", String.valueOf(amount))
                .replace("%balance%", String.valueOf(balance));
    }

    public Database getDatabase() {
        return database;
    }

    public int getStartingBalance() {
        return startingBalance;
    }
}

