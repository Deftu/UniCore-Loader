package xyz.unifycraft.unicore.launchwrapper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.launchwrapper.Launch;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.deftu.fd.FileDownloaderFactory;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class UniCoreLoaderBase {
    protected static final String dataUrl = System.getProperty("unicore.loader.data.baseurl", "https://raw.githubusercontent.com/UnifyCraft/UniCore/main/data/v1");
    protected static final File dataDir = new File("UnifyCraft");
    protected static final File downloadsDir = new File(dataDir, "Downloads");

    protected final String stage;
    protected final Gson gson;
    protected final FileDownloaderFactory fileDownloaderFactory;
    //#if MC<=11202
    private final JsonParser jsonParser = new JsonParser();
    //#endif

    public UniCoreLoaderBase(@NotNull String stage) {
        this.stage = stage;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        this.fileDownloaderFactory = FileDownloaderFactory.create()
                .withUserAgent("Mozilla/5.0 (UniCore Loader)");
    }

    public abstract void initialize();
    public abstract void withProperties(File versionDir, String gameVersion, String gamePlatform);
    public abstract void withData(UniCoreData data, UniCoreData localData);

    /**
     * Converts a {@link String} to a Gson {@link JsonElement}.
     * This is done using the appropriate methods for all Minecraft versions.
     *
     * @param json The JSON string to parse.
     * @return The parsed element.
     */
    public final JsonElement parseJson(@NotNull String json) {
        //#if MC<=11202
        return jsonParser.parse(json);
        //#else
        //$$ JsonParser.parseString(json);
        //#endif
    }

    /**
     * Provides the md5 checksum of an input stream, this is usually used to compare two files.
     *
     * @param inputStream The input stream that needs to be checked.
     * @return The md5 checksum of the provided file.
     */
    public static String fetchChecksum(@NotNull InputStream inputStream) {
        try {
            return DigestUtils.md5Hex(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Provides the md5 checksum of a file, this is usually used to compare two files.
     *
     * @param path The path to the file that needs to be checked.
     * @return The md5 checksum of the provided file.
     */
    public static String fetchChecksum(@NotNull Path path) {
        try (InputStream inputStream = Files.newInputStream(path)) {
            return fetchChecksum(inputStream);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Adds an external file to the current classpath.
     *
     * @param classLoader The classloader to use for loading.
     * @param path The path of the file.
     */
    public static void addToClasspath(@Nullable URLClassLoader classLoader, @NotNull Path path) {
        try {
            //#if FORGE == 1
            URL url = path.toUri().toURL();
            Launch.classLoader.addURL(url);
            addUrlHack(url);
            //#else
            //$$ FabricLauncherBase.getLauncher().addToClassPath(path);
            //#endif
        } catch (Exception e) {
            throw new IllegalStateException("There was an error adding a path to the classpath.", e);
        }
    }

    //#if FORGE == 1
    private static void addUrlHack(URL url) {
        try {
            ClassLoader classLoader = Launch.classLoader.getClass().getClassLoader();
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(classLoader, url);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
    //#endif

    public static HttpURLConnection prepareConnection(@NotNull HttpURLConnection connection) {
        try {
            connection.setRequestMethod("GET");
            connection.addRequestProperty("User-Agent", "Mozilla/5.0 (UniCore Loader)");
            connection.setUseCaches(true);
            connection.setReadTimeout(30 * 1000);
            connection.setConnectTimeout(30 * 1000);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            return connection;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to prepare a connection.", e);
        }
    }

    public static HttpURLConnection createConnection(@NotNull URL url) {
        try {
            URLConnection connection = url.openConnection();
            if (connection instanceof HttpURLConnection) {
                return prepareConnection((HttpURLConnection) connection);
            } else throw new ProtocolException("The URL provided did not create a HttpURLConnection.");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to prepare a connection.", e);
        }
    }

    public static HttpURLConnection createConnection(@NotNull String url) {
        try {
            URLConnection connection = new URL(url).openConnection();
            if (connection instanceof HttpURLConnection) {
                return prepareConnection((HttpURLConnection) connection);
            } else throw new ProtocolException("The URL provided did not create a HttpURLConnection.");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to prepare a connection.", e);
        }
    }

    public final String getStage() {
        return stage;
    }

    public Gson getGson() {
        return gson;
    }

    static {
        if (!dataDir.exists() && !dataDir.mkdirs())
            throw new IllegalStateException("Failed to create UnifyCraft data directory.");
        if (!downloadsDir.exists() && !downloadsDir.mkdirs())
            throw new IllegalStateException("Failed to create UnifyCraft downloads directory.");
    }
}