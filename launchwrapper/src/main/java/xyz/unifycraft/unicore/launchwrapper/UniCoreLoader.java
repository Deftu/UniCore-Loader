package xyz.unifycraft.unicore.launchwrapper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.launchwrapper.Launch;
import xyz.deftu.fd.FileDownloader;

import java.io.*;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.ServiceLoader;

public class UniCoreLoader extends UniCoreLoaderBase {
    private final String gameVersion;
    private final String gamePlatform;

    private File versionDir;

    private UniCoreData localData;
    private UniCoreData data;

    public UniCoreLoader(String gameVersion, String gamePlatform) {
        super("launchwrapper");
        this.gameVersion = gameVersion;
        this.gamePlatform = gamePlatform;
    }

    public void initialize() {
        // Set the version directory.
        versionDir = new File(new File(dataDir, "UniCore"), gameVersion);
        if (!versionDir.exists() && !versionDir.mkdirs())
            throw new UniCoreLoadException("We couldn't create the UniCore version directory!");

        initializeData();
        initializeLoader();
    }

    private void initializeData() {
        File dataFile = new File(versionDir, "data.json");
        localData = readData(dataFile);

        // Download the file, or continue if we can't.
        try {
            FileDownloader downloader = FileDownloader.create(downloadsDir)
                    .withCaches(false);
            downloader.download(System.getProperty("unicore.data.url", "https://raw.githubusercontent.com/UnifyCraft/UniCore/main/data/v1/data.json"));
            downloader.validate();
            downloader.complete(dataFile);
        } catch (Exception ignored) {
            if (!dataFile.exists()) {
                throw new UniCoreLoadException("We couldn't download the UniCore data file, and it doesn't already exist!");
            }
        }

        // Read the data we just downloaded.
        data = readData(dataFile);
    }

    private UniCoreData readData(File file) {
        JsonElement raw = parseJson(fetchFileContent(file.toPath()));
        if (!raw.isJsonObject()) return null;
        JsonObject object = raw.getAsJsonObject();
        return gson.fromJson(object, UniCoreData.class);
    }

    private void initializeLoader() {
        File loaderFile = new File(versionDir, String.format("unicore-loader-%s-%s-%s.jar", gameVersion, gamePlatform, data.getLoaderVersion()));

        try {
            FileDownloader downloader = FileDownloader.create(downloadsDir)
                    .withCaches(false);
            downloader.download(System.getProperty("unicore.loader.url", data.getLoaderUrl())
                    .replaceAll("\\{gameversion\\}", gameVersion)
                    .replaceAll("\\{platform\\}", gamePlatform)
                    .replaceAll("\\{version\\}", data.getVersion()));
            downloader.validate();
            downloader.complete(loaderFile);
        } catch (Exception ignored) {
            if (!loaderFile.exists()) {
                throw new UniCoreLoadException("We couldn't download the UniCore loader, and it doesn't already exist!");
            }
        }

        addToClasspath(Launch.classLoader, loaderFile.toPath());
        //#if FORGE==1
        //Launch.classLoader.addClassLoaderExclusion("xyz.unifycraft.unicore.");
        //#endif

        ServiceLoader<UniCoreLoaderBase> service = ServiceLoader.load(UniCoreLoaderBase.class);
        Iterator<UniCoreLoaderBase> iterator = service.iterator();
        if (!iterator.hasNext())
            throw new UniCoreLoadException("No UniCore loader found!");
        UniCoreLoaderBase loader = iterator.next();
        loader.withProperties(versionDir, gameVersion, gamePlatform);
        loader.withData(data, localData);
        loader.initialize();
    }

    public void withProperties(File versionDir, String gameVersion, String gamePlatform) {
        throw new UnsupportedOperationException();
    }

    public void withData(UniCoreData data, UniCoreData localData) {
        throw new UnsupportedOperationException();
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public String getGamePlatform() {
        return gamePlatform;
    }

    public String fetchFileContent(Path path) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path.toFile()));
            StringBuilder builder = new StringBuilder();
            String str;
            while ((str = bufferedReader.readLine()) != null) builder.append(str);
            return builder.toString();
        } catch (Exception e) {
            return null;
        }
    }
}