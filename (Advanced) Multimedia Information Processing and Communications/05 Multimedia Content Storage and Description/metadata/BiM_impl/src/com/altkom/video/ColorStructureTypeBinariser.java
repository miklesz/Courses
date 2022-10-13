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
   An object of this class can binarise an element of the type ColorStructureType as of the
   ISO/IEC JTC1/SC29/WG11/M7476 (Part 3: Visual).
*/

public class ColorStructureTypeBinariser extends Binariser {
  // elements/attributes strings
  private final String VALUES_TXT="Values";
  private final String COLOR_QUANT="colorQuant";

  private final int[] NOV = new int[] { -1, 32, 64, 128, 256 };

  final int COLOR_STRUCT=0;
  final int VALUES=1;
  final int SKIP=-1;

  // binariser state identifier
  private int state;
  private int colorQuant, numOfValues;
  private int values[];

  public void startElement (String nameSpace, String localName, String qualified, Attributes attrs) throws SAXException {
    super.startElement (nameSpace, localName, qualified, attrs);

    if(isAtRootLevel()){
      state=COLOR_STRUCT;

      try{
        colorQuant=Integer.parseInt(attrs.getValue(COLOR_QUANT));
      }catch(NumberFormatException nfe){
        throw new SAXException("ColorStructure - colorQuant not found");
      }

      numOfValues = NOV[colorQuant];
      /*if(numOfValues!=32 && numOfValues!=64 && numOfValues!=128 && numOfValues!=256){
        throw new SAXException("ColorStructure - illegal value colorQuant=" + numOfValues);
      }*/
      values=new int[numOfValues];
    }else{
      if(localName.equals(VALUES_TXT)){
        state=VALUES;
      }
    }
  }


  public void characters (char [] chars, int index, int length) throws SAXException {
    switch(state){
      case COLOR_STRUCT:
      case SKIP:
        break;

      case VALUES:
        StringTokenizer tokenizer=new StringTokenizer(new String(chars, index, length));
        for(int i=0; i<numOfValues; i++){
          if(tokenizer.hasMoreTokens()){
            int value;
            try{
              values[i]=Integer.parseInt(new String(tokenizer.nextToken()));
            }catch(NumberFormatException nfe){
              throw new SAXException("ColorStructure - illegal numerical format");
            }
          }else{
            throw new SAXException("ColorStructure - value not found");
          }
        }
        state=SKIP;
        break;
    }
  }


  public void endElement (String nameSpace, String localName, String qualified) throws SAXException {
    super.endElement (nameSpace, localName, qualified);

    if(isFinished()){
      finish();
    }
  }


  public void finish(){
    if((currentChunkWriterUsed == null)){
      return;
    }

    // write colorQuant
    /*switch(numOfValues){
      case 32:
        currentChunkWriterUsed.writeByte(1,3);    // 001
        break;

      case 64:
        currentChunkWriterUsed.writeByte(2,3);    // 010
        break;

      case 128:
        currentChunkWriterUsed.writeByte(3,3);    // 011
        break;

      case 256:
        currentChunkWriterUsed.writeByte(4,3);    // 100
        break;
    }*/
    currentChunkWriterUsed.writeByte(colorQuant, 3);

    // write NumOfValues - why? it can be determined from colorQuant
    currentChunkWriterUsed.writeByte(numOfValues-1);

    //write values
    for(int k=0; k<numOfValues; k++){
      currentChunkWriterUsed.writeByte(values[k]);
    }
  }


  public int decode(BitToBitDataInputStream dis,Writer w)   {
    return decode(dis, w, DNS+COLOR_STRUCTURE, null);
  }


  public int decode(BitToBitDataInputStream dis,Writer w, String name, String attrs)   {
    if (DEBUG) System.out.println("ColorStructure-decode");

    long before = dis.getReadedBits();

    try{
      String ns = getNameSpace(name);
      int colorQuant=dis.readByte(3);
      int numOfValues=dis.readInt(8)+1;

      w.write("\n<" + name );
      if(attrs != null){
        w.write(" " + attrs );
      }
      w.write(" " + COLOR_QUANT + "=\"" + colorQuant + "\">\n");

      // write values
      w.write("<" + ns+VALUES_TXT + ">");
      for(int i=0; i<numOfValues; i++){
        w.write(" " + dis.readInt(8) );
      }
      w.write("</" + ns+VALUES_TXT + ">\n");

       w.write("</" + name + ">\n" );
    }catch (Exception e){
      System.out.println("ColorStructure: Unable to decode bitstream");
      e.printStackTrace();
    }

    return (int)(dis.getReadedBits()-before);
  }
}
