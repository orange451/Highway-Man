package highwayman;

import org.bukkit.plugin.java.JavaPlugin;

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
	}
	
	@Override
	public void onDisable() {
		//
	}
}
