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

import merchants.api.Merchant;
import merchants.api.MerchantInventory;
import merchants.api.MerchantOffer;
import merchants.api.MerchantSession;
import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftInventoryMerchant;

public class SCraftInventoryMerchant extends CraftInventoryMerchant implements MerchantInventory {

    public SCraftInventoryMerchant(SInventoryMerchant merchant) {
        super(merchant);
    }

    @Override
    public Merchant getMerchant() {
        return ((SInventoryMerchant) this.inventory).merchant;
    }

    @Override
    public int getSelectedOfferIndex() {
        return ((SInventoryMerchant) this.inventory).currentIndex;
    }

    @Override
    public MerchantOffer getSelectedOffer() {
        return this.getMerchant().getOfferAt(this.getSelectedOfferIndex());
    }

    @Override
    public MerchantSession getSession() {
        return ((SInventoryMerchant) this.inventory).getOwner();
    }
}