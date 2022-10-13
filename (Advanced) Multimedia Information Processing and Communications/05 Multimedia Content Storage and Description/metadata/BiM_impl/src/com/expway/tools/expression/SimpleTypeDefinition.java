/***********************************************************************
This software module was originally developed by Cédric Thiénot (Expway)
Claude Seyrat (Expway) and Grégoire Pau (Expway) in the course of 
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

Expway retains full right to use the code for his/her own purpose, 
assign or donate the code to a third party and to inhibit third parties 
from using the code for non MPEG-7 Systems (ISO/IEC 15938-1) conforming 
products. 

This copyright notice must be included in all copies or derivative works.

Copyright Expway © 2001.
************************************************************************/

package com.expway.tools.expression;

import com.expway.tools.compression.SimpleTypeInstance;
import com.expway.tools.compression.TypeEncoder;
import com.expway.tools.io.ChunkWriter;

import com.expway.binarisation.CodingParameters;

import com.siemens.*;

import com.expway.tools.io.BitToBitDataInputStream;

import com.expway.util.URIRegistry;

import java.io.*;
import java.util.*;

// Particularite des datatypes ils sont a la fois definition et encoder

abstract public class SimpleTypeDefinition extends TypeDefinition  {
    
    static final public String FAKE_BASETYPEFACET = "base";    
    public static SimpleTypeDefinition MIXED = new StringDataType("MIXED","string");
    public static String MIXED_KEY = "$$MIXED";

    static final boolean DEBUGCONTEXT=false;

    static SimpleTypeDefinition newSimpleTypeDefinition(String name, String primitive){
        String rawName=URIRegistry.getWithoutPrefix(name);

        // Enleve le prefixe
        // Faire le lien avec XMLSchema, plutôt !
        primitive=URIRegistry.getWithoutPrefix(primitive);

        

        if (primitive.equals("integer"))
            return new IntegerDataType(name,primitive);
        else if (primitive.equals("float"))
            return new FloatDataType(name,primitive);
        else if (primitive.equals("double"))
            return new DoubleDataType(name,primitive);
        else if (primitive.equals("list"))
            return new ListDataType(name,primitive);
        else if (primitive.equals("union"))
            return new UnionDataType(name,primitive);
        else if (primitive.equals("boolean"))
            return new BooleanDataType(name,primitive);
        else if (primitive.equals("ID"))
            return new IDDataType(name,primitive);
        else if (CodingParameters.bSpecificTimeDatatypes) {
            if (rawName.equals("basicTimePointType"))
                return new BasicTimePointDataType(name,primitive,2047);
            else if (rawName.equals("basicDurationType"))
                return new BasicDurationDataType(name,primitive,1023);
            else if (rawName.equals("timePointType"))
                return new BasicTimePointDataType(name,primitive,1983);
            else if (rawName.equals("durationType"))
                return new BasicDurationDataType(name,primitive,1015);
            else if (rawName.equals("mediaTimePointType"))
                return new BasicTimePointDataType(name,primitive,1982);
            else if (rawName.equals("mediaDurationType"))
                return new BasicDurationDataType(name,primitive,1014);
        } 

        // By default, use the StringDataType
        return new StringDataType(name,primitive);
    }
    
    String primitiveDatatype = null;

    TreeMap facets = new TreeMap();

    public SimpleTypeDefinition(String name,String primitive){
        super(name);
        primitiveDatatype = primitive;
    }

    public TypeEncoder newInstance()  {return new SimpleTypeInstance(this);  }
    public boolean ignoreWhiteSpace() {return false;}

    public boolean isItBootstrapDefinition() {
        return URIRegistry.getWithoutPrefix(getName()).equals(primitiveDatatype);
    }    

    public void realize(TypeDefinitions tds) throws DefinitionException {
        super.realize(tds);
    }


    

    // ------------------------------------------------------------
    // Methodes Abstraites

    public void encodeItInto(String value, ChunkWriter cw){
        Thread.dumpStack();
        throw new RuntimeException("SimpleTypeDefinition: call to encodeItInto name="+name+" value="+value);
    }

    public void decode(BitToBitDataInputStream bis, Writer w){
        Thread.dumpStack();    
        throw new RuntimeException("SimpleTypeDefinition: call to decode name="+name);
    }
    
    

    public void setFacetValue(String name, String value){
        if (facets.containsKey(name) && name.equals("enumeration")){
            facets.put(name,facets.get(name)+", " + value);
        }
        else
            facets.put(name,value);

        internalSetFacetValue(name,value);
    }

    public void encodeItIntoContextAware(String value, ChunkWriter cw){
        String primtype=primitiveDatatype;
        String type=name;

        if (value==null) value="";
        
        
            encodeItInto(value,cw);
    }

    public void decodeContextAware(BitToBitDataInputStream bis,Writer w){
        
            decode(bis,w);
    }

    abstract public void internalSetFacetValue(String name,String value); 

    public boolean isItUsedHere(String s){
        if (primitiveDatatype.equals(s)) return true;
        return false;
    }

}
