import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Objects;

public class Main {
    public static void main(String[] args) throws Exception {
        Map<String, String> environmentVariables = System.getenv();
        String token = environmentVariables.get("AZURE_TOKEN");
        String username = environmentVariables.get("USERNAME");
        String blob = environmentVariables.get("BLOB");

        String filename = "sample_file.txt";
        User omar = new User(username, token);

        URL url = Main.class.getResource(filename);
        File file = new File(Objects.requireNonNull(url).getPath());
        FileUploader uploader = new FileUploader(file, omar);

        Boolean success = uploader.
                uploadTo(blob, filename, FileType.TEXT);

        System.out.println(success);
    }
}
