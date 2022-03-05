package xyz.unifycraft.unicore.stage0;

import com.google.gson.*;
//#if FORGE == 1
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.ForgeVersion;
//#else
//$$ import net.fabricmc.loader.impl.launch.FabricLauncherBase;
//$$ import net.fabricmc.loader.impl.FabricLoaderImpl;
//#endif

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.ServiceLoader;

public class UniCoreSetup {
    private static boolean initialized = false;
    private static File dataDir;
    private static File versionDir;

    private static Gson gson;
    private static UniCoreData data;

    public static void initialize() {
        if (initialized) return;
        initialized = true;
        dataDir = new File(new File("UnifyCraft"), "UniCore");
        if (!dataDir.exists()) dataDir.mkdirs();
        versionDir = new File(dataDir, getGameVersion());
        if (!versionDir.exists()) versionDir.mkdirs();

        { // Download/retrieve and map the version schema.
            gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

            File file = new File(versionDir, "data.json");
            String content;
            try {
                content = fetchUrlContent(System.getProperty("unicore.stage0.data.url", "https://raw.githubusercontent.com/UnifyCraft/UniCore/main/data/v1/data.json"));
            } catch (Exception e) {
                if (file.exists()) content = fetchFileContent(file.toPath());
                else throw new IllegalStateException("Couldn't load data.");
            }

            JsonElement raw = parseJson(content);
            if (!raw.isJsonObject()) throw new IllegalStateException("The raw content of the data file was not as expected! Oh no! (type was: " + raw.getClass().getSimpleName() + ")");
            JsonObject json = raw.getAsJsonObject();
            if (!writeToFile(file, gson.toJson(json))) throw new IllegalStateException("Failed to write to data file.");
            data = gson.fromJson(json, UniCoreData.class);
        }

        { // Download the stage 1 loader and load it.
            String url = data.getLoaderUrl()
                    .replaceAll("\\{version}", data.getLoaderVersion())
                    .replaceAll("\\{gameversion}", getGameVersion())
                    .replaceAll("\\{platform}", getPlatform());
            File file = new File(versionDir, "UniCore-Loader-Stage1-" + getGameVersion() + "-" + getPlatform() + "-" + data.getLoaderVersion() + ".jar");
            if (!file.exists()) downloadFile(url, file.toPath());
            addToClasspath(file.toPath());
        }

        { // Initialize the stage 1 loader.
            ServiceLoader<UniCoreLoader> serviceLoader = ServiceLoader.load(UniCoreLoader.class);
            Iterator<UniCoreLoader> iterator = serviceLoader.iterator();
            UniCoreLoader loader;
            if (iterator.hasNext()) {
                loader = iterator.next();
                if (iterator.hasNext()) throw new IllegalStateException("There is more than one implementation of UniCoreLoader present.");
            } else throw new IllegalStateException("An implementation of UniCoreLoader isn't present.");
            loader.initialize();
        }
    }

    public static String getGameVersion() {
        //#if FORGE == 1
        return ForgeVersion.mcVersion;
        //#else
        //$$ return FabricLoaderImpl.INSTANCE.getGameProvider().getNormalizedGameVersion();
        //#endif
    }

    public static String getPlatform() {
        //#if FORGE == 1
        return "forge";
        //#else
        //$$ return "fabric";
        //#endif
    }

    public static String fetchUrlContent(String url) {
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

    public static boolean writeToFile(File file, String content) {
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

    public static String fetchFileContent(Path path) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path.toFile()));
            StringBuilder builder = new StringBuilder();
            String str;
            while ((str = bufferedReader.readLine()) != null) builder.append(str);
            return builder.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void downloadFile(String url, Path path) {
        try {
            File file = path.toFile();
            URL theUrl = new URL(url);
            InputStream input = theUrl.openStream();
            ReadableByteChannel channel = Channels.newChannel(input);
            FileOutputStream output = new FileOutputStream(file);
            output.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonElement parseJson(String json) {
        //#if MC<=11202
        return new JsonParser().parse(json);
        //#else
        //$$ JsonParser.parseString(json);
        //#endif
    }

    public static void addToClasspath(Path path) {
        try {
            //#if FORGE == 1
            Launch.classLoader.addURL(path.toUri().toURL());
            //#else
            //$$ FabricLauncherBase.getLauncher().addToClassPath(path);
            //#endif
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static File getDataDir() {
        return dataDir;
    }

    public static File getVersionDir() {
        return versionDir;
    }

    public static Gson getGson() {
        return gson;
    }

    public static UniCoreData getData() {
        return data;
    }
}