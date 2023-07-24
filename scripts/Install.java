import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Installs the project dependencies.
 */
@SuppressWarnings({"PMD.NoPackage", "PMD.UseUtilityClass"})
class Install {

	/**
	 * Script entry point.
	 * @param args The command line arguments.
	 */
	public static void main(String... args) throws InterruptedException, IOException {
		shellExec("ivy -cache lib -cachepath .classpath");
	}

	/**
	 * Executes the specified command in a shell.
	 * @param command The command to execute.
	 * @return The exit code of the executed command.
	 */
	private static int shellExec(String command) throws InterruptedException, IOException {
		var shell = System.getProperty("os.name").startsWith("Windows") ? List.of("cmd.exe", "/c") : List.of("/bin/sh", "-c");
		var cmdList = Stream.concat(shell.stream(), Stream.of(Objects.requireNonNull(command)));
		var process = Runtime.getRuntime().exec(cmdList.toArray(String[]::new));
		Stream.concat(process.errorReader().lines(), process.inputReader().lines()).parallel().forEach(System.out::println);
		return process.waitFor();
	}
}
