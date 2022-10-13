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
   An object of this class can binarise an element of the type GoFGoPColorType as of the
   ISO/IEC JTC1/SC29/WG11/M7476 (Part 3: Visual).
*/

public class GoFGoPColorTypeBinariser extends Binariser {
  // elements/attributes strings
  private final String AGGREGATION_ATTR="aggregation";
  private final String AVERAGE_TXT="Average";
  private final String MEDIAN_TXT="Median";
  private final String INTERSECTION_TXT="Intersection";
  private final String S_COLOR="ScalableColor";

  // aggregation identifiers - table 24 in N7476
  private final int AVERAGE=0;
  private final int MEDIAN=1;
  private final int INTERSECTION=2;

  // binariser state identifier
  private final int GO_F_GO_P=0;
  private final int SCALABLE=1;
  private final int SKIP=-1;

  // local variables
  private int state;
  private int typeId;
  private Binariser scalableColorBinariser = null;

  public void startElement (String nameSpace, String localName, String qualified, Attributes attrs) throws SAXException {
    super.startElement (nameSpace, localName, qualified, attrs);

    if(isAtRootLevel()){
      state=GO_F_GO_P;
      scalableColorBinariser=null;

      String type=attrs.getValue(AGGREGATION_ATTR);
      if(type==null){
        throw new SAXException("GoFGopColor - aggregation not found");
      }

      if(type.equals(AVERAGE_TXT)){
        typeId=AVERAGE;
      }else{
        if(type.equals(MEDIAN_TXT)){
          typeId=MEDIAN;
        }else{
          if(type.equals(INTERSECTION_TXT)){
            typeId=INTERSECTION;
          }
        }
      }
    }else{
      if(localName.equals(S_COLOR)){
         state = SCALABLE;
        scalableColorBinariser=new ScalableColorTypeBinariser(); //BinarisationConfig.getBinariserForType ("ScalableColorType");
        scalableColorBinariser.takeDescriptor(null);    // do not write anything to the binary stream
      }
    }

    //if(scalableColorBinariser!=null){
    //  scalableColorBinariser.startElement(nameSpace, localName, qualified, attrs);
    //}
    if (state == SCALABLE)
      scalableColorBinariser.startElement(nameSpace, localName, qualified, attrs);
  }


  public void characters (char [] chars, int index, int length) throws SAXException {
    if(scalableColorBinariser!=null){
      scalableColorBinariser.characters(chars, index, length);
    }
  }


  public void endElement (String nameSpace, String localName, String qualified) throws SAXException {
    super.endElement (nameSpace, localName, qualified);

      if(state == SCALABLE){
        scalableColorBinariser.endElement(nameSpace, localName, qualified);
        if (scalableColorBinariser.isFinished())
         state = SKIP;
      }

    if(isFinished()){
      finish();
    }
  }


  public void finish(){
    if((currentChunkWriterUsed == null)){
      return;
    }

    // write aggregaton type
    currentChunkWriterUsed.writeByte(typeId,2);

    // write ScalableColor descriptor
    if(scalableColorBinariser != null){
      scalableColorBinariser.takeDescriptor(currentChunkWriterUsed);
      scalableColorBinariser.finish();
    }
  }


  public int decode(BitToBitDataInputStream dis,Writer w)   {
    return decode(dis, w, DNS+GOF_GOP_COLOR, null);
  }


  public int decode(BitToBitDataInputStream dis,Writer w, String name, String attrs)   {
    if (DEBUG) System.out.println("GoFGoPColor-decode");
    long before = dis.getReadedBits();

    try{
      String ns = getNameSpace(name);
      // read aggregation type
      int aggregType=dis.readByte(2);
      String aggregTypeTable[]={AVERAGE_TXT, MEDIAN_TXT, INTERSECTION_TXT};

      w.write("\n<" + name );
      if(attrs != null){
        w.write(" " + attrs );
      }
      w.write(" " + AGGREGATION_ATTR + "=\"" + aggregTypeTable[aggregType]   + "\">\n");

      // decode ScalableColor element
      (new ScalableColorTypeBinariser()).decode(dis, w, ns+S_COLOR, null);

      w.write("</" + name + ">\n" );
    }catch (Exception e){
      System.out.println("GoFGoPColor: Unable to decode bitstream");
      e.printStackTrace();
    }

    return (int)(dis.getReadedBits()-before);
  }
}
