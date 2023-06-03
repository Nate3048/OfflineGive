package commands;

import DB.YourDatabaseManager;
import itemaux.ItemStackSerializer;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.bukkit.Bukkit.getServer;

public class GiveOfflineCommand implements CommandExecutor {
    private Connection connection; // Connessione al database SQL
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        this.connection = YourDatabaseManager.getConnection(); // Ottieni la connessione al database
        if (command.getName().equalsIgnoreCase("giveoffline")) {
            if (args.length < 3) {
                sender.sendMessage("Utilizzo corretto: /giveoffline <player> <item> <quantity>");
                return true;
            }

            String targetPlayerName = args[0];



            Player targetPlayer = getServer().getPlayer(targetPlayerName);
            if (targetPlayer != null) {
                if (targetPlayer.isOnline()) {
                    // Il giocatore è online, puoi dare l'oggetto direttamente
                    ItemStack item = getItemFromArgs(args);
                    targetPlayer.getInventory().addItem(item);
                    sender.sendMessage("Oggetto dato con successo al giocatore " + targetPlayer.getName());

                } else {
                    // Il giocatore è offline, salva l'oggetto in sospeso
                    // Controllo se il giocatore esiste già nella tabella users
                    try {
                        PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE player_name = ?");
                        statement.setString(1, targetPlayerName);
                        ResultSet resultSet = statement.executeQuery();

                        // Se il giocatore non esiste nella tabella users, lo inserisco
                        if (!resultSet.next()) {
                            PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO users (player_name) VALUES (?)");
                            insertStatement.setString(1, targetPlayerName);
                            insertStatement.executeUpdate();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    ItemStack item = getItemFromArgs(args);
                    String serializedItem = ItemStackSerializer.serializeItemStack(item);

                    try {
                        PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO give (item_obj, player_name) VALUES (?, ?)");
                        insertStatement.setString(1, serializedItem);
                        insertStatement.setString(2, targetPlayerName);
                        insertStatement.executeUpdate();

                        sender.sendMessage("Oggetto in sospeso per il giocatore " + targetPlayerName);
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
                return true;
            }
        }
        return false;
    }


    private ItemStack getItemFromArgs(String[] args) {
        Material material;
        try {
            material = Material.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            // Il tipo di oggetto specificato non è valido
            return null;
        }

        ItemStack item = new ItemStack(material);

        if (args.length >= 3) {
            int quantity;
            try {
                quantity = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                // La quantità specificata non è un numero valido
                return null;
            }

            if (quantity <= 0) {
                // La quantità deve essere un numero positivo
                return null;
            }

            item.setAmount(quantity);
        }

        return item;
    }

}
