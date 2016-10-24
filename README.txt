This repository contains two programs

1) csv_split.java

Program to split CSV files into chunks, each chunk containing all the lines
with a given key column.

Usage: java csv_split KeyColumnName MaxRecords FileName

For example, you can split the file test.csv

"Hello","Borld"
1,2
3,4
5,6
1,3
1,4
1,5
3,7

With the command

java csv_split Hello 3 test.csv

You will generate a set of files called test-00001.csv test-00002.csv, etc.
The program will group all the records with the same value of the key column
(appearing in the same order as they did in the original file). The file
will collect records until it reaches MaxRecords number of records, and then
a new file will be started.

This program uses the Apache Commons CSV parser, v1,4, from here:
https://commons.apache.org/proper/commons-csv/download_csv.cgi

To build:

export CLASSPATH=".:./commons-csv-1.4/commons-csv-1.4.jar"
javac csv_split.java




2) csv_reduce.java

Program to produce a CSV file containing only a specified subset of the columns
in the input CSV file

Usage: java csv_reduce columnsFile FileName

The columnsFile is a plain text (US_ASCII) file containing the names of the
columns to be copied to the output file, one per line. The input file must have
a .csv extension, and the ouput file will be named -small.csv

If a column is required in the output file that isn't present in the input CSV,
an error will be generated.

To build:

export CLASSPATH=".:./commons-csv-1.4/commons-csv-1.4.jar"
javac csv_split.java



The code in this project is licensed under the Mozilla Public License v2.0.
Copyright (c) 2016 Noodle Partners
