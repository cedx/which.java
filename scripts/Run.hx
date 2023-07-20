import sys.FileSystem;

/** Runs a Java main class. **/
function main() {
	final jar = "bin/io.belin.which.jar";
	if (!FileSystem.exists(jar)) Sys.command("lix Dist");
	Tools.setClassPath(jar);
	Sys.command("java", Sys.args());
}
