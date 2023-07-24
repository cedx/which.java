import static io.belin.which.Finder.which;

/**
 * Finds all instances of an executable and returns them one at a time.
 */
@SuppressWarnings({"PMD.NoPackage", "PMD.UseUtilityClass"})
class Stream {

	/**
	 * Application entry point.
	 * @param args The command line arguments.
	 */
	@SuppressWarnings("PMD.SystemPrintln")
	public static void main(String... args) {
		System.out.println("The 'foobar' command is available at these locations:");
		which("foo").stream().forEach(path -> System.out.println("- " + path));
	}
}
