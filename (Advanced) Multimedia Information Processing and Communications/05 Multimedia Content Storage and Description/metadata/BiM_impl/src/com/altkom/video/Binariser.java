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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
   This is the base for all the binarisers designed to convert textual form of descriptors
   to their binary forms. The idea is that every descriptor has its own binarising class
   that extends this class. The parser has to determine the appropriate binariser everytime it
   finds a new element and redirect SAX2 calls to the binariser as long as the binariser does
   not return true as the result of the isFinished method. The additional method takeDescriptor
   is intended to notify the binariser that a new descriptor has been found, and that its binary
   form should be saved to the pointed ChunkWriter of the com.expway.tools.compression package.
   There are three more added methods: takeDescriptor(), isFinished() and getChunkWriter(). The
   first creates a new ChunkWriter for this binariser, the second tells if the binarisation
   is finished, and the thir returns the ChunkWriter used to store the binary form of the
   binarised element.
*/
public abstract class Binariser extends DefaultHandler {

   static boolean DEBUG = false;//indicates debug mode

   //
   // Names of descriptors.
   //
   static final String CAMERA_MOTION                = "CameraMotion";
   static final String COLOR_LAYOUT                 = "ColorLayout";
   static final String COLOR_SPACE                  = "ColorSpace";
   static final String COLOR_STRUCTURE              = "ColorStructure";
   static final String COLOR_QUANTIZATION           = "ColorQuantization";
   static final String CONTOUR_SHAPE                = "ContourShape";
   static final String DOMINANT_COLOR               = "DominantColor";
   static final String EDGE_HISTOGRAM               = "EdgeHistogram";
   static final String FACE_RECOGNITION             = "FaceRecognition";
   static final String GOF_GOP_COLOR                = "GoFGoPColor";
   static final String GRID_LAYOUT                  = "GridLayout";
   static final String HOMOGENEOUS_TEXTURE          = "HomogeneousTexture";
   static final String IRREGULAR_VISUAL_TIME_SERIES = "IrregularVisualTimeSeries";
   static final String MOTION_ACTIVITY              = "MotionActivity";
   static final String MOTION_TRAJECTORY            = "MotionTrajectory";
   static final String MULTIPLE_VIEW                = "MultipleView";
   static final String PARAMETRIC_MOTION            = "ParametricMotion";
   static final String REGION_LOCATOR               = "RegionLocator";
   static final String REGION_SHAPE                 = "RegionShape";
   static final String REGULAR_VISUAL_TIME_SERIES   = "RegularVisualTimeSeries";
   static final String SCALABLE_COLOR               = "ScalableColor";
   static final String SHAPE_3D                     = "Shape3D";
   static final String SPATIAL_2D_COORDINATE_SYSTEM = "Spatial2DCoordinateSystem";
   static final String SPATIO_TEMPORAL_LOCATOR      = "SpatioTemporalLocator";
   static final String TEMPORAL_INTERPOLATION       = "TemporalInterpolation";
   static final String TEXTURE_BROWSING             = "TextureBrowsing";
   //
   //Names MUST be in array in specific order (according to 'Assignment of IDs to descriptors' table
   //in chapter '5.1. Grid Layout' of the MPEG7 part 3: Visual document) so index of name entry is
   //equal to the descriptor binary ID (which is used in GridLayout, TimeSeries, MultipleView)
   //
   static final String[] DESCRIPTOR_NAMES = {
      "", CAMERA_MOTION, COLOR_LAYOUT, COLOR_SPACE, COLOR_STRUCTURE, COLOR_QUANTIZATION,
      CONTOUR_SHAPE, DOMINANT_COLOR, EDGE_HISTOGRAM, FACE_RECOGNITION, GOF_GOP_COLOR, GRID_LAYOUT,
      HOMOGENEOUS_TEXTURE, IRREGULAR_VISUAL_TIME_SERIES, MOTION_ACTIVITY, MOTION_TRAJECTORY,
      MULTIPLE_VIEW, PARAMETRIC_MOTION, REGION_LOCATOR, REGION_SHAPE, REGULAR_VISUAL_TIME_SERIES,
      SCALABLE_COLOR, SHAPE_3D, SPATIAL_2D_COORDINATE_SYSTEM, SPATIO_TEMPORAL_LOCATOR,
      TEMPORAL_INTERPOLATION, TEXTURE_BROWSING
   };

    /**
       Every time a beginning of a new element is found this variable is increased. Then every
       time an end of an element is found this variable is decreased. If the variable is
       equal to 0, binarisation of an element is finished.
    */
    private int currentDepthInTheXML = 0;

    /**
       This is a reference to the ChunkWriter used to store the binary form of the binarised
       element. This variable can be set by the takeDescriptor methods, and it can be read
       by the getChunkWriter method.
       @see #takeDescriptor()
       @see #takeDescriptor(ChunkWriter)
       @see #getChunkWriter()
    */
    protected ChunkWriter currentChunkWriterUsed = new ChunkWriter ();

    /**
       This method should be called once everytime a new element that should be binarisied by
       this binariser is found. This method sets the currentChunkWriterUsed varibale to
       its parameter. It also resets the currentDepthInTheXML to 0.
       @param chunkWriter the bitstream to save the binary form of the descriptor
       @see #currentChunkWriterUsed
       @see #currentDepthInTheXML
    */
    public void takeDescriptor (ChunkWriter chunkWriter) {
        currentChunkWriterUsed = chunkWriter;
        currentDepthInTheXML = 0;
    }

    /**
       This method should be called once everytime a new element that should be binarisied by
       this binariser is found. This method sets the currentChunkWriterUsed varibale to
       a newly created ChunkWriter object. It also resets the currentDepthInTheXML to 0.
       @see #currentChunkWriterUsed
       @see #currentDepthInTheXML
    */
    public void takeDescriptor () {
        currentChunkWriterUsed = new ChunkWriter ();
        currentDepthInTheXML = 0;
    }

    /**
       Returns the reference to the currently used ChunkWriter. By default this method returns
       the value of the currentChunkWriterUsed variable.
       @return the reference to the currently used ChunkWriter
       @see #currentChunkWriterUsed
    */
    public ChunkWriter getChunkWriter () {
        return currentChunkWriterUsed;
    }

    /**
       Method taken from the SAX2 model. The default method increases the currentDepthInTheXML
       variable and does nothing more. You can call this method at the very beginning of your
       method in the class that extends this class to keep this default mechanism working or
       you can redefine this mechanism by redefining the endElement, isAtRootLevel and isFinished
       methods.
       @param nameSpace URI of the name space of the element
       @param localName the name of the element in its name space
       @param qualified the qualified name of the element (with the name space prefix)
       @param attributes the attributes of the element
       @throws SAXException when something went wrong
       @see #currentDepthInTheXML
       @see #endElement(String, String, String)
       @see #isFinished()
       @see #isAtRootLevel()
    */
    public void startElement (String nameSpace, String localName, String qualified, Attributes attributes)
        throws SAXException {
        currentDepthInTheXML++;
    }

    /**
       Method taken from the SAX2 model. It is called everytime an end of an element is
       found. The default method decreases the currentDepthInTheXML variable and does nothing
       more. You can call this method at the very beginning of your method in the class that
       extends this class to keep this default mechanism working or you can redefine this
       mechanism by redefining the startElement, isAtRootLevel and isFinished methods.
       @param nameSpace URI of the name space of the element
       @param localName the name of the element in its name space
       @param qualified the qualified name of the element (with the name space prefix)
       @throws SAXException when something went wrong
       @see #currentDepthInTheXML
       @see #startElement(String, String, String, Attributes)
       @see #isFinished()
       @see #isAtRootLevel()
    */
    public void endElement (String nameSpace, String localName, String qualified)
        throws SAXException {
        currentDepthInTheXML--;
    }

    /**
       Returns true if the binarisation of an element is finished, false otherwise. By default
       this method checks if the value of the currentDepthInTheXML is less that or equal to 0.
       If it is, the method returns true. You do not have to redefine this method in the class
       that extends this class to keep this default mechanism working or you can redefine this
       mechanism by redefining the startElement, isAtRootLevel and endElement methods.
       @return true if the binarisation of an element is finished
       @see #startElement(String, String, String, Attributes)
       @see #endElement(String, String, String)
       @see #isAtRootLevel()
    */
    public boolean isFinished () {
        return currentDepthInTheXML <= 0;
    }

    /**
       Returns true if the parsing of the element has reached the root of the element. By default
       this method checks if the value of the currentDepthInTheXML is equal to 1. If it is, the
       method returns true. You do not have to redefine this method in the class that extends
       this class to keep this default mechanism working or you can redefine this mechanism by
       redefining the startElement, isFinished and endElement methods.
       @return true if the binarisation of an element is finished
       @see #startElement(String, String, String, Attributes)
       @see #endElement(String, String, String)
       @see #isFinished()
    */
    public boolean isAtRootLevel () {
        return currentDepthInTheXML == 1;
    }

    /**
     * write descriptor to the binary stream
     * (should be abstract)
     */
    public abstract void finish();


   /**
    * decode descriptor from the binary stream. Names the element with 'name' and assign to it
    * additional attributes stored in 'attrs' (if not null).
    * (should be abstract)
    * @return number of read bits
    */
   public abstract int decode(BitToBitDataInputStream dis, Writer w, String name, String attrs);

    /**
     * decode descriptor from the binary stream. Names the element with default name (like type of
     * the descriptor without ending 'Type') and without any additional attributes.
     * (should call decode with four parameters specifying appropriate name and eventually
     * additional attributes - can't implement that here, because appropriate name is not known -
     * should be abstract)
     * @return number of read bits
     */
   public abstract int decode(BitToBitDataInputStream dis, Writer w);

   /**
    * finds and returns namespace in name.
    */
   String getNameSpace(String name)
   {
      int idx = name.indexOf(':');
      if (idx == -1)
         return "";
      else
         return name.substring(0, idx+1);
   }

   /* for debugging and testing decoding  */
   static final String DNS = "q3:"; //namespace added to decoding elements when 2-parameters decode is called
}

