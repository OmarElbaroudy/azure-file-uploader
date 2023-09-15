import com.squareup.okhttp.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FileUploader {
    private final int THREAD_POOL = 3;
    private final int CHUNK_SIZE = 100_000;

    private final File file;
    private final User user;
    private final long fileSize;

    public FileUploader(@NotNull File file, User user) {
        this.user = user;
        this.file = file;
        this.fileSize = file.length();

        System.out.println("fileSize " + fileSize);
    }

    public boolean uploadTo(String fileShare, String fileName, FileType type) throws Exception {
        try {
            return upload(getCreateFileURL(fileShare, fileName, type))
                    && write(getWriteToFileURL(fileShare, fileName, type));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getCreateFileURL(String fileShare, String fileName, FileType type) {
        return "https://" + user.accountName() + ".file.core.windows.net/"
                + fileShare + "/" + fileName + type + user.token();
    }

    public String getWriteToFileURL(String fileShare, String fileName, FileType type) {
        return "https://" + user.accountName() + ".file.core.windows.net/"
                + fileShare + "/" + fileName + type + "?comp=range&" +
                user.token().substring(1);
    }

    private boolean upload(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        MediaType TEXT = MediaType.parse("text/txt; charset=utf-8");
        RequestBody body = RequestBody.create(TEXT, "");

        Request req = new Request.Builder().url(url)
                .put(body)
                .header("x-ms-type", "file")
                .header("x-ms-content-length", String.valueOf(fileSize))
                .header("x-ms-file-permission", "inherit")
                .header("x-ms-file-attributes", "none")
                .header("x-ms-file-creation-time", "now")
                .header("x-ms-file-last-write-time", "now")
                .build();

        Response res = client.newCall(req).execute();
        return res.isSuccessful();
    }

    public boolean write(String url) throws Exception {
        //get the number of file segments given the chunk size and file size
        int cnt = (int) ((fileSize + CHUNK_SIZE - 1) / CHUNK_SIZE);

        //RangeWriter is Callable<Boolean> that will
        //read a chunk from the file and upload it to ADLS
        ArrayList<RangeWriter> arr = new ArrayList<>();

        for (long i = 0; i < cnt; i++) {
            int length = i != cnt - 1 ? CHUNK_SIZE :
                    ((int) (fileSize - i * CHUNK_SIZE));

            arr.add(new RangeWriter(url, file, i * CHUNK_SIZE, length));
        }

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL);
        List<Future<Boolean>> ret = executor.invokeAll(arr);
        executor.shutdown();

        //check if all threads executed successfully
        for (var f : ret){
            if (!f.get()) return false;
        }

        return true;
    }

}
