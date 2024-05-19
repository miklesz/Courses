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

package com.expway.schema.utils;

import java.util.Collection;

/**
 * StringMap.java
 *
 * TODO verifier que tous les methodes sont bien surchargees
 * Created: Mon Aug 28 13:58:09 2000
 *
 * @author Claude Seyrat & Cedric Thienot
 * @version 1.0
 */

public class StringMap extends java.util.HashMap {

    public StringMap () {
        super();
    }

    // Greg (pour libérer de la place mémoire)
    public static void freeStatic() {
    	Key.freeStatic();
    }

    public boolean containsKey(String aString){
        Key aK =  Key.createKey(aString);
        boolean b = super.containsKey(aK);
        Key.release(aK);
        return b;
    }
    public Collection values(){return super.values();}

    public Object get(String  aString) {
        Key aK =  Key.createKey(aString);
        Object o = super.get(aK);
        Key.release(aK);
        return o;
    }

    public Object put(String aString, Object value) {
        Key aK =  Key.createKey(aString);
        Object o = super.put(aK,value);
        Key.release(aK);
        return o;

    }
}

final class Key {
    // Pool        
    final static int poolSize= 3 ;
    static Key[] keyPool = new Key[poolSize];
    static int index = -1;
    
    // Greg (pour libérer de la place mémoire)
    public static void freeStatic() {
    	keyPool= new Key[poolSize];
    }
        
    static void release(Key aK){
        if (index == poolSize-1) return;
        index ++;
        keyPool[index]=aK;
    }

    static Key createKey(String s){
        if( index <0 )
            return new Key(s);
        Key aK = keyPool[index];
        aK.setKey(s);
        index--;
        return aK;
    }

    // key

    private String key;
    private int hashCode;

    public Key(String key){
        setKey(key);
    }

    public void setKey(String key) {
        this.key = key;
        hashCode = key.hashCode();
    }

    // caching the hash code speeds lookup

    public int hashCode(){
        return hashCode;
    }

    public boolean equals(Object object){
        if (this == object)
            return true;
        else if (object == null || getClass() != object.getClass())
            return false;
        Key other = (Key) object;
        return key.equals(other.key);
    }
}

