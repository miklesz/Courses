/***********************************************************************
This software module was originally developed by C�dric Thi�not (Expway)
Claude Seyrat (Expway) and Gr�goire Pau (Expway) in the course of 
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

Copyright Expway � 2001.
************************************************************************/

/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xerces" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package com.expway.schema;


/**
 *             Collection of symbols used to parse a Schema Grammar
 *             We have temporary references to this class from
 *             SchemaImporter but we will be moving all the
 *             SchemaImporter functionality to the Grammar classes
 * 
 * @author jeffrey rodriguez
 */
public final  class SchemaSymbols {
    public static final String URI_SCHEMAFORSCHEMA =  "http://www.w3.org/2001/XMLSchema";
    public static final String DEFAULTPREFIX =  "xs";
    public static final String XSI_SCHEMALOCACTION =  "schemaLocation";
    public static final String XSI_NONAMESPACESCHEMALOCACTION =  "noNamespaceSchemaLocation";
    public static final String XSI_TYPE =  "type";
    public static final String ANYTYPE =  "anyType";

    public static final String ELT_ALL =  "all";
    public static final String ELT_ANNOTATION =  "annotation";
    public static final String ELT_ANY =  "any";
    public static final String ELT_WILDCARD = "any";
    public static final String ELT_APPINFO = "appinfo";
    public static final String ELT_ANYATTRIBUTE =  "anyAttribute";
    public static final String ELT_ATTRIBUTE =  "attribute";
    public static final String ELT_ATTRIBUTEGROUP =  "attributeGroup";
    public static final String ELT_CHOICE =  "choice";
    public static final String ELT_COMPLEXTYPE =  "complexType";
    public static final String ELT_COMPLEXCONTENT =  "complexContent";
    public static final String ELT_CONTENT =  "content";
    public static final String ELT_DIMENSION =  "dimension";
    public static final String ELT_DOCUMENTATION =  "documentation";
    public static final String ELT_DURATION =  "duration";
    public static final String ELT_ELEMENT =  "element";
    public static final String ELT_ENCODING =  "encoding";
    public static final String ELT_ENUMERATION =  "enumeration";
    public static final String ELT_EXTENSION =  "extension";
    public static final String ELT_FIELD =  "field";
    public static final String ELT_FRACTIONSDIGITS =  "fractionDigits";
    public static final String ELT_GROUP =  "group";
    public static final String ELT_IMPORT =  "import";
    public static final String ELT_INCLUDE =  "include";
    public static final String ELT_KEY =  "key";
    public static final String ELT_KEYREF =  "keyref";
    public static final String ELT_LENGTH =  "length";
    public static final String ELT_LIST =  "list";
    public static final String ELT_MAXEXCLUSIVE =  "maxExclusive";
    public static final String ELT_MAXINCLUSIVE =  "maxInclusive";
    public static final String ELT_MAXLENGTH =  "maxLength";
    public static final String ELT_MINEXCLUSIVE =  "minExclusive";
    public static final String ELT_MININCLUSIVE =  "minInclusive";
    public static final String ELT_MINLENGTH =  "minLength";
    public static final String ELT_NOTATION =  "notation";
    public static final String ELT_PATTERN =  "pattern";
    public static final String ELT_PERIOD =  "period";
    public static final String ELT_PRECISION =  "precision";
    public static final String ELT_RESTRICTION =  "restriction";
    public static final String ELT_SCALE =  "scale";
    public static final String ELT_SCHEMA =  "schema";
    public static final String ELT_SELECTOR =  "selector";
    public static final String ELT_SEQUENCE =  "sequence";
    public static final String ELT_SIMPLECONTENT =  "simpleContent";
    public static final String ELT_SIMPLETYPE =  "simpleType";
    public static final String ELT_UNION =  "union";
    public static final String ELT_WHITESPACE =  "whiteSpace";
    public static final String ATT_ABSTRACT =  "abstract";
    public static final String ATT_ATTRIBUTEFORMDEFAULT =  "attributeFormDefault";
    public static final String ATT_BASE =  "base";
    public static final String ATT_BLOCK =  "block";
    public static final String ATT_BLOCKDEFAULT =  "blockDefault";
    public static final String ATT_CONTENT  = "content";
    public static final String ATT_DEFAULT =  "default";
    public static final String ATT_DERIVEDBY = "derivedBy";
    public static final String ATT_ELEMENTFORMDEFAULT =  "elementFormDefault";
    public static final String ATT_FINAL =  "final";
    public static final String ATT_FINALDEFAULT =  "finalDefault";
    public static final String ATT_FIXED =  "fixed";
    public static final String ATT_FORM =  "form";
    public static final String ATT_ITEMTYPE =  "itemType";
    public static final String ATT_MAXOCCURS =  "maxOccurs";
    public static final String ATT_MEMBERTYPES =  "memberTypes";
    public static final String ATT_MINOCCURS =  "minOccurs";
    public static final String ATT_MIXED =  "mixed";
    public static final String ATT_NAME =  "name";
    public static final String ATT_NAMESPACE =  "namespace";
    public static final String ATT_NULLABLE =  "nullable";
    public static final String ATT_PROCESSCONTENTS =  "processContents";
    public static final String ATT_REF =  "ref";
    public static final String ATT_REFER =  "refer";
    public static final String ATT_SCHEMALOCATION =  "schemaLocation";
    public static final String ATT_SOURCE =  "source";
    public static final String ATT_SUBSTITUTION =  "substitutionGroup";
    public static final String ATT_SYSTEM =  "system";
    public static final String ATT_TARGETNAMESPACE =  "targetNamespace";
    public static final String ATT_TYPE =  "type";
    public static final String ATT_USE =  "use";
    public static final String ATT_VALUE = "value";
    public static final String ATTVAL_TWOPOUNDANY =  "##any";
    public static final String ATTVAL_TWOPOUNDLOCAL =  "##local";
    public static final String ATTVAL_TWOPOUNDOTHER =  "##other";
    public static final String ATTVAL_POUNDALL =  "#all";
    public static final String ATTVAL_BASE64 =  "base64";
    public static final String ATTVAL_BOOLEAN =  "boolean";
    public static final String ATTVAL_COLLAPSE =  "collapse";
    public static final String ATTVAL_DEFAULT =  "default";
    public static final String ATTVAL_ELEMENTONLY =  "elementOnly";
    public static final String ATTVAL_EMPTY =  "empty";
    public static final String ATTVAL_EXTENSION =  "extension";
    public static final String ATTVAL_FALSE =  "false";
    public static final String ATTVAL_FIXED =  "fixed";
    public static final String ATTVAL_HEX =  "hex";
    public static final String ATTVAL_ID =  "ID";
    public static final String ATTVAL_LAX =  "lax";
    public static final String ATTVAL_LIST =  "list";
    public static final String ATTVAL_MAXLENGTH =  "maxLength";
    public static final String ATTVAL_MINLENGTH =  "minLength";
    public static final String ATTVAL_MIXED =  "mixed";
    public static final String ATTVAL_NCNAME =  "NCName";
    public static final String ATTVAL_OPTIONAL =  "optional";
    public static final String ATTVAL_PRESERVE =  "preserve";
    public static final String ATTVAL_PROHIBITED =  "prohibited";
    public static final String ATTVAL_QNAME =  "QName";
    public static final String ATTVAL_QUALIFIED =  "qualified";
    public static final String ATTVAL_REPLACE =  "replace";
    public static final String ATTVAL_REQUIRED =  "required";
    public static final String ATTVAL_RESTRICTION =  "restriction";
    public static final String ATTVAL_SKIP =  "skip";
    public static final String ATTVAL_STRING =  "string";
    public static final String ATTVAL_TEXTONLY =  "textOnly";
    public static final String ATTVAL_TIMEDURATION =  "timeDuration";
    public static final String ATTVAL_TRUE =  "true";
    public static final String ATTVAL_UNQUALIFIED =  "unqualified";
    public static final String ATTVAL_URI =  "uri";
    public static final String ATTVAL_URIREFERENCE =  "uriReference";
    public static final String ATTVAL_EQUIVCLASS = "substitutionGroup";
    public static final String ATTVAL_UNBOUNDED = "unbounded";


    public static final int                     EMPTY_SET = 0;          
    public static final int                     EXTENSION = 1;
    public static final int                     RESTRICTION = 2;
    public static final int                     REPRODUCTION = 4;
    public static final int                     LIST = 8;
    public static final int                     ENUMERATION = 16;
    public static final int                     EQUIVCLASS = 32;

    public static final int                     CHOICE = 0;                     // group orders
    public static final int                     SEQUENCE = 1;           // group orders
    public static final int                     ALL = 2;                        // group orders

    public static final int                     INFINITY = -1;          // used for maxOccurs

    public static final int NULLABLE = 1;
    public static final int ABSTRACT = 2;

}
