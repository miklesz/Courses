set CLASSPATH=../../lib/xerces.jar;../../lib/bim.jar;../../lib/gnu-regexp-1.1.3.jar
java com.expway.ref.BiMEncoder ehd.xml ehd.bim 
java com.expway.ref.BiMDecoder ehd.bim ehd_out.xml 
