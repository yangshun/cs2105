#!/bin/bash

# Remove all generated files from previous runs
rm *.class
if [ -s "file-recv.txt" ]
	then
	rm file-recv.txt
fi

# Compile java files
javac FileSender.java
javac FileReceiver.java

# Run the Receiver files
echo "FileReceiver will run on port 2105"
java FileReceiver 2105 file-recv.txt

echo ""
echo "File received"

# Compare SHA1 values of sent and received files
echo "Comparing SHA1 values:"
SHA1=$(openssl sha1 file-send.txt)
SHA2=$(openssl sha1 file-recv.txt)
if  [ ${SHA1#*' '} == ${SHA2#*' '} ]
then
	echo "SHA1 values equal"
	echo "Test passed!"
else
	echo "SHA1 values not the same"
	echo "Test failed..."
fi