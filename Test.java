import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Test {
    public static void main(String[] args) throws IOException {
        Path path = Paths.get(URI.create("gs://hammerlab-spark/conf/full"));
        System.out.println(Files.readAllLines(path, StandardCharsets.UTF_8).size());
    }
}
