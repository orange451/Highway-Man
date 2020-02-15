package highwayman;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import spawnhuman.SpawnHuman;

public class BanditSpawner {
	public int NEXT_SPAWN = 0;
	
	public BanditSpawner() {
		
		// Load the CFG
		new BanditData();
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(SpawnHuman.plugin, new Runnable() {
			@Override
			public void run() {
				NEXT_SPAWN--;
				
				if ( NEXT_SPAWN > 0 )
					return;
				NEXT_SPAWN = 120 + (int) (Math.random() * 120);
				
				Collection<? extends Player> players = Bukkit.getOnlinePlayers();
				if ( players.size() == 0 )
					return;
				
				int maxAmt = (int) Math.max(players.size() * BanditData.NPCS_PER_PLAYER, 2);
				
				if ( SpawnHuman.npcs().length >= maxAmt || !BanditData.loaded )
					return;
				
				Player randomPlayer = players.toArray(new Player[players.size()])[(int) (Math.random()*players.size())];
				
				// Find free location around player
				boolean found = false;
				int tries = 0;
				Location loc = randomPlayer.getLocation();
				while ( !found && tries < 8 ) {
					tries++;
					float dir = (float) (Math.random()*Math.PI*2);
					
					float dist = (float) (BanditData.MINIMUM_SPAWN_DISTANCE + Math.random() * (BanditData.MAXIMUM_SPAWN_DISTANCE-BanditData.MINIMUM_SPAWN_DISTANCE));
					int xx = (int) (Math.cos(dir)*dist);
					int zz = (int) (Math.sin(dir)*dist);
					int yy = (int) (loc.getY()-16);
					
					for (int i = 0; i < 32; i++) {
						Block b1 = loc.getWorld().getBlockAt(loc.getBlockX()+xx, yy+i, loc.getBlockZ()+zz);
						
						if ( b1.getY() < BanditData.MINIMUM_SPAWN_Y )
							continue;
						
						if ( b1.getY() > BanditData.MAXIMUM_SPAWN_Y )
							continue;

						if ( !b1.getType().isSolid() )
							continue;
						
						if ( !BanditData.ALLOWED_WORLDS.contains(b1.getWorld()) )
							continue;
						
						if ( !BanditData.BLOCKS_CAN_SPAWN_ON.contains(b1.getType()) )
							continue;
						
						Location above1 = b1.getLocation().add(0,1,0);
						Location above2 = b1.getLocation().add(0,2,0);
						if ( !above1.getBlock().getType().isSolid() && !above2.getBlock().getType().isSolid() ) {
							loc = above1;
							found = true;
						}
					}
				}
				
				if ( found ) {
					new BanditNPC(loc);
				}
			}
		}, 1, 1);
	}
}
