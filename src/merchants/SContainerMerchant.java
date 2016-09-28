/**
 * This file is part of MerchantsAPI.
 * <p>
 * Copyright (c) 2014, Cybermaxke
 * <p>
 * MerchantsAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * MerchantsAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with MerchantsAPI. If not, see <http://www.gnu.org/licenses/>.
 */
package merchants;

import net.minecraft.server.v1_10_R1.*;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventoryView;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

public class SContainerMerchant extends ContainerMerchant {
    // The field of the merchant inventory in the container
    private static Field fieldInventoryMerchant;

    // The merchant we are trading with
    private final SMerchant merchant;

    // The bukkit instance
    private CraftInventoryView bukkitEntity;

    public SContainerMerchant(EntityPlayer customer, SMerchant merchant) throws Exception {
        super(customer.inventory, merchant, customer.world);
        this.merchant = merchant;

        // Search the field
        if (fieldInventoryMerchant == null) {
            for (Field field : ContainerMerchant.class.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.getType().isAssignableFrom(InventoryMerchant.class)) {
                    fieldInventoryMerchant = field;
                }
            }
        }

        final SInventoryMerchant inventory = new SInventoryMerchant(customer, merchant);

        this.setSlot(0, new Slot(inventory, 0, 36, 53));
        this.setSlot(1, new Slot(inventory, 1, 62, 53));
        this.setSlot(2, new SSlotMerchantResult(customer, merchant, inventory, 2, 120, 53));

        fieldInventoryMerchant.setAccessible(true);
        fieldInventoryMerchant.set(this, inventory);

        final SCraftInventoryMerchant craftInventory = new SCraftInventoryMerchant(inventory);
        inventory.setCraftInventory(craftInventory);

        this.bukkitEntity = new CraftInventoryView(customer.getBukkitEntity(), craftInventory, this);
    }

    /**
     * Sets the slot at the raw index.
     *
     * @param index the index
     * @param slot  the slot
     */
    void setSlot(int index, Slot slot) {
        // Set the raw index of the slot
        slot.rawSlotIndex = index;

        // Override the slot at the index
        this.c.set(index, slot);
    }

    @Override
    public CraftInventoryView getBukkitView() {
        return this.bukkitEntity;
    }

    @Override
    public boolean a(EntityHuman human) {
        return this.merchant.hasCustomer((Player) human.getBukkitEntity());
    }

    @Override
    public void b(EntityHuman human) {
        super.b(human);

        // Called when the human is finished
        this.merchant.removeCustomer((Player) human.getBukkitEntity());
    }
}