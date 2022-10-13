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
import java.util.ArrayList;

import com.expway.tools.io.ChunkWriter;
import com.expway.tools.io.BitToBitDataInputStream;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
   An object of this class can binarise an element of the type CameraMotionType as of the
   ISO/IEC JTC1/SC29/WG11/M7476 (Part 3: Visual).
*/
public class CameraMotionTypeBinariser extends Binariser
{
   /* names of elements */
   private static final String SEGMENT = "Segment";

   private ArrayList segments;

   private String currentElement;
   private Binariser currentSegment;

   public void startElement(String nameSpace, String localName, String qualified, Attributes attrs)
            throws SAXException
   {
      super.startElement(nameSpace, localName, qualified, attrs);
      if (DEBUG) System.out.println("CameraMotion: startElement: "+qualified);

      if (isAtRootLevel())
      {
         segments = new ArrayList();
         currentElement = null;
      }
      else if (localName.equals(SEGMENT))
      {
         currentElement = SEGMENT;
         currentSegment = new CameraMotionSegmentTypeBinariser();
         currentSegment.takeDescriptor(null);
         currentSegment.startElement(nameSpace, localName, qualified, attrs);
         segments.add(currentSegment);
      }
      else if (currentElement == SEGMENT)
         currentSegment.startElement(nameSpace, localName, qualified, attrs);
   }

   public void characters(char[] cdata, int index, int length) throws SAXException
   {
      if (currentElement == SEGMENT)
         currentSegment.characters(cdata, index, length);
   }

   public void endElement(String nameSpace, String localName, String qualified)
            throws SAXException
   {
      super.endElement(nameSpace, localName, qualified);

      if (currentElement == SEGMENT)
      {
         currentSegment.endElement(nameSpace, localName, qualified);
         if (currentSegment.isFinished())
            currentElement = null;
      }

      if (isFinished())
         finish();
   }

   public void finish()
   {
      if (currentChunkWriterUsed == null)
         return;

      int nos = segments.size();
      currentChunkWriterUsed.writeInfiniteLong(nos, 4);  //LoopSegments
      for (int i = 0; i < nos; i++)
      {
         Binariser bin = (Binariser)segments.get(i);
         bin.takeDescriptor(currentChunkWriterUsed);
         bin.finish();
      }
   }

   public int decode(BitToBitDataInputStream dis, Writer w, String name, String attrs)
   {
      long before = dis.getReadedBits();
      try
      {
         String ns = getNameSpace(name);
         Binariser cms = new CameraMotionSegmentTypeBinariser();

         w.write("<"+name+((attrs != null) ? " "+attrs : "")+">\n");
         int nos = (int)dis.readInfiniteLong(4); //LoopSegments; writing int, so it is O.K.
         for (int i = 0; i < nos; i++)
            cms.decode(dis, w, ns+SEGMENT, null);
         w.write("</"+name+">\n");
      }
      catch (Exception x)
      {
         System.out.println("CameraMotion: Unable to decode bitstream");
         x.printStackTrace();
      }

      return (int)(dis.getReadedBits()-before);
   }

   public int decode(BitToBitDataInputStream dis, Writer w)
   {
      return decode(dis, w, DNS+CAMERA_MOTION, null);
   }
}

class CameraMotionSegmentTypeBinariser extends Binariser
{
   /* names of elements */
   private static final String TIME        = "MediaTime";
   private static final String FOCUS       = "FocusOfExpansion";
   private static final String HORZ_POS    = "HorizontalPosition";
   private static final String VERT_POS    = "VerticalPosition";
   private static final String FRACTIONAL  = "FractionalPresence";
   private static final String AMOUNT      = "AmountOfMotion";
   private static final String TRACK_LEFT  = "TrackLeft";
   private static final String TRACK_RIGHT = "TrackRight";
   private static final String BOOM_DOWN   = "BoomDown";
   private static final String BOOM_UP     = "BoomUp";
   private static final String DOLLY_FWD   = "DollyForward";
   private static final String DOLLY_BACK  = "DollyBackward";
   private static final String PAN_LEFT    = "PanLeft";
   private static final String PAN_RIGHT   = "PanRight";
   private static final String TILT_DOWN   = "TiltDown";
   private static final String TILT_UP     = "TiltUp";
   private static final String ROLL_CLOCK  = "RollClockwise";
   private static final String ROLL_ANTI   = "RollAnticlockwise";
   private static final String ZOOM_IN     = "ZoomIn";
   private static final String ZOOM_OUT    = "ZoomOut";
   private static final String FIXED       = "Fixed";
   /* binary values */
   private static final String[] BAMT = { //values of amount of motion: index - binary value, BAMT[index] - XML value (string)
      TRACK_LEFT, TRACK_RIGHT, BOOM_DOWN, BOOM_UP, DOLLY_FWD, DOLLY_BACK, PAN_LEFT, PAN_RIGHT,
      TILT_DOWN, TILT_UP, ROLL_CLOCK, ROLL_ANTI, ZOOM_IN, ZOOM_OUT, FIXED
   };
   /* constants */
   private static final int NOT_PRESENT = -1;

   MediaTimeTypeBinariser mediaTime;
   private boolean foeFlag;
   private float horzPos, vertPos;
   //values in FRACTIONAL
   private boolean fractFlag; //for distinguish between mixture and non-mixture mode
   private boolean inFractional; //for distinguish between FRACTIONAL and AMOUNT elements -> they have the same names
   int[] fp;
   //values in AMOUNT
   int[] am;

   private String currentElement;

   public void startElement(String nameSpace, String localName, String qualified, Attributes attrs)
            throws SAXException
   {
      super.startElement(nameSpace, localName, qualified, attrs);
      if (DEBUG) System.out.println("CameraMotionSegmentTypeBinariser: startElement: "+qualified);

      if (isAtRootLevel())
      {
         mediaTime = null;
         foeFlag = fractFlag = false;
         fp = new int[BAMT.length];
         for (int i = 0; i < fp.length; i++)
            fp[i] = NOT_PRESENT; //don't want to create so many boolean flags
         am = new int[fp.length];
         for (int i = 0; i < am.length; i++)
            am[i] = NOT_PRESENT; //don't want to create so many boolean flags
         currentElement = null;
      }
      else if (localName.equals(TIME))
      {
         mediaTime = new MediaTimeTypeBinariser();
         mediaTime.takeDescriptor(null);
         mediaTime.startElement(nameSpace, localName, qualified, attrs);
         currentElement = TIME;
      }
      else if (localName.equals(FOCUS))
         foeFlag = true;
      else if (localName.equals(HORZ_POS))
         currentElement = HORZ_POS;
      else if (localName.equals(VERT_POS))
         currentElement = VERT_POS;
      else if (localName.equals(FRACTIONAL))
         fractFlag = inFractional = true;
      else if (localName.equals(AMOUNT))
         inFractional = false;
      else if (currentElement == TIME)
         mediaTime.startElement(nameSpace, localName, qualified, attrs);
      else
         for (int i = 0; i < BAMT.length; i++)
            if (localName.equals(BAMT[i]))
            {
               currentElement = BAMT[i];
               if (!inFractional && (currentElement == FIXED))
                  am[i] = 0; //FIXED in AMOUNT is empty, characters may not be called, '0' means that element was present (other than NOT_PRESENT)
               break;
            }
   }

   public void characters(char[] cdata, int index, int length) throws SAXException
   {
      String str = new String(cdata, index, length).trim();

      if (currentElement == TIME)
         mediaTime.characters(cdata, index, length);
      else if (currentElement == HORZ_POS)
      {
         horzPos = Float.parseFloat(str);
         currentElement = null;
      }
      else if (currentElement == VERT_POS)
      {
         vertPos = Float.parseFloat(str);
         currentElement = null;
      }
      else
         for (int i = 0; i < BAMT.length; i++)
            if (currentElement == BAMT[i])
            {
               if (inFractional)
                  fp[i] = Integer.parseInt(str);
               else
                  am[i] = (currentElement == FIXED) ? 0 : Integer.parseInt(str); //for FIXED '0' means that element was present (other than NOT_PRESENT)
            }
   }

   public void endElement(String nameSpace, String localName, String qualified) throws SAXException
   {
      super.endElement(nameSpace, localName, qualified);

      if (currentElement == TIME)
      {
         mediaTime.endElement(nameSpace, localName, qualified);
         if (mediaTime.isFinished())
            currentElement = null;
      }

      if (isFinished())
         finish();
   }

   public void finish()
   {
      if (currentChunkWriterUsed == null)
         return;

      currentChunkWriterUsed.writeBoolean(!fractFlag);                  //XsiTypeID; in mixture mode (with FRACTIONAL) must write 0
      mediaTime.takeDescriptor(currentChunkWriterUsed);
      mediaTime.finish();                                               //MediaTime
      currentChunkWriterUsed.writeBoolean(foeFlag);                     //FlagFOE
      if (foeFlag)
      {
         currentChunkWriterUsed.writeFloat(horzPos);                    //HorizontalPosition
         currentChunkWriterUsed.writeFloat(vertPos);                    //VerticalPosition
      }
      if (fractFlag)
      {
         for (int i = 0; i < fp.length; i++)
         {
            currentChunkWriterUsed.writeBoolean (fp[i] != NOT_PRESENT); //FlagFP_TL ... FlagFP_FI
            if (fp[i] != NOT_PRESENT)
               currentChunkWriterUsed.writeByte(fp[i], 7);              //TrackLeft ... Fixed
         }
         for (int i = 0; i < am.length-1; i++) //am.length-1 because FIXED is not saved in AM
         {
            currentChunkWriterUsed.writeBoolean(am[i] != NOT_PRESENT);  //FlagAM_TL ... FalgAM_ZO
            if (am[i] != NOT_PRESENT)
               currentChunkWriterUsed.writeInt(am[i], 11);              //TrackLeft ... ZoomOut
         }
      } //if fractFlag
      else
      {
         for (int i = 0; i < am.length; i++)
            if (am[i] != NOT_PRESENT)
            {
               currentChunkWriterUsed.writeByte(i, 4);                  //FlagAM_Type
               if (BAMT[i] != FIXED)
                  currentChunkWriterUsed.writeInt(am[i], 11);           //TrackLeft ... ZoomOut
            }
      } //else fractFlag
   }

   public int decode(BitToBitDataInputStream dis, Writer w, String name, String attrs)
   {
      long before = dis.getReadedBits();
      try
      {
         String ns = getNameSpace(name);
         w.write("<"+name+((attrs != null) ? " "+attrs : "")+">\n");
         boolean mix = !dis.readBoolean();                                                         //XsiTypeID; in mixture mode '0' is written
         new MediaTimeTypeBinariser().decode(dis, w, ns+TIME, null);                           //MediaTime
         boolean foe = dis.readBoolean();                                                          //FlagFOE
         if (foe)
         {
            w.write("<"+ns+FOCUS+">\n");
            w.write("<"+ns+HORZ_POS+">"+dis.readFloat()+"</"+ns+HORZ_POS+">\n");           //HorizontalPosition
            w.write("<"+ns+VERT_POS+">"+dis.readFloat()+"</"+ns+VERT_POS+">\n");           //VerticalPosition
            w.write("</"+ns+FOCUS+">\n");
         }
         if (mix)
         {
            w.write("<"+ns+FRACTIONAL+">\n");
            for (int i = 0; i < BAMT.length; i++)
            {
               if (dis.readBoolean())
                  w.write("<"+ns+BAMT[i]+">"+dis.readByte(7)+"</"+ns+BAMT[i]+">\n");       //FlagFP_TL ... FlagFP_FI
            }
            w.write("</"+ns+FRACTIONAL+">\n");
            w.write("<"+ns+AMOUNT+">\n");
            for (int i = 0; i < BAMT.length-1; i++) //BAMT.length-1 because FIXED isn't stored
            {
               if (dis.readBoolean())                                                              //FlagAM_TL ... FlagAM_ZO
                  w.write("<"+ns+BAMT[i]+">"+dis.readInt(11)+"</"+ns+BAMT[i]+">\n");       //TrackLeft ... ZoomOut
            }
            w.write("</"+ns+AMOUNT+">\n");
         }//if mix
         else //non-mixture mode
         {
            w.write("<"+ns+AMOUNT+">\n");
            int amtype = dis.readByte(4);                                                          //FlagAM_TYPE
            if (BAMT[amtype] != FIXED)
               w.write("<"+ns+BAMT[amtype]+">"+dis.readInt(11)+"</"+ns+BAMT[amtype]+">\n");//TrackLeft ... ZoomOut
            else //BAMT[amtype] == FIXED
               w.write("<"+ns+FIXED+"/>\n");
            w.write("</"+ns+AMOUNT+">\n");
         }//else mix
         w.write("</"+name+">\n");
      }
      catch (Exception x)
      {
         System.out.println("CameraMotionSegment: Unable to decode bitstream");
         x.printStackTrace();
      }

      return (int)(dis.getReadedBits()-before);
   }

   public int decode(BitToBitDataInputStream dis, Writer w)
   {
      return decode(dis, w, DNS+"Segment", null); //this descriptor is used only in CameraMotion, so its name isnt't in DESCRIPTOR_NAMES table
   }
}