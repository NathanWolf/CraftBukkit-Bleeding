package org.bukkit.craftbukkit.inventory;

import net.minecraft.server.IInventory;
import org.bukkit.inventory.AnvilInventory;

public class CraftInventoryAnvil extends CraftInventory implements AnvilInventory {

    public CraftInventoryAnvil(IInventory inventory, IInventory resultInventory) {
        super(inventory, resultInventory);
    }
}
