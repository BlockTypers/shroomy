package com.blocktyper.shroomy;

/*
 * License: https://www.gnu.org/licenses/gpl-3.0.en.html
 * Source: https://github.com/spaarkimus/shroomy
 * dev.bukkit: https://dev.bukkit.org/bukkit-plugins/shroomy/
 */

import java.io.File;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ShroomyPlugin extends JavaPlugin implements Listener {

	public static int MAX_FOOD_LEVEL = 20;

	public static Material DEFAULT_MATERIAL = Material.BEETROOT;
	public static boolean DEFAULT_USE_POPPERS = true;
	public static String DEFAULT_ROASTED_MUSHROOM_NAME = "Roasted mushroom";
	public static int DEFAULT_POPPER_DELAY_SEC = 4;
	public static int DEFAULT_POPPER_FOOD_LEVEL_DELTA = 2;
	public static int DEFAULT_POPPER_SLOW_SEC = 3;
	public static int DEFAULT_POPPER_SLOW_MAGNITUDE = 5;

	public static String KEY_RESULT_MATERIAL = "result-material";
	public static String KEY_USE_POPPERS = "use-poppers";
	public static String KEY_POPPER_DELAY_SEC = "popper-delay-sec";
	public static String KEY_POPPER_FOOD_LEVEL_DELTA = "popper-food-level-delta";
	public static String KEY_POPPER_SLOW_SEC = "popper-slow-sec";
	public static String KEY_POPPER_SLOW_MAGNITUDE = "popper-slow-magnitude";

	private String roastedMushroomName;
	private Material resultMaterial;
	private boolean usePoppers;
	private int popDelaySec;
	private int popperFoodLevelDelta;
	private int popperSlowSec;
	private int popperSlowMagnitude;

	private Map<String, Date> lastTimePopperWasClickedMap;
	
	private boolean bundleLoadFailed = false;

	public void onEnable() {
		createConfig();
		getServer().getPluginManager().registerEvents(this, this);

		roastedMushroomName = getLocalizedMessage(LOCALIZED_KEY_ROASTED_MUSHROOM);

		if (roastedMushroomName == null || roastedMushroomName.trim().isEmpty()) {
			roastedMushroomName = DEFAULT_ROASTED_MUSHROOM_NAME;
		}

		String resultMaterialString = getConfig().getString(KEY_RESULT_MATERIAL);
		resultMaterial = Material.valueOf(resultMaterialString);
		if (resultMaterial == null) {
			resultMaterial = DEFAULT_MATERIAL;
		}

		if (getConfig().contains(KEY_USE_POPPERS)) {
			usePoppers = getConfig().getBoolean(KEY_USE_POPPERS);
		} else {
			usePoppers = DEFAULT_USE_POPPERS;
		}

		if (getConfig().contains(KEY_POPPER_DELAY_SEC)) {
			popDelaySec = getConfig().getInt(KEY_POPPER_DELAY_SEC);
		} else {
			popDelaySec = DEFAULT_POPPER_DELAY_SEC;
		}

		if (getConfig().contains(KEY_POPPER_FOOD_LEVEL_DELTA)) {
			popperFoodLevelDelta = getConfig().getInt(KEY_POPPER_FOOD_LEVEL_DELTA);
		} else {
			popperFoodLevelDelta = DEFAULT_POPPER_FOOD_LEVEL_DELTA;
		}

		if (getConfig().contains(KEY_POPPER_SLOW_SEC)) {
			popperSlowSec = getConfig().getInt(KEY_POPPER_SLOW_SEC);
		} else {
			popperSlowSec = DEFAULT_POPPER_SLOW_SEC;
		}

		if (getConfig().contains(KEY_POPPER_SLOW_MAGNITUDE)) {
			popperSlowMagnitude = getConfig().getInt(KEY_POPPER_SLOW_MAGNITUDE);
		} else {
			popperSlowMagnitude = DEFAULT_POPPER_SLOW_MAGNITUDE;
		}

		popperFoodLevelDelta = getConfig().getInt(KEY_POPPER_FOOD_LEVEL_DELTA);

		if (usePoppers) {
			lastTimePopperWasClickedMap = new HashMap<String, Date>();
			getLogger().info("Using \"" + roastedMushroomName + "\" poppers.");
			getLogger().info("Popper sec delay: " + popDelaySec);
			getLogger().info("Popper food level delta: " + popperFoodLevelDelta);
			getLogger().info("Popper slow sec: " + popperSlowSec);
			getLogger().info("Popper slow magnitude: " + popperSlowMagnitude);
			getServer().addRecipe(new FurnaceRecipe(new ItemStack(Material.RED_MUSHROOM), Material.RED_MUSHROOM));
			getServer().addRecipe(new FurnaceRecipe(new ItemStack(Material.RED_MUSHROOM), Material.BROWN_MUSHROOM));
		} else {
			getLogger().info("Using \"" + roastedMushroomName + "\" in the form of " + resultMaterial.toString());
			getServer().addRecipe(new FurnaceRecipe(new ItemStack(resultMaterial), Material.RED_MUSHROOM));
			getServer().addRecipe(new FurnaceRecipe(new ItemStack(resultMaterial), Material.BROWN_MUSHROOM));
		}

	}

	// begin
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
	public void furnaceSmeltMushroom(FurnaceSmeltEvent event) {
		if (!event.getSource().getType().equals(Material.BROWN_MUSHROOM)
				&& !event.getSource().getType().equals(Material.RED_MUSHROOM)) {
			return;
		}
		ItemStack result = event.getResult();
		ItemMeta itemMeta = result.getItemMeta();
		itemMeta.setDisplayName(getLocalizedMessage(LOCALIZED_KEY_ROASTED_MUSHROOM));
		result.setItemMeta(itemMeta);
		event.setResult(result);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
	public void roastedMushroomRightClick(PlayerInteractEvent event) {
		try {
			if (!usePoppers || event.getAction() == null) {
				return;
			}

			if (!event.getAction().equals(Action.RIGHT_CLICK_AIR)
					&& !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
				return;
			}

			if (event.getItem() == null || event.getItem().getItemMeta() == null) {
				return;
			}

			if (event.getItem().getItemMeta().getDisplayName() == null
					|| !event.getItem().getItemMeta().getDisplayName().equals(roastedMushroomName)) {
				return;
			}

			if (!event.getItem().getType().equals(Material.BROWN_MUSHROOM)
					&& !event.getItem().getType().equals(Material.RED_MUSHROOM)) {
				return;
			}
			// this is a roasted mushroom popper. cancel event so player can't
			// plant
			event.setCancelled(true);

			String playerName = event.getPlayer().getName();
			Date now = new Date();
			if (!lastTimePopperWasClickedMap.containsKey(playerName)
					|| lastTimePopperWasClickedMap.get(playerName) == null) {
				lastTimePopperWasClickedMap.put(playerName, now);
			} else {
				long secondsSinceLastPop = (now.getTime() - lastTimePopperWasClickedMap.get(playerName).getTime())
						/ 1000;
				if (secondsSinceLastPop < popDelaySec) {
					// it is too soon to pop
					event.getPlayer().sendMessage(getLocalizedMessage(LOCALIZED_KEY_TOO_SOON_TO_POP));
					return;
				}
			}

			int newFoodLevel = event.getPlayer().getFoodLevel() + popperFoodLevelDelta;

			if (newFoodLevel > MAX_FOOD_LEVEL) {
				event.getPlayer().setFoodLevel(MAX_FOOD_LEVEL);
			} else {
				event.getPlayer().setFoodLevel(newFoodLevel);
			}

			if(popperSlowSec > 0){
				event.getPlayer()
				.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, popperSlowSec * 20, popperSlowMagnitude));
			}
			

			int amount = event.getPlayer().getEquipment().getItemInMainHand().getAmount();
			if(amount == 1){
				event.getPlayer().getInventory().remove(event.getItem());
			}else{
				event.getPlayer().getEquipment().getItemInMainHand().setAmount(amount - 1);
			}
			lastTimePopperWasClickedMap.put(playerName, now);
		} catch (Exception e) {
			getLogger().warning("issue eating Roasted mushroom popper: " + e.getMessage());
		}

	}

	// begin config file initialization
	private void createConfig() {
		getLogger().info("Loading up Config.yml");
		try {
			if (!getDataFolder().exists()) {
				getDataFolder().mkdirs();
			}

			File file = new File(getDataFolder(), "config.yml");
			if (!file.exists()) {
				getLogger().info("Config.yml not found, creating");
				PrintWriter writer = new PrintWriter(file.getAbsolutePath(), "UTF-8");
				writer.println(KEY_RESULT_MATERIAL + ": " + DEFAULT_MATERIAL.toString());
				writer.println(KEY_USE_POPPERS + ": " + DEFAULT_USE_POPPERS);
				writer.println(KEY_POPPER_DELAY_SEC + ": " + DEFAULT_POPPER_DELAY_SEC);
				writer.println(KEY_POPPER_FOOD_LEVEL_DELTA + ": " + DEFAULT_POPPER_FOOD_LEVEL_DELTA);
				writer.println(KEY_POPPER_SLOW_SEC + ": " + DEFAULT_POPPER_SLOW_SEC);
				writer.println(KEY_POPPER_SLOW_MAGNITUDE + ": " + DEFAULT_POPPER_SLOW_MAGNITUDE);

				writer.close();
			} else {
				getLogger().info("Config.yml found, loading");
			}
			getLogger().info("Done loading up Config.yml");
		} catch (Exception e) {
			getLogger().warning("Error loading Config.yml: " + e.getMessage());
		}

	}
	// end config file initialization

	// begin localization
	private String LOCALIZED_KEY_TOO_SOON_TO_POP = "mushroom.pop.too.soon";
	private String LOCALIZED_KEY_ROASTED_MUSHROOM = "mushroom.roasted";
	private Locale defaultLocale = Locale.getDefault();
	private ResourceBundle bundle = null;

	private String getLocalizedMessage(String key) {
		
		if(bundleLoadFailed){
			return key;
		}
		
		if(bundle == null){
			try {
				bundle = ResourceBundle.getBundle("Messages", defaultLocale);
			} catch (Exception e) {
				getLogger().warning("Messages bundle did not load successfully from default location.");
			}
			if(bundle == null){
				getLogger().info("Checking for Messages bundle in secondary location (resources/Messages).");
				try {
					bundle = ResourceBundle.getBundle("resources/Messages", defaultLocale);
				} catch (Exception e) {
					getLogger().warning("Messages bundle did not load successfully from secondary location (resources/Messages).");
				}
				
				if(bundle == null){
					getLogger().warning("Messages will appear as dot separated key names.  Please remove this plugin from your plugin folder if this behaviour is not desired.");
					bundleLoadFailed = true;
					return key;
				}else{
					getLogger().info("Messages bundle loaded successfully from secondary location (resources/Messages).");
				}
			}else{
				getLogger().info("Messages bundle loaded successfully from default location.");
			}
		}
		
		
		String value = bundle.getString(key);
		try {
			value = key != null ? (value != null && !value.trim().isEmpty() ? value : key) : "null key";
		} catch (Exception e) {
			value = "error value";
		}
		return value;
	}
	// end localization
}
