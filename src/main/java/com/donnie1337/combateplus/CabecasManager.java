package com.donnie1337.combateplus;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public final class CabecasManager implements Listener {
    private final CombatePlus plugin;

    public CabecasManager(CombatePlus plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        // A configuração é consultada no momento do drop.
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.getConfig().getBoolean("cabecas.jogadores.ativado", true)) return;

        double chance = plugin.getConfig().getDouble("cabecas.jogadores.chance", 25.0D);
        if (!rolou(chance)) return;

        Player player = event.getEntity();
        ItemStack cabeca = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta rawMeta = cabeca.getItemMeta();

        if (rawMeta instanceof SkullMeta meta) {
            String nome = plugin.getConfig().getString("cabecas.jogadores.nome", "&fCabeça de %player%");
            meta.setDisplayName(color(nome.replace("%player%", player.getName())));

            List<String> lore = plugin.getConfig().getStringList("cabecas.jogadores.lore");
            if (!lore.isEmpty()) {
                meta.setLore(lore.stream()
                        .map(linha -> color(linha.replace("%player%", player.getName())))
                        .toList());
            }

            meta.setOwningPlayer(player);
            cabeca.setItemMeta(meta);
        }

        event.getDrops().add(cabeca);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player) return;
        if (!plugin.getConfig().getBoolean("cabecas.entidades.ativado", true)) return;

        EntityType type = entity.getType();
        String path = "cabecas.entidades.tipos." + type.name();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(path);
        if (section == null || !section.getBoolean("ativado", false)) return;

        double chance = section.getDouble("chance",
                plugin.getConfig().getDouble("cabecas.entidades.chance-padrao", 10.0D));
        if (!rolou(chance)) return;

        String materialNome = section.getString("material", "PLAYER_HEAD");
        Material material;
        try {
            material = Material.valueOf(materialNome.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().warning("Material inválido em " + path + ".material: " + materialNome);
            return;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String nome = section.getString("nome", "&fCabeça de " + nomeEntidade(type));
            meta.setDisplayName(color(nome));
            List<String> lore = section.getStringList("lore");
            if (!lore.isEmpty()) meta.setLore(lore.stream().map(this::color).toList());
            item.setItemMeta(meta);
        }

        event.getDrops().add(item);
    }

    private boolean rolou(double chance) {
        if (chance <= 0.0D) return false;
        if (chance >= 100.0D) return true;
        return ThreadLocalRandom.current().nextDouble(100.0D) < chance;
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private String nomeEntidade(EntityType type) {
        String raw = type.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        StringBuilder result = new StringBuilder();
        for (String parte : raw.split(" ")) {
            if (!result.isEmpty()) result.append(' ');
            result.append(Character.toUpperCase(parte.charAt(0))).append(parte.substring(1));
        }
        return result.toString();
    }
}
