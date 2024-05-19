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

package com.expway.schema.instance;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Map;
import java.util.Hashtable;

import java.io.Writer;
import java.io.IOException;
import java.io.CharArrayWriter;

/**
 * AnonymousTypeRawSchemaConstructor.java
 *
 *
 * Created: Thu Aug 31 19:00:06 2000
 *
 * @author 
 * @version
 */

public class AnonymousTypeRawSchemaConstructor  {

    //    Collection subConstructors = new ArrayList();

    // the writer

    Writer myWriter;

    static final String PREFIXID="AC";
    
    MyInteger numID ;
    Map globalElementWithAnonymousTypeMap;

    String anonymousTypesRawSchema="";
    public AnonymousTypeRawSchemaConstructor (Writer writer) {
        myWriter = writer;
        globalElementWithAnonymousTypeMap = new Hashtable();
        numID = new MyInteger();
    }
    public AnonymousTypeRawSchemaConstructor (Writer writer, Map agb, MyInteger num) {
        myWriter = writer;
        globalElementWithAnonymousTypeMap = agb;
        numID = num;
    }
    /*
    public void addType(Type aType){
        list.add(aType);
    }
    */

    public void write(String a){
        try{
            myWriter.write(a);
        }catch (IOException e){e.printStackTrace();}
    }

    public void close(){
        try{
            myWriter.close();
        }catch (IOException e){e.printStackTrace();}
    }
    /**
     * give an id for a anonymous declaration
     */

    String getNewID(){
         return PREFIXID+""+numID.getIntAndIncrease(); 
    }
    /**
     * si l'element n'est pas declare globalement envoyer elementName avec null
     */
    public String constructRawSchema(Type anonymousType,String elementName) throws SchemaException {
        String anonymousTypeID;
        AnonymousTypeRawSchemaConstructor subConstructor;
        if (anonymousType.getAnonymousID() != null)
            return anonymousType.getAnonymousID();
        // si l'element est global
        if (elementName != null){
            // si l'anonyme type a deja ete genere
            if (globalElementWithAnonymousTypeMap.containsKey(elementName)) 
                return (String) globalElementWithAnonymousTypeMap.get(elementName);
            // si l'anonyme type n'a pas deja ete genere
            else {
                anonymousTypeID =getNewID();
                globalElementWithAnonymousTypeMap.put(elementName,anonymousTypeID);
            }
        } else anonymousTypeID = getNewID();     
        anonymousType.setAnonymousID(anonymousTypeID);
        subConstructor = new AnonymousTypeRawSchemaConstructor(new CharArrayWriter(),
                                                               globalElementWithAnonymousTypeMap,
                                                               numID);
        // construit le string  correspondant au type
        anonymousType.writeRawSchemaWithName(subConstructor,anonymousTypeID);
        anonymousTypesRawSchema += Schema.CHARFINAL+subConstructor.getRawSchema(); 
        
        // retourne l'id genere pour le type anonyme
        return anonymousTypeID;
    }    
    
  
    String getRawSchema(){
        return myWriter.toString()
            + anonymousTypesRawSchema;
    }
   
    public void writeAnonymousType(){
        write(anonymousTypesRawSchema);
    }


    class MyInteger{
        int num=1;
        int getIntAndIncrease(){return num++;}
    }
        
}// AnonymousTypeRawSchemaConstructor
