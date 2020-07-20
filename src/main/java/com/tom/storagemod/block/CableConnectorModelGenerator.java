package com.tom.storagemod.block;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.util.Direction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CableConnectorModelGenerator {

	public static void main(String[] args) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		File assets = new File("src/main/resources/assets/toms_storage");
		File models = new File(assets, "models/block/cable_connector");
		File blockstates = new File(assets, "blockstates");
		List<Map<String, Object>> multipart = new ArrayList<>();
		multipart.add(Collections.singletonMap("apply", Collections.singletonMap("model", "toms_storage:block/cable_base")));
		String[] ROT = new String[] {
				"x90",
				"x-90",
				"",
				"y180",
				"y270",
				"y90"
		};
		for (Direction side : Direction.values()) {
			{
				Map<String, Object> part = new HashMap<>();
				multipart.add(part);
				part.put("when", Collections.singletonMap(side.getName2(), "false"));
				Map<String, Object> model = new HashMap<>();
				part.put("apply", model);
				model.put("model", "toms_storage:block/cable_closed");
				model.put("uvlock", "true");
				if(!ROT[side.ordinal()].isEmpty())model.put(ROT[side.ordinal()].substring(0, 1), Integer.parseInt(ROT[side.ordinal()].substring(1)));
			}{
				Map<String, Object> part = new HashMap<>();
				multipart.add(part);
				part.put("when", Collections.singletonMap(side.getName2(), "true"));
				Map<String, Object> model = new HashMap<>();
				part.put("apply", model);
				model.put("model", "toms_storage:block/cable_open");
				model.put("uvlock", "true");
				if(!ROT[side.ordinal()].isEmpty())model.put(ROT[side.ordinal()].substring(0, 1), Integer.parseInt(ROT[side.ordinal()].substring(1)));
			}
		}
		for (Direction facing : Direction.values()) {
			{
				Map<String, Object> part = new HashMap<>();
				multipart.add(part);
				part.put("when", Collections.singletonMap("facing", facing.getName2()));
				Map<String, Object> model = new HashMap<>();
				part.put("apply", model);
				model.put("model", "toms_storage:block/cable_connector/base");
				if(!ROT[facing.ordinal()].isEmpty())model.put(ROT[facing.ordinal()].substring(0, 1), Integer.parseInt(ROT[facing.ordinal()].substring(1)));
			}
			{
				Map<String, Object> part = new HashMap<>();
				multipart.add(part);
				Map<String, Object> when = new HashMap<>();
				part.put("when", when);
				when.put("facing", facing.getName2());
				when.put(facing.getName2(), "true");
				Map<String, Object> model = new HashMap<>();
				part.put("apply", model);
				model.put("model", "toms_storage:block/cable_connector/ext");
				if(!ROT[facing.ordinal()].isEmpty())model.put(ROT[facing.ordinal()].substring(0, 1), Integer.parseInt(ROT[facing.ordinal()].substring(1)));
			}
		}
		/*for (DyeColor color : DyeColor.values()) {
			for (Direction facing : Direction.values()) {
				{
					Map<String, Object> part = new HashMap<>();
					multipart.add(part);
					Map<String, Object> when = new HashMap<>();
					part.put("when", when);
					when.put("facing", facing.getName2());
					//when.put("color", color.getName());
					Map<String, Object> model = new HashMap<>();
					part.put("apply", model);
					model.put("model", "toms_storage:block/cable_connector/color_" + color.getName());
					if(!ROT[facing.ordinal()].isEmpty())model.put(ROT[facing.ordinal()].substring(0, 1), Integer.parseInt(ROT[facing.ordinal()].substring(1)));
				}
			}
			Map<String, Object> model = new HashMap<>();
			model.put("parent", "toms_storage:block/cable_connector/color_base");
			model.put("textures", Collections.singletonMap("tex", "block/" + color.getName() + "_wool"));
			File out = new File(models, "color_" + color.getName() + ".json");
			try (PrintWriter wr = new PrintWriter(out)){
				gson.toJson(model, wr);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}*/
		File out = new File(blockstates, "ts.inventory_cable_connector.json");
		try (PrintWriter wr = new PrintWriter(out)){
			gson.toJson(Collections.singletonMap("multipart", multipart), wr);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
