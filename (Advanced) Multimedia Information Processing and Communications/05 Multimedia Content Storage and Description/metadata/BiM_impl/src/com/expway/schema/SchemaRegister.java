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

package com.expway.schema;

import java.util.Hashtable;

/**
 * SchemaRegister.java
 *
 *
 * Created: Fri Aug 11 16:25:21 2000
 *
 * @author 
 * @version
 */

public class SchemaRegister  {

    public static final String CLASS_ELT_ANNOTATION = "com.expway.schema.AnnotationHandler";
    public static final String CLASS_ELT_ANY = "com.expway.schema.AnyHandler";
    public static final String CLASS_ELT_ANYATTRIBUTE = "com.expway.schema.AnyAttributeHandler";
    public static final String CLASS_ELT_APPINFO = "com.expway.schema.AppinfoHandler";
    public static final String CLASS_ELT_ATTRIBUTE = "com.expway.schema.AttributeHandler";
    public static final String CLASS_ELT_ATTRIBUTEGROUP = "com.expway.schema.AttributeGroupHandler";
    public static final String CLASS_ELT_COMPLEXCONTENT = "com.expway.schema.ComplexContentHandler";
    public static final String CLASS_ELT_COMPLEXTYPE = "com.expway.schema.ComplexTypeHandler";
    public static final String CLASS_ELT_DOCUMENTATION = "com.expway.schema.DocumentationHandler";
    public static final String CLASS_ELT_DIMENSION = "com.expway.schema.DimensionHandler";
    public static final String CLASS_ELT_ELEMENT = "com.expway.schema.ElementHandler";
    public static final String CLASS_ELT_EXTSIMPLECOMPLEX = "com.expway.schema.ExtSimpleComplexTypeHandler";
    public static final String CLASS_ELT_EXTCOMPLEX = "com.expway.schema.ExtComplexTypeHandler";
    public static final String CLASS_ELT_FACET = "com.expway.schema.FacetHandler";
    public static final String CLASS_ELT_FIELD = "com.expway.schema.FieldHandler";
    public static final String CLASS_ELT_GROUP = "com.expway.schema.SimpleGroupHandler";
    public static final String CLASS_ELT_GLOBALGROUP = "com.expway.schema.GlobalGroupHandler";
    public static final String CLASS_ELT_HFP = "com.expway.schema.HfpHandler";
    public static final String CLASS_ELT_IMPORT = "com.expway.schema.ImportHandler";
    public static final String CLASS_ELT_INCLUDE = "com.expway.schema.IncludeHandler";
    public static final String CLASS_ELT_KEY = "com.expway.schema.KeyHandler";
    public static final String CLASS_ELT_KEYREF = "com.expway.schema.KeyRefHandler";
    public static final String CLASS_ELT_LIST = "com.expway.schema.SimpleTypeListHandler";
    public static final String CLASS_ELT_RESTCOMPLEX = "com.expway.schema.ResComplexTypeHandler";
    public static final String CLASS_ELT_RESTSIMPLE = "com.expway.schema.SimpleTypeRestrictionHandler";
    public static final String CLASS_ELT_RESTSIMPLECOMPLEX = "com.expway.schema.ResSimpleComplexTypeHandler";
    public static final String CLASS_ELT_SELECTOR = "com.expway.schema.SelectorHandler";
    public static final String CLASS_ELT_SIMPLECONTENT = "com.expway.schema.SimpleContentHandler";
    public static final String CLASS_ELT_SIMPLETYPE = "com.expway.schema.SimpleTypeHandler";
    public static final String CLASS_ELT_SCHEMA = "com.expway.schema.SchemaHandler";
    public static final String CLASS_ELT_UNION = "com.expway.schema.UnionHandler";



    public static void register(){

        HandlerManager.register(CLASS_ELT_ANNOTATION,1,getAnnotationDictionnary());
        HandlerManager.register(CLASS_ELT_IMPORT,1,getEmptyDictionnary());
        HandlerManager.register(CLASS_ELT_ANY,1,getAnyDictionnary());
        HandlerManager.register(CLASS_ELT_ANYATTRIBUTE,1,getAnyDictionnary());
        HandlerManager.register(CLASS_ELT_APPINFO,1,getAppinfoDictionnary());
        HandlerManager.register(CLASS_ELT_ATTRIBUTE,1,getAttributeDictionnary());
        HandlerManager.register(CLASS_ELT_ATTRIBUTEGROUP,1,getAttributeGroupDictionnary());
        HandlerManager.register(CLASS_ELT_COMPLEXTYPE,2,getComplexTypeDictionnary());
        HandlerManager.register(CLASS_ELT_DOCUMENTATION,1,getDocumentationDictionnary());
        HandlerManager.register(CLASS_ELT_DIMENSION,1,getDimensionDictionnary());
        HandlerManager.register(CLASS_ELT_ELEMENT,2,getElementDictionnary());
        HandlerManager.register(CLASS_ELT_FIELD,1,getEmptyDictionnary());
        HandlerManager.register(CLASS_ELT_FACET,1,getFacetDictionnary());
        HandlerManager.register(CLASS_ELT_GLOBALGROUP,1,getGroupDictionnary());
        HandlerManager.register(CLASS_ELT_GROUP,2,getGroupDictionnary());
        HandlerManager.register(CLASS_ELT_INCLUDE,2,getEmptyDictionnary());
        HandlerManager.register(CLASS_ELT_KEY,2,getKeyDictionnary());
        HandlerManager.register(CLASS_ELT_KEYREF,2,getKeyDictionnary());
        HandlerManager.register(CLASS_ELT_LIST,1,getListDictionnary());
        HandlerManager.register(CLASS_ELT_SCHEMA,1,getSchemaDictionnary());
        HandlerManager.register(CLASS_ELT_SELECTOR,1,getEmptyDictionnary());
        HandlerManager.register(CLASS_ELT_SIMPLETYPE,1,getSimpleTypeDictionnary());
        HandlerManager.register(CLASS_ELT_UNION,1,getUnionDictionnary());
        // complexType
        // complexcontent
        HandlerManager.register(CLASS_ELT_COMPLEXCONTENT,1,getComplexContentDictionnary());
        HandlerManager.register(CLASS_ELT_EXTCOMPLEX,1,getExtComplexDictionnary());
        HandlerManager.register(CLASS_ELT_RESTCOMPLEX,1,getExtComplexDictionnary());
        //Simple content
        HandlerManager.register(CLASS_ELT_SIMPLECONTENT,1,getSimpleContentDictionnary());
        HandlerManager.register(CLASS_ELT_EXTSIMPLECOMPLEX,1,getExtSimpleComplextDictionnary());
        HandlerManager.register(CLASS_ELT_RESTSIMPLECOMPLEX,1,getRestSimpleComplextDictionnary());
        // simpleType
        HandlerManager.register(CLASS_ELT_RESTSIMPLE,1,getRestSimpleContentDictionnary());
        HandlerManager.register("com.expway.schema.DefaultLocalHandler",1,new Hashtable());
        // schema Of schma
        HandlerManager.register(CLASS_ELT_HFP,1,getEmptyDictionnary());

    }    

   private static Hashtable getAnnotationDictionnary(){
        Hashtable classHandlerMap = new Hashtable();
        classHandlerMap.put(SchemaSymbols.ELT_DOCUMENTATION,CLASS_ELT_DOCUMENTATION);
        classHandlerMap.put(SchemaSymbols.ELT_APPINFO,CLASS_ELT_APPINFO);
        return classHandlerMap;
    }

   private static Hashtable getListDictionnary(){
        Hashtable classHandlerMap = new Hashtable();
        classHandlerMap.put(SchemaSymbols.ELT_SIMPLETYPE,CLASS_ELT_SIMPLETYPE);
        return classHandlerMap;
    }

   private static Hashtable getKeyDictionnary(){
        Hashtable classHandlerMap = new Hashtable();
        classHandlerMap.put(SchemaSymbols.ELT_FIELD,CLASS_ELT_FIELD);
        classHandlerMap.put(SchemaSymbols.ELT_SELECTOR,CLASS_ELT_SELECTOR);
        return classHandlerMap;
    }

   private static Hashtable getEmptyDictionnary(){
        Hashtable classHandlerMap = new Hashtable();
        return classHandlerMap;
    }

   private static Hashtable getDocumentationDictionnary(){
        Hashtable classHandlerMap = new Hashtable();
        return classHandlerMap;
    }


   private static Hashtable getAnyDictionnary(){
        Hashtable classHandlerMap = new Hashtable();
        return classHandlerMap;
    }

   private static Hashtable getAppinfoDictionnary(){
        Hashtable classHandlerMap = new Hashtable();
        classHandlerMap.put(SchemaSymbols.ELT_DIMENSION,CLASS_ELT_DIMENSION);
        classHandlerMap.put(HfpHandler.HAS_PROPERTY,CLASS_ELT_HFP);
        classHandlerMap.put(HfpHandler.HAS_FACET,CLASS_ELT_HFP);
        return classHandlerMap;
    }
   private static Hashtable getDimensionDictionnary(){
        Hashtable classHandlerMap = new Hashtable();
        return classHandlerMap;
    }

   private static Hashtable getSchemaDictionnary(){
        Hashtable classHandlerMap = new Hashtable();
        classHandlerMap.put(SchemaSymbols.ELT_ANNOTATION,CLASS_ELT_ANNOTATION);
        classHandlerMap.put(SchemaSymbols.ELT_INCLUDE,CLASS_ELT_INCLUDE);
        classHandlerMap.put(SchemaSymbols.ELT_ATTRIBUTE,CLASS_ELT_ATTRIBUTE);
        classHandlerMap.put(SchemaSymbols.ELT_IMPORT,CLASS_ELT_IMPORT);
        classHandlerMap.put(SchemaSymbols.ELT_SIMPLETYPE,CLASS_ELT_SIMPLETYPE);
        classHandlerMap.put(SchemaSymbols.ELT_ELEMENT,CLASS_ELT_ELEMENT);
        classHandlerMap.put(SchemaSymbols.ELT_GROUP,CLASS_ELT_GLOBALGROUP);
        classHandlerMap.put(SchemaSymbols.ELT_COMPLEXTYPE,CLASS_ELT_COMPLEXTYPE);
        classHandlerMap.put(SchemaSymbols.ELT_ATTRIBUTEGROUP,CLASS_ELT_ATTRIBUTEGROUP);
        return classHandlerMap;
    }

   private static Hashtable getUnionDictionnary(){
        Hashtable classHandlerMap = new Hashtable();
        classHandlerMap.put(SchemaSymbols.ELT_ANNOTATION,CLASS_ELT_ANNOTATION);
        classHandlerMap.put(SchemaSymbols.ELT_SIMPLETYPE,CLASS_ELT_SIMPLETYPE);
        return classHandlerMap;
    }

   private static Hashtable getFacetDictionnary(){
        Hashtable classHandlerMap = new Hashtable();
        return classHandlerMap;
    }
   private static Hashtable getAttributeDictionnary(){
        Hashtable classHandlerMap = new Hashtable();
        classHandlerMap.put(SchemaSymbols.ELT_ANNOTATION,CLASS_ELT_ANNOTATION);
        classHandlerMap.put(SchemaSymbols.ELT_SIMPLETYPE,CLASS_ELT_SIMPLETYPE);
        return classHandlerMap;
    }
   private static Hashtable getElementDictionnary(){
        Hashtable classHandlerMap = new Hashtable();
        classHandlerMap.put(SchemaSymbols.ELT_ANNOTATION,CLASS_ELT_ANNOTATION);
        classHandlerMap.put(SchemaSymbols.ELT_SIMPLETYPE,CLASS_ELT_SIMPLETYPE);
        //        classHandlerMap.put(SchemaSymbols.ELT_ELEMENT,CLASS_ELT_ELEMENT);
        classHandlerMap.put(SchemaSymbols.ELT_KEY,CLASS_ELT_KEYREF);
        classHandlerMap.put(SchemaSymbols.ELT_KEYREF,CLASS_ELT_KEY);
        classHandlerMap.put(SchemaSymbols.ELT_COMPLEXTYPE,CLASS_ELT_COMPLEXTYPE);

        return classHandlerMap;
    }
   private static Hashtable getAttributeGroupDictionnary(){
        Hashtable classHandlerMap = new Hashtable();
        classHandlerMap.put(SchemaSymbols.ELT_ANNOTATION,CLASS_ELT_ANNOTATION);
        classHandlerMap.put(SchemaSymbols.ELT_ATTRIBUTE,CLASS_ELT_ATTRIBUTE);
        classHandlerMap.put(SchemaSymbols.ELT_ANYATTRIBUTE,CLASS_ELT_ANYATTRIBUTE);
        classHandlerMap.put(SchemaSymbols.ELT_ATTRIBUTEGROUP,CLASS_ELT_ATTRIBUTEGROUP);
        return classHandlerMap;
    }




    /** initialise comon with complex
     */

    private static Hashtable initCOMPLEXCONTENT(Hashtable classHandlerMap){
       classHandlerMap = SchemaRegister.initFacet(classHandlerMap);
       //        classHandlerMap.put(SchemaSymbols.ELT_SIMPLETYPE,CLASS_ELT_SIMPLETYPE);
        classHandlerMap.put(SchemaSymbols.ELT_ELEMENT,CLASS_ELT_ELEMENT);
        classHandlerMap.put(SchemaSymbols.ELT_COMPLEXTYPE,CLASS_ELT_COMPLEXTYPE);
        classHandlerMap.put(SchemaSymbols.ELT_ANYATTRIBUTE,CLASS_ELT_ANYATTRIBUTE);
        classHandlerMap.put(SchemaSymbols.ELT_ATTRIBUTE,CLASS_ELT_ATTRIBUTE);
        classHandlerMap.put(SchemaSymbols.ELT_CHOICE,CLASS_ELT_GROUP);
        classHandlerMap.put(SchemaSymbols.ELT_GROUP,CLASS_ELT_GLOBALGROUP);
        classHandlerMap.put(SchemaSymbols.ELT_ANNOTATION,CLASS_ELT_ANNOTATION);
        classHandlerMap.put(SchemaSymbols.ELT_SEQUENCE,CLASS_ELT_GROUP);
        classHandlerMap.put(SchemaSymbols.ELT_ALL,CLASS_ELT_GROUP);
        classHandlerMap.put(SchemaSymbols.ELT_ATTRIBUTEGROUP,CLASS_ELT_ATTRIBUTEGROUP);
        return classHandlerMap;
    }

    /////////////////////
    // COMPLEX

   private static Hashtable getComplexTypeDictionnary(){
        Hashtable classHandlerMap = new Hashtable();
        classHandlerMap.put(SchemaSymbols.ELT_ANNOTATION,CLASS_ELT_ANNOTATION);
        classHandlerMap = SchemaRegister.initCOMPLEXCONTENT(classHandlerMap);
        classHandlerMap.put(SchemaSymbols.ELT_COMPLEXCONTENT,CLASS_ELT_COMPLEXCONTENT);
        classHandlerMap.put(SchemaSymbols.ELT_SIMPLECONTENT,CLASS_ELT_SIMPLECONTENT);
        return classHandlerMap;
    }

    ////////////////////////
    // complexContent

   private static Hashtable getComplexContentDictionnary(){
        Hashtable classHandlerMap = new Hashtable();
        classHandlerMap.put(SchemaSymbols.ELT_EXTENSION,CLASS_ELT_EXTCOMPLEX);
        classHandlerMap.put(SchemaSymbols.ELT_RESTRICTION,CLASS_ELT_RESTCOMPLEX);
        return classHandlerMap;
    }

    private static Hashtable getExtComplexDictionnary(){
        Hashtable classHandlerMap = new Hashtable();
        classHandlerMap = SchemaRegister.initCOMPLEXCONTENT(classHandlerMap);
        return classHandlerMap;
    }


    ///////////////////////////
    // simpleContent

    //le simple content du copmplex
   private static Hashtable getSimpleContentDictionnary(){
        Hashtable classHandlerMap = new Hashtable();
        classHandlerMap.put(SchemaSymbols.ELT_EXTENSION,CLASS_ELT_EXTSIMPLECOMPLEX);
        classHandlerMap.put(SchemaSymbols.ELT_RESTRICTION,CLASS_ELT_RESTSIMPLECOMPLEX);
        return classHandlerMap;
    }

    private static Hashtable getRestSimpleComplextDictionnary(){
        Hashtable classHandlerMap = new Hashtable();
        classHandlerMap.put(SchemaSymbols.ELT_ANYATTRIBUTE,CLASS_ELT_ANYATTRIBUTE);
        classHandlerMap.put(SchemaSymbols.ELT_ATTRIBUTE,CLASS_ELT_ATTRIBUTE);
        classHandlerMap.put(SchemaSymbols.ELT_ATTRIBUTEGROUP,CLASS_ELT_ATTRIBUTEGROUP);
        classHandlerMap.put(SchemaSymbols.ELT_SIMPLETYPE,CLASS_ELT_SIMPLETYPE);
        classHandlerMap = SchemaRegister.initFacet(classHandlerMap);
        return classHandlerMap;
    }

    private static Hashtable getExtSimpleComplextDictionnary(){
        Hashtable classHandlerMap = new Hashtable();
        classHandlerMap.put(SchemaSymbols.ELT_ANYATTRIBUTE,CLASS_ELT_ANYATTRIBUTE);
        classHandlerMap.put(SchemaSymbols.ELT_ATTRIBUTE,CLASS_ELT_ATTRIBUTE);
        classHandlerMap.put(SchemaSymbols.ELT_ATTRIBUTEGROUP,CLASS_ELT_ATTRIBUTEGROUP);
        return classHandlerMap;
    }


    //////////////////////////////////
    // SimpleType
    //////////////////////////////////

   private static Hashtable getSimpleTypeDictionnary(){
        Hashtable classHandlerMap = new Hashtable();
        classHandlerMap.put(SchemaSymbols.ELT_ANNOTATION,CLASS_ELT_ANNOTATION);
        classHandlerMap.put(SchemaSymbols.ELT_RESTRICTION,CLASS_ELT_RESTSIMPLE);
        classHandlerMap.put(SchemaSymbols.ELT_LIST,CLASS_ELT_LIST);
        classHandlerMap.put(SchemaSymbols.ELT_UNION,CLASS_ELT_UNION);
        return classHandlerMap;
    }
   private static Hashtable getRestSimpleContentDictionnary(){
        Hashtable classHandlerMap = new Hashtable();
        classHandlerMap.put(SchemaSymbols.ELT_ANNOTATION,CLASS_ELT_ANNOTATION);
        classHandlerMap = SchemaRegister.initFacet(classHandlerMap);
        classHandlerMap.put(SchemaSymbols.ELT_SIMPLETYPE,CLASS_ELT_SIMPLETYPE);
        return classHandlerMap;
    }

   private static Hashtable getGroupDictionnary(){
        Hashtable classHandlerMap = new Hashtable();
        classHandlerMap.put(SchemaSymbols.ELT_ANNOTATION,CLASS_ELT_ANNOTATION);
        classHandlerMap.put(SchemaSymbols.ELT_ELEMENT,CLASS_ELT_ELEMENT);
        classHandlerMap.put(SchemaSymbols.ELT_CHOICE,CLASS_ELT_GROUP);
        classHandlerMap.put(SchemaSymbols.ELT_SEQUENCE,CLASS_ELT_GROUP);
        classHandlerMap.put(SchemaSymbols.ELT_ANY,CLASS_ELT_ANY);
        classHandlerMap.put(SchemaSymbols.ELT_ALL,CLASS_ELT_GROUP);
       classHandlerMap.put(SchemaSymbols.ELT_GROUP,CLASS_ELT_GLOBALGROUP);
         return classHandlerMap;
    }

    /**
     * inittialize all the possible facet
     */

    private static Hashtable initFacet(Hashtable classHandlerMap){
        classHandlerMap.put(SchemaSymbols.ELT_MININCLUSIVE,CLASS_ELT_FACET);
        classHandlerMap.put(SchemaSymbols.ELT_MINEXCLUSIVE,CLASS_ELT_FACET);
        classHandlerMap.put(SchemaSymbols.ELT_PATTERN,CLASS_ELT_FACET);
        classHandlerMap.put(SchemaSymbols.ELT_FRACTIONSDIGITS,CLASS_ELT_FACET);
        classHandlerMap.put(SchemaSymbols.ELT_MAXEXCLUSIVE,CLASS_ELT_FACET);
        classHandlerMap.put(SchemaSymbols.ELT_MAXINCLUSIVE,CLASS_ELT_FACET);
        classHandlerMap.put(SchemaSymbols.ELT_MAXEXCLUSIVE,CLASS_ELT_FACET);
        classHandlerMap.put(SchemaSymbols.ELT_MAXLENGTH,CLASS_ELT_FACET);
        classHandlerMap.put(SchemaSymbols.ELT_MINLENGTH,CLASS_ELT_FACET);
        classHandlerMap.put(SchemaSymbols.ELT_MAXINCLUSIVE,CLASS_ELT_FACET);
        classHandlerMap.put(SchemaSymbols.ELT_ENUMERATION,CLASS_ELT_FACET);
        classHandlerMap.put(SchemaSymbols.ELT_LENGTH,CLASS_ELT_FACET);
        classHandlerMap.put(SchemaSymbols.ELT_WHITESPACE,CLASS_ELT_FACET);
        return classHandlerMap;
    }

}// SchemaRegister
