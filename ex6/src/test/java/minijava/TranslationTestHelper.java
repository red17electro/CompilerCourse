package minijava;

import main.MiniJavaCompiler;
import minijava.ast.MJProgram;
import minillvm.analysis.Checks;
import minillvm.ast.Prog;
import minillvm.tomips.MiniLLVMToMips;
import mips.ast.MipsProg;
import org.junit.Assert;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TranslationTestHelper {

	public static final String LLVM_LLI_EXE;
	public static final String LLVM_OPT_EXE;


	private final static int mipsTimeOut = 10000;
	private static File testOutputFolder = new File("testoutput");
	static {
		Map<String, String> env = System.getenv();
		if (env.containsKey("LLVM_COMPILER_PATH")) {
			String LLVM_BASE = env.get("LLVM_COMPILER_PATH");
			LLVM_LLI_EXE = Paths.get(LLVM_BASE, "lli").toString();
			LLVM_OPT_EXE = Paths.get(LLVM_BASE, "opt").toString();
		} else {
			LLVM_LLI_EXE = "lli";
			LLVM_OPT_EXE = "opt";
		}
		testOutputFolder.mkdirs();
	}

	/**
	 * Normalizes line endings to Unix style
	 * @param input the input with mixed line endings
	 * @return the input with only Unix newline line endings
	 */
	public static String normalize(String input) {
		return input.replaceAll("\r\n", "\n");
	}


	public static void testLLVMTranslation(String inputName, String miniJavaProgram) throws Exception {
		MiniJavaCompiler compiler = new MiniJavaCompiler();
		compiler.compileString(inputName, miniJavaProgram);
		assertEquals(Collections.emptyList(), compiler.getSyntaxErrors());
		assertEquals(Collections.emptyList(), compiler.getTypeErrors());
		MJProgram program = compiler.getJavaProgram();
		Prog llvmProg = compiler.getLlvmProg();

		String llvmOut = llvmProg.toString();

		File llvmOutFile = new File(testOutputFolder, inputName.replace(".java", "") + ".ll");
		Files.write(llvmOutFile.toPath(), llvmOut.getBytes(StandardCharsets.UTF_8));

		// check llvm prog
		new Checks().checkProgram(llvmProg);

		// get some input for testing the program
		String programInput = randomInput();

		// run java program and record the output
		String javaOutput = "";
		Throwable runtimeErrorInJava = null;
		try {
			javaOutput = runJavaProgAndGetOut(inputName, miniJavaProgram, getMainClassName(program), programInput);
		} catch (InvocationTargetException ex) {
			runtimeErrorInJava = ex.getCause();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		// verify the llvm program with
		verifyLlvm(llvmProg);
		// run the llvm program and compare its output with the output of Java
		try {
			String llvmOutput = runLlvmCodeAndGetoutput(llvmProg, programInput);
			Assert.assertEquals(javaOutput, llvmOutput);
			if (runtimeErrorInJava != null) {
				Assert.fail("Java program stopped with runtime error '" + runtimeErrorInJava + "', but LLVM stopped normally.");
			}
		} catch (ProgramExecutionException e) {
			if (e.exitCode == 222) { // special error code returned by LLVM-error instruction
				Assert.assertNotNull("Llvm interpreter gave error, but there should be no error.\nOutput: \n" + e.getOutput(), runtimeErrorInJava);
			} else {
				throw e;
			}
		}

	}

	public static void testMIPSTranslation(String inputName, String miniJavaProgram) throws Exception {
		MiniJavaCompiler compiler = new MiniJavaCompiler();
		compiler.compileString(inputName, miniJavaProgram);
		assertEquals(Collections.emptyList(), compiler.getSyntaxErrors());
		assertEquals(Collections.emptyList(), compiler.getTypeErrors());
		MJProgram program = compiler.getJavaProgram();
		Prog llvmProg = compiler.getLlvmProg();

		String llvmOut = llvmProg.toString();

		File llvmOutFile = new File(testOutputFolder, inputName.replace(".java", "") + ".ll");
		Files.write(llvmOutFile.toPath(), llvmOut.getBytes(StandardCharsets.UTF_8));

		// check llvm prog
		new Checks().checkProgram(llvmProg);

		// get some input for testing the program
		String programInput = randomInput();

		// run java program and record the output
		String javaOutput = "";
		Throwable runtimeErrorInJava = null;
		try {
			javaOutput = runJavaProgAndGetOut(inputName, miniJavaProgram, getMainClassName(program), programInput);
		} catch (InvocationTargetException ex) {
			runtimeErrorInJava = ex.getCause();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		// verify the llvm program with
		verifyLlvm(llvmProg);

		// to mips
		MipsProg mipsprog = MiniLLVMToMips.translateProgram(llvmProg);
		System.out.println(mipsprog);
		try{
			String mipsoutput = runMipsCodeAndGetoutput(mipsprog);
			Assert.assertEquals(javaOutput, mipsoutput);
		} catch(ProgramExecutionException e){
			if (e.exitCode == 222) {
				Assert.assertNotNull("Mips interpreter gave error, but there should be no error. The error was: " + e, runtimeErrorInJava);
			} else {
				throw e;
			}
		}

	}

	public static void testMIPSTranslation(Prog llvmProg) throws Exception {
		boolean llvmError = false;

		// get some input for testing the program
		String programInput = randomInput();

		// verify the llvm program with
		verifyLlvm(llvmProg);
		// run the llvm program and compare its output with the output of Java
		String llvmOutput = null;
		try {
			llvmOutput = runLlvmCodeAndGetoutput(llvmProg, programInput);
		} catch (ProgramExecutionException e) {
			if (e.exitCode == 222) { // special error code returned by LLVM-error instruction
				llvmError = true;
			} else {
				throw e;
			}
		}

		// to mips
		MipsProg mipsprog = MiniLLVMToMips.translateProgram(llvmProg);
		System.out.println(mipsprog);
		try{
			String mipsoutput = runMipsCodeAndGetoutput(mipsprog);
			if (llvmError) {
				Assert.fail("LLVM program stopped with runtime error, but MIPS stopped normally");
			}
			Assert.assertEquals(llvmOutput, mipsoutput);
		} catch(ProgramExecutionException e){
			if (e.exitCode == 222) {
				Assert.assertTrue("Mips interpreter gave error, but there should be no error. The error was: " + e, llvmError);
			} else {
				throw e;
			}
		}
	}

	private static String runMipsCodeAndGetoutput(MipsProg mipsprog) throws IOException, InterruptedException, ProgramExecutionException{
		byte[] code = mipsprog.toString().getBytes();
		File mipsSource = File.createTempFile("mips", ".asm");
		Files.write(mipsSource.toPath(), code);
		String sourceFileName = mipsSource.getAbsolutePath();
		ProcessBuilder pb = new ProcessBuilder("java", "-Djava.awt.headless=true", "-jar", getMipsJar(), "nc" ,sourceFileName);
		Process mars = pb.start();
		long spimStartTime = System.currentTimeMillis();
		int res;

		while (true) {
			try {
				res = mars.exitValue();
				break;
			} catch (IllegalThreadStateException e) {
				Assert.assertTrue(
						"Timeout when calling SPIM",
						(System.currentTimeMillis() - spimStartTime) < mipsTimeOut);
			}
			Thread.sleep(50);
			Thread.yield();
		}
		byte[] output = bytesFromInput(mars.getInputStream());
		byte[] errs = bytesFromInput(mars.getErrorStream());
		if(res != 0){
			throw new ProgramExecutionException(mars.exitValue(), output, errs, "could not complete tool " + pb.command()
					+ ", exit code: " + res);
		}
		String outstr = normalize(new String(output));
		String mipsOutput = outstr.substring(0, outstr.length()-1);//remove last \n generated from mars
		System.out.println("output of mips");
		System.out.println(mipsOutput);
		return mipsOutput;
	}

	private static String getMipsJar() {
		File marsJarFile = new File("tools/Mars4_5.jar");
		if (marsJarFile.exists()) {
			return marsJarFile.getAbsolutePath();
		}
		throw new RuntimeException("Could not find the Mars jar file.");
	}

	private static String randomInput() {
		return "*random";
	}

	private static String getMainClassName(MJProgram program) {
		return program.getMainClass().getName();
	}



	private static String runJavaProgAndGetOut(String inputName, String miniJavaProgram, String mainClassName, String input) throws Exception {
		File tempDir = new File("temp/");
		File tempFile = new File(tempDir, inputName);
		tempDir.mkdirs();
		try {
			File fakeSystem = createFakeSystemFile(tempDir);
			File file = createJavaFile(tempDir, inputName, miniJavaProgram);
			Files.copy(file.toPath(), tempFile.toPath());

			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			compiler.run(null, null, null, tempFile.getAbsolutePath(), fakeSystem.getAbsolutePath());

			URLClassLoader classLoader = URLClassLoader
					.newInstance(new URL[] { tempDir.toURI().toURL() });
			Class<?> cls = Class.forName(mainClassName, true, classLoader);
			String[] args = new String[0];
			Method main = cls.getMethod("main", args.getClass());
			if (!main.isAccessible()) {
				main.setAccessible(true);
			}

			PrintStream originalOut = System.out;
			InputStream originalIn = System.in;
			try {
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				PrintStream ps = new PrintStream(os);
				System.setOut(ps);
				System.setIn(new ByteArrayInputStream(input.getBytes()));

				main.invoke(null, new Object[] { args });
				return normalize(os.toString());
			} finally {
				System.setOut(originalOut);
				System.setIn(originalIn);
			}
		} finally {
			tempFile.delete();
		}
	}

	private static File createJavaFile(File tempDir, String inputName, String miniJavaProgram) throws IOException {
		File f = new File(tempDir, inputName);
		if (f.exists()) {
			f.delete();
		}
		Files.write(f.toPath(), miniJavaProgram.getBytes(StandardCharsets.UTF_8));
		return f;
	}

	private static File createFakeSystemFile(File tempDir) throws FileNotFoundException {
		File f = new File(tempDir, "System.java");
		if (f.exists()) {
			f.delete();
		}

		try (PrintWriter writer = new PrintWriter(f)) {
			writer.println("public class System {");
			writer.println("	public static Out out = new Out();");
			writer.println("	public static In in = new In();");
			writer.println("	public static class Out {");
			writer.println("		public void println(int i) {");
			writer.println("			java.lang.System.out.println(i);");
			writer.println("		}");
			writer.println("	}");
			writer.println("	public static class In {");
			writer.println("		public int read() {");
			writer.println("			try {");
			writer.println("				return java.lang.System.in.read();");
			writer.println("			} catch (java.io.IOException e) {");
			writer.println("				return -1;");
			writer.println("			}");
			writer.println("		}");
			writer.println("	}");
			writer.println("}");
		}
		return f;
	}

	private static void verifyLlvm(Prog llp) throws IOException, InterruptedException {
		byte[] code = llp.toString().getBytes();
		try {
			runProgram(new ProcessBuilder(LLVM_OPT_EXE, "-analyze", "-verify"), code);
		} catch (ProgramExecutionException e) {
			throw new RuntimeException("The LLVM verifier found a problem : \n"  + new String(e.errs));
		}
	}

	public static String runLlvmCodeAndGetoutput(Prog llp, String programInput) throws Exception {
		byte[] code = llp.toString().getBytes();
		File tempFile = File.createTempFile("llvm", "ll");
		Files.write(tempFile.toPath(), code);
		byte[] output = runProgram(new ProcessBuilder(LLVM_LLI_EXE, tempFile.getAbsolutePath()), programInput.getBytes());
		return normalize(new String(output));
	}

	private static byte[] runProgram(ProcessBuilder pb, byte[] stdin)
			throws IOException, InterruptedException, ProgramExecutionException {
		Process p = pb.start();
		p.getOutputStream().write(stdin);
		p.getOutputStream().close();
		int res = p.waitFor();
		byte[] output = bytesFromInput(p.getInputStream());
		byte[] errs = bytesFromInput(p.getErrorStream());
//		System.out.println("output of " + pb.command());
//		System.out.println(new String(output));
//		System.out.println("output errors of " + pb.command());
//		System.out.println(new String(errs));
		if (res != 0) {
			throw new ProgramExecutionException(res, output, errs, "could not complete tool " + pb.command()
					+ ", exit code: " + res);
		}
		return output;
	}

	static class ProgramExecutionException extends Exception {
		private int exitCode;
		private byte[] output;
		private byte[] errs;

		public ProgramExecutionException(int exitCode, byte[] output, byte[] errs, String msg) {
			super(msg);
			this.exitCode = exitCode;
			this.output = output;
			this.errs = errs;
		}

		@Override
		public String toString() {
			String msg = getMessage();
			msg += "\nExit code: " + exitCode;
			if (errs.length > 0) {
				msg += "\nErrors: " + new String(errs);
			} else if (output.length > 0) {
				msg += "\nOutput: " + new String(output);
			}
			return msg;
		}

		public String getOutput() {
			return new String(output, StandardCharsets.UTF_8);
		}
	}

	private static byte[] bytesFromInput(InputStream is) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int nRead;
		byte[] data = new byte[16384];

		while ((nRead = is.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}

		buffer.flush();

		return buffer.toByteArray();
	}
}
