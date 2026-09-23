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
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Method;
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
            Map.entry(EntityType.CHICKEN, "47479088e936d4fdf73373fa4e5888080cfed1cb43af2c3aa2be7c6631a9e0"),
            Map.entry(EntityType.COW, "ecc2d8fe1a004916e9f2d9d5043071aa21821bb80b221a44d5be9ca9da417419"),
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
            Map.entry(EntityType.DROWNED, "c84df79c49104b198cdad6d99fd0d0bcf1531c92d4ab6269e40b7d3cbbb8e98c"),
            Map.entry(EntityType.ELDER_GUARDIAN, "30f868caf19cf2124f0fef98e6b8773d27fbf42d93aab06b22ee033b2aee6447"),
            Map.entry(EntityType.ENDER_DRAGON, "ffcdae586b52403b92b1857ee4331bac636af08bab92ba5750a54a83331a6353"),
            Map.entry(EntityType.GUARDIAN, "932c24524c82ab3b3e57c2052c533f13dd8c0beb8bdd06369bb2554da86c123"),
            Map.entry(EntityType.NAUTILUS, "2968cac7af588bc410ecb47635f1ecab1962929cc788c756775f859c98da142"),
            Map.entry(EntityType.DONKEY, "7201245fcdc6cc42694d8e67e566282e07bc00a0a397fc726609c2789fd9e6b1"),
            Map.entry(EntityType.FOX, "d5513865b7f003b379f1fc1bab9710e1fd383ea181acea475a2306fd769b076"),
            Map.entry(EntityType.FROG, "79672bef44ed3f6ee3de0d5b47a9bf2cfbf7888ad5a3a8c2fbbef97a27d8811a"),
            Map.entry(EntityType.GOAT, "42d0f31734b54d42e03d641848357aaea627765cc0454c42414aa46c8df42c15"),
            Map.entry(EntityType.HORSE, "246fcc9d88764b7219f6ed45ecd7af73c409d2daddc9ddf63911fac15da66ccb"),
            Map.entry(EntityType.LLAMA, "9f7d90b305aa64313c8d4404d8d652a96eba8a754b67f4347dcccdd5a6a63398"),
            Map.entry(EntityType.MOOSHROOM, "d0bc61b9757a7b83e03cd2507a2157913c2cf016e7c096a4d6cf1fe1b8db"),
            Map.entry(EntityType.MULE, "46dcda265e57e4f51b145aacbf5b59bdc6099ffd3cce0a661b2c0065d80930d8"),
            Map.entry(EntityType.OCELOT, "8c433c1347313b23b67eec92f8807aed2566ec29fd416bdf7a59c22596628355"),
            Map.entry(EntityType.PANDA, "8c6325b76cd5c51a91fb57cb49f1111259bee01ae28786c26697c7c40498fa80"),
            Map.entry(EntityType.PARROT, "e2439a109494bca415c48b99d634545baee172fb28d936fd402f547383a53eca"),
            Map.entry(EntityType.PIG, "3a9e18e56b9693e66383e6c1c58cc1b936078cba087451fd3b12567658c1c"),
            Map.entry(EntityType.POLAR_BEAR, "f072d8cb8395c2bc84547e6ac9b8edeed78c8658c0ed1b883c629c3a7e90"),
            Map.entry(EntityType.RABBIT, "d27d5c0dd6ff514edb3aa392107702501d50bf90bc5e5822aecb1d86c2d4ba92"),
            Map.entry(EntityType.SHEEP, "88170c84a763f8d3ac345c913d54a8654b20e35ec291844f16ebd386a7b0023d"),
            Map.entry(EntityType.SNIFFER, "43f3be09a7353eeae94d88320cb5b242de2f719c0e5c16a486327c605db1d463"),
            Map.entry(EntityType.WOLF, "2d0b44b1fec58f60f0ec96f9244305147dae4707e0f1b535754a8397a9ee680f"),
            Map.entry(EntityType.ZOMBIE_VILLAGER, "1e3fc332103254b77675b7a4784d3c99c5e15765be43445c907ad97d7fcafe18"),
            Map.entry(EntityType.SPIDER, "5f7e82446fab1e41577ba70ab40e290ef841c245233011f39459ac6f852c8331"),
            Map.entry(EntityType.CAVE_SPIDER, "41645dfd77d09923107b3496e94eeb5c30329f97efc96ed76e226e98224"),
            Map.entry(EntityType.BREEZE, "a275728af7e6a29c88125b675a39d88ae9919bb61fdc200337fed6ab0c49d65c"),
            Map.entry(EntityType.WITCH, "fce6604157fc4ab5591e4bcf507a749918ee9c41e357d47376e0ee7342074c90"),
            Map.entry(EntityType.CREAKING, "77b5be72769ccff1a6cb77c5848e01d7e5704a3d349c0737ff93cb54d02380ac"),
            Map.entry(EntityType.BOGGED, "a3b9003ba2d05562c75119b8a62185c67130e9282f7acbac4bc2824c21eb95d9"),
            Map.entry(EntityType.STRAY, "6572747a639d2240feeae5c81c6874e6ee7547b599e74546490dc75fa2089186"),
            Map.entry(EntityType.PHANTOM, "7e95153ec23284b283f00d19d29756f244313a061b70ac03b97d236ee57bd982"),
            Map.entry(EntityType.SLIME, "bb13133a8fb4ef00b71ef9bab639a66fbc7d5cffcc190c1df74bf2161dfd3ec7"),
            Map.entry(EntityType.SILVERFISH, "da91dab8391af5fda54acd2c0b18fbd819b865e1a8f1d623813fa761e924540"),
            Map.entry(EntityType.HUSK, "c096164f81950a5cc0e33e87999f98cde792517f4d7f99a647a9aedab23ae58"),
            Map.entry(EntityType.EVOKER, "e79f133a85fe00d3cf252a04d6f2eb2521fe299c08e0d8b7edbf962740a23909"),
            Map.entry(EntityType.ILLUSIONER, "4639d325f4494258a473a93a3b47f34a0c51b3fceaf59fee87205a5e7ff31f68"),
            Map.entry(EntityType.PILLAGER, "4aee6bb37cbfc92b0d86db5ada4790c64ff4468d68b84942fde04405e8ef5333"),
            Map.entry(EntityType.RAVAGER, "cd20bf52ec390a0799299184fc678bf84cf732bb1bd78fd1c4b441858f0235a8"),
            Map.entry(EntityType.VEX, "5e7330c7d5cd8a0a55ab9e95321535ac7ae30fe837c37ea9e53bea7ba2de86b"),
            Map.entry(EntityType.VINDICATOR, "9e1cab382458e843ac4356e3e00e1d35c36f449fa1a84488ab2c6557b392d"),
            Map.entry(EntityType.VILLAGER, "f605a50dd59e5227701a51970ab0479ef57fdad9099073538fa713b26cce9fc3"),
            Map.entry(EntityType.WANDERING_TRADER, "499d585a9abf59fae277bb684d24070cef21e35609a3e18a9bd5dcf73a46ab93"),
            Map.entry(EntityType.SKELETON_HORSE, "47effce35132c86ff72bcae77dfbb1d22587e94df3cbc2570ed17cf8973a"),
            Map.entry(EntityType.TRADER_LLAMA, "5a4eed85697c78f462c4eb5653b05b76576c1178f704f3c5676f505d8f3983b4"),
            Map.entry(EntityType.ZOMBIE_HORSE, "d22950f2d3efddb18de86f8f55ac518dce73f12a6e0f8636d551d8eb480ceec"),
            Map.entry(EntityType.IRON_GOLEM, "b69d0d4711153a089c5567a749b27879c769d3bdcea6fda9d6f66e93dd8c4512"),
            Map.entry(EntityType.SNOW_GOLEM, "9aed9fe4ed0893e325f4fbd32b093c1cc562cba27ff73359d356f1c288e441f9"),
            Map.entry(EntityType.ENDERMAN, "aacb357709d8cdf1cd9c9dbe313e7bab3276ae84234982e93e13839ab7cc5d16"),
            Map.entry(EntityType.ENDERMITE, "5bc7b9d36fb92b6bf292be73d32c6c5b0ecc25b44323a541fae1f1e67e393a3e"),
            Map.entry(EntityType.SHULKER, "94c0b73f063561490fd018a9dae07ca7d2e0537508556ae8b3340a5c9fb53008"),
            Map.entry(EntityType.BLAZE, "b20657e24b56e1b2f8fc219da1de788c0c24f36388b1a409d0cd2d8dba44aa3b"),
            Map.entry(EntityType.GHAST, "de8a38e9afbd3da10d19b577c55c7bfd6b4f2e407e44d4017b23be9167abff02"),
            Map.entry(EntityType.HOGLIN, "4409dc402a9fc3c7b892c44e5cd34a4a01d44419d05df8316f2e2d862ae0ba9c"),
            Map.entry(EntityType.MAGMA_CUBE, "a1c97a06efde04d00287bf20416404ab2103e10f08623087e1b0c1264a1c0f0c"),
            Map.entry(EntityType.PIGLIN_BRUTE, "3e300e9027349c4907497438bac29e3a4c87a848c50b34c21242727b57f4e1cf"),
            Map.entry(EntityType.STRIDER, "65ccbb547820b667cc9d3bc9fff1e3d65da2375655a3427b30e1d009eeb272ce"),
            Map.entry(EntityType.ZOGLIN, "db8bf7cadefa10f957a2fb938b4ba1225d9c89772ae7dd8d40c3176aa0433f1d"),
            Map.entry(EntityType.ZOMBIFIED_PIGLIN, "93c3b28bf7afa4c60431b549f0d1518d74a771c06ede12634c554f1601f2ddd1"),
            Map.entry(EntityType.WARDEN, "53c4970510fb0f99be3f0c3d5a5919c6eeef04e433120e20107c66aba675a9b7"),
            Map.entry(EntityType.WITHER, "5da97c06c07a035bd8746b48d32dc04e84160bd161f26be1272b6271251aaa7")
    );

    private static final Map<EntityType, String> NOMES_PT_BR = Map.ofEntries(
            Map.entry(EntityType.ALLAY, "Allay"),
            Map.entry(EntityType.ARMADILLO, "Tatu"),
            Map.entry(EntityType.BAT, "Morcego"),
            Map.entry(EntityType.BEE, "Abelha"),
            Map.entry(EntityType.BLAZE, "Blaze"),
            Map.entry(EntityType.BOGGED, "Pantanoso"),
            Map.entry(EntityType.BREEZE, "Vórtice"),
            Map.entry(EntityType.CAMEL, "Camelo"),
            Map.entry(EntityType.CAT, "Gato"),
            Map.entry(EntityType.CAVE_SPIDER, "Aranha das Cavernas"),
            Map.entry(EntityType.CHICKEN, "Galinha"),
            Map.entry(EntityType.COD, "Bacalhau"),
            Map.entry(EntityType.COW, "Vaca"),
            Map.entry(EntityType.CREAKING, "Rangente"),
            Map.entry(EntityType.CREEPER, "Creeper"),
            Map.entry(EntityType.DOLPHIN, "Golfinho"),
            Map.entry(EntityType.DONKEY, "Burro"),
            Map.entry(EntityType.DROWNED, "Afogado"),
            Map.entry(EntityType.ELDER_GUARDIAN, "Guardião-Mestre"),
            Map.entry(EntityType.ENDER_DRAGON, "Dragão Ender"),
            Map.entry(EntityType.ENDERMAN, "Enderman"),
            Map.entry(EntityType.ENDERMITE, "Endermite"),
            Map.entry(EntityType.EVOKER, "Invocador"),
            Map.entry(EntityType.FOX, "Raposa"),
            Map.entry(EntityType.FROG, "Sapo"),
            Map.entry(EntityType.GHAST, "Ghast"),
            Map.entry(EntityType.GLOW_SQUID, "Lula-Brilhante"),
            Map.entry(EntityType.GOAT, "Cabra"),
            Map.entry(EntityType.GUARDIAN, "Guardião"),
            Map.entry(EntityType.HOGLIN, "Hoglin"),
            Map.entry(EntityType.HORSE, "Cavalo"),
            Map.entry(EntityType.HUSK, "Zumbi-Múmia"),
            Map.entry(EntityType.ILLUSIONER, "Ilusionista"),
            Map.entry(EntityType.IRON_GOLEM, "Golem de Ferro"),
            Map.entry(EntityType.LLAMA, "Lhama"),
            Map.entry(EntityType.MAGMA_CUBE, "Cubo de Magma"),
            Map.entry(EntityType.MOOSHROOM, "Mooshroom"),
            Map.entry(EntityType.MULE, "Mula"),
            Map.entry(EntityType.NAUTILUS, "Náutilo"),
            Map.entry(EntityType.OCELOT, "Jaguatirica"),
            Map.entry(EntityType.PANDA, "Panda"),
            Map.entry(EntityType.PARROT, "Papagaio"),
            Map.entry(EntityType.PHANTOM, "Espectro"),
            Map.entry(EntityType.PIG, "Porco"),
            Map.entry(EntityType.PIGLIN, "Piglin"),
            Map.entry(EntityType.PIGLIN_BRUTE, "Piglin Bárbaro"),
            Map.entry(EntityType.PILLAGER, "Saqueador"),
            Map.entry(EntityType.POLAR_BEAR, "Urso-Polar"),
            Map.entry(EntityType.PUFFERFISH, "Baiacu"),
            Map.entry(EntityType.RABBIT, "Coelho"),
            Map.entry(EntityType.RAVAGER, "Devastador"),
            Map.entry(EntityType.SALMON, "Salmão"),
            Map.entry(EntityType.SHEEP, "Ovelha"),
            Map.entry(EntityType.SHULKER, "Shulker"),
            Map.entry(EntityType.SILVERFISH, "Traça"),
            Map.entry(EntityType.SKELETON, "Esqueleto"),
            Map.entry(EntityType.SKELETON_HORSE, "Cavalo-Esqueleto"),
            Map.entry(EntityType.SLIME, "Slime"),
            Map.entry(EntityType.SNIFFER, "Farejador"),
            Map.entry(EntityType.SNOW_GOLEM, "Golem de Neve"),
            Map.entry(EntityType.SPIDER, "Aranha"),
            Map.entry(EntityType.SQUID, "Lula"),
            Map.entry(EntityType.STRAY, "Errante"),
            Map.entry(EntityType.STRIDER, "Lavagante"),
            Map.entry(EntityType.TADPOLE, "Girino"),
            Map.entry(EntityType.TRADER_LLAMA, "Lhama do Vendedor"),
            Map.entry(EntityType.TROPICAL_FISH, "Peixe Tropical"),
            Map.entry(EntityType.TURTLE, "Tartaruga"),
            Map.entry(EntityType.VEX, "Vex"),
            Map.entry(EntityType.VILLAGER, "Aldeão"),
            Map.entry(EntityType.VINDICATOR, "Vingador"),
            Map.entry(EntityType.WANDERING_TRADER, "Vendedor Ambulante"),
            Map.entry(EntityType.WARDEN, "Defensor"),
            Map.entry(EntityType.WITCH, "Bruxa"),
            Map.entry(EntityType.WITHER, "Wither"),
            Map.entry(EntityType.WITHER_SKELETON, "Esqueleto Wither"),
            Map.entry(EntityType.WOLF, "Lobo"),
            Map.entry(EntityType.ZOGLIN, "Zoglin"),
            Map.entry(EntityType.ZOMBIE, "Zumbi"),
            Map.entry(EntityType.ZOMBIE_HORSE, "Cavalo-Zumbi"),
            Map.entry(EntityType.ZOMBIE_VILLAGER, "Aldeão Zumbi"),
            Map.entry(EntityType.ZOMBIFIED_PIGLIN, "Piglin-Zumbi")
    );

    private static final Map<String, String> TEXTURAS_ESPECIAIS = Map.of(
            "charged_creeper", "3511e4a3d5add6a54499abad10d799d06ce45cba9e520afd2008608a6288b7e7"
    );

    private static final Map<String, String> NOMES_ESPECIAIS = Map.of(
            "charged_creeper", "Creeper Carregado"
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
        String chave = nome.toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        String texturaEspecial = TEXTURAS_ESPECIAIS.get(chave);
        if (texturaEspecial != null) {
            return criarCabecaComTextura(texturaEspecial, NOMES_ESPECIAIS.getOrDefault(chave, "Creeper Carregado"));
        }

        EntityType tipo = resolverTipo(nome);
        if (tipo == null) return null;
        return criarCabeca(tipo);
    }

    private ItemStack criarCabecaComTextura(String textura, String nome) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta rawMeta = item.getItemMeta();
        if (!(rawMeta instanceof SkullMeta meta)) return item;

        meta.setDisplayName(color("&fCabeça de " + nome));
        meta.setMaxStackSize(64);
        aplicarTextura(meta, textura, nome);

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack criarCabeca(EntityType tipo) {
        Material material = switch (tipo) {
            case ZOMBIE -> Material.ZOMBIE_HEAD;
            case SKELETON -> Material.SKELETON_SKULL;
            case CREEPER -> Material.CREEPER_HEAD;
            case PIGLIN -> Material.PIGLIN_HEAD;
            case WITHER_SKELETON -> Material.WITHER_SKELETON_SKULL;
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
            meta.setMaxStackSize(64);
            aplicarTextura(meta, textura, tipo.name());
        } else if (material != Material.PLAYER_HEAD) {
            meta.setMaxStackSize(64);
        }

        item.setItemMeta(meta);
        return item;
    }

    private void aplicarTextura(SkullMeta meta, String textura, String identificador) {
        if (textura == null || textura.isBlank()) {
            plugin.getLogger().warning("Textura ausente para " + identificador + ".");
            return;
        }

        try {
            URL url = new URL("https://textures.minecraft.net/texture/" + textura);
            UUID profileId = UUID.nameUUIDFromBytes(
                    ("combateplus:mob-head:" + textura).getBytes(StandardCharsets.UTF_8));
            PlayerProfile profile = Bukkit.createPlayerProfile(profileId);
            profile.getTextures().setSkin(url);

            // Paper expõe setPlayerProfile, que grava o ResolvableProfile diretamente.
            // O fallback mantém compatibilidade com a API Spigot.
            try {
                Method setPlayerProfile = meta.getClass().getMethod("setPlayerProfile", PlayerProfile.class);
                setPlayerProfile.invoke(meta, profile);
            } catch (ReflectiveOperationException | SecurityException ignored) {
                meta.setOwnerProfile(profile);
            }
        } catch (MalformedURLException | IllegalArgumentException exception) {
            plugin.getLogger().warning("Textura inválida para " + identificador + ": " + textura);
        }
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
                Map.entry("girino", EntityType.TADPOLE),
                Map.entry("afogado", EntityType.DROWNED),
                Map.entry("drowned", EntityType.DROWNED),
                Map.entry("guardiao_mestre", EntityType.ELDER_GUARDIAN),
                Map.entry("guardiao mestre", EntityType.ELDER_GUARDIAN),
                Map.entry("elder_guardian", EntityType.ELDER_GUARDIAN),
                Map.entry("guardiao", EntityType.GUARDIAN),
                Map.entry("guardian", EntityType.GUARDIAN),
                Map.entry("nautilo", EntityType.NAUTILUS),
                Map.entry("nautilus", EntityType.NAUTILUS)
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
        String nomeTraduzido = NOMES_PT_BR.get(type);
        if (nomeTraduzido != null) return nomeTraduzido;

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
