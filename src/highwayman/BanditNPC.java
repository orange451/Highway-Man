package highwayman;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import spawnhuman.SpawnHuman;
import spawnhuman.ai.HostilePlayerNPC;
import spawnhuman.etc.RarityItem;

public class BanditNPC extends HostilePlayerNPC {
	
	public BanditNPC(Location location) {
		super(ChatColor.translateAlternateColorCodes('&', BanditData.DISPLAY_NAME), location);
		
		// Set skin
		this.setSkinName(BanditData.SKIN_NAME);
		
		// Mark this npc as to not attack other npcs of the same type
		this.canAttackSameNPCs = false;
		this.canAttackPassiveMobs = false;
		
		ItemStack weapon = RarityItem.pick(BanditData.WEAPONS, BanditData.RANDOM_GEAR_DURABILITY);
		ItemStack helmet = RarityItem.pick(BanditData.ARMOR_HELMET, BanditData.RANDOM_GEAR_DURABILITY);
		ItemStack chestplate = RarityItem.pick(BanditData.ARMOR_CHESTPLATE, BanditData.RANDOM_GEAR_DURABILITY);
		ItemStack leggings = RarityItem.pick(BanditData.ARMOR_LEGGINGS, BanditData.RANDOM_GEAR_DURABILITY);
		ItemStack boots = RarityItem.pick(BanditData.ARMOR_BOOTS, BanditData.RANDOM_GEAR_DURABILITY);
		
		ItemStack[] drops = new ItemStack[BanditData.DROP_ITEMS];
		for (int i = 0; i < drops.length; i++) {
			drops[i] = RarityItem.pick(BanditData.INVENTORY);
		}
		
		// Apply armor with delay (sometimes disappears)
		/*Bukkit.getScheduler().scheduleSyncDelayedTask(SpawnHuman.plugin, new Runnable() {
			@Override
			public void run() {*/
				getPlayer().getInventory().setItemInMainHand(weapon);
				getPlayer().getInventory().setHelmet(helmet);
				getPlayer().getInventory().setChestplate(chestplate);
				getPlayer().getInventory().setLeggings(leggings);
				getPlayer().getInventory().setBoots(boots);
				getPlayer().getInventory().addItem(drops);
				
				// Ensure max health
				getPlayer().setMaxHealth(BanditData.INITIAL_HEALTH);
				getPlayer().resetMaxHealth();
				getPlayer().setHealth(BanditData.INITIAL_HEALTH);
				
				if ( weapon.getType().equals(Material.BOW) )
					getPlayer().getInventory().addItem(new ItemStack(Material.ARROW, 10 + (int)(Math.random()*10)));
			//}
		//}, 10);
	}

}
