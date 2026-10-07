package com.donnie1337.combateplus;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class CombatTagListener implements Listener {
    private final CombatePlus plugin;
    private final CombatTagManager manager;

    public CombatTagListener(CombatePlus plugin, CombatTagManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPvpDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        Player attacker = resolvePlayerDamager(event.getDamager());
        if (attacker == null || attacker.getUniqueId().equals(victim.getUniqueId())) return;

        manager.tag(attacker);
        manager.tag(victim);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!manager.isTagged(player)) return;

        String raw = event.getMessage();
        if (raw == null || raw.length() < 2) return;

        String label = raw.substring(1).trim().split("\\s+", 2)[0].toLowerCase(Locale.ROOT);
        int namespace = label.indexOf(':');
        if (namespace >= 0 && namespace + 1 < label.length()) {
            label = label.substring(namespace + 1);
        }

        if (!blockedCommands().contains(label)) return;

        event.setCancelled(true);
        String message = plugin.getConfig().getString(
                "mensagens.combat-tag-comando-bloqueado",
                "&c&lᴄᴏᴍʙᴀᴛᴇ &8• &cVocê não pode usar este comando em combate. Aguarde &f{tempo}s&c."
        );
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message)
                .replace("{tempo}", String.valueOf(manager.remainingSeconds(player))));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        manager.clear(event.getPlayer(), false);
    }

    private Player resolvePlayerDamager(Entity damager) {
        if (damager instanceof Player player) return player;
        if (damager instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Player player) return player;
        }
        return null;
    }

    private Set<String> blockedCommands() {
        List<String> configured = plugin.getConfig().getStringList("combat-tag.comandos-bloqueados");
        if (configured.isEmpty()) {
            configured = List.of(
                    "rtp", "home", "homes", "sethome", "tpa", "tpaqui",
                    "tpaccept", "tpaceitar", "spawn", "pvp"
            );
        }

        Set<String> result = new HashSet<>();
        for (String command : configured) {
            if (command == null) continue;
            String normalized = command.trim().toLowerCase(Locale.ROOT);
            if (normalized.startsWith("/")) normalized = normalized.substring(1);
            int namespace = normalized.indexOf(':');
            if (namespace >= 0 && namespace + 1 < normalized.length()) {
                normalized = normalized.substring(namespace + 1);
            }
            if (!normalized.isBlank()) result.add(normalized);
        }
        return result;
    }
}
