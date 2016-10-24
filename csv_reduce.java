import org.apache.commons.csv.*;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.*;

public class csv_reduce {
	public static boolean debug = true;
    public static void main(String[] args) throws IOException {
		// Set up parameters
		String usage = "Usage: java csv_reduce columnsFile FileName";
		if(args.length != 2) {
			System.out.println(usage);
			System.exit(-1);
		}
		
		List<String> columns = null;
		String columnsFileName = args[0];
		try {
			columns = Files.readAllLines(Paths.get(columnsFileName), StandardCharsets.US_ASCII);
		}
		catch (IOException ex) {
			System.out.println(usage);
			System.out.println("File " + columnsFileName + " could not be read.");
			System.exit(-1);
		}
		columns.remove("");
		if(columns.size()<=0) {
			System.out.println(usage);
			System.out.println("File " + columnsFileName + " has no columns.");
			System.exit(-1);
		}
		Set<String> newHeaderSet = new LinkedHashSet<String>(columns);
		
		String fileName = args[1];
		if(!fileName.endsWith(".csv")) {
			System.out.println(usage);
			System.out.println("FileName must end with \".csv\".");
			System.exit(-1);
		}

		Charset cs = StandardCharsets.ISO_8859_1; // Want one character to be one byte.
		File csvFile = new File(fileName);
		CSVParser parser = null;
		try {
			parser = CSVParser.parse(csvFile, cs, CSVFormat.RFC4180.withHeader());
		}
		catch (IOException ex) {
			System.out.println(usage);
			System.out.println("File " + fileName + " could not be read.");
			System.exit(-1);
		}

		// Get header line of orignal file
		Set<String> headerSet = parser.getHeaderMap().keySet();
		// Check that it has everything we need
		for(String col : newHeaderSet) {
			if(!headerSet.contains(col)) {
				System.out.println("Column " + col +
					" specified for output file is not present in input file.");
				System.exit(-1);
			}
		}

		// Output file name
		String outFileName = fileName.replaceAll("\\.csv$", "-small.csv");

		// First, print the header
		if(debug) {System.out.println("new output file " + outFileName);}
		CSVPrinter out = new CSVPrinter(new OutputStreamWriter(
			new FileOutputStream(outFileName), cs),
			CSVFormat.RFC4180.withQuoteMode(QuoteMode.ALL));
		out.printRecord(newHeaderSet);
		
		// Loop over the records
		for (CSVRecord csvRecord : parser) {
			long recordNumber = csvRecord.getRecordNumber();
			if(recordNumber < 1) {
				// skip header row
				continue;
			}
			// Loop over the output columns
			for(String colName : newHeaderSet) {
				out.print(csvRecord.get(colName));
			}
			out.println();
        }
        parser.close();
		out.close();
	}
}
