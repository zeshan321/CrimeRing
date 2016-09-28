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
package merchants.api;

import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public interface MerchantAPI {

    /**
     * Creates a new {@link Merchant} with the specified title.
     *
     * @param title The title
     * @return The merchant
     */
    Merchant newMerchant(String title);

    /**
     * Creates a new {@link Merchant} with the specified title.
     *
     * @param title     The title
     * @param jsonTitle Title in json format
     * @return The merchant
     */
    Merchant newMerchant(String title, boolean jsonTitle);

    /**
     * Creates a new {@link MerchantOffer} with the trade items.
     *
     * @param result     The resulting item stack
     * @param firstItem  The first item stack
     * @param secondItem The second item stack
     * @return The merchant offer
     */
    MerchantOffer newOffer(ItemStack result, ItemStack firstItem, @Nullable ItemStack secondItem);

    /**
     * Creates a new {@link MerchantOffer} with the trade items.
     *
     * @param result    The resulting item stack
     * @param firstItem The first item stack
     * @return The merchant offer
     */
    MerchantOffer newOffer(ItemStack result, ItemStack firstItem);

}
