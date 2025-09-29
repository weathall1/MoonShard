package com.yourpackage;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

// PlaceholderAPI 擴展
class MoonShardPlaceholder extends PlaceholderExpansion {
    private final MoonShard plugin;

    public MoonShardPlaceholder(MoonShard plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "moonshard";
    }

    @Override
    public String getAuthor() {
        return "YourName";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) return "";
        if (identifier.equals("eco")) {
            return String.valueOf(plugin.getDatabase().getBalance(player.getUniqueId()));
        }
        return null;
    }
}