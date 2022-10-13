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
   An object of this class can binarise an element of the type MediaTimeType as of the
   ISO/IEC JTC1/SC29/WG11/M7476 (Part 5: Visual).
*/
public class MediaTimeTypeBinariser extends Binariser
{
   /* names of elements */
   private static final String MEDIA_TIME         = "MediaTime";
   private static final String M_TIME_PT          = "MediaTimePoint";
   private static final String M_REL_TIME_PT      = "MediaRelTimePoint";
   private static final String M_REL_INCR_TIME_PT = "MediaRelIncrTimePoint";
   private static final String M_DURATION         = "MediaDuration";
   private static final String M_INCR_DURATION    = "MediaIncrDuration";
   /* binary values */
   private static final int B_REL_INCR_TIME_PT = 0; //binary values of MediaTimePointSelect
   private static final int B_REL_TIME_PT      = 1;
   private static final int B_TIME_PT          = 2;
   private static final int B_NO_DURATION      = 0; //binary values of MediaDurationPointSelect
   private static final int B_DURATION         = 2;
   private static final int B_INCR_DURATION    = 3;
   /* constants */

   BasicMedia timePt;
   BasicMedia duration;

   private String currentElement;

   public void startElement(String nameSpace, String localName, String qualified, Attributes attrs)
            throws SAXException
   {
      super.startElement (nameSpace, localName, qualified, attrs);

      if (DEBUG) System.out.println("MediaTime: startElement: "+qualified);

      if (isAtRootLevel())
      {
         timePt = duration = null;
         currentElement = null;
      }
      else if (localName.equals(M_TIME_PT))
      {
         timePt = new MediaTimePointType();
         currentElement = M_TIME_PT;
      }
      else if (localName.equals(M_REL_TIME_PT))
      {
         MediaRelTimePointType mrtp = new MediaRelTimePointType();
         mrtp.setTimeBase(attrs.getValue(MediaRelTimePointType.TIME_BASE));
         timePt = mrtp;
         currentElement = M_REL_TIME_PT;
      }
      else if (localName.equals(M_REL_INCR_TIME_PT))
      {
         MediaRelIncrTimePointType mritp = new MediaRelIncrTimePointType();
         mritp.setTimeUnit(attrs.getValue(MediaRelIncrTimePointType.TIME_UNIT));
         mritp.setTimeBase(attrs.getValue(MediaRelIncrTimePointType.TIME_BASE));
         timePt = mritp;
         currentElement = M_REL_INCR_TIME_PT;
      }
      else if (localName.equals(M_DURATION))
      {
         duration = new MediaDurationType();
         currentElement = M_DURATION;
      }
      else if (localName.equals(M_INCR_DURATION))
      {
         MediaIncrDurationType mid = new MediaIncrDurationType();
         mid.setTimeUnit(attrs.getValue(MediaIncrDurationType.TIME_UNIT));
         duration = mid;
         currentElement = M_INCR_DURATION;
      }
   }

   public void characters (char [] cdata, int index, int length) throws SAXException
   {
      if (DEBUG) System.out.println("MediaTime: characters");

      if ((currentElement == M_TIME_PT) || (currentElement == M_REL_TIME_PT) ||
               (currentElement == M_REL_INCR_TIME_PT))
      {
         timePt.setContent(new String(cdata, index, length));
         currentElement = null;
      }
      else if ((currentElement == M_DURATION) || (currentElement == M_INCR_DURATION))
      {
         duration.setContent(new String(cdata, index, length));
         currentElement = null;
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

      try
      {
         if (DEBUG) System.out.println("MediaTime: finish");

         int select;
         if (timePt instanceof MediaRelIncrTimePointType)
            select = B_REL_INCR_TIME_PT;
         else if (timePt instanceof MediaRelTimePointType)
            select = B_REL_TIME_PT;
         else //if (timePt instanceof MediaTimePointType)
            select = B_TIME_PT;
         currentChunkWriterUsed.writeByte(select, 2);    //MediaTimePointSelect
         timePt.writeInto(currentChunkWriterUsed);       //MediaRelIncrTimePoint, MediaRelTimePoint or MediaTimePoint
         if (duration == null)
            select = B_NO_DURATION;
         else if (duration instanceof MediaDurationType)
            select = B_DURATION;
         else if (duration instanceof MediaIncrDurationType)
            select = B_INCR_DURATION;
         currentChunkWriterUsed.writeByte(select, 2);    //MediaDurationSelect
         if (select != B_NO_DURATION)
            duration.writeInto(currentChunkWriterUsed);  //MediaDuration or MediaIncrDuration
      }
      catch (Exception x)
      {
         System.out.println("MediaTime: can't finish descriptor.");
         x.printStackTrace();
      }
   }

   public int decode(BitToBitDataInputStream dis, Writer w, String name, String attrs)
   {
      long before = dis.getReadedBits();
      try
      {
         if (DEBUG) System.out.println("MediaTime: decode");

         String ns = getNameSpace(name);
         w.write("<"+name+((attrs != null) ? " "+attrs : "")+">\n");
         int select = dis.readByte(2);                                                          //MediaTimePointSelect
         if (DEBUG) System.out.println("MediaTime: decode: select: "+select);
         switch (select)
         {
            case B_REL_INCR_TIME_PT:
               if (DEBUG) System.out.println("MediaTime; decoding MediaRelIncrTimePoint");
               new MediaRelIncrTimePointType().decode(dis, w, ns+M_REL_INCR_TIME_PT, null); //MediaRelIncrTimePoint
               break;
            case B_REL_TIME_PT:
               if (DEBUG) System.out.println("MediaTime; decoding MediaRelTimePoint");
               new MediaRelTimePointType().decode(dis, w, ns+M_REL_TIME_PT, null);          //MediaRelTimePoint
               break;
            case B_TIME_PT:
               if (DEBUG) System.out.println("MediaTime; decoding MediaTimePoint");
               new MediaTimePointType().decode(dis, w, ns+M_TIME_PT, null);                 //MediaTimePoint
               break;
         }
         select = dis.readByte(2);                                                              //MediaDurationSelect
         if (DEBUG) System.out.println("MediaTime: decode: select: "+select);
         switch (select)
         {
            case B_DURATION:
               if (DEBUG) System.out.println("MediaTime; decoding MediaDuration");
               new MediaDurationType().decode(dis, w, ns+M_DURATION, null);                 //MediaTimePoint
               break;
            case B_INCR_DURATION:
               if (DEBUG) System.out.println("MediaTime; decoding MediaIncrDuration");
               new MediaIncrDurationType().decode(dis, w, ns+M_INCR_DURATION, null);        //MediaRelIncrTimePoint
               break;
         }
         w.write("</"+name+">\n");
      }
      catch (Exception x)
      {
         System.out.println("MediaTime: Unable to decode bitstream");
         x.printStackTrace();
      }

      if (DEBUG) System.out.println("MediaTime; decoded");

      return (int)(dis.getReadedBits()-before);
   }

   public int decode(BitToBitDataInputStream dis, Writer w)
   {
      return decode(dis, w, DNS+MEDIA_TIME, null);
   }
}
