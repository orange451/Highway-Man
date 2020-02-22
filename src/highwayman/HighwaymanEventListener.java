package highwayman;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import spawnhuman.EntityPlayerNPC;
import spawnhuman.SpawnHuman;

public class HighwaymanEventListener implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDeath(PlayerDeathEvent event) {

		// Make sure it's a NPC
		EntityPlayerNPC npc = SpawnHuman.matchNPC(event.getEntity());
		if ( npc == null )
			return;
		
		// Make sure it's a Bandit!
		if ( !(npc instanceof BanditNPC) )
			return;
		
		// If we dont want a death message, set it to null!
		if ( !BanditData.DISPLAY_DEATH_MESSAGE )
			event.setDeathMessage(null);
	}
}
