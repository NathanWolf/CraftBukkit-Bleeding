package org.bukkit.craftbukkit.entity;

import java.util.Collection;

import net.minecraft.server.EntityPotion;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;

public class CraftThrownPotion extends CraftProjectile implements ThrownPotion {
    private Collection<PotionEffect> effects = null;

    public CraftThrownPotion(CraftServer server, EntityPotion entity) {
        super(server, entity);
    }

    public Collection<PotionEffect> getEffects() {
        if (effects == null) {
            effects = Potion.getBrewer().getEffectsFromDamage(getHandle().getPotionValue());
        }

        return effects;
    }

    public PotionMeta getItemMeta() {
        return (PotionMeta) CraftItemStack.asBukkitCopy(getHandle().c).getItemMeta();
    }

    public boolean setItemMeta(PotionMeta meta) {
        ItemStack stack = CraftItemStack.asBukkitCopy(getHandle().c);
        if (stack.setItemMeta(meta)) {
            getHandle().c = CraftItemStack.asNMSCopy(stack);
            return true;
        }
        return false;
    }

    @Override
    public EntityPotion getHandle() {
        return (EntityPotion) entity;
    }

    @Override
    public String toString() {
        return "CraftThrownPotion";
    }

    public EntityType getType() {
        return EntityType.SPLASH_POTION;
    }
}
