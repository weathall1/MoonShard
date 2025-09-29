package com.yourpackage;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MoonShardCommand implements CommandExecutor {
    private final MoonShard plugin;

    public MoonShardCommand(MoonShard plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("moonshard.admin")) {
            sender.sendMessage(plugin.getMessage("no-permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("用法: /moonshard <give|take|check|set> <玩家> [數量]");
            return true;
        }

        String subCommand = args[0].toLowerCase();
        String playerName = args[1];
        Player target = Bukkit.getPlayerExact(playerName);

        if (target == null) {
            sender.sendMessage(plugin.getMessage("player-not-found").replace("%player%", playerName));
            return true;
        }

        UUID uuid = target.getUniqueId();

        switch (subCommand) {
            case "give":
                if (args.length < 3) {
                    sender.sendMessage("用法: /moonshard give <玩家> <數量>");
                    return true;
                }
                handleGive(sender, target, playerName, args[2]);
                break;
            case "take":
                if (args.length < 3) {
                    sender.sendMessage("用法: /moonshard take <玩家> <數量>");
                    return true;
                }
                handleTake(sender, target, playerName, args[2]);
                break;
            case "set":
                if (args.length < 3) {
                    sender.sendMessage("用法: /moonshard set <玩家> <數量>");
                    return true;
                }
                handleSet(sender, target, playerName, args[2]);
                break;
            case "check":
                handleCheck(sender, target, playerName);
                break;
            default:
                sender.sendMessage("無效的子命令！");
        }
        return true;
    }

    private void handleGive(CommandSender sender, Player target, String playerName, String amountStr) {
        int amount = parseAmount(sender, amountStr);
        if (amount < 0) return;

        int balance = plugin.getDatabase().getBalance(target.getUniqueId());
        plugin.getDatabase().updateBalance(target.getUniqueId(), balance + amount);
        target.sendMessage(plugin.getMessage("give-success", playerName, amount, balance + amount));
    }

    private void handleTake(CommandSender sender, Player target, String playerName, String amountStr) {
        int amount = parseAmount(sender, amountStr);
        if (amount < 0) return;

        int balance = plugin.getDatabase().getBalance(target.getUniqueId());
        if (balance < amount) {
            sender.sendMessage(plugin.getMessage("insufficient-balance").replace("%player%", playerName));
            return;
        }
        plugin.getDatabase().updateBalance(target.getUniqueId(), balance - amount);
        target.sendMessage(plugin.getMessage("take-success", playerName, amount, balance - amount));
    }

    private void handleSet(CommandSender sender, Player target, String playerName, String amountStr) {
        int amount = parseAmount(sender, amountStr);
        if (amount < 0) return;

        plugin.getDatabase().updateBalance(target.getUniqueId(), amount);
        target.sendMessage(plugin.getMessage("set-success").replace("%player%", playerName).replace("%amount%", String.valueOf(amount)));
    }

    private void handleCheck(CommandSender sender, Player target, String playerName) {
        int balance = plugin.getDatabase().getBalance(target.getUniqueId());
        target.sendMessage(plugin.getMessage("check", playerName, 0, balance));
    }

    private int parseAmount(CommandSender sender, String amountStr) {
        try {
            int amount = Integer.parseInt(amountStr);
            if (amount < 0) {
                sender.sendMessage(plugin.getMessage("invalid-amount"));
                return -1;
            }
            return amount;
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getMessage("invalid-amount"));
            return -1;
        }
    }
}