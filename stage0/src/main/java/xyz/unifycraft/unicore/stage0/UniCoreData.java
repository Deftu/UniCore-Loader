package xyz.unifycraft.unicore.stage0;

import com.google.gson.annotations.SerializedName;

public class UniCoreData {
    @SerializedName("version") private String version;
    @SerializedName("loader_version") private String loaderVersion;

    @SerializedName("url") private String url;
    @SerializedName("loader_url") private String loaderUrl;

    @SerializedName("mixins") private String mixins;
    @SerializedName("initializer") private UniCoreInitializerData initializer;

    public String getVersion() {
        return version;
    }

    public String getLoaderVersion() {
        return loaderVersion;
    }

    public String getUrl() {
        return url;
    }

    public String getLoaderUrl() {
        return loaderUrl;
    }

    public String getMixins() {
        return mixins;
    }

    public UniCoreInitializerData getInitializer() {
        return initializer;
    }

    public static class UniCoreInitializerData {
        @SerializedName("class") private String clz;
        @SerializedName("method") private String method;

        public String getClassName() {
            return clz;
        }

        public String getMethod() {
            return method;
        }
    }
}