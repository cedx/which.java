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
		cleanDirectory(Path.of("bin"), Path.of("which.ps1"));
		cleanDirectory(Path.of("var"), Path.of(".gitkeep"));
	}

	/**
	 * Recursively deletes all files in the specified directory.
	 * @param directory The directory to clean.
	 * @param exclude A file name to exclude from the deletion.
	 */
	private static void cleanDirectory(Path directory, Path exclude) throws IOException {
		Files.walk(Objects.requireNonNull(directory))
			.skip(1)
			.sorted(Comparator.reverseOrder())
			.filter(file -> !file.getFileName().equals(Objects.requireNonNull(exclude)))
			.map(Path::toFile)
			.forEach(File::delete);
	}
}
