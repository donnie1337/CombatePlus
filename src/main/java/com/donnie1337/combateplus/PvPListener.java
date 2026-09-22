package com.donnie1337.combateplus;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityKnockbackByEntityEvent;
import org.bukkit.event.entity.EntityKnockbackEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PvPListener implements Listener {
    private final CombatePlus plugin;
    private final Map<UUID, Double> originalAttackSpeed = new HashMap<>();

    public PvPListener(CombatePlus plugin) {
        this.plugin = plugin;
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
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        restoreAttackSpeed(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!plugin.getConfig().getBoolean("pvp-1-8.escudo.ignorar-bloqueio", true)) {
            return;
        }

        if (!(event.getEntity() instanceof Player player) || !player.isBlocking()) {
            return;
        }

        // A 1.8 não possuía escudos. Removemos somente a redução de bloqueio,
        // preservando as demais reduções de dano (armadura, resistência etc.).
        if (event.isApplicable(org.bukkit.event.entity.EntityDamageEvent.DamageModifier.BLOCKING)) {
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
    }
}
