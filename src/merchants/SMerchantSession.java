package merchants;

import merchants.api.Merchant;
import merchants.api.MerchantSession;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class SMerchantSession implements MerchantSession {

    private final Merchant merchant;
    private final Inventory inventory;
    private final Player customer;

    SMerchantSession(Merchant merchant, Inventory inventory, Player customer) {
        this.inventory = inventory;
        this.merchant = merchant;
        this.customer = customer;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    @Override
    public Player getCustomer() {
        return this.customer;
    }

    @Override
    public Merchant getMerchant() {
        return this.merchant;
    }
}