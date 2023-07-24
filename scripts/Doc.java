import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Builds the documentation.
 */
@SuppressWarnings({"PMD.NoPackage", "PMD.ShortClassName", "PMD.UseUtilityClass"})
class Doc {

	/**
	 * The base package of this library.
	 */
	static final String pack = "io.belin.which";

	/**
	 * Script entry point.
	 * @param args The command line arguments.
	 */
	public static void main(String... args) throws InterruptedException, IOException {
		for (var file: List.of("CHANGELOG.md", "LICENSE.md"))
			Files.copy(Path.of(file), Path.of("docs").resolve(file.toLowerCase(Locale.getDefault())), StandardCopyOption.REPLACE_EXISTING);

		if (Files.exists(Path.of("docs/api"))) removeDirectory(Path.of("docs/api"));
		exec("javadoc -d docs/api --source-path=src " + pack);
		Files.copy(Path.of("docs/favicon.ico"), Path.of("docs/api/favicon.ico"));
	}

	/**
	 * Executes the specified command.
	 * @param command The command to execute.
	 * @return The exit code of the executed command.
	 */
	private static int exec(String command) throws InterruptedException, IOException {
		var process = Runtime.getRuntime().exec(Objects.requireNonNull(command));
		Stream.concat(process.errorReader().lines(), process.inputReader().lines()).parallel().forEach(System.out::println);
		return process.waitFor();
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
