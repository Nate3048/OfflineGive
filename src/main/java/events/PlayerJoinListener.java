package events;

import DB.YourDatabaseManager;
import itemaux.ItemStackSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.*;

public class PlayerJoinListener implements Listener {

    private Connection connection; // Connessione al database SQL

    public PlayerJoinListener() {
        this.connection = YourDatabaseManager.getConnection(); // Ottieni la connessione al database
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM give WHERE player_name = ?");
            statement.setString(1, playerName);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String itemString = resultSet.getString("item_obj");
                int quantity = resultSet.getInt("quantity");

                // Eseguo l'operazione per dare l'oggetto al giocatore
                ItemStack item = ItemStackSerializer.deserializeItemStack(itemString);
                item.setAmount(quantity);
                player.getInventory().addItem(item);
            }

            // Rimuovo gli oggetti in sospeso dal database
            PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM give WHERE player_name = ?");
            deleteStatement.setString(1, playerName);
            deleteStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    // Gestione delle eccezioni durante la chiusura della connessione
                    e.printStackTrace();
                }
            }
        }
    }


    private ItemStack createItemStackFromItemId(int itemId) {
        Material material = null;

        for (Material mat : Material.values()) {
            if (mat.getId() == itemId) {
                material = mat;
                break;
            }
        }

        if (material == null) {
            // L'ID dell'oggetto non corrisponde a nessun tipo di oggetto esistente
            return null;
        }

        return new ItemStack(material);
    }

}
