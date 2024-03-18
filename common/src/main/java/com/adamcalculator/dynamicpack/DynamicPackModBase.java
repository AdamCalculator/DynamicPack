package com.adamcalculator.dynamicpack;

import com.adamcalculator.dynamicpack.pack.Pack;
import com.adamcalculator.dynamicpack.pack.Remote;
import com.adamcalculator.dynamicpack.util.AFiles;
import com.adamcalculator.dynamicpack.util.Out;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class DynamicPackModBase {
	public static final String CLIENT_FILE = "dynamicmcpack.json";
	public static final String MINECRAFT_META = "pack.mcmeta";

	public static DynamicPackModBase INSTANCE;

	private boolean isPacksScanning = false;
	private List<Pack> packs = new ArrayList<>();
	private File gameDir;
	private File resourcePacks;
	private boolean minecraftInitialized = false;


	public void init(File gameDir) {
		if (INSTANCE != null) {
			throw new RuntimeException("Already initialized!");
		}
		INSTANCE = this;
		this.gameDir = gameDir;
		this.resourcePacks = new File(gameDir, "resourcepacks");
		this.resourcePacks.mkdirs();
		Remote.initRemoteTypes();

		startSyncThread();
	}

	public abstract void startSyncThread();

	public void rescanPacks() {
		if (isPacksScanning) {
			Out.warn("rescanPacks already in scanning!");
			return;
		}
		isPacksScanning = true;
		packs.clear();

		for (File packFile : AFiles.lists(resourcePacks)) {
			try {
				PackUtil.openPackFileSystem(packFile, path -> {
					Path dynamicPackPath = path.resolve(CLIENT_FILE);
					if (Files.exists(dynamicPackPath)) {
						Out.println(" + Pack " + packFile.getName() + " supported by mod!");
                        try {
                            processPack(packFile, PackUtil.readJson(dynamicPackPath));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
						Out.println(" - Pack " + packFile.getName() + " not supported by mod.");
					}
                });
			} catch (Exception e) {
				Out.error("Error while processing pack: " + packFile, e);
			}
		}
		isPacksScanning = false;
	}



	private void processPack(File location, JSONObject json) {
		long formatVersion = json.getLong("formatVersion");
		if (formatVersion == 1) {
			Pack pack = new Pack(location, json);
			packs.add(pack);

		} else {
			throw new RuntimeException("Unsupported formatVersion: " + formatVersion);
		}
	}

	public boolean isResourcePackActive(Pack pack) throws IOException {
		for (String readLine : Files.readAllLines(new File(getGameDir(), "options.txt").toPath(), StandardCharsets.UTF_8)) {
			if (readLine.startsWith("resourcePacks:")) {
				String name = "file/" + pack.getLocation().getName();
				if (readLine.contains(name)) {
					return true;
				}
			}
		}
		return false;
	}

	public File getGameDir() {
		return gameDir;
	}

	public Pack[] getPacks() {
		return packs.toArray(new Pack[0]);
	}

	public void minecraftInitialized() {
		this.minecraftInitialized = true;
	}

	public boolean isMinecraftInitialized() {
		return minecraftInitialized;
	}

	public abstract String getCurrentGameVersion();
}