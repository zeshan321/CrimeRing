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

import merchants.api.MerchantTradeListener;
import net.minecraft.server.v1_10_R1.*;
import org.bukkit.entity.Player;

public class SSlotMerchantResult extends SlotMerchantResult {
    private final SMerchant merchant;

    public SSlotMerchantResult(EntityPlayer player, SMerchant merchant, InventoryMerchant inventory, int index, int x, int y) {
        super(player, merchant, inventory, index, x, y);
        this.merchant = merchant;
    }

    @Override
    public void a(EntityHuman human, ItemStack itemStack) {
        // Reset the on trade
        this.merchant.onTrade = null;

        // Handle it like default
        super.a(human, itemStack);

        // Catch the on trade
        if (this.merchant.onTrade != null) {
            // This will cause the update to be send to all the players
            // except the user, that is done client side since 1.8
            this.merchant.onTradePlayer = (EntityPlayer) human;
            // Increment uses
            this.merchant.onTrade.g();
            // Reset the field for the gc
            this.merchant.onTradePlayer = null;

            for (MerchantTradeListener handler : this.merchant.handlers) {
                handler.onTrade(this.merchant, this.merchant.onTrade, (Player) human.getBukkitEntity());
            }
        }
    }
}