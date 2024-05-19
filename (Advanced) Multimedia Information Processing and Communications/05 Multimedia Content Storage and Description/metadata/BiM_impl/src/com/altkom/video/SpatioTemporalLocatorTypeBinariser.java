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
import java.io.StringWriter;
import java.io.UTFDataFormatException;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.expway.tools.io.ChunkWriter;
import com.expway.tools.io.BitToBitDataInputStream;

/**
   An object of this class can binarise an element of the type SpatioTemporalLocatorType as of the
   ISO/IEC JTC1/SC29/WG11/M7476 (Part 3: Visual).
*/
public class SpatioTemporalLocatorTypeBinariser extends Binariser
{
   /* names of elements */
   private static final String COORD_REF = "CoordRef";
   private static final String REF       = "ref";
   private static final String SPAT_REF  = "spatialRef";
   private static final String FIGURE    = "FigureTrajectory";
   private static final String PARAMETER = "ParameterTrajectory";
   private static final String MEDIA     = "MediaTime";
   /* binary values */
   private static final int FIGURE_TYPE    = 0; //values of TypeOfTrajectory in binary format
   private static final int PARAMETER_TYPE = 1;
   private static final int MEDIA_TYPE     = 2;

   boolean coordFlag, spatialRef;
   String ref;
   private ArrayList trajectories;

   private String currentElement;
   private Binariser currentTraj;

   public void startElement(String nameSpace, String localName, String qualified, Attributes attrs)
            throws SAXException
   {
      super.startElement(nameSpace, localName, qualified, attrs);
      if (DEBUG) System.out.println("SpatioTemporalLocator: startElement: "+localName);

      if (isAtRootLevel())
      {
         trajectories = new ArrayList();
         coordFlag = false;
         currentElement = null;
      }
      else if (localName.equals(COORD_REF))
      {
         coordFlag = true;
         ref = attrs.getValue(REF).trim();
         spatialRef = Boolean.valueOf(attrs.getValue(SPAT_REF)).booleanValue();
         currentElement = null;
      }
      else if (localName.equals(FIGURE))
      {
         currentElement = FIGURE;
         currentTraj = new FigureTrajectoryTypeBinariser();
         currentTraj.takeDescriptor(null);
         currentTraj.startElement(nameSpace, localName, qualified, attrs);
         trajectories.add(currentTraj);
      }
      else if (localName.equals(PARAMETER))
      {
         currentElement = PARAMETER;
         currentTraj = new ParameterTrajectoryTypeBinariser();
         currentTraj.takeDescriptor(null);
         currentTraj.startElement(nameSpace, localName, qualified, attrs);
         trajectories.add(currentTraj);
      }
      else if (localName.equals(MEDIA) &&
                  (currentElement != FIGURE) && (currentElement != PARAMETER)) //this is needed, because element MediaTime exists in SpatioTemporalLocator, but also in FigureTrajectory and ParameterTrajectory
      {
         currentElement = MEDIA;
         currentTraj = new MediaTimeTypeBinariser();
         currentTraj.takeDescriptor(null);
         currentTraj.startElement(nameSpace, localName, qualified, attrs);
         trajectories.add(currentTraj);
      }
      else if ((currentElement == FIGURE) || (currentElement == PARAMETER) ||
                  (currentElement == MEDIA))
         currentTraj.startElement(nameSpace, localName, qualified, attrs);
   }

   public void characters(char[] cdata, int index, int length) throws SAXException
   {
      if ((currentElement == FIGURE) || (currentElement == PARAMETER) || (currentElement == MEDIA))
         currentTraj.characters(cdata, index, length);
   }

   public void endElement(String nameSpace, String localName, String qualified)
            throws SAXException
   {
      super.endElement(nameSpace, localName, qualified);

      if ((currentElement == FIGURE) || (currentElement == PARAMETER) || (currentElement == MEDIA))
      {
         currentTraj.endElement(nameSpace, localName, qualified);
         if (currentTraj.isFinished())
            currentElement = null;
      }

      if (isFinished())
         finish();
   }

   public void finish()
   {
      if (currentChunkWriterUsed == null)
         return;

      if (DEBUG) System.out.println("SpatioTemporalLocator: finish");
      try
      {
         currentChunkWriterUsed.writeBoolean(coordFlag);             //CoordFlag
         if (coordFlag)
         {
            currentChunkWriterUsed.writeUTF(ref);                    //ref
            currentChunkWriterUsed.writeBoolean(spatialRef);         //spatialRef
         }
         int not = trajectories.size();
         currentChunkWriterUsed.writeInfiniteLong(not, 4);           //NumOfRefRegions
         for (int i = 0; i < not; i++)
         {
            Binariser bin = (Binariser)trajectories.get(i);
            int type;
            if (bin instanceof FigureTrajectoryTypeBinariser)
               type = FIGURE_TYPE;
            else if (bin instanceof ParameterTrajectoryTypeBinariser)
               type = PARAMETER_TYPE;
            else //if (bin instanceof MediaTimeTypeBinariser)
               type = MEDIA_TYPE;
            currentChunkWriterUsed.writeByte(type, 2);               //TypeOfTrajectory
            bin.takeDescriptor(currentChunkWriterUsed);
            bin.finish();                                            //FigureTrajectory, ParameterTrajectory
         }
      }
      catch (java.io.UTFDataFormatException x)
      {
         System.out.println("SpatioTemporalLocator: can't finish descriptor.");
         x.printStackTrace();
      }
   }

   public int decode(BitToBitDataInputStream dis, Writer w, String name, String attrs)
   {
      long before = dis.getReadedBits();
      try
      {
         String ns = getNameSpace(name);
         if (DEBUG) System.out.println("SpatioTemporalLocator: decode");
         w.write("<"+name+((attrs != null) ? " "+attrs : "")+">\n");
         if (dis.readBoolean())                                                           //CoordFlag
            w.write("<"+ns+COORD_REF+" "+REF+"=\""+dis.readUTF()+"\" "+               //ref
                     SPAT_REF+"=\""+(dis.readBoolean() ? "true" : "false")+"\"/>\n");     //spatialRef
         int not = (int)dis.readInfiniteLong(4);                                          //NumOfRefRegions
         Binariser ft = null, pt = null, mt = null;
         for (int i = 0; i < not; i++)
         {
            int type = dis.readByte(2);                                                   //TypeOfTrajectory
            if (type == FIGURE_TYPE)
            {
               if (ft == null)
                  ft = new FigureTrajectoryTypeBinariser();
               ft.decode(dis, w, ns+FIGURE, null);                                    //FigureTrajectory
            }
            else if (type == PARAMETER_TYPE)
            {
               if (pt == null)
                  pt = new ParameterTrajectoryTypeBinariser();
               pt.decode(dis, w, ns+PARAMETER, null);                                 //ParameterTrajectory
            }
            else //if (type == MEDIA)
            {
               if (mt == null)
                  mt = new MediaTimeTypeBinariser();
               mt.decode(dis, w, ns+MEDIA, null);                                     //MediaTime
            }
         }
         w.write("</"+name+">\n");
      }
      catch (Exception x)
      {
         System.out.println("SpatioTemporalLocator: Unable to decode bitstream");
         x.printStackTrace();
      }

      return (int)(dis.getReadedBits()-before);
   }

   public int decode(BitToBitDataInputStream dis, Writer w)
   {
      return decode(dis, w, DNS+SPATIO_TEMPORAL_LOCATOR, null);
   }
}

class FigureTrajectoryTypeBinariser extends Binariser
{
   /* names of elements */
   private static final String TYPE        = "type";
   private static final String T_RECT      = "rectangle";
   private static final String T_ELLIPSE   = "ellipse";
   private static final String T_POLYGON   = "polygon";
   private static final String MEDIA       = "MediaTime";
   private static final String VERTEX      = "Vertex";
   private static final String DEPTH       = "Depth";
   /* binary values */
   private static final int B_RECT    = 1;   //binary values of type
   private static final int B_ELLIPSE = 2;
   private static final int B_POLYGON = 3;
   /* constants */
   private static final int MAX_VERTICES = 63;

   Binariser mediaTime, depth;
   int type, noOfVertices;
   Binariser[] vertices;

   private Binariser currentVertex;
   private String currentElement;

   public void startElement(String nameSpace, String localName, String qualified, Attributes attrs)
            throws SAXException
   {
      super.startElement(nameSpace, localName, qualified, attrs);
      if (DEBUG) System.out.println("FigureTrajectory: startElement: "+qualified);

      if (isAtRootLevel())
      {
         String str = attrs.getValue(TYPE);
         if (str.equals(T_RECT))
            type = B_RECT;
         else if (str.equals(T_ELLIPSE))
            type = B_ELLIPSE;
         else //if (str.equals(T_POLYGON))
            type = B_POLYGON;
         vertices = new Binariser[MAX_VERTICES];
         noOfVertices = 0;
         mediaTime = depth = null;
      }
      else if (localName.equals(MEDIA))
      {
         currentElement = MEDIA;
         mediaTime = new MediaTimeTypeBinariser();
         mediaTime.takeDescriptor(null);
         mediaTime.startElement(nameSpace, localName, qualified, attrs);
      }
      else if (localName.equals(VERTEX))
      {
         currentElement = VERTEX;
         currentVertex = new TemporalInterpolationTypeBinariser();
         currentVertex.takeDescriptor(null);
         currentVertex.startElement(nameSpace, localName, qualified, attrs);
         vertices[noOfVertices++] = currentVertex;
      }
      else if (localName.equals(DEPTH))
      {
         currentElement = DEPTH;
         depth = new TemporalInterpolationTypeBinariser();
         depth.takeDescriptor(null);
         depth.startElement(nameSpace, localName, qualified, attrs);
      }
      else if (currentElement == MEDIA)
         mediaTime.startElement(nameSpace, localName, qualified, attrs);
      else if (currentElement == VERTEX)
         currentVertex.startElement(nameSpace, localName, qualified, attrs);
      else if (currentElement == DEPTH)
         depth.startElement(nameSpace, localName, qualified, attrs);
   }

   public void characters(char[] cdata, int index, int length) throws SAXException
   {
      if (currentElement == MEDIA)
         mediaTime.characters(cdata, index, length);
      else if (currentElement == VERTEX)
         currentVertex.characters(cdata, index, length);
      else if (currentElement == DEPTH)
         depth.characters(cdata, index, length);
   }

   public void endElement(String nameSpace, String localName, String qualified) throws SAXException
   {
      super.endElement(nameSpace, localName, qualified);

      if (currentElement == MEDIA)
      {
         mediaTime.endElement(nameSpace, localName, qualified);
         if (mediaTime.isFinished())
            currentElement = null;
      }
      else if (currentElement == VERTEX)
      {
         currentVertex.endElement(nameSpace, localName, qualified);
         if (currentVertex.isFinished())
            currentElement = null;
      }
      else if (currentElement == DEPTH)
      {
         depth.endElement(nameSpace, localName, qualified);
         if (depth.isFinished())
            currentElement = null;
      }

      if (isFinished())
         finish();
   }

   public void finish()
   {
      if (currentChunkWriterUsed == null)
         return;

      if (DEBUG) System.out.println("FigureTrajectory: finish");

      mediaTime.takeDescriptor(currentChunkWriterUsed);
      mediaTime.finish();                                                              //MediaTime
      currentChunkWriterUsed.writeByte((type == B_POLYGON) ? noOfVertices : type, 6);  //type
      for (int i = 0; i < noOfVertices; i++)
      {
         vertices[i].takeDescriptor(currentChunkWriterUsed);
         vertices[i].finish();                                                         //Vertex[i]
      }
      currentChunkWriterUsed.writeBoolean(depth != null);                              //DepthFlag
      if (depth != null)
      {
         depth.takeDescriptor(currentChunkWriterUsed);
         depth.finish();                                                               //Depth
      }
   }

   public int decode(BitToBitDataInputStream dis, Writer w, String name, String attrs)
   {
      long before = dis.getReadedBits();
      try
      {
         String ns = getNameSpace(name);
         if (DEBUG) System.out.println("FigureTrajectory: decode");
         StringWriter mediaBuf = new StringWriter();
         Binariser bin = new MediaTimeTypeBinariser();
         bin.decode(dis, mediaBuf, ns+MEDIA, null);  //MediaTime
         int nov;
         String stype;
         int type;
         switch (type = dis.readByte(6))                 //type
         {
            case 0:         nov = 0;    stype = "";        break; //forbidden
            case B_RECT:    nov = 3;    stype = T_RECT;    break;
            case B_ELLIPSE: nov = 3;    stype = T_ELLIPSE; break;
            default:        nov = type; stype = T_POLYGON; break;
         };
         w.write("<"+name+((attrs != null) ? " "+attrs : "")+" "+TYPE+"=\""+stype+"\">\n");
         w.write(mediaBuf.toString());
         bin = new TemporalInterpolationTypeBinariser();
         for (int i = 0; i < nov; i++)
            bin.decode(dis, w, ns+VERTEX, null);     //Vertex[i]
         if (dis.readBoolean())                          //DepthFlag
            bin.decode(dis, w, ns+DEPTH, null);      //Depth
         w.write("</"+name+">\n");
      }
      catch (Exception x)
      {
         System.out.println("FigureTrajectory: Unable to decode bitstream");
         x.printStackTrace();
      }

      return (int)(dis.getReadedBits()-before);
   }

   public int decode(BitToBitDataInputStream dis, Writer w)
   {
      return decode(dis, w, DNS+"FigureTrajectory", null); //this descriptor is used only in SpatioTemporalLocator, so its name isnt't in DESCRIPTOR_NAMES table
   }
}

class ParameterTrajectoryTypeBinariser extends Binariser
{
   /* names of elements */
   private static final String MODEL        = "motionModel";
   private static final String STILL        = "still";
   private static final String TRANSL       = "translation";
   private static final String ROTATION     = "rotationAndScaling";
   private static final String AFFINE       = "affine";
   private static final String PERSPECT     = "perspective";
   private static final String PARABOLIC    = "parabolic";
   private static final String ELLIPSE_FLAG = "ellipseFlag";
   private static final String MEDIA        = "MediaTime";
   private static final String INIT_REG     = "InitialRegion";
   private static final String PARAMETERS   = "Parameters";
   private static final String DEPTH        = "Depth";
   /* binary values */
   private static final int B_STILL     = 0;   //binary values of motion model
   private static final int B_TRANSL    = 1;
   private static final int B_ROTATION  = 2;
   private static final int B_AFFINE    = 3;
   private static final int B_PERSPECT  = 4;
   private static final int B_PARABOLIC = 5;

   int motionModel;
   boolean ellipseFlag;
   Binariser mediaTime, region, depth, parameters;

   private String currentElement;
   private int currParIdx;

   public void startElement(String nameSpace, String localName, String qualified, Attributes attrs)
            throws SAXException
   {
      super.startElement(nameSpace, localName, qualified, attrs);
      if (DEBUG) System.out.println("ParameterTrajectory: startElement: "+qualified);
      if (isAtRootLevel())
      {
         String str = attrs.getValue(MODEL);
         if (str.equals(STILL))
            motionModel = B_STILL;
         else if (str.equals(TRANSL))
            motionModel = B_TRANSL;
         else if (str.equals(ROTATION))
            motionModel = B_ROTATION;
         else if (str.equals(AFFINE))
            motionModel = B_AFFINE;
         else if (str.equals(PERSPECT))
            motionModel = B_PERSPECT;
         else //if (str.equals(PARABOLIC))
            motionModel = B_PARABOLIC;
         str = attrs.getValue(ELLIPSE_FLAG);
         ellipseFlag = (str != null) ? Boolean.valueOf(str).booleanValue() : false;
         mediaTime = parameters = depth = region = null;
      }
      else if (localName.equals(MEDIA))
      {
         currentElement = MEDIA;
         mediaTime = new MediaTimeTypeBinariser();
         mediaTime.takeDescriptor(null);
         mediaTime.startElement(nameSpace, localName, qualified, attrs);
      }
      else if (localName.equals(INIT_REG))
      {
         currentElement = INIT_REG;
         region = new RegionLocatorTypeBinariser();
         region.takeDescriptor(null);
         region.startElement(nameSpace, localName, qualified, attrs);
      }
      else if (localName.equals(PARAMETERS))
      {
         currentElement = PARAMETERS;
         parameters = new TemporalInterpolationTypeBinariser();
         parameters.takeDescriptor(null);
         parameters.startElement(nameSpace, localName, qualified, attrs);
      }
      else if (localName.equals(DEPTH))
      {
         currentElement = DEPTH;
         depth = new TemporalInterpolationTypeBinariser();
         depth.takeDescriptor(null);
         depth.startElement(nameSpace, localName, qualified, attrs);
      }
      else if (currentElement == MEDIA)
         mediaTime.startElement(nameSpace, localName, qualified, attrs);
      else if (currentElement == INIT_REG)
         region.startElement(nameSpace, localName, qualified, attrs);
      else if (currentElement == PARAMETERS)
         parameters.startElement(nameSpace, localName, qualified, attrs);
      else if (currentElement == DEPTH)
         depth.startElement(nameSpace, localName, qualified, attrs);
   }

   public void characters(char[] cdata, int index, int length) throws SAXException
   {
      if (currentElement == MEDIA)
         mediaTime.characters(cdata, index, length);
      else if (currentElement == INIT_REG)
         region.characters(cdata, index, length);
      else if (currentElement == PARAMETERS)
         parameters.characters(cdata, index, length);
      else if (currentElement == DEPTH)
         depth.characters(cdata, index, length);
   }

   public void endElement(String nameSpace, String localName, String qualified) throws SAXException
   {
      super.endElement(nameSpace, localName, qualified);

      if (currentElement == MEDIA)
      {
         mediaTime.endElement(nameSpace, localName, qualified);
         if (mediaTime.isFinished())
            currentElement = null;
      }
      else if (currentElement == INIT_REG)
      {
         region.endElement(nameSpace, localName, qualified);
         if (region.isFinished())
            currentElement = null;
      }
      else if (currentElement == PARAMETERS)
      {
         parameters.endElement(nameSpace, localName, qualified);
         if (parameters.isFinished())
            currentElement = null;
      }
      else if (currentElement == DEPTH)
      {
         depth.endElement(nameSpace, localName, qualified);
         if (depth.isFinished())
            currentElement = null;
      }

      if (isFinished())
         finish();
   }

   public void finish()
   {
      if (currentChunkWriterUsed == null)
         return;

      if (DEBUG) System.out.println("ParameterTrajectory: finish");

      currentChunkWriterUsed.writeByte(motionModel, 3);     //motionModel
      currentChunkWriterUsed.writeBoolean(ellipseFlag);     //ellipseFlag
      mediaTime.takeDescriptor(currentChunkWriterUsed);
      mediaTime.finish();                                   //MediaTime
      region.takeDescriptor(currentChunkWriterUsed);
      region.finish();                                      //InitialRegion
      if (parameters != null)
      {
         parameters.takeDescriptor(currentChunkWriterUsed);
         parameters.finish();                               //Parameters
      }
      currentChunkWriterUsed.writeBoolean(depth != null);   //DepthFlag
      if (depth != null)
      {
         depth.takeDescriptor(currentChunkWriterUsed);
         depth.finish();                                    //Depth
      }
   }

   public int decode(BitToBitDataInputStream dis, Writer w, String name, String attrs)
   {
      long before = dis.getReadedBits();
      try
      {
         String ns = getNameSpace(name);
         if (DEBUG) System.out.println("ParameterTrajectory: decode");
         int mm = dis.readByte(3);                                                              //motionModel
         String smm = "";
         int nop = 0;
         switch (mm)
         {
            case B_STILL:     smm = STILL;     break;
            case B_TRANSL:    smm = TRANSL;    break;
            case B_ROTATION:  smm = ROTATION;  break;
            case B_AFFINE:    smm = AFFINE;    break;
            case B_PERSPECT:  smm = PERSPECT;  break;
            case B_PARABOLIC: smm = PARABOLIC; break;
         };
         w.write("<"+name+((attrs != null) ? " "+attrs : "")+" "+MODEL+"=\""+smm+"\" "+
                  ELLIPSE_FLAG+"=\""+(dis.readBoolean() ? "true" : "false")+"\">\n");           //ellipseFlag
         new MediaTimeTypeBinariser().decode(dis, w, ns+MEDIA, null);                       //MediaTime
         new RegionLocatorTypeBinariser().decode(dis, w, ns+INIT_REG, null);                //InitialRegion
         Binariser bin = null;
         if (mm != B_STILL)                                                                     //Parameters
            (bin = new TemporalInterpolationTypeBinariser()).decode(dis, w, ns+PARAMETERS, null);
         if (dis.readBoolean())                                                                 //DepthFlag
         {
            if (bin == null)
               bin = new TemporalInterpolationTypeBinariser();
            bin.decode(dis, w, ns+DEPTH, null);                                             //Depth
         }
         w.write("</"+name+">\n");
      }
      catch (Exception x)
      {
         System.out.println("ParameterTrajectory: Unable to decode bitstream");
         x.printStackTrace();
      }

      return (int)(dis.getReadedBits()-before);
   }

   public int decode(BitToBitDataInputStream dis, Writer w)
   {
      return decode(dis, w, DNS+"FigureTrajectory", null); //this descriptor is used only in SpatioTemporalLocator, so its name isnt't in DESCRIPTOR_NAMES table
   }
}