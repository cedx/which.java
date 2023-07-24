import static io.belin.which.Finder.which;

/**
 * Finds the first instance of an executable.
 */
@SuppressWarnings({"PMD.NoPackage", "PMD.UseUtilityClass"})
class First {

	/**
	 * Application entry point.
	 * @param args The command line arguments.
	 */
	@SuppressWarnings("PMD.SystemPrintln")
	public static void main(String... args) {
		var path = which("foobar").first();
		if (path.isEmpty()) System.err.println("The 'foobar' command cannot be found.");
		else System.out.println("The 'foobar' command is located at: " + path.get());
	}
}
