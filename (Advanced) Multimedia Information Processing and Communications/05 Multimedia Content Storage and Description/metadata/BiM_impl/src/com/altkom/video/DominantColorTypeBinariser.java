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
   An object of this class can binarise an element of the type DominantColorType as of the
   ISO/IEC JTC1/SC29/WG11/M7476 (Part 3: Visual).
*/
public class DominantColorTypeBinariser extends Binariser {
  // elements/attributes strings
  //private final String DOMINANT_COLOR_DESCR="DominatColor";
  private final String COLOR_SPACE_DESCR="ColorSpace";
  private final String COLOR_QUANT_DESCR="ColorQuantization";
  private final String SPATIAL_COHERENCY_DESCR="SpatialCoherency";
  private final String VALUE_DESCR="Value";
  private final String PERCENTAGE_DESCR="Percentage";
  private final String INDEX_DESCR="Index";
  private final String VARIANCE_DESCR="ColorVariance";

  // binariser state identifier
  private final int DOMINANT=0;
  private final int COLOR_SPACE=1;
  private final int COLOR_QUANT=2;
  private final int SPATIAL_COHERENCY=3;
  private final int VALUE=4;
  private final int PERCENTAGE=5;
  private final int INDEX=6;
  private final int VARIANCE=7;
  private final int SKIP=-1;

  // local variables
  private int state;
  private ColorSpaceTypeBinariser colorSpaceBinariser;
  private ColorQuantizationTypeBinariser colorQuantBinariser;
  private boolean variancePresent;
  private int spatialCoherency;
  private int percentage, colorIndex[], colorVariance[];
  private Vector colorList;

  public void startElement(String nameSpace, String localName, String qualified, Attributes attrs)  throws SAXException {
    super.startElement (nameSpace, localName, qualified, attrs);

    if(isAtRootLevel()){
      state=DOMINANT;
      variancePresent=false;
      colorSpaceBinariser=null;
      colorQuantBinariser=null;
      colorList=new Vector();
    }else{
      if(localName.equals(COLOR_SPACE_DESCR)){
        state=COLOR_SPACE;
        colorSpaceBinariser=new ColorSpaceTypeBinariser();
        colorSpaceBinariser.takeDescriptor(null);   // do not write to binary stream
      }

      if(localName.equals(COLOR_QUANT_DESCR)){
        state=COLOR_QUANT;
        colorQuantBinariser=new ColorQuantizationTypeBinariser();
        colorQuantBinariser.takeDescriptor(null);   // do not write to binary stream
      }

      if(localName.equals(SPATIAL_COHERENCY_DESCR)){
        state=SPATIAL_COHERENCY;
      }

      if(localName.equals(VALUE_DESCR)){
        state=VALUE;
        colorIndex=new int[3];
        colorVariance=new int[3];
      }

      if(localName.equals(PERCENTAGE_DESCR)){
        state=PERCENTAGE;
      }

      if(localName.equals(INDEX_DESCR)){
        state=INDEX;
      }

      if(localName.equals(VARIANCE_DESCR)){
        variancePresent=true;
        state=VARIANCE;
      }
    }

    if(state==COLOR_SPACE){
      colorSpaceBinariser.startElement(nameSpace, localName, qualified, attrs);
    }

    if(state==COLOR_QUANT){
      colorQuantBinariser.startElement(nameSpace, localName, qualified, attrs);
    }
  }


  public void characters (char [] chars, int index, int length) throws SAXException {
    String data;
    StringTokenizer tokenizer;

    switch(state){
      case COLOR_SPACE:
        colorSpaceBinariser.characters(chars, index, length);
        break;

      case COLOR_QUANT:
        colorQuantBinariser.characters(chars, index, length);
        break;

      case SPATIAL_COHERENCY:
        data=new String(chars, index, length);
        try{
          spatialCoherency=Integer.parseInt(data.trim());
        }catch(NumberFormatException nfe){
          throw new SAXException("DominantColor: SpatialCoherency - unrecognized format");
        }
        state=SKIP;
        break;

      case VALUE:
        break;

      case PERCENTAGE:
        data=new String(chars, index, length);
        try{
          percentage=Integer.parseInt(data.trim());
        }catch(NumberFormatException nfe){
          throw new SAXException("DominantColor: Percentage - unrecognized format");
        }
        state=VALUE;      // do not read more data
        break;

      case INDEX:
        tokenizer=new StringTokenizer(new String(chars, index, length));
        for(int i=0; i<3; i++){
          try{
            colorIndex[i]=Integer.parseInt(tokenizer.nextToken());
          }catch(NumberFormatException nfe){
            throw new SAXException("DominantColor: Index - unrecognized format");
          }
        }
        state=VALUE;      // do not read more data
        break;

      case VARIANCE:
        tokenizer=new StringTokenizer(new String(chars, index, length));
        for(int i=0; i<3; i++){
          try{
            colorVariance[i]=Integer.parseInt(tokenizer.nextToken());
          }catch(NumberFormatException nfe){
            throw new SAXException("DominantColor: ColorVariance - unrecognized format");
          }
        }
        state=VALUE;    // do not read more data
        break;

      default:
        break;
    }
  }


  public void endElement (String nameSpace, String localName, String qualified) throws SAXException {
    super.endElement (nameSpace, localName, qualified);

    switch(state){
      case COLOR_SPACE:
        colorSpaceBinariser.endElement (nameSpace, localName, qualified);
        if(colorSpaceBinariser.isFinished()){
          state=SKIP;
        }
        break;

      case COLOR_QUANT:
        colorQuantBinariser.endElement (nameSpace, localName, qualified);
        if(colorQuantBinariser.isFinished()){
          state=SKIP;
        }
        break;

      case SPATIAL_COHERENCY:
        state=SKIP;            // to avoid next reading of the SpatialCoherency
        break;

      case VALUE:
        if(localName.equals(VALUE_DESCR)){
          colorList.add(new ColorData(percentage, colorIndex, colorVariance));
          state=SKIP;
        }
        break;

      default:
        break;
    }

    if(isFinished()){
      finish();
    }
  }


  public void finish(){
    if((currentChunkWriterUsed == null)){
      return;
    }

    // write Size - the number of dominant colors
    currentChunkWriterUsed.writeByte(colorList.size()-1, 3);

    // write ColorSpace if present
    if(colorSpaceBinariser==null){
      currentChunkWriterUsed.writeBoolean(false);
    }else{
      currentChunkWriterUsed.writeBoolean(true);
      colorSpaceBinariser.takeDescriptor(currentChunkWriterUsed);
      colorSpaceBinariser.finish();
    }

    // write ColorQuantization if present
    if(colorQuantBinariser==null){
      currentChunkWriterUsed.writeBoolean(false);
    }else{
      currentChunkWriterUsed.writeBoolean(true);
      colorQuantBinariser.takeDescriptor(currentChunkWriterUsed);
      colorQuantBinariser.finish();
    }

    // write VariancePresent
    currentChunkWriterUsed.writeBoolean(variancePresent);

    // write SpatialCoherency
    currentChunkWriterUsed.writeByte(spatialCoherency, 5);

    // write dominant colors data
    Enumeration enum=colorList.elements();
    while(enum.hasMoreElements()){
      ColorData data=(ColorData)(enum.nextElement());
      currentChunkWriterUsed.writeByte(data.percentage, 5);
      for(int i=0; i<3; i++){
        currentChunkWriterUsed.writeByte(data.index[i], getNumberOfBitsForIndex(i));
        if(variancePresent){
         // for(int i=0; i<3; i++){
            currentChunkWriterUsed.writeByte(data.variance[i], 1);
         // }
        }
      }
    }
  }

  /** derives number of bits for the index representation
   *  if ColorQunatization not present returns default value 5, otherwise number of
   *  bits required to represent numberOfBins for the specified component
   *  @param componentNum number/order of the color component
   */
  private int getNumberOfBitsForIndex(int componentNum){
    if(colorQuantBinariser==null){
      return 5;
    }else{
      return numBits(colorQuantBinariser.getEncodedNumberOfBins(componentNum));
    }
  }

  /**
   * calculates number bits required for the binary representation of the given number (ceil(log2))
   */
  private int numBits(int number){
    int i=0;
    while(number>0){
      i++;
      number=number >> 1;
    }
    return i;
  }

  /**
   *  used to store dominant color data
   */
  private class ColorData{
    int percentage;
    int index[];
    int variance[];

    ColorData(int percentage, int index[], int variance[]){
      this.percentage=percentage;
      this.index=index;
      this.variance=variance;
    }
  }


  public int decode(BitToBitDataInputStream dis,Writer w)   {
    return decode(dis, w, DNS+DOMINANT_COLOR, null);
  }


  public int decode(BitToBitDataInputStream dis,Writer w, String name, String attrs)   {
    if (DEBUG) System.out.println("DominantColor-decode");
    ColorSpaceTypeBinariser colorSpaceBinariser=null;
    ColorQuantizationTypeBinariser colorQuantBinariser=null;

    long before = dis.getReadedBits();

    try{
      String ns = getNameSpace(name);
      // read Size
      int size=dis.readByte(3)+1;
      w.write("\n<" + name);
      if(attrs != null){
        w.write(" " + attrs );
      }
      w.write(">\n");

      // read ColorSpacePresent flag
      if(dis.readBoolean()){
        colorSpaceBinariser=new ColorSpaceTypeBinariser();
        colorSpaceBinariser.decode(dis, w, ns+COLOR_SPACE_DESCR, null);
      }

      // read ColorQuantizationPresent flag
      if(dis.readBoolean()){
        colorQuantBinariser=new ColorQuantizationTypeBinariser();
        if(colorSpaceBinariser!=null){
          colorQuantBinariser.setColorSpace(colorSpaceBinariser.getDecodedSpaceType());
        }
        colorQuantBinariser.decode(dis, w, ns+COLOR_QUANT_DESCR, null);
      }

      // read VariancePresent
      boolean variancePresent=dis.readBoolean();

      // read/write SpatialCoherency
      int spatialCoherency=dis.readByte(5);
      w.write("  <" + ns+SPATIAL_COHERENCY_DESCR + "> " + spatialCoherency + " </" + ns+SPATIAL_COHERENCY_DESCR + ">\n");

      // read/write dominant color values
      for(int k=0; k<size; k++){
        w.write("  <" + ns+VALUE_DESCR + ">\n");
        int percentage=dis.readByte(5);
        w.write("    <" + ns+PERCENTAGE_DESCR + "> " +  percentage + "</"+ns+PERCENTAGE_DESCR+">\n");

        int[] colVar = (variancePresent ? new int[3] : null);
        // read/write Index
        w.write("    <" + ns+INDEX_DESCR + "> ");
        for(int m=0; m<3; m++){        // what if space==Monochromatic and number of components==1 ???
          // determine number of bits used for index representation
          int numBitsForIndex;
          if(colorQuantBinariser==null){
            numBitsForIndex=5;
          }else{
            numBitsForIndex=numBits(colorQuantBinariser.getDecodeddNumberOfBins(m));
          }
          int index=dis.readInt(numBitsForIndex);
          w.write(index + " ");
          if (variancePresent)
            colVar[m] = dis.readByte(1);
        }
        w.write("</" + ns+INDEX_DESCR + ">\n");

        // read/write ColorVariance if present
        if(variancePresent){
          w.write("    <" + ns+VARIANCE_DESCR + "> ");
          for(int m=0; m<3; m++){        // what if space==Monochromatic and number of components==1 ???
          //  int variance=dis.readByte(1); --> must be read after Index
          //  w.write(variance + " ");
            w.write(colVar[m]+" ");
          }
          w.write("</" + ns+VARIANCE_DESCR + ">\n");
        }
        w.write("  </" + ns+VALUE_DESCR + ">\n");
      }
      w.write("</" + name + ">\n");
    }catch (Exception e){
      System.out.println("DominantColor: Unable to decode bitstream");
      e.printStackTrace();
    }

    return (int)(dis.getReadedBits()-before);
  }
}

