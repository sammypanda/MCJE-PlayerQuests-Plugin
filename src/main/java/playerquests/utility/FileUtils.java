package playerquests.utility;

import java.io.File; // to manipulate files
import java.io.IOException; // thrown if a file cannot be created or written to
import java.nio.file.Files; // create/modify files

import playerquests.Core; // to retrieve Singletons

/**
 * Tools to do file operations
 */
public class FileUtils {

    /**
     * Should be accessed statically.
     */
    private FileUtils() {}

    /**
     * Creates a new file with the specific filename and content.
     * @param filename string child path + filename
     * @param content bytes for the file content
     * @throws IOException when the file cannot be created
     */
    public static void create(String filename, byte[] content) throws IOException {
        File file = new File(Core.getPlugin().getDataFolder(), filename);

        try {
            Files.write(file.toPath(), content);
        } catch (IOException e) {
            throw new IOException("Could not create the '" + filename + "' file.", e);
        }
    }
}
