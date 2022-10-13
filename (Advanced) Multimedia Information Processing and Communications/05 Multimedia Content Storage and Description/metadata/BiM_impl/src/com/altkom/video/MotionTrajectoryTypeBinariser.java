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

import com.expway.tools.io.ChunkWriter;
import com.expway.tools.io.BitToBitDataInputStream;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
   An object of this class can binarise an element of the type MotionTrajectoryType as of the
   ISO/IEC JTC1/SC29/WG11/M7476 (Part 3: Visual).
*/
public class MotionTrajectoryTypeBinariser extends Binariser
{
   /* names of elements */
   private static final String CAM_FOLLOWS = "cameraFollows";
   private static final String COORD_REF   = "CoordRef";
   private static final String REF         = "ref";
   private static final String SPAT_REF    = "spatialRef";
   private static final String COORD_DEF   = "CoordDef";
   private static final String UNITS       = "units";
   private static final String HEIGHT      = "pictureHeight";
   private static final String WIDTH       = "pictureWidth";
   private static final String WID_HEI     = "pictureWidthAndHeight";
   private static final String METER       = "meter";
   private static final String REPR        = "Repr";
   private static final String R_X         = "x";
   private static final String R_Y         = "y";
   private static final String PARAMS      = "Params";
   /* binary values */
   private static final int CF_NOT_SPECIFIED = 0;  //values of cameraFollows in binary format
   private static final int CF_NO            = 2;
   private static final int CF_YES           = 3;
   private static final int U_WID_HEI = 0; //values of units in binary format
   private static final int U_HEIGHT  = 1;
   private static final int U_WIDTH   = 2;
   private static final int U_METER   = 3;

   private boolean coordRefFlag, reprFlag, spatRefFlag;
   private String ref;
   int cameraFollows, units, reprX, reprY, reprZ;
   Binariser tempInterpBinariser;

   private String currentElement;

   public void startElement(String nameSpace, String localName, String qualified, Attributes attrs)
            throws SAXException
   {
      super.startElement(nameSpace, localName, qualified, attrs);

      if (DEBUG) System.out.println("MotionTrajectory: startElement: "+qualified);

      if (isAtRootLevel())
      {
         reprFlag = false;
         String cf = attrs.getValue(CAM_FOLLOWS);
         cameraFollows = (cf == null) ? CF_NOT_SPECIFIED : (cf.equals("false") ? CF_NO : CF_YES);
         currentElement = null;
      }
      else if (localName.equals(COORD_REF))
      {
         coordRefFlag = true;
         ref = attrs.getValue(REF);
         spatRefFlag = attrs.getValue(SPAT_REF).equals("true");
      }
      else if (localName.equals(COORD_DEF))
      {
         coordRefFlag = false;
         String u = attrs.getValue(UNITS);
         if (u.equals(WID_HEI))
            units = U_WID_HEI;
         else if (u.equals(HEIGHT))
            units = U_HEIGHT;
         else if (u.equals(WIDTH))
            units = U_WIDTH;
         else //if (u.equals(METER))
            units = U_METER;
      }
      else if (localName.equals(REPR))
      {
         reprFlag = true;
         reprX = Integer.parseInt(attrs.getValue(R_X).trim());
         reprY = Integer.parseInt(attrs.getValue(R_Y).trim());
      }
      else if (localName.equals(PARAMS))
      {
         currentElement = PARAMS;
         tempInterpBinariser = new TemporalInterpolationTypeBinariser();
         tempInterpBinariser.takeDescriptor(null);
         tempInterpBinariser.startElement(nameSpace, localName, qualified, attrs);
      }
      else if (currentElement == PARAMS)
         tempInterpBinariser.startElement(nameSpace, localName, qualified, attrs);
   }

   public void characters(char[] cdata, int index, int length) throws SAXException
   {
      if (currentElement == PARAMS)
         tempInterpBinariser.characters(cdata, index, length);
   }

   public void endElement (String nameSpace, String localName, String qualified)
            throws SAXException
   {
      super.endElement(nameSpace, localName, qualified);

      if (currentElement == PARAMS)
      {
         tempInterpBinariser.endElement(nameSpace, localName, qualified);
         if (tempInterpBinariser.isFinished())
            currentElement = null;
      }

      if (isFinished())
         finish();
   }

   public void finish()
   {
      if (currentChunkWriterUsed == null)
         return;

      try
      {
         currentChunkWriterUsed.writeByte(cameraFollows, 2);   //cameraFollows
         currentChunkWriterUsed.writeBoolean(coordRefFlag);    //CoordFlag
         if (coordRefFlag)
         {
            currentChunkWriterUsed.writeUTF(ref);              //ref
            currentChunkWriterUsed.writeBoolean(spatRefFlag);  //spatialRef
         }
         else
         {
            currentChunkWriterUsed.writeByte(units, 2);        //units
            currentChunkWriterUsed.writeBoolean(reprFlag);     //CoordCodingLength
            if (reprFlag)
            {
               currentChunkWriterUsed.writeByte(reprX);        //xRepr
               currentChunkWriterUsed.writeByte(reprY);        //yRepr
            }
         }
         tempInterpBinariser.takeDescriptor(currentChunkWriterUsed);
         //if (reprFlag)
         //   tempInterpBinariser.enableQuantization(reprX, reprY, units, xsize, ysize);
         tempInterpBinariser.finish();                         //Params
      }
      catch (java.io.UTFDataFormatException x)
      {
         System.out.println("MotionTrajectory: Can't binarise descriptor.");
         x.printStackTrace();
      }
   }

   public int decode(BitToBitDataInputStream dis, Writer w, String name, String attrs)
   {
      long before = dis.getReadedBits();
      try
      {
         String ns = getNameSpace(name);
         Binariser bin = new TemporalInterpolationTypeBinariser();

         w.write("<"+name+((attrs != null) ? " "+attrs : ""));
         int cf = dis.readByte(2);                                                  //cameraFollows
         if (cf != CF_NOT_SPECIFIED)
            w.write(" "+CAM_FOLLOWS+"=\""+((cf == CF_YES) ? "true" : "false")+"\"");
         w.write(">\n");
         if (dis.readBoolean())                                                     //CoordFlag
         {
            String ref = dis.readUTF();
            w.write("<"+ns+COORD_REF+" "+REF+"=\""+ref+"\" "+SPAT_REF+"=\""+    //ref
               (dis.readBoolean() ? "true" : "false")+"\"/>\n");                    //spatioalRef
         }
         else
         {
            int u = dis.readByte(2);                                                //units
            String us = "";
            switch (u)
            {
               case U_WID_HEI: us = WID_HEI; break;
               case U_WIDTH:   us = WIDTH;   break;
               case U_HEIGHT:  us = HEIGHT;  break;
               case U_METER:   us = METER;   break;
            }
            w.write("<"+ns+COORD_DEF+" " +UNITS+"=\""+us+"\">\n");
            if (dis.readBoolean())                                                  //CoordCodingLength
            {
               int rx = dis.readInt(8);                                             //xRepr
               int ry = dis.readInt(8);                                             //yRepr
               //if (u == U_WID_HEI)
			      //   bin.enableQuantization(rx, ry, u, xsize, ysize);
               w.write("<"+ns+REPR+" "+R_X+"=\""+rx+"\" "+R_Y+"=\""+ry+"\"/>\n");
            }
            w.write("</"+ns+COORD_DEF+">\n");
         }
         bin.decode(dis, w, ns+PARAMS, null);                                   //Params
         w.write("</"+name+">\n");
      }
      catch (Exception e)
      {
         System.out.println("MotionTrajectory: Unable to decode bitstream");
         e.printStackTrace();
      }

      return (int)(dis.getReadedBits()-before);
   }

   public int decode(BitToBitDataInputStream dis, Writer w)
   {
      return decode(dis, w, DNS+MOTION_TRAJECTORY, null);
   }
}