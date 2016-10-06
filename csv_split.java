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
import java.util.*;

public class csv_split {
	public static boolean debug = true;
    public static void main(String[] args) throws IOException {
		// Set up parameters
		String usage = "Usage: java csv_split KeyColumnName MaxRecords FileName";
		if(args.length != 3) {
			System.out.println(usage);
			System.exit(-1);
		}
		String keyName = args[0];
		int maxRecords = 0;
		try {
			maxRecords = Integer.parseInt(args[1]);
		}
		catch (NumberFormatException ex) {
			// nothing
		}
		if(maxRecords < 1) {
			System.out.println(usage);
			System.out.println("MaxRecords must be a number.");
			System.exit(-1);
		}

		String fileName = args[2];
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

		// Get header line to write into subfiles
		Set<String> headerSet = parser.getHeaderMap().keySet();

		// Find the lists of locations for each key
		// The first is a LinkedHashMap because we want the order to be the same on the way out
        Map<String,ArrayList<Long>> characterPositionMap = new LinkedHashMap<String,ArrayList<Long>>(); 
		Map<String,ArrayList<Long>> recordNumberMap = new HashMap<String,ArrayList<Long>>();
        for (CSVRecord csvRecord : parser) {
			long recordNumber = csvRecord.getRecordNumber();
			long characterPosition = csvRecord.getCharacterPosition();
			if(recordNumber < 1) {
				// skip header row
				continue;
			}
			String key = csvRecord.get(keyName);
			if(!characterPositionMap.containsKey(key)) {
				characterPositionMap.put(key, new ArrayList<Long>());
				recordNumberMap.put(key, new ArrayList<Long>());
			}
			recordNumberMap.get(key).add((Long) recordNumber);
			characterPositionMap.get(key).add(characterPosition);
        }
        parser.close();
        parser = null;

		// Step 2: Find rows and output

		// Output file name
		String outFileNameFormat = fileName.replace("%", "-");
		outFileNameFormat = outFileNameFormat.replaceAll("\\.csv$", "-%1\\$05d.csv");

		// Big loop over keys of the Maps (which are values of the key column
		int fileNum = 0;
		int records = 0;
		CSVParser p = null;
		Iterator<CSVRecord> itr = null;
		long lastRecordNumber = -10;
		CSVPrinter out = null;
		for(String key: characterPositionMap.keySet()) {
			ArrayList<Long> characterPositionList = characterPositionMap.get(key);
			ArrayList<Long> recordNumberList = recordNumberMap.get(key);
			int n = characterPositionList.size();
			if(debug) {System.out.println("Starting: " + key + ", records are " + recordNumberList);}
			for(int i=0;i<n;i++) {
				long recordNumber = recordNumberList.get(i);
				long characterPosition = characterPositionList.get(i);
				if(p != null && lastRecordNumber+1 != recordNumber) {
					p.close();
					p = null;
					itr = null;
				}
				lastRecordNumber = recordNumber;
				if(p==null) {
					if(debug) {System.out.println("Creating new parser: characterPosition is " +
						characterPosition + " recordNumber is " + recordNumber);}
					RandomAccessFile raf = new RandomAccessFile(fileName, "r");
					raf.seek(characterPosition);
					InputStreamReader isr = new InputStreamReader(new FileInputStream(raf.getFD()), cs);
					p = new CSVParser(isr, CSVFormat.RFC4180, characterPosition, recordNumber);
					itr = p.iterator();
				}
				if(!itr.hasNext()) {
					// Shouldn't happen
					throw new IOException();
				}
				CSVRecord r = itr.next();
				if(out == null) {
					fileNum ++;
					String outFileName = String.format(outFileNameFormat, fileNum);
					if(debug) {System.out.println("new output file " + outFileName);}
					out = new CSVPrinter(new OutputStreamWriter(
						new FileOutputStream(outFileName), cs),
						CSVFormat.RFC4180.withQuoteMode(QuoteMode.ALL));
					out.printRecord(headerSet);
				}

				out.printRecord(r);
				records++;
			}
			if(records > maxRecords) {
				out.close();
				out = null;
				records = 0;
			}
		}
		if(out != null) {
			out.close();
			out = null;
		}
	}
}
