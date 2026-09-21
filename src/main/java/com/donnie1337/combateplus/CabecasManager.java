package com.donnie1337.combateplus;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
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
        if (!plugin.getConfig().getBoolean("cabecas.mobs-e-animais", true)) return;

        double chance = plugin.getConfig().getDouble("cabecas.chance-mobs-e-animais", 10.0D);
        if (!rolou(chance)) return;

        ItemStack cabeca = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = cabeca.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(color("&fCabeça de " + nomeEntidade(entity)));
            cabeca.setItemMeta(meta);
        }

        event.getDrops().add(cabeca);
    }


    public ItemStack criarCabecaPorNome(String nome) {
        EntityType tipo = resolverTipo(nome);
        if (tipo == null) return null;

        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color("&fCabeça de " + nomeEntidade(tipo)));
            item.setItemMeta(meta);
        }
        return item;
    }

    private EntityType resolverTipo(String nome) {
        String alvo = nome.toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');

        java.util.Map<String, EntityType> aliases = java.util.Map.ofEntries(
                java.util.Map.entry("ovelha", EntityType.SHEEP),
                java.util.Map.entry("porco", EntityType.PIG),
                java.util.Map.entry("vaca", EntityType.COW),
                java.util.Map.entry("galinha", EntityType.CHICKEN),
                java.util.Map.entry("cavalo", EntityType.HORSE),
                java.util.Map.entry("lobo", EntityType.WOLF),
                java.util.Map.entry("cachorro", EntityType.WOLF),
                java.util.Map.entry("gato", EntityType.CAT),
                java.util.Map.entry("coelho", EntityType.RABBIT),
                java.util.Map.entry("zumbi", EntityType.ZOMBIE),
                java.util.Map.entry("esqueleto", EntityType.SKELETON),
                java.util.Map.entry("creeper", EntityType.CREEPER),
                java.util.Map.entry("piglin", EntityType.PIGLIN)
        );

        EntityType alias = aliases.get(alvo);
        if (alias != null) return alias;

        for (EntityType candidate : EntityType.values()) {
            if (candidate.name().equalsIgnoreCase(alvo)) return candidate;
        }
        return null;
    }

    private boolean rolou(double chance) {
        if (chance <= 0.0D) return false;
        if (chance >= 100.0D) return true;
        return ThreadLocalRandom.current().nextDouble(100.0D) < chance;
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private String nomeEntidade(LivingEntity entity) {
        return nomeEntidade(entity.getType());
    }

    private String nomeEntidade(EntityType type) {
        String raw = type.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        StringBuilder result = new StringBuilder();

        for (String parte : raw.split(" ")) {
            if (!result.isEmpty()) result.append(' ');
            result.append(Character.toUpperCase(parte.charAt(0)))
                    .append(parte.substring(1));
        }

        return result.toString();
    }}
