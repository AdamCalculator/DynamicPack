package com.adamcalculator.dynamicpack;

import com.adamcalculator.dynamicpack.pack.Pack;
import com.adamcalculator.dynamicpack.pack.Remote;
import com.adamcalculator.dynamicpack.util.AFiles;
import com.adamcalculator.dynamicpack.util.FailedOpenPackFileSystemException;
import com.adamcalculator.dynamicpack.util.Out;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class DynamicPackModBase {
	public static final String CLIENT_FILE = "dynamicmcpack.json";
	public static final String MINECRAFT_META = "pack.mcmeta";

	public static DynamicPackModBase INSTANCE;
	protected static int manuallySyncThreadCounter = 0;
	public boolean rescanPacksBlocked = false;

	private boolean isPacksScanning = false;
	private HashMap<String, Pack> packs = new HashMap<>();
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

	/**
	 * ONLY FOR FIRST INIT RUN! FOR MANUALLY USE startManuallySync!!!!!
	 */
	public abstract void startSyncThread();

	public abstract void startManuallySync();

	public void rescanPacks() {
		if (isPacksScanning) {
			Out.warn("rescanPacks already in scanning!");
			return;
		}
		if (rescanPacksBlocked) {
			Out.warn("rescanPacks blocked");
			return;
		}
		isPacksScanning = true;
		List<String> forDelete = new ArrayList<>(packs.keySet());
		for (File packFile : AFiles.lists(resourcePacks)) {
			try {
				PackUtil.openPackFileSystem(packFile, path -> {
					Path dynamicPackPath = path.resolve(CLIENT_FILE);
					if (Files.exists(dynamicPackPath)) {
						Out.println("+ Pack " + packFile.getName() + " supported by mod!");
                        try {
                            processPack(packFile, PackUtil.readJson(dynamicPackPath));
							forDelete.remove(packFile.getName());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
						Out.println("- Pack " + packFile.getName() + " not supported by mod.");
					}
                });
			} catch (Exception e) {
				if (e instanceof FailedOpenPackFileSystemException) {
					Out.warn("Error while processing pack " + packFile.getName() + ": " + e.getMessage());
				} else {
					Out.error("Error while processing pack: " + packFile.getName(), e);
				}
			}
		}
		for (String s : forDelete) {
			Out.println("Pack " + s + " no longer exists!");
			packs.remove(s);
		}
		isPacksScanning = false;
	}



	private void processPack(File location, JSONObject json) {
		long formatVersion = json.getLong("formatVersion");
		Pack oldestPack = packs.getOrDefault(location.getName(), null);
		if (formatVersion == 1) {
			Pack pack = new Pack(location, json);
			if (oldestPack != null) {
				pack.saveReScanData(oldestPack);
			}
			packs.put(location.getName(), pack);

		} else {
			throw new RuntimeException("Unsupported formatVersion: " + formatVersion);
		}
	}

	/**
	 * API FOR MODPACKERS etc all-in-one packs
	 * @param host host to add.
	 * @param requester any object. It is recommended that .toString explicitly give out your name.
	 */
	public static void addAllowedHosts(String host, Object requester) throws Exception {
		Mod.addAllowedHosts(host, requester);
	}

	public boolean isNameIsDynamic(String name) {
		return getDynamicPackByMinecraftName(name) != null;
	}

	public Pack getDynamicPackByMinecraftName(String name) {
		for (Pack pack : getPacks()) {
			if (("file/" + pack.getName()).equals(name)) {
				return pack;
			}
		}
		return null;
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
		return packs.values().toArray(new Pack[0]);
	}

	public void minecraftInitialized() {
		this.minecraftInitialized = true;
	}

	public boolean isMinecraftInitialized() {
		return minecraftInitialized;
	}

	public abstract String getCurrentGameVersion();

	public abstract boolean checkResourcePackMetaValid(String s) throws Exception;
}