package itemaux;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;

import java.util.Base64;
import java.util.List;

public class ItemStackSerializer {
    public static String serializeItemStack(ItemStack itemStack) {
        String itemString = "";

        // Serializza il tipo di oggetto e la quantità
        itemString += itemStack.getType().toString() + ":" + itemStack.getAmount();

        // Serializza i dati specifici dell'oggetto (metadata)
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore != null && !lore.isEmpty()) {
                itemString += ":";
                for (String line : lore) {
                    itemString += line + "|";
                }
                itemString = itemString.substring(0, itemString.length() - 1); // Rimuovi l'ultimo carattere "|"
            }
        }

        // Codifica la stringa risultante in Base64
        return Base64.getEncoder().encodeToString(itemString.getBytes());
    }

    public static ItemStack deserializeItemStack(String itemString) {
        byte[] data = Base64.getDecoder().decode(itemString);
        itemString = new String(data);

        String[] parts = itemString.split(":");
        if (parts.length >= 2) {
            String itemType = parts[0];
            int amount = Integer.parseInt(parts[1]);

            ItemStack itemStack = new ItemStack(Material.valueOf(itemType));
            itemStack.setAmount(amount);

            if (parts.length > 2) {
                ItemMeta meta = itemStack.getItemMeta();
                List<String> lore = meta.getLore();

                String[] loreLines = parts[2].split("\\|");
                for (String line : loreLines) {
                    lore.add(line);
                }

                meta.setLore(lore);
                itemStack.setItemMeta(meta);
            }

            return itemStack;
        }

        return null;
    }
}

