package xyz.unifycraft.unicore.stage0;

import java.io.*;

public class CopyInputStream extends InputStream {
    private final InputStream stream;
    private final InputStream copy;
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    public CopyInputStream(InputStream stream) {
        this.stream = stream;
        copy();
        copy = new ByteArrayInputStream(output.toByteArray());
    }

    private void copy() {
        try {
            int read;
            byte[] buffer = new byte[1024];
            while ((read = stream.read(buffer)) > 0) output.write(buffer, 0, read);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int read() throws IOException {
        return copy.read();
    }

    public CopyInputStream createCopy() {
        return new CopyInputStream(new ByteArrayInputStream(output.toByteArray()));
    }
}