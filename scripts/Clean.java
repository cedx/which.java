import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;

/**
 * Deletes all generated files.
 */
@SuppressWarnings({"PMD.NoPackage", "PMD.UseUtilityClass"})
class Clean {

	/**
	 * Script entry point.
	 * @param args The command line arguments.
	 */
	public static void main(String... args) throws IOException {
		if (Files.exists(Path.of("bin"))) removeDirectory(Path.of("bin"));
		cleanDirectory(Path.of("var"));
	}

	/**
	 * Recursively deletes all files in the specified directory.
	 * @param directory The directory to clean.
	 */
	private static void cleanDirectory(Path directory) throws IOException {
		Files.walk(Objects.requireNonNull(directory))
			.skip(1)
			.sorted(Comparator.reverseOrder())
			.filter(file -> !file.getFileName().equals(Path.of(".gitkeep")))
			.map(Path::toFile)
			.forEach(File::delete);
	}

	/**
	 * Recursively deletes the specified directory.
	 * @param directory The directory to delete.
	 */
	private static void removeDirectory(Path directory) throws IOException {
		Files.walk(Objects.requireNonNull(directory))
			.sorted(Comparator.reverseOrder())
			.map(Path::toFile)
			.forEach(File::delete);
	}
}
