set CLASSPATH=../../lib/xerces.jar;../../lib/bim.jar;../../lib/gnu-regexp-1.1.3.jar
java com.expway.ref.BiMEncoder visualExamples.xml visualExamples.bim
java com.expway.ref.BiMDecoder visualExamples.bim visualExamples_D.xml

java com.expway.ref.BiMEncoder visualExamples_D.xml visualExamples2.bim
java com.expway.ref.BiMDecoder visualExamples2.bim visualExamples_D2.xml

java com.expway.ref.BiMEncoder monsterJr4Ver3.xml monsterJr4Ver3.bim
java com.expway.ref.BiMDecoder monsterJr4Ver3.bim monsterJr4Ver3_D.xml

java com.expway.ref.BiMEncoder monsterJr4Ver3_D.xml monsterJr4Ver32.bim
java com.expway.ref.BiMDecoder monsterJr4Ver32.bim monsterJr4Ver3_D2.xml

java com.expway.ref.BiMEncoder mdsExamplesClause4_7.xml mdsExamplesClause4_7.bim
java com.expway.ref.BiMDecoder mdsExamplesClause4_7.bim mdsExamplesClause4_7_D.xml

java com.expway.ref.BiMEncoder mdsExamplesClause8_10.xml mdsExamplesClause8_10.bim
java com.expway.ref.BiMDecoder mdsExamplesClause8_10.bim mdsExamplesClause8_10_D.xml

java com.expway.ref.BiMEncoder mdsExamplesClause11_12.xml mdsExamplesClause11_12.bim
java com.expway.ref.BiMDecoder mdsExamplesClause11_12.bim mdsExamplesClause11_12_D.xml

java com.expway.ref.BiMEncoder mdsExamplesClause13_15.xml mdsExamplesClause13_15.bim
java com.expway.ref.BiMDecoder mdsExamplesClause13_15.bim mdsExamplesClause13_15_D.xml

java com.expway.ref.BiMEncoder mdsExamplesClause17.xml mdsExamplesClause17.bim
java com.expway.ref.BiMDecoder mdsExamplesClause17.bim mdsExamplesClause17_D.xml

