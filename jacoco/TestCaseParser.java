import java.util.*;
import java.io.*;
import java.nio.file.*;

public class TestCaseParser {

	private static void runTest(String in, String out) throws Exception {
		ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
		System.setOut(new PrintStream(myOut));
		String[] args = null;

		InputStream input = System.in;
		FileInputStream fips = new FileInputStream(new File(in));
		System.setIn(fips);
		Exchange.main(args);
		System.setIn(input);

        String output = myOut.toString();
        System.setOut(oldOut);

        Scanner sc = new Scanner(new File(out));
        List<String> expected = new ArrayList<>();

        while (sc.hasNextLine()) {
            expected.add(sc.nextLine());
        }

        List<String> actual = new ArrayList<>();

        for (String line : output.split("\n")) {
            actual.add(line);
        }

        PrintWriter writer = new PrintWriter(new File(in.replace(".in", "") + ".diff"));

		int lineNum = 1;

        while (!actual.isEmpty() || !expected.isEmpty()) {
			String actualLine = actual.size() > 0 ? actual.remove(0) : "";
			String expectedLine = expected.size() > 0 ? expected.remove(0) : "";

			if (!actualLine.equals(expectedLine)) {
				writer.println("Line: " + lineNum);
				writer.println("E: " + expectedLine);
				writer.println("A: " + actualLine);
				writer.println();
			}

			lineNum++;
        }

        writer.flush();
        writer.close();
	}

    public static void main(String[] args) {

		try {
            File f = new File("tests");

			List<File> files = new ArrayList<>();

            Files.find(Paths.get("tests"), 
                       Integer.MAX_VALUE, 
                       (filePath, fileAttr) -> filePath.toString().contains(".in") || filePath.toString().contains(".out"))
                       .forEach(path -> files.add(path.toFile()));

			HashMap<String, String> fileMap = new HashMap<>();

			for (File file : files) {
				if (file.getCanonicalPath().contains(".in")) {
					String output = file.getCanonicalPath().replace(".in", ".out");
					for (File fileOut : files) {
						if (fileOut.getCanonicalPath().equals(output)) {
							fileMap.put(file.getCanonicalPath(), fileOut.getCanonicalPath());
						}
					}
				}
			}

			for (Map.Entry<String, String> entry : fileMap.entrySet()) {
				runTest(entry.getKey(), entry.getValue());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
