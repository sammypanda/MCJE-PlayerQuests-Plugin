package playerquests.utility;

import java.io.File; // to manipulate files
import java.io.IOException; // thrown if a file cannot be created or written to
import java.nio.file.Files; // create/modify files
import java.nio.file.Path; // used to locate files on the filesystem
import java.nio.file.Paths; // for getting path type
import java.nio.file.StandardOpenOption;

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

    /**
     * Gets a file with the specific filename and content.
     * @param filename relative child path + filename
     * @return the string content of the file
     * @throws IOException when the file cannot be read
     */
    public static String get(String filename) throws IOException {
        Path fullPath = Paths.get(Core.getPlugin().getDataFolder() + "/" + filename);

        try {
            return Files.readString(fullPath);
        } catch (IOException e) {
            throw new IOException("Could not read the '" + filename + "' file.", e);
        }
    }

    /**
     * Deletes a file at the specified path.
     * @param filename relative child path + filename
     * @throws IOException when the file cannot be deleted
     */
    public static void delete(String filename) throws IOException {
        Path fullPath = Paths.get(Core.getPlugin().getDataFolder() + "/" + filename);

        try {
            Files.delete(fullPath);
        } catch (IOException e) {
            throw new IOException("Could not delete the '" + filename + "' file.", e);
        }
    }

    /**
     * Checks if a file at the specified path exists.
     * @param filename relative child path + filename
     * @throws IOException when the file cannot be read
     * @return if the file exists
     */
    public static boolean check(String filename) throws IOException {
        Path fullPath = Paths.get(Core.getPlugin().getDataFolder() + "/" + filename);

        return Files.exists(fullPath);
    }

    public static void append(String filename, byte[] content) throws IOException {
        Path fullPath = Paths.get(Core.getPlugin().getDataFolder() + "/" + filename);

        byte[] oldContent;
        try {
            // Read existing file content if it exists
            oldContent = Files.readAllBytes(fullPath);
        } catch (IOException e) {
            oldContent = new byte[0];
        }

        // Combine old and new content
        byte[] combinedContent = new byte[oldContent.length + content.length];
        System.arraycopy(oldContent, 0, combinedContent, 0, oldContent.length);
        System.arraycopy(content, 0, combinedContent, oldContent.length, content.length);

        try {
            // Write combined content to file
            Files.write(
                fullPath,
                combinedContent,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            throw new IOException("Could not write to the '" + filename + "' file.", e);
        }
    }
}
