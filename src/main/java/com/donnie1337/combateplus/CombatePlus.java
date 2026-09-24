package com.donnie1337.combateplus;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class CombatePlus extends JavaPlugin implements CommandExecutor {
    public PvPListener getPvpListener() {
        return pvpListener;
    }

    public CabecasManager getCabecasManager() {
        return cabecasManager;
    }

    private CabecasManager cabecasManager;
    private PvPListener pvpListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        cabecasManager = new CabecasManager(this);
        pvpListener = new PvPListener(this);

        getServer().getPluginManager().registerEvents(cabecasManager, this);
        getServer().getPluginManager().registerEvents(pvpListener, this);

        for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
            pvpListener.applyAttackSpeed(player);
        }

        if (getCommand("combateplus") != null) {
            getCommand("combateplus").setExecutor(this);
        }
        if (getCommand("pvp") != null) {
            getCommand("pvp").setExecutor(this);
        }
        if (getCommand("head") != null) {
            getCommand("head").setExecutor(new HeadCommand(this));
        }

        getLogger().info("CombatePlus ativado.");
    }

    @Override
    public void onDisable() {
        if (pvpListener != null) {
            pvpListener.restoreAll();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("pvp")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(message("mensagens.apenas-jogadores"));
                return true;
            }

            if (pvpListener.isPvpForced(player)) {
                sender.sendMessage(message(
                        "mensagens.pvp-obrigatorio",
                        "&c&lᴘᴠᴘ &8• &cO PvP é obrigatório no Nether e no The End."
                ));
                return true;
            }

            boolean enabled = pvpListener.togglePvp(player);
            sender.sendMessage(message(enabled ? "mensagens.pvp-ativado" : "mensagens.pvp-desativado"));
            return true;
        }

        if (!sender.hasPermission("combateplus.admin")) {
            sender.sendMessage(message("mensagens.sem-permissao"));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            cabecasManager.reload();

            for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
                pvpListener.applyAttackSpeed(player);
            }

            sender.sendMessage(message("mensagens.recarregado"));
            return true;
        }

        sender.sendMessage(message("mensagens.uso"));
        return true;
    }

    private String message(String path) {
        return message(path, "");
    }

    private String message(String path, String fallback) {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString(path, fallback));
    }
}
