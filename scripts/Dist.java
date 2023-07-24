import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Packages the project.
 */
@SuppressWarnings({"PMD.NoPackage", "PMD.ShortClassName", "PMD.UseUtilityClass"})
class Dist {

	/**
	 * The base package of this library.
	 */
	static final String pack = "io.belin.which";

	/**
	 * Script entry point.
	 * @param args The command line arguments.
	 */
	public static void main(String... args) throws InterruptedException, IOException {
		for (var script: List.of("Clean", "Build", "Version")) exec("java scripts/%s.java".formatted(script));

		var directory = Files.createDirectories(Path.of("bin/META-INF"));
		var license = Path.of("LICENSE.md");
		Files.copy(license, directory.resolve(license));

		exec("jar --create --file=bin/%s.jar --manifest=etc/manifest.properties -C bin .".formatted(pack));
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
}
