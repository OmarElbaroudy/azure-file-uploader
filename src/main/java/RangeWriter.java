import com.squareup.okhttp.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

public class RangeWriter implements Callable<Boolean> {
    private final File file;
    private final String url;
    private final int length;
    private final long offset;

    public RangeWriter(String url, File file, long offset, int length) {
        this.url = url;
        this.file = file;
        this.length = length;
        this.offset = offset;
    }

    @Override
    public Boolean call() throws Exception {
        try (InputStream stream = new FileInputStream(file)) {
            long skipped = stream.skip(offset);
            byte[] content = new byte[length];
            int read = stream.read(content);

            return read == length && skipped == offset &&
                    writeRange(url, offset, content);
        }
    }

    private String getRange(long offset, long length) {
        return "bytes=" + (offset) + "-" + (offset + length - 1);
    }

    private boolean writeRange(String url, long offset, byte[] content) throws IOException {
        OkHttpClient client = new OkHttpClient();
        MediaType TEXT = MediaType.parse("text/txt; charset=utf-8");

        RequestBody body = RequestBody.create(TEXT, content);
        long bodySize = body.contentLength();

        Request req = new Request.Builder().url(url)
                .put(body)
                .header("x-ms-type", "file")
                .header("x-ms-file-permission", "inherit")
                .header("x-ms-file-attributes", "none")
                .header("x-ms-file-last-write-time", "now")
                .header("Content-Length", String.valueOf(bodySize))
                .header("x-ms-write", "update")
                .header("x-ms-range", getRange(offset, bodySize))
                .build();

        System.out.println(getRange(offset, bodySize));
        Response res = client.newCall(req).execute();
        return res.isSuccessful();
    }
}
