package com.adamcalculator.dynamicpack;

import com.adamcalculator.dynamicpack.pack.Pack;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class DynamicPackMod implements ClientModInitializer {
	public static final String CLIENT_FILE = "dynamicmcpack.json";
	public static List<Pack> packs = new ArrayList<>();
	public static File gameDir;
	public static File resourcePacks;

	@Override
	public void onInitializeClient() {
		gameDir = FabricLoader.getInstance().getGameDir().toFile();
		resourcePacks = new File(gameDir, "resourcepacks");
		resourcePacks.mkdirs();

		rescanPacks();

		DebugThread.startDebug();
	}

	public static void rescanPacks() {
		packs.clear();

		for (File packFile : AFiles.lists(resourcePacks)) {
			Out.println("file: " + packFile.getName());
			try {
				if (packFile.isDirectory()) {
					File dynamic = new File(packFile, CLIENT_FILE);
					if (AFiles.exists(dynamic)) {
						processPack(packFile, new JSONObject(AFiles.read(dynamic)));
					}

				} else if (packFile.getName().endsWith(".zip")) {
					ZipFile zipFile = new ZipFile(packFile);
					ZipEntry entry = zipFile.getEntry(CLIENT_FILE);
					if (entry != null) {
						processPack(packFile, PackUtil.readJson(zipFile.getInputStream(entry)));
					}
				}
			} catch (Exception e) {
				Out.error("Error while processing pack: " + packFile, e);
			}
		}
	}



	public static void processPack(File location, JSONObject json) {
		Out.println("pack " + location + ": " + json);

		long formatVersion = json.getLong("formatVersion");
		if (formatVersion == 1) {

			Pack pack = new Pack(location, json);
			packs.add(pack);

		} else {
			throw new RuntimeException("Unsupported formatVersion!");
		}

	}
}