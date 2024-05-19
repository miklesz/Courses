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
import java.util.Vector;
import java.util.Enumeration;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.expway.tools.io.ChunkWriter;
import com.expway.tools.io.BitToBitDataInputStream;

/**
   An object of this class can binarise an element of the type ColorQuantizationType as of the
   ISO/IEC JTC1/SC29/WG11/M7476 (Part 3: Visual).
*/

public class ColorQuantizationTypeBinariser extends Binariser {
  // elements/attributes strings
  //private final String COLOR_QUANTIZATION="ColorQuantization";
  private final String COMPONENT="Component";
  private final String NUMBER_OF_BINS="NumOfBins";

  // binariser state identifiers
  private final int QUANT=0;
  private final int COMP=1;
  private final int NUM_BINS=2;
  private final int SKIP=-1;

  // local variables
  private int colorSpaceId=ColorSpaceTypeBinariser.RGB;   // stores current color space, defaults RGB space
  private int state;
  private Vector quantData;
  private int compId;
  private int numOfBins;



  public void setColorSpace(int colorSpaceId){
    this.colorSpaceId=colorSpaceId;
  }


  public void startElement (String nameSpace, String localName, String qualified, Attributes attrs) throws SAXException {
    super.startElement (nameSpace, localName, qualified, attrs);

    if(isAtRootLevel()){
      quantData=new Vector();
      state=QUANT;
    }else{
      if(localName.equals(COMPONENT)){
        state=COMP;
      }

      if(localName.equals(NUMBER_OF_BINS)){
        state=NUM_BINS;
      }
    }
  }


  public void characters (char [] chars, int index, int length) throws SAXException {
    switch(state){
      case QUANT:
      case SKIP:
        break;      // do not read any data

      case COMP:
        String data=(new String(chars, index, length)).trim();
        compId=0x1F;   // reserved value, indicates unrecocnized component type

        if(data.equals("R")){
            compId=0x0;
        }
        if(data.equals("G")){
            compId=0x01;
        }
        if(data.equals("B")){
            compId=0x02;
        }
        if(data.equals("Y")){
            compId=0x03;
        }
        if(data.equals("Cb")){
            compId=0x04;
        }
        if(data.equals("Cr")){
            compId=0x05;
        }
        if(data.equals("H")){
            compId=0x06;
        }
        if(data.equals("S")){
            compId=0x07;
        }
        if(data.equals("V")){
            compId=0x08;
        }
        if(data.equals("Max")){
            compId=0x09;
        }
        if(data.equals("Min")){
            compId=0x0A;
        }
        if(data.equals("Diff")){
            compId=0x0B;
        }
        if(data.equals("Sum")){
            compId=0x0C;
        }
        state=SKIP;        // do not read any data unless new element started
        break;

      case NUM_BINS:
        try{
          numOfBins=Integer.parseInt((new String(chars, index, length)).trim());
        }catch(NumberFormatException nfe){
          throw new SAXException("ColorQuantization - unrecognized NumOfBins");
        }
        state=SKIP;        // do not read any data unless new element started
        break;
    }
  }


  public void endElement (String nameSpace, String localName, String qualified) throws SAXException {
    super.endElement (nameSpace, localName, qualified);

    if(localName.equals(NUMBER_OF_BINS)){
      quantData.add(new QuantizationParam(compId, numOfBins));
      return;
    }

    if(isFinished()){
      finish();
    }
  }


  public void finish(){
    if((currentChunkWriterUsed == null)){
      return;
    }

    Enumeration enum=quantData.elements();
    while(enum.hasMoreElements()){
      QuantizationParam param=(QuantizationParam)(enum.nextElement());
      currentChunkWriterUsed.writeByte(param.compId, 5);
      currentChunkWriterUsed.writeInt(param.numOfBins, 12);
    }
  }

  /**
   * returns the numberOfBins in the encoded desriptor
   * @param compNum the number/order of the component
   * used in DominantColorTypeBinariser
   */
  public int getEncodedNumberOfBins(int compNum){
    QuantizationParam param=(QuantizationParam)(quantData.elementAt(compNum));
    return param.numOfBins;
  }

  /**
   * returns the numberOfBins in the decoded binary stream
   * @param compNum the number/order of the component
   * used in DominantColorTypeBinariser
   */
  public int getDecodeddNumberOfBins(int compNum){
    return decodedNumBins[compNum];
  }


  // used to store color component quantization parameters
  private class QuantizationParam{
    int compId;
    int numOfBins;

    QuantizationParam(int compId, int numOfBins){
      this.compId=compId;
      this.numOfBins=numOfBins;
    }
  }

  int decodedNumBins[];


  public int decode(BitToBitDataInputStream dis,Writer w)   {
    return decode(dis, w, DNS+COLOR_QUANTIZATION, null);
  }


  public int decode(BitToBitDataInputStream dis,Writer w, String name, String attrs)   {
    if (DEBUG) System.out.println("ColorQuantization-decode");
    String compName[]={"R", "G", "B", "Y", "Cb", "Cr", "H", "S", "V", "Max", "Min", "Diff", "Sum"};
    long before = dis.getReadedBits();

    try{
      String ns = getNameSpace(name);
      int numOfComponents= colorSpaceId==ColorSpaceTypeBinariser.MONOCHROME ? 1 : 3;
      decodedNumBins=new int[numOfComponents];

      w.write("\n<" + name );
      if(attrs != null){
        w.write(" " + attrs );
      }
      w.write(">\n" );

      for(int k=0; k<numOfComponents; k++){
        int compId=dis.readByte(5);
        decodedNumBins[k]=dis.readInt(12);
        w.write("  <" + ns+COMPONENT + ">" + compName[compId] + "</" + ns+COMPONENT + ">\n" );
        w.write("  <" + ns+NUMBER_OF_BINS + ">" + decodedNumBins[k] + "</" + ns+NUMBER_OF_BINS + ">\n" );
      }
      w.write("</" + name + ">\n" );
    }catch (Exception e){
      System.out.println("ColorQuantization: Unable to decode bitstream");
      e.printStackTrace();
    }

    return (int)(dis.getReadedBits()-before);
  }
}


