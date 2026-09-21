package com.donnie1337.combateplus;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class HeadCommand implements CommandExecutor {
    private final CombatePlus plugin;

    public HeadCommand(CombatePlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(color("&cEste comando só pode ser usado por um jogador."));
            return true;
        }

        if (!player.hasPermission("combateplus.head")) {
            player.sendMessage(color("&c&lᴄᴏᴍʙᴀᴛᴇᴘʟᴜs &8• &cVocê não tem permissão."));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(color("&e&lᴄᴏᴍʙᴀᴛᴇᴘʟᴜs &8• &fUse: &7/head <mob>"));
            return true;
        }

        ItemStack cabeca = plugin.getCabecasManager().criarCabecaPorNome(args[0]);
        if (cabeca == null) {
            player.sendMessage(color("&c&lᴄᴏᴍʙᴀᴛᴇᴘʟᴜs &8• &cMob ou animal não encontrado."));
            return true;
        }

        player.getInventory().addItem(cabeca);
        player.sendMessage(color("&a&lᴄᴏᴍʙᴀᴛᴇᴘʟᴜs &8• &aVocê recebeu a cabeça de &f" + args[0] + "&a."));
        return true;
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
