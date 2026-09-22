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
import org.bukkit.event.entity.EntityKnockbackByEntityEvent;
import org.bukkit.event.entity.EntityKnockbackEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
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
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class PvPListener implements Listener {
    private final CombatePlus plugin;
    private final Map<UUID, Double> originalAttackSpeed = new HashMap<>();
    private final Set<UUID> swordBlocking = new HashSet<>();
    private final Set<UUID> pvpDisabled = new HashSet<>();
    private final NamespacedKey visualBlockKey;
    private static final String PVP_TEAM_PREFIX = "combateplus_pvp_";

    public boolean isPvpEnabled(Player player) {
        return player != null && !pvpDisabled.contains(player.getUniqueId());
    }

    public boolean togglePvp(Player player) {
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

        team.setSuffix(isPvpEnabled(player) ? " §a⚔" : " §c⚔");
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
        prepareSwordAnimation(event.getPlayer());
        updatePvpIndicator(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        stopSwordBlocking(event.getPlayer());
        clearPvpIndicator(event.getPlayer());
        restoreAttackSpeed(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (!plugin.getConfig().getBoolean("pvp-1-8.bloqueio-espada.ativado", true)) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (isSword(event.getItem())) {
                Player player = event.getPlayer();
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

        // A API de ConsumableComponent mudou entre builds 26.x.
        // Use reflexão para não quebrar o plugin com NoSuchMethodError quando
        // o servidor não expõe ItemMeta.hasConsumable()/getConsumable().
        try {
            Class<?> componentClass = Class.forName(
                    "org.bukkit.inventory.meta.components.consumable.ConsumableComponent"
            );
            Class<?> animationClass = Class.forName(
                    "org.bukkit.inventory.meta.components.consumable.ConsumableComponent$Animation"
            );

            Object animationBlock = Enum.valueOf(
                    animationClass.asSubclass(Enum.class), "BLOCK"
            );

            ItemStack[] contents = player.getInventory().getContents();
            for (int slot = 0; slot < contents.length; slot++) {
                ItemStack item = contents[slot];
                if (!isSword(item)) continue;

                ItemMeta meta = item.getItemMeta();
                if (meta == null) continue;

                java.lang.reflect.Method hasConsumable =
                        ItemMeta.class.getMethod("hasConsumable");
                java.lang.reflect.Method getConsumable =
                        ItemMeta.class.getMethod("getConsumable");
                java.lang.reflect.Method setConsumable =
                        ItemMeta.class.getMethod("setConsumable", componentClass);

                if (Boolean.TRUE.equals(hasConsumable.invoke(meta))) {
                    continue;
                }

                Object consumable = getConsumable.invoke(meta);
                componentClass.getMethod("setAnimation", animationClass)
                        .invoke(consumable, animationBlock);
                componentClass.getMethod("setConsumeSeconds", float.class)
                        .invoke(consumable, 86400.0f);
                componentClass.getMethod("setConsumeParticles", boolean.class)
                        .invoke(consumable, false);
                setConsumable.invoke(meta, consumable);

                meta.getPersistentDataContainer().set(
                        visualBlockKey, PersistentDataType.BYTE, (byte) 1
                );
                item.setItemMeta(meta);
                contents[slot] = item;
            }
            player.getInventory().setContents(contents);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // Build do Paper sem a API de ConsumableComponent: a animação
            // é simplesmente ignorada, sem impedir o jogador de entrar.
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

    private void stopSwordBlocking(Player player) {
        swordBlocking.remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!plugin.getConfig().getBoolean("pvp-1-8.escudo.ignorar-bloqueio", true)) {
            return;
        }

        if (!(event.getEntity() instanceof Player player) || !player.isBlocking()) {
            return;
        }

        // O núcleo de bloqueio de espada acima controla a proteção 1.8.
        // Aqui impedimos que o bloqueio moderno de escudo também reduza o dano.
        if (!swordBlocking.contains(player.getUniqueId())
                && event.isApplicable(org.bukkit.event.entity.EntityDamageEvent.DamageModifier.BLOCKING)) {
            event.setDamage(
                    org.bukkit.event.entity.EntityDamageEvent.DamageModifier.BLOCKING,
                    0.0
            );
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onKnockback(EntityKnockbackEvent event) {
        if (!plugin.getConfig().getBoolean("pvp-1-8.knockback.ativado", true)) {
            return;
        }

        Vector knockback = event.getFinalKnockback();
        double horizontal = plugin.getConfig().getDouble(
                "pvp-1-8.knockback.multiplicador-horizontal", 1.0
        );
        double vertical = plugin.getConfig().getDouble(
                "pvp-1-8.knockback.multiplicador-vertical", 1.0
        );

        knockback.setX(knockback.getX() * horizontal);
        knockback.setZ(knockback.getZ() * horizontal);
        knockback.setY(knockback.getY() * vertical);

        if (plugin.getConfig().getBoolean("pvp-1-8.knockback.sprint-bonus.ativado", true)
                && event instanceof EntityKnockbackByEntityEvent byEntity
                && byEntity.getSourceEntity() instanceof Player attacker
                && attacker.isSprinting()) {
            double bonus = plugin.getConfig().getDouble(
                    "pvp-1-8.knockback.sprint-bonus.multiplicador", 1.0
            );
            Vector raw = event.getKnockback();
            Vector horizontalDirection = new Vector(raw.getX(), 0, raw.getZ());

            if (horizontalDirection.lengthSquared() > 0.000001) {
                horizontalDirection.normalize().multiply(
                        plugin.getConfig().getDouble(
                                "pvp-1-8.knockback.sprint-bonus.forca-extra", 0.0
                        ) * bonus
                );
                knockback.add(horizontalDirection);
            }
        }

        event.setFinalKnockback(knockback);
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

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            clearPvpIndicator(player);
        }
        swordBlocking.clear();
        pvpDisabled.clear();
    }
}
