rm *.class
rm received.txt

javac FileSender.java
javac FileReceiver.java

echo "FileReceiver will run on port 2105"
java FileReceiver 2105 received.txt
