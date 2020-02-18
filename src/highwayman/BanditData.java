package highwayman;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import spawnhuman.SpawnHuman;
import spawnhuman.etc.JSONUtil;
import spawnhuman.etc.RarityItem;

public class BanditData {

	public static RarityItem[] WEAPONS;
	public static RarityItem[] ARMOR_CHESTPLATE;
	public static RarityItem[] ARMOR_HELMET;
	public static RarityItem[] ARMOR_LEGGINGS;
	public static RarityItem[] ARMOR_BOOTS;
	public static RarityItem[] INVENTORY;

	public static boolean RANDOM_GEAR_DURABILITY;
	public static double NPCS_PER_PLAYER;

	public static String DISPLAY_NAME;
	public static String SKIN_NAME;
	public static int DROP_ITEMS;
	public static int INITIAL_HEALTH;

	public static int MINIMUM_SPAWN_Y;
	public static int MAXIMUM_SPAWN_Y;
	public static int MINIMUM_SPAWN_DISTANCE;
	public static int MAXIMUM_SPAWN_DISTANCE;
	public static HashSet<Material> BLOCKS_CAN_SPAWN_ON;
	public static HashSet<World> ALLOWED_WORLDS;
	
	public static boolean loaded;
	
	static {
		ALLOWED_WORLDS = new HashSet<World>();
		BLOCKS_CAN_SPAWN_ON = new HashSet<Material>();
		DISPLAY_NAME = "&cHighway Man&f";
		SKIN_NAME = "Highway Man";
		MINIMUM_SPAWN_Y = -1;
		MAXIMUM_SPAWN_Y = 1024;
		MINIMUM_SPAWN_DISTANCE = 70;
		MAXIMUM_SPAWN_DISTANCE = 94;
		DROP_ITEMS = 1;
		NPCS_PER_PLAYER = 1;
		RANDOM_GEAR_DURABILITY = true;
		INITIAL_HEALTH = 10;
	}

	public BanditData() {
		JSONParser parser = new JSONParser();
		JSONObject jObject = null;

		// Make sure we have a data folder
		SpawnHuman.plugin.getDataFolder().mkdirs();

		File configFile = new File(SpawnHuman.plugin.getDataFolder() + File.separator + "config.json");
		if ( !configFile.exists() ) {
			jObject = newConfig();
			String writeString = JSONUtil.prettyPrint(jObject);
			
			try {
				FileWriter f = new FileWriter(configFile);
				
				f.write(writeString);
				
				f.flush();
				f.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			FileReader reader = new FileReader(configFile);
			jObject = (JSONObject) parser.parse(reader);
			
			reader.close();
		} catch (Exception e) {
			System.out.println("ERROR READING CONFIG FILE: " + configFile);
			e.printStackTrace();
			return;
		}

		try {
			// Spawner Data
			Object spawnData = jObject.get("SPAWN_DATA");
			if ( spawnData == null )
				throw new Exception("SPAWN_DATA data not correctly defined.");
			loadSpawnData((JSONObject) spawnData);
			
			// General Data
			loadGeneralData( jObject );
	
			// Inventory Data
			WEAPONS = readItems(jObject, "WEAPONS");
			ARMOR_CHESTPLATE = readItems(jObject, "ARMOR_CHESTPLATE");
			ARMOR_HELMET = readItems(jObject, "ARMOR_HELMET");
			ARMOR_LEGGINGS = readItems(jObject, "ARMOR_LEGGINGS");
			ARMOR_BOOTS = readItems(jObject, "ARMOR_BOOTS");
			INVENTORY = readItems(jObject, "INVENTORY");

			if ( ALLOWED_WORLDS.size() == 0 )
				throw new Exception("ALLOWED_WORLDS not correctly configured for your server.");
			
			if ( BLOCKS_CAN_SPAWN_ON.size() == 0 )
				throw new Exception("NPC cannot spawn on any blocks. BLOCKS_CAN_SPAWN_ON incorrectly defined.");
			
			// Mark as loaded
			loaded = true;
		} catch(Exception e) { 
			System.out.println("------------------------------");
			System.out.println("Error loading Highwayman Data. Please report the following error to the plugin developer:");
			e.printStackTrace();
		}
		
		if ( loaded ) {
			System.out.println("-------------------------------------------");
			System.out.println("- Highwayman config file correctly parsed -");
			System.out.println("-------------------------------------------");
		}
	}

	private void loadGeneralData(JSONObject jObject) {
		System.out.println("\tLoading general data");
		DISPLAY_NAME = readStringSafe( jObject, "DISPLAY_NAME", DISPLAY_NAME );
		SKIN_NAME = readStringSafe( jObject, "SKIN_NAME", SKIN_NAME );
		DROP_ITEMS = readIntSafe( jObject, "DROP_ITEMS", DROP_ITEMS );
		INITIAL_HEALTH  = readIntSafe( jObject, "INITIAL_HEALTH", INITIAL_HEALTH);
		RANDOM_GEAR_DURABILITY = readBooleanSafe( jObject, "RANDOM_GEAR_DURABILITY", RANDOM_GEAR_DURABILITY );
		
		// INITIAL_HEALTH Must be within bounds 0-50
		if ( INITIAL_HEALTH < 0 || INITIAL_HEALTH > 50 ) {
			INITIAL_HEALTH = Math.min(50, Math.max(0, INITIAL_HEALTH));
			System.err.println("Field INITIAL_HEALTH is outside of bounds [0-50]. It has been capped to be: " + INITIAL_HEALTH);
		}
	}

	private void loadSpawnData(JSONObject spawnData) {
		System.out.println("\tLoading SPAWN_DATA");
		MINIMUM_SPAWN_Y = readIntSafe( spawnData, "MINIMUM_SPAWN_Y", MINIMUM_SPAWN_Y );
		MAXIMUM_SPAWN_Y = readIntSafe( spawnData, "MAXIMUM_SPAWN_Y", MAXIMUM_SPAWN_Y );
		MINIMUM_SPAWN_DISTANCE = readIntSafe( spawnData, "MINIMUM_SPAWN_DISTANCE", MINIMUM_SPAWN_DISTANCE );
		MAXIMUM_SPAWN_DISTANCE = readIntSafe( spawnData, "MAXIMUM_SPAWN_DISTANCE", MAXIMUM_SPAWN_DISTANCE );

		NPCS_PER_PLAYER = readDoubleSafe( spawnData, "NPCS_PER_PLAYER", NPCS_PER_PLAYER);

		JSONArray allowedWorlds = (JSONArray) spawnData.get("ALLOWED_WORLDS");
		for (int i = 0; i < allowedWorlds.size(); i++) {
			World w = Bukkit.getWorld((String) allowedWorlds.get(i));
			if ( w == null ) {
				System.out.println("\t\tMissing world: " + w);
			} else {
				ALLOWED_WORLDS.add(w);
			}
		}

		JSONArray allowedBlocks = (JSONArray) spawnData.get("BLOCKS_CAN_SPAWN_ON");
		for (int i = 0; i < allowedBlocks.size(); i++) {
			Material m = Material.matchMaterial((String) allowedBlocks.get(i));
			if ( m == null ) {
				System.out.println("\t\tMissing block: " + m);
			} else {
				BLOCKS_CAN_SPAWN_ON.add(m);
			}
		}
	}

	private String readStringSafe(JSONObject data, String key, String def) {
		Object v = data.get(key);
		if ( v == null ) {
			System.out.println("\t\tMissing " + key + ". Default: " + def);
			return def;
		}
		
		return v.toString();
	}

	private boolean readBooleanSafe(JSONObject data, String key, boolean def) {
		Object v = data.get(key);
		if ( v == null ) {
			System.out.println("\t\tMissing " + key + ". Default: " + def);
			return def;
		}
		
		return (boolean)v;
	}

	private int readIntSafe(JSONObject data, String key, int def) {
		Object v = data.get(key);
		if ( v == null ) {
			System.out.println("\t\tMissing " + key + ". Default: " + def);
			return def;
		}
		
		return Math.toIntExact((long) v);
	}
	
	private double readDoubleSafe(JSONObject data, String key, double def) {
		Object v = data.get(key);
		if ( v == null ) {
			System.out.println("\t\tMissing " + key + ". Default: " + def);
			return def;
		}
		
		return Double.parseDouble(v.toString());
	}

	private RarityItem[] readItems(JSONObject jObject, String name) {
		System.out.println("\tLoading " + name);
		List<RarityItem> temp = new ArrayList<RarityItem>();
		
		Object t = jObject.get(name);
		JSONArray data = (JSONArray) t;

		for (int i = 0; i < data.size(); i++) {
			JSONObject item = (JSONObject) data.get(i);

			String mt = (String) item.get("Material");
			Material material = Material.matchMaterial(mt);
			if ( material == null ) {
				System.out.println("\t\tMissing material: " + mt);
				continue;
			}
			
			int amount = Math.toIntExact((long) item.get("Amount"));
			int rarity = Math.toIntExact((long) item.get("Rarity"));

			ItemStack stack = new ItemStack(material, amount);
			RarityItem rarityItem = new RarityItem( stack, rarity );

			temp.add(rarityItem);
		}

		return temp.toArray(new RarityItem[temp.size()]);
	}

	private JSONObject newConfig() {
		JSONObject base = new JSONObject();
		base.put("DISPLAY_NAME", DISPLAY_NAME);
		base.put("SKIN_NAME", SKIN_NAME);
		base.put("DROP_ITEMS", DROP_ITEMS);
		base.put("INITIAL_HEALTH", INITIAL_HEALTH);
		base.put("RANDOM_GEAR_DURABILITY", RANDOM_GEAR_DURABILITY);


		JSONObject SPAWN_DATA = new JSONObject();
		{
			SPAWN_DATA.put("NPCS_PER_PLAYER", NPCS_PER_PLAYER);
			SPAWN_DATA.put("MINIMUM_SPAWN_Y", MINIMUM_SPAWN_Y);
			SPAWN_DATA.put("MAXIMUM_SPAWN_Y", MAXIMUM_SPAWN_Y);
			
			SPAWN_DATA.put("MINIMUM_SPAWN_DISTANCE", MINIMUM_SPAWN_DISTANCE);
			SPAWN_DATA.put("MAXIMUM_SPAWN_DISTANCE", MAXIMUM_SPAWN_DISTANCE);

			JSONArray BLOCKS_CAN_SPAWN_ON = new JSONArray();
			BLOCKS_CAN_SPAWN_ON.add("GRASS_BLOCK");
			SPAWN_DATA.put("BLOCKS_CAN_SPAWN_ON", BLOCKS_CAN_SPAWN_ON);

			JSONArray ALLOWED_WORLDS = new JSONArray();
			ALLOWED_WORLDS.add("world");
			SPAWN_DATA.put("ALLOWED_WORLDS", ALLOWED_WORLDS);
		}
		base.put("SPAWN_DATA", SPAWN_DATA);

		base.put("_comment", "Rarity is defined as the higher the number the more common that item will occur.");

		JSONArray WEAPONS = new JSONArray();
		addItem(WEAPONS, "DIAMOND_SWORD", 1, 1);
		addItem(WEAPONS, "IRON_AXE", 1, 3);
		addItem(WEAPONS, "IRON_SWORD", 1, 5);
		addItem(WEAPONS, "STONE_AXE", 1, 8);
		addItem(WEAPONS, "BOW", 1, 14);
		addItem(WEAPONS, "STONE_SWORD", 1, 24);
		addItem(WEAPONS, "WOODEN_SWORD", 1, 30);
		base.put("WEAPONS", WEAPONS);

		JSONArray ARMOR_CHESTPLATE = new JSONArray();
		addItem(ARMOR_CHESTPLATE, "CHAINMAIL_CHESTPLATE", 1, 1);
		addItem(ARMOR_CHESTPLATE, "IRON_CHESTPLATE", 1, 2);
		addItem(ARMOR_CHESTPLATE, "GOLDEN_CHESTPLATE", 1, 3);
		addItem(ARMOR_CHESTPLATE, "LEATHER_CHESTPLATE", 1, 20);
		addItem(ARMOR_CHESTPLATE, "AIR", 1, 35);
		base.put("ARMOR_CHESTPLATE", ARMOR_CHESTPLATE);

		JSONArray ARMOR_HELMET = new JSONArray();
		addItem(ARMOR_HELMET, "TURTLE_HELMET", 1, 1);
		addItem(ARMOR_HELMET, "CHAINMAIL_HELMET", 1, 2);
		addItem(ARMOR_HELMET, "IRON_HELMET", 1, 3);
		addItem(ARMOR_HELMET, "GOLDEN_HELMET", 1, 4);
		addItem(ARMOR_HELMET, "LEATHER_HELMET", 1, 24);
		addItem(ARMOR_HELMET, "AIR", 1, 38);
		base.put("ARMOR_HELMET", ARMOR_HELMET);

		JSONArray ARMOR_LEGGINGS = new JSONArray();
		addItem(ARMOR_LEGGINGS, "AIR", 1, 1);
		base.put("ARMOR_LEGGINGS", ARMOR_LEGGINGS);

		JSONArray ARMOR_BOOTS = new JSONArray();
		addItem(ARMOR_BOOTS, "AIR", 1, 1);
		base.put("ARMOR_BOOTS", ARMOR_BOOTS);

		JSONArray INVENTORY = new JSONArray();
		addItem(INVENTORY, "MUSIC_DISC_WAIT", 1, 1);
		addItem(INVENTORY, "GOLDEN_APPLE", 1, 1);
		addItem(INVENTORY, "SADDLE", 1, 1);
		addItem(INVENTORY, "COMPASS", 1, 2);
		addItem(INVENTORY, "MAP", 1, 2);
		addItem(INVENTORY, "COOKIE", 1, 2);
		addItem(INVENTORY, "COOKIE", 1, 2);
		addItem(INVENTORY, "APPLE", 1, 3);
		addItem(INVENTORY, "COOKED_CHICKEN", 1, 3);
		addItem(INVENTORY, "COOKED_CHICKEN", 1, 3);
		addItem(INVENTORY, "TROPICAL_FISH", 1, 5);
		addItem(INVENTORY, "RABBIT_STEW", 1, 6);
		addItem(INVENTORY, "MUSHROOM_STEW", 1, 7);
		addItem(INVENTORY, "BREAD", 1, 10);
		addItem(INVENTORY, "AIR", 1, 32);
		base.put("INVENTORY", INVENTORY);

		return base;
	}

	private void addItem(JSONArray array, String material, int amount, int rarity) {
		JSONObject obj = new JSONObject();
		obj.put("Material", material);
		obj.put("Amount", amount);
		obj.put("Rarity", rarity);

		array.add(obj);
	}
}
