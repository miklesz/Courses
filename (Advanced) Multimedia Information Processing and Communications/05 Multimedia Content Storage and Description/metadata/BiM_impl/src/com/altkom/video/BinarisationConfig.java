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

import java.util.HashMap;
import java.util.Map;

/**
   This class stores the references to the instances of binarisers (classes that
   extend the Binariser class). The idea is to have only one instance of each
   binariser for all elements it can binarise. The class has two maps: one that maps
   names of types of elements to their binarisers, and one that maps names of elements
   to their binarisers. The first map is package private and it is introduced for the test
   purposes, the second map has to be accessed via the getBinariserForType method.
*/
public class BinarisationConfig {

   /**
      Known binarisers. Keys: the qualified names of the elements. Values: references to
      instances of the classes that implement the Binariser interface, and that are
      appropriate for binarisationof the element of the given qualified name. It is
      absolutely required to initalize this Map during the processing of the Schema
      for the parsed XML document!
      @see #registerElement(String, String)
   */
   static Map binarisers = new HashMap ();

   /**
      Known binarisers for types of elements. Keys: names of types of elements. Values:
      references to instances of class that implement the Binariser interface.
      @see #getBinariserForType(String)
   */
   private static Map typeBinarisers = new HashMap ();

   /**
      Returns a reference to an instance of class that implements the Binariser interface
      for the given type of elements. If no such class is available, returns null. If no
      such class is known in the typeBinarisers map, but an appropriate class is available,
      the method loads and instantiates it, and stores in the mentioned map. The class
      to binarise a type has to be named xxxxBinariser, where the xxxx is the name of the
      type as written in the Schema. The class has to be a member of the com.altkom.video
      package. The class has to have a non-private default constructor. The class has to
      extend the Binariser class.
      @param typeName name of the type to be binarised
      @return reference to an instance of a binariser or null if none available
      @see #typeBinarisers
   */
    public static Binariser getBinariserForType (String typeName) {
        Binariser known = (Binariser) (typeBinarisers.get (typeName));
        if (known != null) return known;
        try {
            String binariserName = "com.altkom.video." + typeName + "Binariser";
            Binariser binariser = (Binariser) (Class.forName (binariserName).newInstance ());
            typeBinarisers.put (typeName, binariser);
            return binariser;
        }
        catch (ClassNotFoundException cnfex) {
            return null;
        }
        catch (InstantiationException iex) {
            return null;
        }
        catch (IllegalAccessException iaex) {
            return null;
        }
    }

   /**
      This is designed to build the map of the element names to the element types. As the
      Schema for the processed document if processed, the associations between names
      of elements and their types are found. Everytime a new association is found, this
      method should be called. The method returns true if it knows a binariser for the
      given type. The name of the binariser class is created as xxxxBinariser, where the
      xxxx is the name of the type of element we would like to binarise. For example the
      GridLayoutType descriptor has to be binarised by the GridLayoutTypeBinariser class
      that extends the Binariser class, belongs to the com.altkom.video package,
      and has a non-private default constructor. The method instantiates the class. Then
      the XML Schema can have something like &lt;element name="GridLayout"
      type="GridLayoutType"&gt;. In such situation the method should be called with
      qualifiedName="GridLayout" and descriptorTypeName="GridLayoutType".
      @param qualifiedName the qualified name of the elements we want to binarise as
         associated by the XML Schema to a given type
      @param descriptorTypeName the type of the binarisied element as defined by the
         XML Schema
      @return true if the appropriate binariser is known, false otherwise
   */
   static boolean registerElement (String qualifiedName, String descriptorTypeName) {
      Binariser binariser = getBinariserForType (descriptorTypeName);
      if (binariser == null) return false;
      binarisers.put (qualifiedName, binariser);
      return true;
      }
   }

