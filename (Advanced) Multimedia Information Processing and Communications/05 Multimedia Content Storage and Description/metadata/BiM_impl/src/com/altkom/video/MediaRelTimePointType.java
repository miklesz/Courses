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

import com.expway.tools.io.ChunkWriter;
import com.expway.tools.io.BitToBitDataInputStream;

import java.io.Writer;
import java.io.IOException;

public class MediaRelTimePointType extends MediaTimeOffsetType
{
   public static final String TIME_BASE = "mediaTimeBase";

   String timeBase;

   public MediaRelTimePointType()
   {
      super();
   }

   public MediaRelTimePointType(String content, String timeBase)
   {
      super(content);

      setTimeBase(timeBase);
   }

   public void setTimeBase(String timeBase)
   {
      this.timeBase = (timeBase != null) ? timeBase.trim() : null;
   }

   public String getTimeBase()
   {
      return timeBase;
   }

   public void writeInto(ChunkWriter cw) throws IOException
   {
      String tb = getTimeBase();
      cw.writeBoolean(tb != null);  //IsMediaTimeBase
      if (tb != null)
         cw.writeUTF(tb);           //mediaTimeBase
      super.writeInto(cw);          //mediaTimeOffset
   }

   public int decode(BitToBitDataInputStream dis, Writer w, String name, String attrs)
         throws IOException
   {
      long before = dis.getReadedBits();

      w.write("<"+name+((attrs != null) ? " "+attrs : ""));
      if (dis.readBoolean())                                //IsMediaTimeBase
         w.write(" "+TIME_BASE+"=\""+dis.readUTF()+"\"");   //mediaTimeBase
      w.write(">");
      readContentFrom(dis, w);                              //mediaTimeOffset
      w.write("</"+name+">\n");

      return (int)(dis.getReadedBits()-before);
   }
};