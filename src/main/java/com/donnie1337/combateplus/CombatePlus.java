package com.donnie1337.combateplus;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class CombatePlus extends JavaPlugin implements CommandExecutor {
    private CabecasManager cabecasManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        cabecasManager = new CabecasManager(this);
        getServer().getPluginManager().registerEvents(cabecasManager, this);
        if (getCommand("combateplus") != null) {
            getCommand("combateplus").setExecutor(this);
        }
        getLogger().info("CombatePlus ativado.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("combateplus.admin")) {
            sender.sendMessage(message("mensagens.sem-permissao"));
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            cabecasManager.reload();
            sender.sendMessage(message("mensagens.recarregado"));
            return true;
        }
        sender.sendMessage(message("mensagens.uso"));
        return true;
    }

    private String message(String path) {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString(path, ""));
    }
}
