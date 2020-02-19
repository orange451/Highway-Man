package highwayman;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import spawnhuman.EntityPlayerNPC;
import spawnhuman.ai.HostilePlayerNPC;

public class HighwaymanMain extends JavaPlugin {
	public static JavaPlugin plugin;
	
	public HighwaymanMain() {
		plugin = this;
	}
	
	@Override
	public void onEnable() {
		// Load config
		new BanditData();
		
		// Start spawner
		new BanditSpawner();
		
		this.getCommand("ha-test").setExecutor(new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] arg3) {
				if ( !(sender instanceof Player) )
					return false;
				
				if ( !sender.isOp() )
					return false;
				
				Player p = (Player)sender;
				new BanditNPC(p.getLocation().add(Math.random()-Math.random(), 0, Math.random()-Math.random()));
				return true;
			}
		});

	}
	
	@Override
	public void onDisable() {
		//
	}
}
