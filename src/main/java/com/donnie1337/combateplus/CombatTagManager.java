package com.donnie1337.combateplus;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CombatTagManager {
    private final CombatePlus plugin;
    private final Map<UUID, Long> taggedUntil = new ConcurrentHashMap<>();
    private BukkitTask expiryTask;

    public CombatTagManager(CombatePlus plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stopTask();
        expiryTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::expireTags, 20L, 20L);
    }

    public void shutdown() {
        stopTask();
        taggedUntil.clear();
    }

    public void tag(Player player) {
        if (player == null || !plugin.getConfig().getBoolean("combat-tag.ativado", true)) return;

        long durationSeconds = Math.max(1L, plugin.getConfig().getLong("combat-tag.duracao-segundos", 15L));
        long now = System.currentTimeMillis();
        UUID uuid = player.getUniqueId();
        boolean wasTagged = isTagged(uuid);

        taggedUntil.put(uuid, now + durationSeconds * 1000L);

        if (!wasTagged) {
            player.sendMessage(message(
                    "mensagens.combat-tag-iniciado",
                    "&c&lᴄᴏᴍʙᴀᴛᴇ &8• &fVocê entrou em combate por &c{tempo}s&f."
            ).replace("{tempo}", String.valueOf(durationSeconds)));
        }
    }

    public boolean isTagged(Player player) {
        return player != null && isTagged(player.getUniqueId());
    }

    public boolean isTagged(UUID uuid) {
        if (uuid == null) return false;
        Long until = taggedUntil.get(uuid);
        if (until == null) return false;
        if (until <= System.currentTimeMillis()) {
            taggedUntil.remove(uuid, until);
            return false;
        }
        return true;
    }

    public long remainingSeconds(Player player) {
        if (player == null) return 0L;
        Long until = taggedUntil.get(player.getUniqueId());
        if (until == null) return 0L;
        long remainingMillis = until - System.currentTimeMillis();
        return remainingMillis <= 0L ? 0L : (remainingMillis + 999L) / 1000L;
    }

    public void clear(Player player, boolean notify) {
        if (player == null) return;
        boolean removed = taggedUntil.remove(player.getUniqueId()) != null;
        if (removed && notify && player.isOnline()) {
            player.sendMessage(message(
                    "mensagens.combat-tag-finalizado",
                    "&a&lᴄᴏᴍʙᴀᴛᴇ &8• &fVocê saiu de combate."
            ));
        }
    }

    private void expireTags() {
        long now = System.currentTimeMillis();
        for (Map.Entry<UUID, Long> entry : taggedUntil.entrySet()) {
            if (entry.getValue() > now) continue;
            if (!taggedUntil.remove(entry.getKey(), entry.getValue())) continue;

            Player player = plugin.getServer().getPlayer(entry.getKey());
            if (player != null && player.isOnline()) {
                player.sendMessage(message(
                        "mensagens.combat-tag-finalizado",
                        "&a&lᴄᴏᴍʙᴀᴛᴇ &8• &fVocê saiu de combate."
                ));
            }
        }
    }

    private String message(String path, String fallback) {
        return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString(path, fallback));
    }

    private void stopTask() {
        if (expiryTask != null) {
            expiryTask.cancel();
            expiryTask = null;
        }
    }
}
