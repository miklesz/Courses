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
   An object of this class can binarise an element of the type ColorSpaceType as of the
   ISO/IEC JTC1/SC29/WG11/M7476 (Part 3: Visual).
*/

public class ColorSpaceTypeBinariser extends Binariser {
  // color space type identifiers
  static final int RGB=0;
  static final int YCbCr=1;
  static final int HSV=2;
  static final int HMMD=3;
  static final int LINEAR_MATRIX=4;
  static final int MONOCHROME=5;

  // elements/attributes strings
  //private final String COLOR_SPACE="ColorSpace";
  private final String REFERENCE_FLAG="colorReferenceFlag";
  private final String SPACE_TYPE="type";
  private final String TRANS_MATRIX="ColorTransMat";

  // color space type strings
  private final String RGB_SPACE="RGB";
  private final String YCbCr_SPACE="YCbCr";
  private final String HSV_SPACE="HSV";
  private final String HMMD_SPACE="HMMD";
  private final String LINEAR_MATRIX_SPACE="LinearMatrix";
  private final String MONOCHROME_SPACE="Monochrome";

  // binariser state identifier
  private final int SPACE=0;
  private final int COEFF=1;
  private final int SKIP=-1;

  // local variables
  private int state;

  private int typeId;
  private boolean refFlag;
  private int coeffs[];

  public ColorSpaceTypeBinariser() {
  }

  public void startElement (String nameSpace, String localName, String qualified, Attributes attrs) throws SAXException {
    super.startElement (nameSpace, localName, qualified, attrs);

    if(isAtRootLevel()){
      state=SPACE;
      refFlag=Boolean.valueOf(attrs.getValue(REFERENCE_FLAG)).booleanValue();
      String type=attrs.getValue(SPACE_TYPE);
      typeId=RGB;        // default value 0000 - RGB color space

      if(type.equals(YCbCr_SPACE)){
        typeId=YCbCr;          // 0001
      }
      if(type.equals(HSV_SPACE)){
        typeId=HSV;       // 0010
      }
      if(type.equals(HMMD_SPACE)){
        typeId=HMMD;       // 0011
      }
      if(type.equals(LINEAR_MATRIX_SPACE)){
        typeId=LINEAR_MATRIX;       // 0100
      }
      if(type.equals(MONOCHROME_SPACE)){
        typeId=MONOCHROME;       // 0101
      }
    }else{
      if(typeId==LINEAR_MATRIX && !localName.equals(TRANS_MATRIX)){
          throw new SAXException("ColorTransformationMatrix not found");
      }
      state=COEFF;
      coeffs=new int[9];
    }
  }


  public void characters (char [] chars, int index, int length) throws SAXException {
    switch(state){
        case SKIP:
        case SPACE:
            break;  // do not read any data

        case COEFF:
            String data=new String (chars, index, length);
            StringTokenizer tokenizer=new StringTokenizer(data);

            for(int i=0; i<9; i++){
                if(tokenizer.hasMoreElements()){
                    String text=(String)(tokenizer.nextElement());
                    try{
                      coeffs[i]=Integer.parseInt(text);
                    }catch(NumberFormatException nfe){
                      throw new SAXException("Unknown coefficient format: " + text);
                    }

                }else{
                    throw new SAXException("Coefficient No. " + (i+1) + " not found");
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

    // write referenceFlag and spaceType
    currentChunkWriterUsed.writeBoolean(refFlag);
    currentChunkWriterUsed.writeByte(typeId, 4);

    if(typeId==LINEAR_MATRIX){      // write color transformation coefficients
      for(int i=0; i<coeffs.length; i++){
        currentChunkWriterUsed.writeInt(coeffs[i],16);
      }
    }
  }

  private int decodedSpaceType;

  public int getDecodedSpaceType(){
    return decodedSpaceType;
  }


  public int decode(BitToBitDataInputStream dis,Writer w)   {
    return decode(dis, w, DNS+COLOR_SPACE, null);
  }


  public int decode(BitToBitDataInputStream dis,Writer w, String name, String attrs)   {
    if (DEBUG) System.out.println("ColorSpace-decode");
    long before = dis.getReadedBits();
    try{
      String ns = getNameSpace(name);
      int refFlag=dis.readByte(1);
      decodedSpaceType=dis.readByte(4);

      w.write("\n<" + name );
      if(attrs != null){
        w.write(" " + attrs );
      }
      switch(decodedSpaceType){
        case RGB:
          w.write(" " + SPACE_TYPE + "=\"" + RGB_SPACE + "\" ");
          break;

        case YCbCr:
          w.write(" " + SPACE_TYPE + "=\"" + YCbCr_SPACE + "\" ");
          break;

        case HSV:
          w.write(" " + SPACE_TYPE + "=\"" + HSV_SPACE + "\" ");
          break;

        case HMMD:
          w.write(" " + SPACE_TYPE + "=\"" + HMMD_SPACE + "\" ");
          break;

        case LINEAR_MATRIX:
          w.write(" " + SPACE_TYPE + "=\"" + LINEAR_MATRIX_SPACE + "\" ");
          break;

        case MONOCHROME:
          w.write(" " + SPACE_TYPE + "=\"" + MONOCHROME_SPACE + "\" ");
          break;
      }

      w.write(" " + REFERENCE_FLAG + "=\"" + (refFlag==1 ? "true" : "false") + "\"");

      if(decodedSpaceType==LINEAR_MATRIX){
        w.write(">\n");
        w.write("  <" + ns+TRANS_MATRIX + ">");
        for(int i=0; i<9; i++){
          int coeff=dis.readInt(16);
          w.write(" " + coeff);
        }
        w.write("  </" + ns+TRANS_MATRIX + ">\n");
        w.write("</" + name + ">\n");
      }else{
        w.write("/>\n");
      }
    }catch (Exception e){
      System.out.println("ColorSpace: Unable to decode bitstream");
      e.printStackTrace();
    }

    return (int)(dis.getReadedBits()-before);
  }
}
