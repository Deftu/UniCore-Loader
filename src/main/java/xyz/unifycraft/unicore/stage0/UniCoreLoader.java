package xyz.unifycraft.unicore.stage0;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import xyz.deftu.fd.FileDownloader;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Objects;
import java.util.ServiceLoader;

public class UniCoreLoader extends UniCoreLoaderBase {
    private final String gameVersion;
    private final String gamePlatform;
    private final ITweaker launchwrapperTweaker;

    private File versionDir;

    private UniCoreData localData;
    private UniCoreData data;

    public UniCoreLoader(String gameVersion, String gamePlatform, ITweaker launchwrapperTweaker) {
        super("stage0");
        this.gameVersion = gameVersion;
        this.gamePlatform = gamePlatform;
        this.launchwrapperTweaker = launchwrapperTweaker;
    }

    public void initialize() {
        versionDir = new File(new File(dataDir, "UniCore"), gameVersion);
        if (!versionDir.exists()) versionDir.mkdirs();

        // Download/retrieve and map the UniCore data.
        File dataFile = new File(versionDir, "data.json");
        String fileContent = fetchFileContent(dataFile.toPath());
        String content;
        try {
            content = fetchUrlContent(System.getProperty("unicore.stage0.data.url", "https://raw.githubusercontent.com/UnifyCraft/UniCore/main/data/v1/data.json"));
            if (fileContent == null) fileContent = content;
        } catch (Exception e) {
            if (dataFile.exists()) content = fileContent;
            else throw new IllegalStateException("Couldn't load data.");
        }
        JsonElement rawData = parseJson(content);
        if (!rawData.isJsonObject())
            throw new IllegalStateException("The raw content of the data file was not as expected! Oh no! (type was: " + rawData.getClass().getSimpleName() + ")");
        JsonObject json = rawData.getAsJsonObject();
        if (fileContent != null) {
            JsonElement rawLocalData = parseJson(fileContent);
            if (!rawLocalData.isJsonObject())
                throw new IllegalStateException("The raw content of the data file was not as expected! Oh no! (type was: " + rawData.getClass().getSimpleName() + ")");
            localData = gson.fromJson(rawLocalData.getAsJsonObject(), UniCoreData.class);
        }
        if (!writeToFile(dataFile, gson.toJson(json)))
            throw new IllegalStateException("Failed to write to data file.");
        data = gson.fromJson(json, UniCoreData.class);

        // Download the stage 1 loader and load it.
        String url = data.getLoaderUrl().replaceAll("\\{version}", data.getLoaderVersion()).replaceAll("\\{gameversion}", gameVersion).replaceAll("\\{platform}", gamePlatform);
        File stage1File = new File(versionDir, "UniCore-Loader-Stage1-" + gameVersion + "-" + gamePlatform + "-" + data.getLoaderVersion() + ".jar");
        File localStage1File = new File(versionDir, "UniCore-Loader-Stage1-" + gameVersion + "-" + gamePlatform + "-" + localData.getLoaderVersion() + ".jar");
        if (!Objects.equals(localData.getLoaderVersion(), data.getLoaderVersion()) || !localStage1File.exists()) {
            FileDownloader fileDownloader = fileDownloaderFactory.create(new File(dataDir, "Downloads"), localStage1File);
            fileDownloader.download(url);
            fileDownloader.validate();
            fileDownloader.complete(stage1File);
        }
        addToClasspath(Launch.classLoader, stage1File.toPath());
        //#if FORGE == 1
        Launch.classLoader.addClassLoaderExclusion("xyz.unifycraft.unicore.stage1.");
        //#endif

        // Load and initialize the next stage.
        UniCoreLoaderBase stage1;
        ServiceLoader<UniCoreLoaderBase> serviceLoader = ServiceLoader.load(UniCoreLoaderBase.class);
        Iterator<UniCoreLoaderBase> serviceIterator = serviceLoader.iterator();
        if (serviceIterator.hasNext()) {
            stage1 = serviceIterator.next();
            if (serviceIterator.hasNext()) throw new IllegalStateException("There is more than one stage 1 implementation of the UniCore loader, this is not supported.");
        } else throw new IllegalStateException("There is no stage 1 implementation of the UniCore loader.");
        stage1.withProperties(dataDir, versionDir, gameVersion, gamePlatform);
        stage1.withData(data, localData);
        stage1.initialize();
    }

    protected void withProperties(File dataDir, File versionDir, String gameVersion, String gamePlatform) {
        throw new UnsupportedOperationException();
    }

    protected void withData(UniCoreData data, UniCoreData localData) {
        throw new UnsupportedOperationException();
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public String getGamePlatform() {
        return gamePlatform;
    }

    public String fetchUrlContent(String url) {
        try {
            URL theUrl = new URL(url);
            InputStream input = theUrl.openStream();
            InputStreamReader reader = new InputStreamReader(input);
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuilder builder = new StringBuilder();
            String str;
            while ((str = bufferedReader.readLine()) != null) builder.append(str);
            return builder.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean writeToFile(File file, String content) {
        try {
            if (!file.exists()) file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(content);
            writer.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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