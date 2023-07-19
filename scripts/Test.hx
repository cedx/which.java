using StringTools;

/** Runs the test suite. **/
function main() {
	final pkg = "io.belin.which";
	Sys.command("lix Build --debug");
	Tools.setClassPath();
	Sys.command('javac -d bin -g -Xlint:all,-processing test/${pkg.replace(".", "/")}/*.java');

	final exitCode = Sys.command("java org.junit.platform.console.ConsoleLauncher --select-package=io.belin.which");
	if (exitCode != 0) Sys.exit(exitCode);
}
