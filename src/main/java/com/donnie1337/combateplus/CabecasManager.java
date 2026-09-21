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
            Map.entry(EntityType.ALLAY, "beea845cc0b58ff763decffe11cd1c845c5d09c3b04fe80b0663da5c7c699eb3"),
            Map.entry(EntityType.ARMADILLO, "11e6a22de99193b0bf4912e9272cfe77ff2f0c08ed3c23dad6b738ebb3d89d"),
            Map.entry(EntityType.BAT, "6de75a2cc1c950e82f62abe20d42754379dfad6f5ff546e58f1c09061862bb92"),
            Map.entry(EntityType.BEE, "947322f831e3c168cfbd3e28fe925144b261e79eb39c771349fac55a8126473"),
            Map.entry(EntityType.CAMEL, "3642c9f71131b5df4a8c21c8c6f10684f22abafb8cd68a1d55ac4bf263a53a31"),
            Map.entry(EntityType.CAT, "389b56b5f1eb9b7d6f3be300d7ebf337a40c21f9c88e6e7d1ef7d333050030"),
            Map.entry(EntityType.CHICKEN, "64a98b1c209a271f9fef41c0141466b18a10b779e4b3b6d38084c17b0ab3190b"),
            Map.entry(EntityType.COW, "df3fc46c295a89b9e57947d1a5b1db7d04f93752e75c420e2551ed1d31373c6a"),
            Map.entry(EntityType.AXOLOTL, "43ef6ea6622e3d627d1fba12a0c9caf2310e6b82cf3cae452d888547a3553026"),
            Map.entry(EntityType.COD, "7892d7dd6aadf35f86da27fb63da4edda211df96d2829f691462a4fb1cab0"),
            Map.entry(EntityType.DOLPHIN, "8e9688b950d880b55b7aa2cfcd76e5a0fa94aac6d16f78e833f7443ea29fed3"),
            Map.entry(EntityType.GLOW_SQUID, "d31bbecda582013144aab8c09abe29a21a1243b918327a4c5cd402c2a954180f"),
            Map.entry(EntityType.PUFFERFISH, "9fb158fe5527f059b03e35ab8d6d81c3bc8ce4b63a9c6a3cb83188bda41ea3a1"),
            Map.entry(EntityType.SALMON, "20ea9a223620cdb54b357413d43bd89c4008bca6a227f3b7db97f7733ead5fcf"),
            Map.entry(EntityType.SQUID, "1f27c6e2c48a390c7e8bfdadfa41b52731bb0eccf7075ca878fe9b00cc242d5d"),
            Map.entry(EntityType.TADPOLE, "6cc9b9740bd3adeba52e0ce0a77b3dfdef8d3a40555a4e8bb67d200cd62770d0"),
            Map.entry(EntityType.TROPICAL_FISH, "e3d7767de7acd9fe9e03832574b248344f0ae06f4c1a7c6cb5c4b0923aefe56d"),
            Map.entry(EntityType.TURTLE, "212b58c841b394863dbcc54de1c2ad2648af8f03e648988c1f9cef0bc20ee23c"),
            Map.entry(EntityType.DONKEY, "7201245fcdc6cc42694d8e67e566282e07bc00a0a397fc726609c2789fd9e6b1"),
            Map.entry(EntityType.FOX, "d5513865b7f003b379f1fc1bab9710e1fd383ea181acea475a2306fd769b076"),
            Map.entry(EntityType.FROG, "79672bef44ed3f6ee3de0d5b47a9bf2cfbf7888ad5a3a8c2fbbef97a27d8811a"),
            Map.entry(EntityType.GOAT, "42d0f31734b54d42e03d641848357aaea627765cc0454c42414aa46c8df42c15"),
            Map.entry(EntityType.HORSE, "246fcc9d88764b7219f6ed45ecd7af73c409d2daddc9ddf63911fac15da66ccb"),
            Map.entry(EntityType.LLAMA, "9f7d90b305aa64313c8d4404d8d652a96eba8a754b67f4347dcccdd5a6a63398"),
            Map.entry(EntityType.MOOSHROOM, "da82eb643d056eb26dae5e2ef77db0126f71d62d0e6dfd648ed4e6ecdf0cbb8"),
            Map.entry(EntityType.MULE, "46dcda265e57e4f51b145aacbf5b59bdc6099ffd3cce0a661b2c0065d80930d8"),
            Map.entry(EntityType.OCELOT, "8c433c1347313b23b67eec92f8807aed2566ec29fd416bdf7a59c22596628355"),
            Map.entry(EntityType.PANDA, "df0085926cd8cdf3f1cf71e210cde5daf8708320547bd6df5795859c68d9b3f"),
            Map.entry(EntityType.PARROT, "e2439a109494bca415c48b99d634545baee172fb28d936fd402f547383a53eca"),
            Map.entry(EntityType.PIG, "3a9e18e56b9693e66383e6c1c58cc1b936078cba087451fd3b12567658c1c"),
            Map.entry(EntityType.POLAR_BEAR, "f072d8cb8395c2bc84547e6ac9b8edeed78c8658c0ed1b883c629c3a7e90"),
            Map.entry(EntityType.RABBIT, "90f3c8c5291c0eae68d865fd36e6a5ba6fcf9bbc5a348b58b61e69b318ed14cf"),
            Map.entry(EntityType.SHEEP, "88170c84a763f8d3ac345c913d54a8654b20e35ec291844f16ebd386a7b0023d"),
            Map.entry(EntityType.SNIFFER, "43f3be09a7353eeae94d88320cb5b242de2f719c0e5c16a486327c605db1d463"),
            Map.entry(EntityType.WOLF, "b7c8952bbc932071777ecfa3b2372fd315791d2631c2dcdf3a0ccd733daa30cf")
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

    public List<EntityType> animaisTerrestres() {
        return List.of(
                EntityType.BEE,
                EntityType.ALLAY,
                EntityType.DONKEY,
                EntityType.GOAT,
                EntityType.CAMEL,
                EntityType.HORSE,
                EntityType.RABBIT,
                EntityType.SNIFFER,
                EntityType.CHICKEN,
                EntityType.CAT,
                EntityType.OCELOT,
                EntityType.LLAMA,
                EntityType.WOLF,
                EntityType.MOOSHROOM,
                EntityType.BAT,
                EntityType.MULE,
                EntityType.SHEEP,
                EntityType.PANDA,
                EntityType.PARROT,
                EntityType.PIG,
                EntityType.FOX,
                EntityType.FROG,
                EntityType.ARMADILLO,
                EntityType.POLAR_BEAR,
                EntityType.COW
        );
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
                Map.entry("abelha", EntityType.BEE),
                Map.entry("allay", EntityType.ALLAY),
                Map.entry("tatu", EntityType.ARMADILLO),
                Map.entry("armadillo", EntityType.ARMADILLO),
                Map.entry("morcego", EntityType.BAT),
                Map.entry("bat", EntityType.BAT),
                Map.entry("camelo", EntityType.CAMEL),
                Map.entry("gato", EntityType.CAT),
                Map.entry("galinha", EntityType.CHICKEN),
                Map.entry("vaca", EntityType.COW),
                Map.entry("burro", EntityType.DONKEY),
                Map.entry("raposa", EntityType.FOX),
                Map.entry("sapo", EntityType.FROG),
                Map.entry("cabra", EntityType.GOAT),
                Map.entry("cavalo", EntityType.HORSE),
                Map.entry("jaguatirica", EntityType.OCELOT),
                Map.entry("lhama", EntityType.LLAMA),
                Map.entry("lobo", EntityType.WOLF),
                Map.entry("cachorro", EntityType.WOLF),
                Map.entry("mooshroom", EntityType.MOOSHROOM),
                Map.entry("mula", EntityType.MULE),
                Map.entry("ovelha", EntityType.SHEEP),
                Map.entry("panda", EntityType.PANDA),
                Map.entry("papagaio", EntityType.PARROT),
                Map.entry("porco", EntityType.PIG),
                Map.entry("urso_polar", EntityType.POLAR_BEAR),
                Map.entry("urso polar", EntityType.POLAR_BEAR),
                Map.entry("coelho", EntityType.RABBIT),
                Map.entry("farejador", EntityType.SNIFFER),
                Map.entry("zumbi", EntityType.ZOMBIE),
                Map.entry("esqueleto", EntityType.SKELETON),
                Map.entry("creeper", EntityType.CREEPER),
                Map.entry("piglin", EntityType.PIGLIN),
                Map.entry("axolote", EntityType.AXOLOTL),
                Map.entry("golfinho", EntityType.DOLPHIN),
                Map.entry("lula", EntityType.SQUID),
                Map.entry("lula_brilhante", EntityType.GLOW_SQUID),
                Map.entry("bacalhau", EntityType.COD),
                Map.entry("salmao", EntityType.SALMON),
                Map.entry("peixe_tropical", EntityType.TROPICAL_FISH),
                Map.entry("baiacu", EntityType.PUFFERFISH),
                Map.entry("tartaruga", EntityType.TURTLE),
                Map.entry("girino", EntityType.TADPOLE)
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
