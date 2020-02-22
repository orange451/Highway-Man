package highwayman;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import spawnhuman.SpawnHuman;
import spawnhuman.etc.Ticks;

public class BanditSpawner {
	public long NEXT_SPAWN = 0;
	
	private boolean safeToSpawnPlayer(Location location) {
		Block ground = location.getBlock();
		Block groundPlus1 = ground.getLocation().add(0, 1, 0).getBlock();
		Block groundPlus2 = ground.getLocation().add(0, 2, 0).getBlock();
		
		// Make sure there's air above the block!
		if ( !groundPlus1.getType().equals(Material.AIR) )
			return false;
		
		if ( !groundPlus2.getType().equals(Material.AIR) )
			return false;
		
		// Check for nearby lava
		final int len = 12;
		for (int i = -len/2; i <= len/2; i++) { // x
			for (int j = -len/4; j <= len/4; j++) { // y
				for (int k = -len/2; k <= len/2; k++) { // z
					if ( i == location.getBlockX() && k == location.getBlockZ() )
						continue;
					
					Block checkLava = ground.getLocation().add(i, j, k).getBlock();
					if ( checkLava == null )
						continue;
					
					if ( checkLava.getType().equals(Material.LAVA) )
						return false;
				}
			}
		}
		
		return true;
	}
	
	public BanditSpawner() {
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(SpawnHuman.plugin, new Runnable() {
			@Override
			public void run() {
				NEXT_SPAWN--;
				
				if ( NEXT_SPAWN > 0 )
					return;
				NEXT_SPAWN = Ticks.fromSeconds(6) + (int) (Math.random() * Ticks.fromSeconds(6));
				
				Collection<? extends Player> players = Bukkit.getOnlinePlayers();
				if ( players.size() == 0 )
					return;
				
				int maxAmt = (int) Math.max(players.size() * BanditData.NPCS_PER_PLAYER, 2);
				
				if ( SpawnHuman.npcs().length >= maxAmt || !BanditData.loaded )
					return;
				
				Player randomPlayer = players.toArray(new Player[players.size()])[(int) (Math.random()*players.size())];
				
				// Find free location around player
				int tries = 0;
				Location loc = randomPlayer.getLocation();
				Location spawnLocation = null;
				searchSpawn: while ( spawnLocation == null && tries < 8 ) {
					tries++;
					float dir = (float) (Math.random()*Math.PI*2);
					
					float dist = (float) (BanditData.MINIMUM_SPAWN_DISTANCE + Math.random() * (BanditData.MAXIMUM_SPAWN_DISTANCE-BanditData.MINIMUM_SPAWN_DISTANCE));
					
					final int heightDist = 48;
					int xx = (int) (Math.cos(dir)*dist);
					int zz = (int) (Math.sin(dir)*dist);
					int yy = (int) (loc.getY()-(heightDist/2f));
					
					for (int i = 0; i < heightDist; i++) {
						Location testLocation = new Location(loc.getWorld(), loc.getBlockX()+xx, yy+i, loc.getBlockZ()+zz);
						
						Block potentialGround = testLocation.getBlock();
						if ( potentialGround == null )
							continue;
						
						if ( potentialGround.getY() < BanditData.MINIMUM_SPAWN_Y )
							continue;
						
						if ( potentialGround.getY() > BanditData.MAXIMUM_SPAWN_Y )
							continue;

						if ( !potentialGround.getType().isSolid() )
							continue;
						
						if ( !BanditData.ALLOWED_WORLDS.contains(potentialGround.getWorld()) )
							continue;
						
						if ( !BanditData.BLOCKS_CAN_SPAWN_ON.contains(potentialGround.getType()) )
							continue;
						
						if ( safeToSpawnPlayer(potentialGround.getLocation()) ) {
							spawnLocation = testLocation.clone().add(0, 1, 0);
							break searchSpawn;
						}
					}
				}
				
				// Spawn the Bandit
				if ( spawnLocation != null ) {
					new BanditNPC(spawnLocation);
				}
			}
		}, 1, 1);
	}
}
