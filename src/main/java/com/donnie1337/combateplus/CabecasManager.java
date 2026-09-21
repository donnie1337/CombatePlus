package com.donnie1337.combateplus;

import org.bukkit.Bukkit;
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
import org.bukkit.profile.PlayerProfile;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class CabecasManager implements Listener {
    private final CombatePlus plugin;

    private static final Map<EntityType, String> TEXTURAS = Map.ofEntries(
            Map.entry(EntityType.SHEEP, "88170c84a763f8d3ac345c913d54a8654b20e35ec291844f16ebd386a7b0023d"),
            Map.entry(EntityType.PIG, "d875eb45aca34a4d24c3dc1395fc020ccf37f825a17b054a22fd24b189c24c"),
            Map.entry(EntityType.COW, "d2521e2639ebddab5a89e41f1187a7cee0a55ddaa9956b8676a9fd857c4bf68e"),
            Map.entry(EntityType.CHICKEN, "dc535a78863988fbd28061babc68ed9048dc090ac295c51d34635a326b9512e2"),
            Map.entry(EntityType.HORSE, "60a2db2f1eb93e5978d2dc91a74df43d7b75d9ec0e694fd7f2a652fbd15"),
            Map.entry(EntityType.WOLF, "6b565d929644a8ce14c50dfbba33a788cdb06f23957324283dcee7e06d4eb00"),
            Map.entry(EntityType.CAT, "9aabeb3bd9afa2c22e1b312383be7953640c5b0138aeffbbfa350daaf9fea"),
            Map.entry(EntityType.RABBIT, "5f6f6945d3e2860ff7dd35df790dced38d6c615e44beccb63143758e9b8d24c"),
            Map.entry(EntityType.ZOMBIE, "98a6884497de191592ec42bddae50ebe75edbe2468b968732609530d4171aa11"),
            Map.entry(EntityType.CREEPER, "d8e77b9937b793a63935d1b192d3623fc8c48b82163a3425d9af1931ac19ed"),
            Map.entry(EntityType.PIGLIN, "6086148ff6fb19b94d83b2d6f76e30d72aba9a0b30c455187a5bb2414b5872e51"),
            Map.entry(EntityType.PANDA, "617700e458b0c7460c88dbde7b98d3d7c80fe958b64cb79de1b4bb1f27d9080")
    );

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

        double chance = plugin.getConfig().getDouble("cabecas.chance", 100.0D);
        if (!rolou(chance)) return;

        ItemStack cabeca = criarCabeca(entity.getType());
        if (cabeca != null) {
            event.getDrops().add(cabeca);
        }
    }

    public ItemStack criarCabecaPorNome(String nome) {
        EntityType tipo = resolverTipo(nome);
        if (tipo == null) return null;
        return criarCabeca(tipo);
    }

    private ItemStack criarCabeca(EntityType tipo) {
        Material material = switch (tipo) {
            case ZOMBIE -> Material.ZOMBIE_HEAD;
            case SKELETON -> Material.SKELETON_SKULL;
            case CREEPER -> Material.CREEPER_HEAD;
            case PIGLIN -> Material.PIGLIN_HEAD;
            case WITHER_SKELETON -> Material.WITHER_SKELETON_SKULL;
            case ENDER_DRAGON -> Material.DRAGON_HEAD;
            default -> Material.PLAYER_HEAD;
        };

        String textura = TEXTURAS.get(tipo);

        ItemStack item = new ItemStack(material);
        ItemMeta rawMeta = item.getItemMeta();

        if (!(rawMeta instanceof SkullMeta meta)) return item;

        meta.setDisplayName(color("&fCabeça de " + nomeEntidade(tipo)));

        // Cabeças vanilla usam sua própria textura e não precisam de PlayerProfile.
        // Animais que não possuem um item de cabeça vanilla continuam usando textura customizada.
        if (material == Material.PLAYER_HEAD && textura != null) {
            try {
                PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID(), "CombatePlus");
                profile.getTextures().setSkin(new URL("https://textures.minecraft.net/texture/" + textura));
                meta.setOwnerProfile(profile);
            } catch (MalformedURLException exception) {
                plugin.getLogger().warning("Textura inválida para " + tipo.name() + ".");
            }
        }

        item.setItemMeta(meta);
        return item;
    }

    private EntityType resolverTipo(String nome) {
        String alvo = nome.toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');

        Map<String, EntityType> aliases = Map.ofEntries(
                Map.entry("ovelha", EntityType.SHEEP),
                Map.entry("porco", EntityType.PIG),
                Map.entry("vaca", EntityType.COW),
                Map.entry("galinha", EntityType.CHICKEN),
                Map.entry("cavalo", EntityType.HORSE),
                Map.entry("lobo", EntityType.WOLF),
                Map.entry("cachorro", EntityType.WOLF),
                Map.entry("gato", EntityType.CAT),
                Map.entry("coelho", EntityType.RABBIT),
                Map.entry("zumbi", EntityType.ZOMBIE),
                Map.entry("esqueleto", EntityType.SKELETON),
                Map.entry("creeper", EntityType.CREEPER),
                Map.entry("piglin", EntityType.PIGLIN),
                Map.entry("panda", EntityType.PANDA)
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
    }
}
