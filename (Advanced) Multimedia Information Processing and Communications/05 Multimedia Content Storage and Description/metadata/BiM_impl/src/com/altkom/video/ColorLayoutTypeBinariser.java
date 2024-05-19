/***********************************************************************
This software module was originally developed by
Andrzej Buchowicz (Altkom Akademia SA), Grzegorz Galinski (Altkom Akademia SA)
Marcin Gawlik (Altkom Akademia SA), Jaroslaw Zuk (Altkom Akademia SA) and
Wladyslaw Skarbek (Altkom Akademia SA) in the course of
development of the MPEG-7 Systems (ISO/IEC 15938-1) standard.

This software module is an implementation of a part of one or more
MPEG-7 Systems (ISO/IEC 15938-1) tools as specified by the
MPEG-7 Systems (ISO/IEC 15938-1) standard.

ISO/IEC gives users of the MPEG-7 Systems (ISO/IEC 15938-1) free license
to this software module or modifications thereof for use in hardware or
software products claiming conformance to the MPEG-7 Systems
(ISO/IEC 15938-1).

Those intending to use this software module in hardware or software
products are advised that its use may infringe existing patents.

The original developer of this software module and his/her company, the
subsequent editors and their companies, and ISO/IEC have no liability
for use of this software module or modifications thereof in an
implementation.

Copyright is not released for non MPEG-7 Systems (ISO/IEC 15938-1)
conforming products.

Altkom Akademia SA retains full right to use the code for his/her own purpose,
assign or donate the code to a third party and to inhibit third parties
from using the code for non MPEG-7 Systems (ISO/IEC 15938-1) conforming
products.

This copyright notice must be included in all copies or derivative works.

Copyright Altkom Akademia SA © 2001.
************************************************************************/

package com.altkom.video;

import java.io.Writer;
import java.util.StringTokenizer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.expway.tools.io.ChunkWriter;
import com.expway.tools.io.BitToBitDataInputStream;

/**
   An object of this class can binarise an element of the type ColorLayoutType as of the
   ISO/IEC JTC1/SC29/WG11/M7476 (Part 3: Visual).
*/

public class ColorLayoutTypeBinariser extends Binariser {
  // elements/attributes strings
  private final String YDC ="YDCCoeff";
  private final String CbDC="CbDCCoeff";
  private final String CrDC="CrDCCoeff";
  private final String YAC ="YACCoeff";
  private final String CbAC="CbACCoeff";
  private final String CrAC="CrACCoeff";

  // binariser states indetifiers
  private final int LAYOUT=0;
  private final int Y_DC_COEFF=1;
  private final int Y_AC_COEFF=2;
  private final int Cb_DC_COEFF=3;
  private final int Cb_AC_COEFF=4;
  private final int Cr_DC_COEFF=5;
  private final int Cr_AC_COEFF=6;

  private final int SKIP=-1;

  // local variables
  private int state;
  private int numOfCoeff;
  private int yDc, cbDc, crDc, yAcCoeff[], cbAcCoeff[], crAcCoeff[];
  int yAcIndex, cAcIndex;



  public void startElement (String nameSpace, String localName, String qualified, Attributes attrs) throws SAXException {
    super.startElement (nameSpace, localName, qualified, attrs);

    if(isAtRootLevel()){
      state=LAYOUT;
    }else{
      if(localName.equals(YDC)){
        state=Y_DC_COEFF;
      }

      if(localName.equals(CbDC)){
        state=Cb_DC_COEFF;
      }

      if(localName.equals(CrDC)){
        state=Cr_DC_COEFF;
      }

      if(localName.startsWith(YAC)){
        state=Y_AC_COEFF;
        try{
          numOfCoeff=Integer.parseInt(localName.substring(YAC.length()));
        }catch(NumberFormatException nfe){
          throw new SAXException("ColorLayout - unknown number of Y AC coefficients");
        }
        yAcCoeff=new int[numOfCoeff];
      }

      if(localName.startsWith(CbAC)){
        state=Cb_AC_COEFF;
        try{
          numOfCoeff=Integer.parseInt(localName.substring(CbAC.length()));
        }catch(NumberFormatException nfe){
          throw new SAXException("ColorLayout - unknown number of Cb AC coefficients");
        }
        cbAcCoeff=new int[numOfCoeff];
      }

      if(localName.startsWith(CrAC)){
        state=Cr_AC_COEFF;
        try{
          numOfCoeff=Integer.parseInt(localName.substring(CrAC.length()));
        }catch(NumberFormatException nfe){
          throw new SAXException("ColorLayout - unknown number of Cr AC coefficients");
        }
        crAcCoeff=new int[numOfCoeff];
      }
    }
  }


  public void characters (char [] chars, int index, int length) throws SAXException {
    switch(state){
      case LAYOUT:
      case SKIP:
        break;

      case Y_DC_COEFF:
        yDc=readDcCoeff(new String(chars, index, length));
        state=SKIP;   // do not read any data unless new element starts
        break;

      case Cb_DC_COEFF:
        cbDc=readDcCoeff(new String(chars, index, length));
        state=SKIP;   // do not read any data unless new element starts
        break;

      case Cr_DC_COEFF:
        crDc=readDcCoeff(new String(chars, index, length));
        state=SKIP;   // do not read any data unless new element starts
        break;

      case Y_AC_COEFF:
        readAcCoeff(yAcCoeff, new String(chars, index, length));
        state=SKIP;   // do not read any data unless new element starts
        break;

      case Cb_AC_COEFF:
        readAcCoeff(cbAcCoeff, new String(chars, index, length));
        state=SKIP;   // do not read any data unless new element starts
        break;

      case Cr_AC_COEFF:
        readAcCoeff(crAcCoeff, new String(chars, index, length));
        state=SKIP;   // do not read any data unless new element starts
        break;
    }
  }

  /**
   * used to read DC coefficient
   * @param data <code>String</code> containing coefficient
   * @throws <code>SAXException</code> if data can not be parsed into number
   */
  private int readDcCoeff(String data)  throws SAXException{
    int retVal;

    try{
      retVal=Integer.parseInt(data.trim());
    }catch(NumberFormatException nfe){
      throw new SAXException("ColorLayout - illegal numerical format");
    }

    return retVal;
  }


  /**
   * used to read a vector of AC coefficients.
   * @param table table to which coefficient will be read, number of coefficients to be
   * read is determined from the length of the table
   * @param data <code>String</code> containing all coefficients
   */
  public void readAcCoeff(int table[], String data) throws SAXException{
    StringTokenizer tokenizer=new StringTokenizer(data);

    for(int i=0; i<table.length; i++){
      if(tokenizer.hasMoreTokens()){
        try{
          table[i]=Integer.parseInt(tokenizer.nextToken());
        }catch(NumberFormatException nfe){
          throw new SAXException("ColorLayout - illegal numerical format");
        }
      }else{
        throw new SAXException("ColorLayout - AC coefficient not found");
      }
    }
  }


  public void endElement (String nameSpace, String localName, String qualified) throws SAXException {
    super.endElement (nameSpace, localName, qualified);

    if(isFinished()){
      if(cbAcCoeff.length != crAcCoeff.length){
        throw new SAXException("Number of Cr AC coefficients NOT EQUAL TO Cb AC coefficients");
      }

      yAcIndex=coeffIndex(1+ yAcCoeff.length);
      cAcIndex=coeffIndex(1+ cbAcCoeff.length);

      if( yAcIndex== -1 || cAcIndex==-1){
        throw new SAXException("ColorLayout - illegal number of AC coefficients");
      }

      finish();
    }
  }


  public void finish() {
    if((currentChunkWriterUsed == null)){
      return;
    }

    // write coefficient pattern
    if(yAcCoeff.length==5 && cbAcCoeff.length==2 ){
      currentChunkWriterUsed.writeByte(0,1);    // 0
    }else{
      if(yAcCoeff.length==5 && cbAcCoeff.length==5){
        currentChunkWriterUsed.writeByte(2,2);   // 10
      }else{
        currentChunkWriterUsed.writeByte(3,2);   // 11
        currentChunkWriterUsed.writeByte(yAcIndex, 3);
        currentChunkWriterUsed.writeByte(cAcIndex, 3);
      }
    }

    // write DC coefficients
    currentChunkWriterUsed.writeByte(yDc, 6);
    currentChunkWriterUsed.writeByte(cbDc, 6);
    currentChunkWriterUsed.writeByte(crDc, 6);

    // write AC coefficients
    for(int i=0; i<yAcCoeff.length; i++){
      currentChunkWriterUsed.writeByte(yAcCoeff[i], 5);
    }
    for(int i=0; i<cbAcCoeff.length; i++){
      currentChunkWriterUsed.writeByte(cbAcCoeff[i], 5);
    }
    for(int i=0; i<crAcCoeff.length; i++){
      currentChunkWriterUsed.writeByte(crAcCoeff[i], 5);
    }
  }

  /**
   * used to calculate numOfYCoeff, numOfCCoeff index according to Table 23
   * @param coeff value of the coefficient
   * @return index of the coefficent or -1 if index can not be calculated
   */
  private int coeffIndex(int coeff)  {
    switch(coeff){
      case 1:
        return 0;

      case 3:
        return 1;

      case 6:
        return 2;

      case 10:
        return 3;

      case 15:
        return 4;

      case 21:
        return 5;

      case 28:
        return 6;

      case 64:
        return 7;

      default:
        return -1;
    }
  }

  public int decode(BitToBitDataInputStream dis,Writer w)   {
    return decode(dis, w, DNS+COLOR_LAYOUT, null);
  }

  public int decode(BitToBitDataInputStream dis,Writer w, String name, String attrs)   {
    if (DEBUG) System.out.println("ColorLayout-decode");
    int[] numOfCoeff={1, 3, 6, 10, 15, 21, 28}; // table for decoding numOfXCoeffIndex (table 34 in N7476)

    long before = dis.getReadedBits();

    try{
      String ns = getNameSpace(name);
      w.write("\n<" + name );
      if(attrs != null){
        w.write(" " + attrs );
      }
      w.write(">\n");

      int numOfYCoeffIndex, numOfCCoeffIndex;
      if(dis.readByte(1)==0){   // coeffPattern bit 1 == 0
        numOfYCoeffIndex=2;
        numOfCCoeffIndex=1;
      }else{                 // read second bit of the coeffPattern
        if(dis.readByte(1)==0){   // coeffPattern bit 2 == 0
          numOfYCoeffIndex=2;
          numOfCCoeffIndex=2;
        }else{              // read numOfYCoeffIndex, numOfCCoeffIndex
          numOfYCoeffIndex=dis.readByte(3);
          numOfCCoeffIndex=dis.readByte(3);
        }
      }

      // read/write DC coeff
      w.write("<" + ns+YDC + ">" + dis.readByte(6) + "</" + ns+YDC + ">\n" );
      w.write("<" + ns+CbDC + ">" + dis.readByte(6) + "</" + ns+CbDC + ">\n" );
      w.write("<" + ns+CrDC + ">" + dis.readByte(6) + "</" + ns+CrDC + ">\n" );

      // read/write Y AC coeff
      int numOfYCoeff=numOfCoeff[numOfYCoeffIndex]-1;
      w.write("<" + ns+YAC+numOfYCoeff + ">\n  ");
      for(int i=0; i<numOfYCoeff; i++){
        w.write(" " + dis.readByte(5) );
      }
      w.write("\n</" + ns+YAC+numOfYCoeff + ">\n");

      // read/write Cb AC coeff
      int numOfCCoeff=numOfCoeff[numOfCCoeffIndex]-1;
      w.write("<" + ns+CbAC + numOfCCoeff + ">\n  ");
      for(int i=0; i<numOfCCoeff; i++){
        w.write(" " + dis.readByte(5) );
      }
      w.write("\n</" + ns+CbAC + numOfCCoeff +  ">\n");

      // read/write Cr AC coeff
      w.write("<" + ns+CrAC + numOfCCoeff + ">\n  ");
      for(int i=0; i<numOfCCoeff; i++){
        w.write(" " + dis.readByte(5) );
      }
      w.write("\n</" + ns+CrAC + numOfCCoeff + ">\n");

      w.write("</" + name + ">\n" );
    }catch (Exception e){
      System.out.println("ColorLayout: Unable to decode bitstream");
      e.printStackTrace();
    }

    return (int)(dis.getReadedBits()-before);
  }
}
