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
   An object of this class can binarise an element of the type Shape3D as of the
   ISO/IEC JTC1/SC29/WG11/M7476 (Part 3: Visual).
*/
public class Shape3DTypeBinariser extends Binariser
{
   /* names of elements */
   private static final String BITS_BIN = "bitsPerBin";
   private static final String SPECTRUM = "Spectrum";
   private static final String PLANAR   = "PlanarSurfaces";
   private static final String SINGULAR = "SingularSurfaces";
   /* constants and defaults */
   private static final int DEF_BITS_BIN = 12;

   private int bitsPerBin;
   private int[] spectrum;
   private int planar, singular;

   private String currentElement;

   public void startElement(String nameSpace, String localName, String qualified, Attributes attrs)
            throws SAXException
   {
      super.startElement(nameSpace, localName, qualified, attrs);

      if (DEBUG) System.out.println("Shape3D: startElement: "+qualified);

      if (isAtRootLevel())
      {
         spectrum = null;
         String str = attrs.getValue(BITS_BIN);
         bitsPerBin = (str == null) ? DEF_BITS_BIN : Integer.parseInt(str);
         currentElement = null;
      }
      else if (localName.equals(SPECTRUM))
         currentElement = SPECTRUM;
      else if (localName.equals(PLANAR))
         currentElement = PLANAR;
      else if (localName.equals(SINGULAR))
         currentElement = SINGULAR;
   }

   public void characters(char[] chars, int index, int length)	throws SAXException
   {
      String str = new String(chars, index, length);

      if (DEBUG) System.out.println("Shape3D: chjaracters: "+str);

      if (currentElement == SPECTRUM)
      {
         StringTokenizer st = new StringTokenizer(str);
         spectrum = new int[st.countTokens()];
         for (int i = 0 ; i < spectrum.length; i++)
            spectrum[i] = Integer.parseInt(st.nextToken());
         currentElement = null;
      }
      else if (currentElement == PLANAR)
      {
         planar = Integer.parseInt(str.trim());
         currentElement = null;
      }
      else if (currentElement == SINGULAR)
      {
         singular = Integer.parseInt(str);
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

      currentChunkWriterUsed.writeInt(spectrum.length, 8);           //NumOfBins
      currentChunkWriterUsed.writeByte(bitsPerBin, 4);               //bitsPerBin
      for (int i = 0; i < spectrum.length; i++)
         currentChunkWriterUsed.writeInt(spectrum[i], bitsPerBin);   //Spectrum[i]
      currentChunkWriterUsed.writeInt(planar, bitsPerBin);           //PlanarSurfaces
      currentChunkWriterUsed.writeInt(singular, bitsPerBin);         //SingularSurfaces
   }

   public int decode(BitToBitDataInputStream dis, Writer w, String name, String attrs)
   {
      long before = dis.getReadedBits();
      try
      {
         String ns = getNameSpace(name);
         int nob = dis.readInt(8);                                                     //NumOfBins
         int bpb = dis.readByte(4);                                                    //bitsPerBin
         w.write("<"+name+((attrs != null) ? " "+attrs : "")+" "+BITS_BIN+"=\""+bpb+"\">\n");
         w.write("<"+ns+SPECTRUM+">");
         for (int i = 0; i < nob; i++)
            w.write(dis.readInt(bpb)+" ");                                             //Spectrum[i]
         w.write("</"+ns+SPECTRUM+">\n");
         w.write("<"+ns+PLANAR+">"+dis.readInt(bpb)+"</"+ns+PLANAR+">\n");     //PlanarSurfaces
         w.write("<"+ns+SINGULAR+">"+dis.readInt(bpb)+"</"+ns+SINGULAR+">\n"); //SingularSurfaces
         w.write("</"+name+">\n");
      }
      catch (Exception x)
      {
         System.out.println("Shape3D: Unable to decode bitstream");
         x.printStackTrace();
      }

      return (int)(dis.getReadedBits()-before);
   }

   public int decode(BitToBitDataInputStream dis, Writer w)
   {
      return decode(dis, w, DNS+SHAPE_3D, null);
   }
}
