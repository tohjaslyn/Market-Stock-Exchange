cp jacoco/TestCaseParser.java TestCaseParser.java
javac Exchange.java Market.java Order.java Trade.java Trader.java TestCaseParser.java -g
java -javaagent:jacoco/jacocoagent.jar=output=file,destfile=tmp/jacoco.exec TestCaseParser
mkdir classes
cp *.java classes
java -jar jacoco/jacococli.jar report tmp/jacoco.exec --classfiles Exchange.class --classfiles Market.class --classfiles Order.class --classfiles Trader.class --classfiles Trade.class --sourcefiles classes/ --html jacoco/report
rm -r tmp
rm -r classes
rm TestCaseParser.java
rm TestCaseParser.class
