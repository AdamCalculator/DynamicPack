package com.adamcalculator.dynamicpack;

import com.adamcalculator.dynamicpack.pack.Pack;
import com.adamcalculator.dynamicpack.util.AFiles;
import com.adamcalculator.dynamicpack.util.Out;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public abstract class DynamicPackModBase {
	public static final String CLIENT_FILE = "dynamicmcpack.json";

	public static DynamicPackModBase INSTANCE;

	public static List<Pack> packs = new ArrayList<>();
	private File gameDir;
	private File resourcePacks;


	public void init(File gameDir) {
		if (INSTANCE != null) {
			throw new RuntimeException("Already initialized!");
		}
		INSTANCE = this;
		this.gameDir = gameDir;
		resourcePacks = new File(gameDir, "resourcepacks");
		resourcePacks.mkdirs();

		startSyncThread();
	}

	public abstract void startSyncThread();

	public void rescanPacks() {
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



	private void processPack(File location, JSONObject json) {
		Out.println("pack " + location + ": " + json);

		long formatVersion = json.getLong("formatVersion");
		if (formatVersion == 1) {

			Pack pack = new Pack(location, json);
			packs.add(pack);

		} else {
			throw new RuntimeException("Unsupported formatVersion!");
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
}