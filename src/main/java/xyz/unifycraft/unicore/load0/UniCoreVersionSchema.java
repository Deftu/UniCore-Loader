package xyz.unifycraft.unicore.load0;

import com.google.gson.annotations.SerializedName;

public class UniCoreVersionSchema {
    @SerializedName("unicore_version") private String version;
    @SerializedName("unicore_loader_version") private String loaderVersion;

    @SerializedName("unicore_download_url") private String downloadUrl;
    @SerializedName("unicore_loader_download_url") private String loaderDownloadUrl;

    @SerializedName("unicore_mixins_file") private String mixinsFile;

    public String getVersion() {
        return version;
    }

    public String getLoaderVersion() {
        return loaderVersion;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getLoaderDownloadUrl() {
        return loaderDownloadUrl;
    }

    public String getMixinsFile() {
        return mixinsFile;
    }
}