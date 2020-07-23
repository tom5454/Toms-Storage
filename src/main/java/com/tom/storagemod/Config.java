package com.tom.storagemod;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;

@me.sargunvohra.mcmods.autoconfig1u.annotation.Config(name = "toms_storage")
public class Config implements ConfigData {
	public boolean onlyTrims = false;
	public int invRange = 16;
	public int wirelessRange = 16;
	public int invConnectorMaxCables = 2048;

	/*public static void load() {
		Configuration cfg = new Configuration(new File(FabricLoader.getInstance().getConfigDirectory(), "toms_storage.cfg"));

		invRange = cfg.getInt("inventoryConnectorRange", "general", 16, 4, 256, "Inventory Connector Range");
		onlyTrims = cfg.getBoolean("onlyTrimsConnect", "general", false, "Only Allow Trims to Connect Inventories");
		wirelessRange = cfg.getInt("wirelessReach", "general", 16, 4, 64, "Wireless terminal reach");
	}*/

	@Override
	public void validatePostLoad() throws ValidationException {
		//boolean save = false;
		if(invRange < 4 || invRange > 64) {
			invRange = 16;
			StorageMod.LOGGER.warn("Inventory Connector Range out of bounds, resetting to default");
			//save = true;
		}
		if(wirelessRange < 4 || wirelessRange > 64) {
			wirelessRange = 16;
			StorageMod.LOGGER.warn("Wireless Range out of bounds, resetting to default");
			//save = true;
		}
		if(invConnectorMaxCables < 4) {
			invConnectorMaxCables = 2048;
			StorageMod.LOGGER.warn("Inventory Cable Range out of bounds, resetting to default");
			//save = true;
		}
		//if(save) {}
	}
}
