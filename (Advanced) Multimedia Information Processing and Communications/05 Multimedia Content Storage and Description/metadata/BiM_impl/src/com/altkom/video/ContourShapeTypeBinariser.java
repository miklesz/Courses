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
   An object of this class can binarise an element of the type ContourShape as of the
   ISO/IEC JTC1/SC29/WG11/M7476 (Part 3: Visual).
*/
public class ContourShapeTypeBinariser extends Binariser
{
   /* names of elements */
   private static final String GLOBAL    = "GlobalCurvature";
   private static final String PROTOTYPE = "PrototypeCurvature";
   private static final String HIGHEST   = "HighestPeakY";
   private static final String PEAK      = "Peak";
   private static final String P_X       = "peakX";
   private static final String P_Y       = "peakY";
   /* constants */
   private static final int NO_OF_CURV = 2;
   private static final int MAX_PEAKS  = 62;

   private int[] globalCurv, prototypeCurv, peakX, peakY;
   private int numOfPeaks;
   private int highest;

   private String currentElement;

   public void startElement(String nameSpace, String localName, String qualified, Attributes attrs)
            throws SAXException
   {
      super.startElement (nameSpace, localName, qualified, attrs);

      if (DEBUG) System.out.println ("ContourShape: startElement: "+qualified);

      if (isAtRootLevel())
      {
         globalCurv = new int[NO_OF_CURV];
         prototypeCurv = null;
         peakX = new int[MAX_PEAKS];
         peakY = new int[MAX_PEAKS];
         numOfPeaks = 0;
         currentElement = null;
      }
      else if (localName.equals(GLOBAL))
         currentElement = GLOBAL;
      else if (localName.equals(PROTOTYPE))
      {
         prototypeCurv = new int[NO_OF_CURV];
         currentElement = PROTOTYPE;
      }
      else if (localName.equals(HIGHEST))
         currentElement = HIGHEST;
      else if (localName.equals(PEAK))
      {
         numOfPeaks++;
         peakX[numOfPeaks-1] = Integer.parseInt(attrs.getValue(P_X));
         peakY[numOfPeaks-1] = Integer.parseInt(attrs.getValue(P_Y));
      }
   }

   public void characters(char[] chars, int index, int length)	throws SAXException
   {
      if (currentElement == GLOBAL)
      {
         StringTokenizer st = new StringTokenizer(new String(chars, index, length));
         for (int i = 0; i < globalCurv.length; i++)
            globalCurv[i] = Integer.parseInt(st.nextToken());
         currentElement = null;
      }
      else if (currentElement == PROTOTYPE)
      {
         StringTokenizer st = new StringTokenizer(new String(chars, index, length));
         for (int i = 0; i < prototypeCurv.length; i++)
            prototypeCurv[i] = Integer.parseInt(st.nextToken());
         currentElement = null;
      }
      else if (currentElement == HIGHEST)
      {
         highest = Integer.parseInt(new String(chars, index, length).trim());
         currentElement = null;
      }
   }

   public void endElement(String nameSpace, String localName, String qualified) throws SAXException
   {
      super.endElement(nameSpace, localName, qualified);

      if (isFinished())
         finish();
   }

   public void finish()
   {
      if(currentChunkWriterUsed == null)
         return;

      currentChunkWriterUsed.writeByte(numOfPeaks, 6);            //numberOfPeaks
      currentChunkWriterUsed.writeByte(globalCurv[0], 6);         //GlobalCurvature[0]
      currentChunkWriterUsed.writeByte(globalCurv[1], 6);         //GlobalCurvature[1]
      if (numOfPeaks != 0)
      {
         currentChunkWriterUsed.writeByte(prototypeCurv[0], 6);   //PrototypeCurvature[0]
         currentChunkWriterUsed.writeByte(prototypeCurv[1], 6);   //PrototypeCurvature[1]
      }
      currentChunkWriterUsed.writeByte(highest, 7);               //HighestPeakY
      for (int i = 0; i < numOfPeaks; i++)
      {
         currentChunkWriterUsed.writeByte(peakX[i], 6);           //peakX[i]
         currentChunkWriterUsed.writeByte(peakY[i], 3);           //peakY[i]
      }
   }

   public int decode(BitToBitDataInputStream dis, Writer w, String name, String attrs)
   {
      long before = dis.getReadedBits();
      try
      {
         String ns = getNameSpace(name);
         w.write("<"+name+((attrs != null) ? " "+attrs : "")+">\n");
         int nop = dis.readByte(6);                                                 //numberOfPeaks
         w.write("<"+ns+GLOBAL+">"+dis.readByte(6)+" "+dis.readByte(6)+         //GlobalCurvature
                     "</"+ns+GLOBAL+">\n");
         if (nop != 0)
            w.write("<"+ns+PROTOTYPE+">"+dis.readByte(6)+" "+dis.readByte(6)+   //PrototypeCurvature
                     "</"+ns+PROTOTYPE+">\n");
         w.write("<"+ns+HIGHEST+">"+dis.readByte(7)+"</"+ns+HIGHEST+">\n"); //HighestPeakY
         for (int i = 0; i < nop; i++)
            w.write("<"+ns+PEAK+" "+P_X+"=\""+dis.readByte(6)+"\" "+P_Y+"=\""+  //peakX[i]
                        dis.readByte(3)+"\"/>\n");                                  //peakY[i]
         w.write("</"+name+">\n");
      }
      catch (Exception x)
      {
         System.out.println("ContourShape: Unable to decode bitstream");
         x.printStackTrace();
      }

      return (int)(dis.getReadedBits()-before);
   }

   public int decode(BitToBitDataInputStream dis, Writer w)
   {
      return decode(dis, w, DNS+CONTOUR_SHAPE, null);
   }
}