package com.donnie1337.combateplus;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class PvPListener implements Listener {
    private final CombatePlus plugin;
    private final Map<UUID, Double> originalAttackSpeed = new HashMap<>();
    private final Map<UUID, Double> originalEntityReach = new HashMap<>();
    private final Map<UUID, Long> lastSprintStart = new HashMap<>();
    private final Map<UUID, Long> lastAttack = new HashMap<>();
    private final Set<UUID> swordBlocking = new HashSet<>();
    private final Set<UUID> pvpDisabled = new HashSet<>();
    private final NamespacedKey visualBlockKey;
    private static final String PVP_TEAM_PREFIX = "combateplus_pvp_";

    public boolean isPvpEnabled(Player player) {
        return player != null
                && (isPvpForced(player) || !pvpDisabled.contains(player.getUniqueId()));
    }

    public boolean isPvpForced(Player player) {
        if (player == null) {
            return false;
        }

        org.bukkit.World.Environment environment = player.getWorld().getEnvironment();
        return environment == org.bukkit.World.Environment.NETHER
                || environment == org.bukkit.World.Environment.THE_END;
    }

    public boolean togglePvp(Player player) {
        if (isPvpForced(player)) {
            updatePvpIndicator(player);
            return true;
        }

        UUID uuid = player.getUniqueId();
        if (pvpDisabled.remove(uuid)) {
            updatePvpIndicator(player);
            return true;
        }
        pvpDisabled.add(uuid);
        stopSwordBlocking(player);
        updatePvpIndicator(player);
        return false;
    }

    public PvPListener(CombatePlus plugin) {
        this.plugin = plugin;
        this.visualBlockKey = new NamespacedKey(plugin, "sword-block-animation");
    }

    public void refreshPvpState(Player player) {
        if (player == null) {
            return;
        }

        if (isPvpDefaultDisabledWorld(player) && !isPvpForced(player)) {
            pvpDisabled.add(player.getUniqueId());
            stopSwordBlocking(player);
        }

        updatePvpIndicator(player);
    }

    private boolean isPvpDefaultDisabledWorld(Player player) {
        List<String> worlds = plugin.getConfig().getStringList(
                "pvp-1-8.mundos-pvp-desativado-por-padrao"
        );
        if (worlds.isEmpty()) {
            worlds = List.of("world", "mining");
        }

        String worldName = player.getWorld().getName();
        return worlds.stream().anyMatch(world -> world != null
                && world.equalsIgnoreCase(worldName));
    }

    private void updatePvpIndicator(Player player) {
        Scoreboard scoreboard = plugin.getServer().getScoreboardManager().getMainScoreboard();
        String teamName = PVP_TEAM_PREFIX + player.getUniqueId().toString().replace("-", "").substring(0, 12);
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }

        if (!team.hasEntry(player.getName())) {
            team.addEntry(player.getName());
        }

        // PvP ativo: espada vermelha. PvP desativado: escudo verde.
        team.setSuffix(isPvpEnabled(player) ? " §c⚔" : " §a⛨");
    }

    private void clearPvpIndicator(Player player) {
        Scoreboard scoreboard = plugin.getServer().getScoreboardManager().getMainScoreboard();
        String teamName = PVP_TEAM_PREFIX + player.getUniqueId().toString().replace("-", "").substring(0, 12);
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            return;
        }

        team.removeEntry(player.getName());
        if (team.getEntries().isEmpty()) {
            team.unregister();
        }
    }

    public void applyAttackSpeed(Player player) {
        if (!plugin.getConfig().getBoolean("pvp-1-8.ataque-cooldown.ativado", true)) {
            return;
        }

        AttributeInstance attribute = player.getAttribute(Attribute.ATTACK_SPEED);
        if (attribute == null) {
            return;
        }

        originalAttackSpeed.putIfAbsent(player.getUniqueId(), attribute.getBaseValue());
        double attackSpeed = plugin.getConfig().getDouble("pvp-1-8.ataque-cooldown.velocidade", 100.0);
        attribute.setBaseValue(attackSpeed);
    }

    public void restoreAttackSpeed(Player player) {
        Double original = originalAttackSpeed.remove(player.getUniqueId());
        if (original == null) {
            return;
        }

        AttributeInstance attribute = player.getAttribute(Attribute.ATTACK_SPEED);
        if (attribute != null) {
            attribute.setBaseValue(original);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        applyAttackSpeed(event.getPlayer());
        applyEntityReach(event.getPlayer());
        prepareSwordAnimation(event.getPlayer());
        refreshPvpState(event.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        refreshPvpState(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        stopSwordBlocking(event.getPlayer());
        clearPvpIndicator(event.getPlayer());
        restoreAttackSpeed(event.getPlayer());
        restoreEntityReach(event.getPlayer());
        lastSprintStart.remove(event.getPlayer().getUniqueId());
        lastAttack.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (!plugin.getConfig().getBoolean("pvp-1-8.bloqueio-espada.ativado", true)) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();

            // Escudo moderno tem prioridade quando está equipado na mão secundária.
            // Nesse caso, deixamos o vanilla/Paper controlar o bloqueio do escudo.
            if (isShield(player.getInventory().getItemInOffHand())) {
                stopSwordBlocking(player);
                return;
            }

            // Com espada na mão principal e sem escudo na off-hand, usa o bloqueio 1.8.
            if (isSword(event.getItem()) && isSword(player.getInventory().getItemInMainHand())) {
                prepareSwordAnimation(player);
                swordBlocking.add(player.getUniqueId());
            }
        } else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            stopSwordBlocking(event.getPlayer());
        }
    }

    @EventHandler
    public void onAnimation(PlayerAnimationEvent event) {
        // Um ataque com a mão/espada encerra o bloqueio, como no combate 1.8.
        stopSwordBlocking(event.getPlayer());
    }

    @EventHandler
    public void onHeldItem(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        stopSwordBlocking(player);
        plugin.getServer().getScheduler().runTask(plugin, () -> prepareSwordAnimation(player));
    }

    @EventHandler
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        stopSwordBlocking(event.getPlayer());
    }

    @EventHandler
    public void onSprintToggle(PlayerToggleSprintEvent event) {
        if (event.isSprinting()) {
            lastSprintStart.put(event.getPlayer().getUniqueId(), System.nanoTime());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        if (isMarkedVisualSword(event.getItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPvpToggleDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }

        if (!isPvpEnabled(victim) || !isPvpEnabled(attacker)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSwordBlockDamage(EntityDamageByEntityEvent event) {
        if (!plugin.getConfig().getBoolean("pvp-1-8.bloqueio-espada.ativado", true)) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)
                || !swordBlocking.contains(player.getUniqueId())
                || !isSword(player.getInventory().getItemInMainHand())) {
            return;
        }

        if (!isMeleeDamage(event)) {
            return;
        }

        double reduction = plugin.getConfig().getDouble(
                "pvp-1-8.bloqueio-espada.reducao-dano", 0.50
        );
        reduction = Math.max(0.0, Math.min(1.0, reduction));
        event.setDamage(event.getDamage() * (1.0 - reduction));

        // Receber um hit encerra o bloqueio apenas se configurado.
        if (plugin.getConfig().getBoolean("pvp-1-8.bloqueio-espada.parar-ao-receber-hit", false)) {
            stopSwordBlocking(player);
        }
    }

    private void prepareSwordAnimation(Player player) {
        if (!plugin.getConfig().getBoolean("pvp-1-8.bloqueio-espada.animacao.ativado", true)) {
            return;
        }

        // Paper 26.2 usa o novo Data Component API para a animação de uso.
        // Mantemos reflexão aqui para evitar incompatibilidade de API entre builds.
        try {
            Class<?> dataComponentTypesClass = Class.forName(
                    "io.papermc.paper.datacomponent.DataComponentTypes"
            );
            Class<?> consumableClass = Class.forName(
                    "io.papermc.paper.datacomponent.item.Consumable"
            );
            Class<?> builderClass = Class.forName(
                    "io.papermc.paper.datacomponent.item.Consumable$Builder"
            );
            Class<?> animationClass = Class.forName(
                    "io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation"
            );
            Class<?> valuedTypeClass = Class.forName(
                    "io.papermc.paper.datacomponent.DataComponentType$Valued"
            );

            Object animationBlock = Enum.valueOf(
                    animationClass.asSubclass(Enum.class), "BLOCK"
            );
            Object dataComponentType = dataComponentTypesClass
                    .getField("CONSUMABLE").get(null);
            Object builder = consumableClass.getMethod("consumable").invoke(null);

            builderClass.getMethod("animation", animationClass)
                    .invoke(builder, animationBlock);
            builderClass.getMethod("consumeSeconds", float.class)
                    .invoke(builder, 86400.0f);
            builderClass.getMethod("hasConsumeParticles", boolean.class)
                    .invoke(builder, false);

            Object consumable = builderClass.getMethod("build").invoke(builder);
            java.lang.reflect.Method setData = ItemStack.class.getMethod(
                    "setData", valuedTypeClass, Object.class
            );

            ItemStack[] contents = player.getInventory().getContents();
            for (int slot = 0; slot < contents.length; slot++) {
                ItemStack item = contents[slot];
                if (!isSword(item)) continue;

                setData.invoke(item, dataComponentType, consumable);

                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.getPersistentDataContainer().set(
                            visualBlockKey, PersistentDataType.BYTE, (byte) 1
                    );
                    item.setItemMeta(meta);
                }
                contents[slot] = item;
            }
            player.getInventory().setContents(contents);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // Servidor sem o Data Component API: não força a animação.
        }
    }

    private boolean isMarkedVisualSword(ItemStack item) {
        if (!isSword(item) || !item.hasItemMeta()) return false;
        Byte marker = item.getItemMeta().getPersistentDataContainer().get(visualBlockKey, PersistentDataType.BYTE);
        return marker != null && marker == 1;
    }

    private boolean isMeleeDamage(EntityDamageByEntityEvent event) {
        return event.getDamager() instanceof Player;
    }

    private boolean isSword(ItemStack item) {
        return item != null && item.getType().name().endsWith("_SWORD");
    }

    private boolean isShield(ItemStack item) {
        return item != null && item.getType() == Material.SHIELD;
    }

    private void stopSwordBlocking(Player player) {
        swordBlocking.remove(player.getUniqueId());
    }

    private void applyEntityReach(Player player) {
        if (!plugin.getConfig().getBoolean("pvp-1-8.alcance.ativado", true)) {
            return;
        }

        AttributeInstance attribute = player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE);
        if (attribute == null) {
            return;
        }

        UUID uuid = player.getUniqueId();
        originalEntityReach.putIfAbsent(uuid, attribute.getBaseValue());
        double reach = plugin.getConfig().getDouble("pvp-1-8.alcance.distancia", 3.0);
        attribute.setBaseValue(Math.max(0.0, reach));
    }

    private void restoreEntityReach(Player player) {
        Double original = originalEntityReach.remove(player.getUniqueId());
        if (original == null) {
            return;
        }

        AttributeInstance attribute = player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE);
        if (attribute != null) {
            attribute.setBaseValue(original);
        }
    }

    private boolean hasFreshSprint(Player player) {
        UUID uuid = player.getUniqueId();
        Long sprintStart = lastSprintStart.get(uuid);
        Long attack = lastAttack.get(uuid);
        return sprintStart != null && (attack == null || sprintStart > attack);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSuccessfulAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)
                || !(event.getEntity() instanceof Player)) {
            return;
        }

        UUID uuid = attacker.getUniqueId();
        lastAttack.put(uuid, System.nanoTime());
        attacker.setSprinting(false);
    }

    public void restoreAll() {
        for (UUID uuid : originalAttackSpeed.keySet().toArray(UUID[]::new)) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                restoreAttackSpeed(player);
            } else {
                originalAttackSpeed.remove(uuid);
            }
        }

        for (UUID uuid : originalEntityReach.keySet().toArray(UUID[]::new)) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                restoreEntityReach(player);
            } else {
                originalEntityReach.remove(uuid);
            }
        }

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            clearPvpIndicator(player);
        }
        swordBlocking.clear();
        pvpDisabled.clear();
        lastSprintStart.clear();
        lastAttack.clear();
    }
}
