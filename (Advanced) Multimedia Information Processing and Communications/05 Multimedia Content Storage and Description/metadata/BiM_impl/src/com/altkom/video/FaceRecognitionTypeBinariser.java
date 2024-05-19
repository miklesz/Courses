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
   An object of this class can binarise an element of the type FaceRecognitionType as of the
   ISO/IEC JTC1/SC29/WG11/M7476 (Part 3: Visual).
*/
public class FaceRecognitionTypeBinariser extends Binariser
{
   /* names of elements */
   private static String FEATURE = "Feature";
   /* constants */
   private static int NO_OF_FEATURES = 48;

   private int[] features;

   private String currentElement;

   public void startElement(String nameSpace, String localName, String qualified, Attributes attrs)
            throws SAXException
   {
      super.startElement (nameSpace, localName, qualified, attrs);

      if (DEBUG) System.out.println("FaceRecognition: startElement: "+qualified);

      if (isAtRootLevel())
      {
         features = new int[NO_OF_FEATURES];
         currentElement = null;
      }
      if (localName.equals(FEATURE))
         currentElement = FEATURE;
   }

   public void characters (char [] cdata, int index, int length) throws SAXException
   {
      if (currentElement == FEATURE)
      {
         currentElement = null;

         String data = new String(cdata, index, length);
         StringTokenizer strtok = new StringTokenizer(data);
         for (int i = 0; i < NO_OF_FEATURES; i++)
            features[i] = Integer.parseInt(strtok.nextToken());
      }
   }

   public void endElement (String nameSpace, String localName, String qualified)
            throws SAXException
   {
      super.endElement(nameSpace, localName, qualified);

      if (isFinished())
         finish();
   }

   public void finish()
   {
      if (currentChunkWriterUsed == null)
         return;

      if (DEBUG) System.out.println("FaceRecognition: finish");

      for (int i = 0; i < features.length; i++)
         currentChunkWriterUsed.writeByte(features[i], 5);  //Feature[i]
   }

   public int decode(BitToBitDataInputStream dis, Writer w, String name, String attrs)
   {
      long before = dis.getReadedBits();
      try
      {
         String ns = getNameSpace(name);
         w.write("<"+name+((attrs != null) ? " "+attrs : "")+">\n");
         w.write("<"+ns+FEATURE+">");
         for (int i = 0; i < NO_OF_FEATURES; i++)
            w.write(dis.readByte(5)+" ");                 //Feature[i]
         w.write("</"+ns+FEATURE+">\n");
         w.write("</"+name+">\n");
      }
      catch (Exception x)
      {
         System.out.println("FaceRecognition: Unable to decode bitstream");
         x.printStackTrace();
      }

      return (int)(dis.getReadedBits()-before);
   }

   public int decode(BitToBitDataInputStream dis, Writer w)
   {
      return decode(dis, w, DNS+FACE_RECOGNITION, null);
   }
}
