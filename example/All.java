import static io.belin.which.Finder.which;

/**
 * Finds all instances of an executable.
 */
@SuppressWarnings({"PMD.NoPackage", "PMD.ShortClassName", "PMD.UseUtilityClass"})
class All {

	/**
	 * Application entry point.
	 * @param args The command line arguments.
	 */
	@SuppressWarnings("PMD.SystemPrintln")
	public static void main(String... args) {
		var paths = which("foobar").all();
		if (paths.isEmpty()) System.err.println("The 'foobar' command cannot be found.");
		else {
			System.out.println("The 'foobar' command is available at these locations:");
			for (var path: paths.get()) System.out.println("- " + path);
		}
	}
}
