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

import com.expway.tools.io.ChunkWriter;
import com.expway.tools.io.BitToBitDataInputStream;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
   An object of this class can binarise an element of the type RegionShape as of the
   ISO/IEC JTC1/SC29/WG11/M7476 (Part 3: Visual).
*/
public class RegionShapeTypeBinariser extends Binariser
{
   /* names of elements */
   private static final String MAGNITUDE = "MagnitudeOfART";
   /* constants */
   private static final int NO_OF_ART = 35;

   private int[] mART;

   private String currentElement;

   public void startElement(String nameSpace, String localName, String qualified, Attributes attrs)
            throws SAXException
   {
      super.startElement (nameSpace, localName, qualified, attrs);

      if (DEBUG) System.out.println ("RegionShape: startElement: "+qualified);

      if (isAtRootLevel())
      {
         mART = null;
         currentElement = null;
      }
      else if (localName.equals(MAGNITUDE))
      {
         mART = new int[NO_OF_ART];
         currentElement = MAGNITUDE;
      }
   }

   public void characters(char[] chars, int index, int length)	throws SAXException
   {
      if (currentElement == MAGNITUDE)
      {
         StringTokenizer st = new StringTokenizer(new String(chars,index,length).trim());
         for (int i = 0; i < mART.length; i++)
            mART[i] = Integer.parseInt(st.nextToken());
         currentElement = null;
      }
   }

   public void endElement(String nameSpace, String localName, String qualified) throws SAXException
   {
      super.endElement (nameSpace, localName, qualified);


      if (isFinished())
         finish();
   }

   public void finish()
   {
      if(currentChunkWriterUsed == null)
         return;

      for (int i = 0; i < mART.length; i++)
         currentChunkWriterUsed.writeByte(mART[i], 4);  //MagnitudeOfART[i]
   }

   public int decode(BitToBitDataInputStream dis, Writer w, String name, String attrs)
   {
      long before = dis.getReadedBits();
      try
      {
         String ns = getNameSpace(name);
         w.write("<"+name+((attrs != null) ? " "+attrs : "")+">\n");
         w.write("<"+ns+MAGNITUDE+">");
         for (int i = 0; i < NO_OF_ART; i++)
            w.write(""+dis.readByte(4)+" ");    //MagnitudeOfART[i]
         w.write("</"+ns+MAGNITUDE+">\n");
         w.write("</"+name+">\n");
      }
      catch (Exception x)
      {
         System.out.println("RegionShape: Unable to decode bitstream");
         x.printStackTrace();
      }

      return (int)(dis.getReadedBits()-before);
   }

   public int decode(BitToBitDataInputStream dis, Writer w)
   {
      return decode(dis, w, DNS+REGION_SHAPE, null);
   }
}
