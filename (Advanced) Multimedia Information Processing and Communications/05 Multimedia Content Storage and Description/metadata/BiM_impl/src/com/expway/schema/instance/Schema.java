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

import java.util.Map;
import java.util.Iterator;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;

import java.io.IOException;
import java.io.FileWriter;
import java.io.Writer;

import org.xml.sax.helpers.NamespaceSupport;

import com.expway.schema.utils.*;
import com.expway.schema.GeneralSchemaHandler;
import com.expway.schema.SchemaSymbols;

import com.expway.util.URIRegistry;

import com.expway.binarisation.CodingParameters;



/**
 * Schema.java
 *
 * TODO
 * Created: Mon Aug 28 12:05:39 2000
 *
 * @author Claude Seyrat & Cedric Thienot
 * @version 1.0
 */

public class Schema  extends SchemaObject {

    /**
     * assure une compatibilite sur tous les namespace a raffiner
     */
    // mettre a true pour la nouvelle version
    //    static boolean RWS_NON_REALIZE = CodingParameters.bAllowsCompatibility;
    static boolean RWS_NON_REALIZE = true;

    protected boolean affiche = false ;

    //RAW schema
    static Schema XSDSCHEMA;
    static Schema CURRENTSCHEMA;
    public final static String CHARFINAL ="|\n";


    public static Map READSCHEMA = new Hashtable();

    public static URIRegistry AURIREGISTRY = new URIRegistry();
    ////////////////////////////
    //
    DataType get_AnySimpleType(){
        return (DataType)getXSDschema().getType("anySimpleType");
    }

    private String targetNamespace ="";
    private String targetNamespacePrefix ;
    private int targetNamespacePrefixLength;
    private Map schemas = new HashMap();
    private List schemaImport;
    private String elementFormDefault = SchemaSymbols.ATTVAL_UNQUALIFIED;
    private String attributeFormDefault = SchemaSymbols.ATTVAL_UNQUALIFIED;


    String fileName;
    Collection schemaDeclaration;

    ////////////////////////////////////////////

    public String getTargetNamespace(){
        return targetNamespace;
    }

    public String getTargetNamespacePrefix(){
        return targetNamespacePrefix;
    }



    // Greg (pour libérer de la place mémoire)
    public static void freeStatic() {
        AURIREGISTRY=new URIRegistry();;
        CURRENTSCHEMA=null;
        XSDSCHEMA=null;
        READSCHEMA=new Hashtable();
    }


    public void addSchema(Schema xs){
        schemas.put(xs.targetNamespace,xs);
    }

    void addSchemas(List l){
        for (int i = 0;i<l.size();i++){
            schemas.put(((Schema)l.get(i)).targetNamespace,l.get(i));
        }
    }
    /**
     * return les schemas meme les schemas importes par les schemas importes
     */
    public Map getAllSchemas(){
        return getAllSchemas(new Hashtable());
    }

    private Map getAllSchemas(Map result){
        Iterator i = getSchemaImport().iterator();
        while (i.hasNext()){
            Schema aS =(Schema)i.next();
            if (!result.containsKey(aS.getFileName())){
                result.put(aS.getFileName(),aS);
                result = aS.getAllSchemas(result);
            }
        }
        return result;
    }

    public Schema getSchema(String targetNamespace){
        return (Schema)schemas.get(targetNamespace);
    }

    /**
     */

    public void setElementFormDefault(String  v) throws SchemaException {
        if (v == null) return;
        if (!(v.equals(SchemaSymbols.ATTVAL_UNQUALIFIED) ||
            v.equals(SchemaSymbols.ATTVAL_QUALIFIED)))
            throw new SchemaException(this,
                                      SchemaMessages.getMessages("invalid value"),
                                      new String[] {v,SchemaSymbols.ATT_ELEMENTFORMDEFAULT});
        elementFormDefault = v;
    }


    public void setAttributeFormDefault(String  v) throws SchemaException {
        if (v == null) return;
        if (!(v.equals(SchemaSymbols.ATTVAL_UNQUALIFIED) ||
              v.equals(SchemaSymbols.ATTVAL_QUALIFIED)))
            throw new SchemaException(this,
                                      SchemaMessages.getMessages("invalid value"),
                                      new String[] {v,SchemaSymbols.ATT_ATTRIBUTEFORMDEFAULT});
        attributeFormDefault = v;
    }
       
    /**
     * Get the value of fileName.
     * @return Value of fileName.
     */
    public String getFileName() {return fileName;}
    
    /**
     * Set the value of fileName.
     * @param v  Value to assign to fileName.
     */
    public void setFileName(String  v) {this.fileName = v;}
    
    //------------------------------------------------------------
    // namespace

    public void setTargetNamespace(String a) {
        targetNamespace = a;  
    }

    public void setTargetNamespacePrefix(String a) {
        targetNamespacePrefix = a;  
    }

    public void setNameSupport(NamespaceSupport aN){
        targetNamespacePrefix = aN.getPrefix(targetNamespace);
        if (targetNamespacePrefix != null)
            targetNamespacePrefixLength = targetNamespacePrefix.length()+1;
    }
    public Schema getSchemaInString(String aQRName) throws SchemaException{
        String target;
        int index = aQRName.lastIndexOf(":");
        if (index==-1)
            target = "";
        else
            target = aQRName.substring(0,index);
        return findSchema(target); 
    }
    public String getLocal(String aQRName){

        int index = aQRName.lastIndexOf(":");
        //        System.out.println("           on cherche "+aQRName.substring(index+1));
        return aQRName.substring(index+1);
    }

    Schema findSchema(String target) throws SchemaException {
        //     System.out.println();
        //        System.out.println("    dans   \""+targetNamespace+"\"") ;
        //        System.out.println("           le schema \""+target+"\"");
        if (target.equals(targetNamespace))
            return this;
        Schema schemaR = (Schema) schemas.get(target);
        if (schemaR == null )
            throw new SchemaException(this,
                                      SchemaMessages.getMessages("no schema"),
                                      new String[] {target});
        return schemaR;
    }

    String getUnQualified(String qName){
        // no name space defined
        if (targetNamespacePrefix == null) return qName;
        int index = qName.indexOf(targetNamespacePrefix);
        if (index==-1) return qName;
        return qName.substring(targetNamespacePrefixLength);
    }

    
    // ------------------------------------------------------------
    // the types

    void initializeType(){
        try{
            addType(ComplexType.GenerateANYTYPE());
        }catch (SchemaException e){}// il n'yena pas c'est l'amorcage
    }

    //    private Map types = new StringMap();
    private Map types = new java.util.HashMap();

    public void addType(Type at) throws SchemaException {
        if (at == null){
            //TODO on ajoute un type null
            System.out.println("on ajoute un type null");            
            return;
        }
        if (types.containsKey(at.getName()))
            {
                // ici on fait un test car onpourrait refernir un datatype Primitif?
                Type aDT = getType(at.getName());
                if (aDT.isSimple() && at.isSimple()){
                    if (((DataType)aDT).isPrimitive()){
                        // on retire l'ancien
                        types.remove(at.getName());
                        //§ on declare le nouveau comme primitif
                        ((DataType) at).setPrimitive();
                        ((DataType) at).setPossibleFacets(((DataType)aDT).getPossibleFacets());
                        // on l'ajoute
                        types.put(at.getName(),at);
                        at.setSchema(this);
                    }
                }
                else
                    this.errorHandler.schemaError(new SchemaException(at,
                                                                      SchemaMessages.getMessages("duplicate definition"),
                                                                      new String[] {at.getName()}));
            }
        types.put(at.getName(),at);
        at.setSchema(this);
    }

    public Type getType(String aName) {
        //               System.out.println("on cherche "+ aName + " dans "+this);
        return (Type)types.get(aName);
    }
    public Type getRType(String aQRName) throws SchemaException {
        return (Type)getSchemaInString(aQRName).getType(getLocal(aQRName));
    }

    public Map getTypes(){return types;}

    // return all the type included the import one
    public Map getAllTypes(){
        Map alltypes = new java.util.HashMap();
        alltypes.putAll(getTypes());
        Iterator i = getAllSchemas().values().iterator();
        while (i.hasNext())
            alltypes.putAll(((Schema)i.next()).getTypes());
        //        Iterator i = getSchemaImport().iterator();
        //        while (i.hasNext())
        //            alltypes.putAll(((Schema)i.next()).getAllTypes());
        return alltypes;
    }

    // ------------------------------------------------------------
    // the elements

    private Map elements = new StringMap();

    public void addElement(Element ae) throws SchemaException {
        ae.setGlobal(true);
        if (elements.containsKey(ae.getName()))
            throw new SchemaException(ae,
                                      SchemaMessages.getMessages("duplicate definition"),
                                      new String[] {ae.getName()});
        elements.put(ae.getName(),ae);
        ae.setSchema(this);
    }
    public Element getElement(String aName) {
        //        aName = getUnQualified(aName);
        return (Element)elements.get(aName);
    }
    public Map getElements() {
        return elements;
    }
    public Element getRElement(String aQRName) throws SchemaException {
        return (Element)getSchemaInString(aQRName).getElement(getLocal(aQRName));
    }


    // return all the elements included the import one
    public Map getAllElements(){
        Map map = new java.util.HashMap();
        map.putAll(getElements());
        Iterator i = getAllSchemas().values().iterator();
        while (i.hasNext())
            map.putAll(((Schema)i.next()).getElements());
        return map;
    }

    // ------------------------------------------------------------
    // the groups

    private Map groups = new StringMap();

    public void addGroup(GlobalGroup at) throws SchemaException {
        if (groups.containsKey(at.getName()))
            this.errorHandler.schemaError(new SchemaException(at,
                                                              SchemaMessages.getMessages("duplicate definition"),
                                                              new String[] {at.getName()}));
        groups.put(at.getName(),at);
        at.setSchema(this);
    }

    public GlobalGroup getGroup(String aName) {
        aName = getUnQualified(aName);
        return (GlobalGroup)groups.get(aName);
    }

    public Map getGroups() {return groups;}

    public GlobalGroup getRGroup(String aQRName) throws SchemaException {
        return (GlobalGroup)getSchemaInString(aQRName).getGroup(getLocal(aQRName));
    }

    // return all the elements included the import one
    public Map getAllGroups(){
        Map map = new java.util.HashMap();
        map.putAll(getGroups());
        Iterator i = getAllSchemas().values().iterator();
        while (i.hasNext())
            map.putAll(((Schema)i.next()).getGroups());
        return map;
    }


    private Map attributes = new StringMap();

    public void addAttribute(Attribute ae) throws SchemaException {
        if (attributes.containsKey(ae.getName()))
            this.errorHandler.schemaError(new SchemaException(ae,
                                                              SchemaMessages.getMessages("duplicate definition"),
                                                              new String[] {ae.getName()}));
        attributes.put(ae.getName(),ae);
        ae.setSchema(this);
    }

    public Attribute getAttribute(String aName) {
        return (Attribute)attributes.get(aName);
    }
    public Map getAttributes(){return attributes;}

    public Attribute getRAttribute(String aQRName) throws SchemaException{
        return (Attribute)getSchemaInString(aQRName).getAttribute(getLocal(aQRName));
    }

    // return all the elements included the import one
    public Map getAllAttributes(){
        Map map = new java.util.HashMap();
        map.putAll(getAttributes());
        Iterator i = getAllSchemas().values().iterator();
        while (i.hasNext())
            map.putAll(((Schema)i.next()).getAttributes());
        return map;
    }


    // ------------------------------------------------------------
    // map attributesList

    private Map attributesList = new StringMap();

    public Map getAttributeGroups() {return attributesList;}

    public void addAttributeGroup(AttributeList ae) throws SchemaException {
        if (attributesList.containsKey(ae.getName()))
            this.errorHandler.schemaError(new SchemaException(ae,
                                                              SchemaMessages.getMessages("duplicate definition"),
                                                              new String[] {ae.getName()}));
        attributesList.put(ae.getName(),ae);
        ae.setSchema(this);
    }

    public AttributeList getAttributeGroup(String aName) {
        return (AttributeList)attributesList.get(aName);
    }
    public AttributeList getRAttributeGroup(String aQRName) throws SchemaException{
        return (AttributeList)getSchemaInString(aQRName).getAttributeGroup(getLocal(aQRName));
    }

    // return all the elements included the import one
    public Map getAllAttributeGroups(){
        Map map = new java.util.HashMap();
        map.putAll(getAttributeGroups());
        Iterator i = getAllSchemas().values().iterator();
        while (i.hasNext())
            map.putAll(((Schema)i.next()).getAttributeGroups());
        return map;
    }

    // ------------------------------------------------------------
    // REALIZE
    public void realize() throws SchemaException {
        realize(this);
    }

    public boolean isValid(){
        return numberOfError ==0;
    }
    public int getNumberOfError(){
        return numberOfError;
    }

    int numberOfError = 0;

    public void realize(Schema a) throws SchemaException{
        if (affiche) System.out.println("######################################\n");
        if (affiche) System.out.println("        "+targetNamespace);
        if (affiche) System.out.println("######################################\n");

        if (affiche) System.out.println("##         REALIZE INCLUDE           ##\n");
        realizeInclude();
        if (affiche) System.out.println("##         REALIZE IMPORT           ##\n");
        realizeImport();
        addSchemasImportToSchemas();
        //datatype
        if (affiche) System.out.println("##         REALIZE TYPE             ##\n");
        int i =0;
        Iterator aI = types.values().iterator();
        while (aI.hasNext())
            try{
                Type aT = (Type) aI.next();
                if (affiche) System.out.println(aT.getName());
                aT.realize(this);
            }catch(SchemaException e){
                this.errorHandler.schemaError(e);
                numberOfError++;
            }
        if (affiche) System.out.println("##         REALIZE INHERITANCE      ##\n");
        //datatype
        aI = types.values().iterator();
        while (aI.hasNext())
            try{
                ((Type) aI.next()).realizeWithInheritance(this);
            }catch(SchemaException e){
                this.errorHandler.schemaError(e);
                numberOfError++;
            }
        //element
        if (affiche) System.out.println("##         ELEMENT                  ##\n");
        aI = elements.values().iterator();
        while (aI.hasNext())
            try{
                Element elt = (Element) aI.next();
                if (affiche) System.out.println(elt);
                elt.realize(this);
            }catch(SchemaException e){
                this.errorHandler.schemaError(e);
                numberOfError++;
            }
        //attributesList
        if (affiche) System.out.println("##         Attribute                  ##\n");
        aI = attributes.values().iterator();
        while (aI.hasNext())
            try{
                Attribute elt = (Attribute) aI.next();
                if (affiche) System.out.print(elt);
                elt.realize(this);
            }catch(SchemaException e){
                this.errorHandler.schemaError(e);
                numberOfError++;
            }

        //Group
        if (affiche) System.out.println("##         GROUP                  ##\n");
        aI = getGroups().values().iterator();
        while (aI.hasNext())
            try{
                GlobalGroup elt = (GlobalGroup) aI.next();
                if (affiche) System.out.println(elt);
                elt.realize(this);
            }catch(SchemaException e){
                this.errorHandler.schemaError(e);
                numberOfError++;
            }
        // OK ou KO
        if (numberOfError !=0)
            if (affiche) System.out.println("\nnumber of error :"+ numberOfError);
            else 
                if (affiche) System.out.println("\n The schema with the targetNamespace "
                                                + targetNamespace
                                                +" is correctly designed.");
    }

    //////////////////////////////////////////////////        
    // RWS
    /////////////////////////////////////////////////

    public void toRawSchema(AnonymousTypeRawSchemaConstructor atsc) throws SchemaException {
        // preparation des types
        /* FINALEMENT ON OUBLIE
           Iterator aI = types.values().iterator();
           while (aI.hasNext()){
           Type type = (Type) aI.next();
           try{
           type.createxsiTypeAttribute(this) ;
           //                System.out.println(type.toRawSchema(atsc) +CHARFINAL);          
           } catch (SchemaException e){
           this.errorHandler.schemaError(e);
           }
           }
        */
        //type
        if (!isValid()) {
            System.out.println("correct your error");
            return;
        }
        
        if (affiche) System.out.println("#################  RAW SCHEMA ####################");
        if (affiche) System.out.println("#########TYPE#################");

        Iterator aI = types.values().iterator();
        while (aI.hasNext()){
            Type type = (Type) aI.next();
            if (affiche) System.out.println(type.getName());
            try{
    
    
                type.writeRawSchema(atsc) ;
                atsc.write(CHARFINAL);          
                //                System.out.println(type.toRawSchema(atsc) +CHARFINAL);          
            } catch (SchemaException e){
                this.errorHandler.schemaError(e);
            }
        }
        //element
        if (affiche) atsc.write("######################################\n");
        if (affiche) atsc.write("##         ELEMENT                  ##\n");
        if (affiche) atsc.write("######################################\n");

        aI = elements.values().iterator();
        while (aI.hasNext())
            try{
                Element elt = (Element) aI.next();
                if (affiche) System.out.println(elt);
                elt.writeWithoutOccurenceRawSchema(atsc);
                atsc.write(CHARFINAL);
            }catch(SchemaException e){
                this.errorHandler.schemaError(e);
            }
       //element
        if (affiche) atsc.write("######################################\n");
        if (affiche) atsc.write("##         ATTRIBUT                 ##\n");
        if (affiche) atsc.write("######################################\n");

        aI = attributes.values().iterator();
        while (aI.hasNext())
            try{
                Attribute elt = (Attribute) aI.next();
                if (affiche) System.out.println(elt);
                elt.getRawType(atsc);
                atsc.write(CHARFINAL);
            }catch(SchemaException e){
                this.errorHandler.schemaError(e);
            }
        if (affiche) atsc.write("######################################\n");
        if (affiche) atsc.write("##         Anonymous type           ##\n");
        if (affiche) atsc.write("######################################\n");
        atsc.writeAnonymousType();
        atsc.write(CHARFINAL);

        // XML schema
        if (affiche) atsc.write("######################################\n");
        if (affiche) atsc.write("##     XML schema type              ##\n");
        if (affiche) atsc.write("######################################\n");

        aI = getSchema(SchemaSymbols.URI_SCHEMAFORSCHEMA).types.values().iterator();
        while (aI.hasNext()){
            Type type = (Type) aI.next();
            try{
                type.writeRawSchema(atsc) ;
                //                System.out.println(type.toRawSchema(atsc) +CHARFINAL);          
                atsc.write(CHARFINAL);

            } catch (SchemaException e){
                this.errorHandler.schemaError(e);
            }
        }
        
    }

    public void toRawSchema(String aFileName) throws SchemaException, IOException {
        Writer  afileWriter = new FileWriter(aFileName); 
        if (affiche)
            System.out.println("fichier " + fileName+"\n");
        
        if (affiche)  
            afileWriter = new java.io.CharArrayWriter(); 
        AnonymousTypeRawSchemaConstructor atsc = new AnonymousTypeRawSchemaConstructor(afileWriter);
        // gestion namespace
        writeNamespace(atsc);
        writeProperties(atsc);
        Schema.CURRENTSCHEMA = this;
        toRawSchema(atsc);
        if (affiche)        System.out.println(afileWriter.toString());
        atsc.close();
    }

    // on ajoute les autres schemas
    /* FINALEMENT ON OUBLIE
       void addRawSchemas(AnonymousTypeRawSchemaConstructor atsc) throws SchemaException {
       Iterator i =  schemas.values().iterator();
       while (i.hasNext()){
       Schema aS = (Schema) i.next();
       if (!aS.targetNamespace.equals(SchemaSymbols.URI_SCHEMAFORSCHEMA))
       aS.toRawSchema(atsc);
       }
       }
    */
    /// add uniquement RAW schema


    public int getProvisionalRawSchemaKey(){return AURIREGISTRY.getIndex(this.targetNamespace);}

    void writeProperties(AnonymousTypeRawSchemaConstructor atsc){
        atsc.write("(PROPERTIES");
        atsc.write(" "+getProvisionalRawSchemaKey());
        atsc.write((elementFormDefault.equals(SchemaSymbols.ATTVAL_QUALIFIED)?" QE":" UQE"));
        atsc.write((attributeFormDefault.equals(SchemaSymbols.ATTVAL_QUALIFIED)?" QA":" UQA"));
        atsc.write(")\n");
    }

    void writeNamespace(AnonymousTypeRawSchemaConstructor atsc) throws IOException{

        /*        atsc.write ("(TARGETNAMESPACE ");

                  atsc.write("\""+targetNamespace+"\"");
                  AURIREGISTRY.registerURI(targetNamespace);
                  Iterator it = schemas.values().iterator();
                  while (it.hasNext()){
                  Schema as = (Schema)it.next();
                  atsc.write(" \""+as.targetNamespace+"\"");
                  AURIREGISTRY.registerURI(as.targetNamespace);
                  }
                  atsc.write (")\n ");
        */
        AURIREGISTRY.registerURI(targetNamespace);
        Iterator it = schemas.values().iterator();
        while (it.hasNext()){
            Schema as = (Schema)it.next();
            AURIREGISTRY.registerURI(as.targetNamespace);
        }
        atsc.write(AURIREGISTRY.toTargetNamespace());
    }
        
    // Initialisation

    public void initialize(){
        // prepare the built in datatype
        Schema xs = getXSDschema();
        schemas.put(xs.targetNamespace,xs);
        AURIREGISTRY = new URIRegistry();
    }

    public Schema getXSDschema(){
        if (Schema.XSDSCHEMA != null) return Schema.XSDSCHEMA;
        Schema.XSDSCHEMA = new Schema();
        Schema.XSDSCHEMA.setTargetNamespace(SchemaSymbols.URI_SCHEMAFORSCHEMA);
        Schema.XSDSCHEMA.setTargetNamespacePrefix(SchemaSymbols.DEFAULTPREFIX);
        DataType.generateBuiltInDataTypes(Schema.XSDSCHEMA);
        try{
            Schema.XSDSCHEMA.realize();
        }catch(SchemaException e){
            //            System.out.println(">>>> "+e);
            
        }
        Schema.XSDSCHEMA.initializeType();
        return Schema.XSDSCHEMA;
    }
    // ------------------------------------------------------------
    //ERROR HANDLER 

    //TODO
    private ErrorHandler errorHandler = new DefaultErrorHandler();
    public void setErrorHandler(ErrorHandler aEH){errorHandler = aEH;}
    public ErrorHandler getErrorHandler(){ return errorHandler;}

    
          

    //
    // include
    public void setInclude(String in) {
        if (affiche) System.out.println("on doit inclure "+in);
        getIncludedSchemaNames().add(in);        
    }
    ///
    // import
    // TODO verifier si le simport sont bons
    // en effet doit on transmettre les imports?
    void addSchemasImportToSchemas(){
        if (schemaImport == null) return;

        addSchemas(schemaImport);
    }

    // donne la liste des schemas importes
    public List getSchemaImport(){
        if (schemaImport == null)
            schemaImport = new ArrayList();
        return schemaImport;
    }

    public void addSchemaImport(Schema xs){
        getSchemaImport().add(xs);
    }

    public void addSchemasImport(Collection c){
        getSchemaImport().addAll(c);
    }

    // les impoprt textuels

    public void setImport(String[] im){
        getSchemaDeclaration().add(im);
    }

    public void addSchemaDeclaration(Collection c){
        getSchemaDeclaration().addAll(c);
    }

    public void realizeImport()throws SchemaException{
        Schema ns;
        if (schemaDeclaration == null) return;
        Iterator i = schemaDeclaration.iterator();
        while (i.hasNext()){
            String[] v = (String[])i.next();
            String localUri = createImportFileName(v[0],v[1]);
            if (!Schema.READSCHEMA.containsKey(localUri))
                try{
                    //                ns = GeneralSchemaHandler.getSchema(createImportFileName(v[0],v[1]),false);
                    ns = GeneralSchemaHandler.getAndRawSchema(createImportFileName(v[0],v[1]),createImportFileName(v[0],v[1])+".rws",false);
                } catch(Exception e){throw new SchemaException(this,e.getMessage());}
            else 
                ns=(Schema)Schema.READSCHEMA.get(localUri);
            if (!ns.targetNamespace.equals(v[0]))
                this.errorHandler.schemaError(new SchemaException(this,
                                                                  SchemaMessages.getMessages("invalid targetNamespace"),
                                                                  new String[] {ns.targetNamespace,v[0]}));
            addSchemaImport(ns);
        }
    }
    // TODO sans doute plus complique ici je repars du fichier actuel
    String createImportFileName(String namespace, String schemaLocation){
        //        System.out.println(namespace+"/"+schemaLocation);
        
        if ((new java.io.File(namespace+"/"+schemaLocation)).exists())
            System.out.println("coco");
        return (new java.io.File(fileName)).getParent()+"/"+schemaLocation;
        //        return "http://www.w3.org/2001/SMIL20/WD/"+schemaLocation;
    }

    /**
     * Get the value of schemaDeclaration.
     * @return value of schemaDeclaration.
     */
    public Collection getSchemaDeclaration() {
        if (schemaDeclaration == null)
            schemaDeclaration = new Vector();
        return schemaDeclaration;
    }
    
    /**
     * Set the value of schemaDeclaration.
     * @param v  Value to assign to schemaDeclaration.
     */
    public void setSchemaDeclaration(Collection  v) {
        this.schemaDeclaration = v;
    }

    ////////////////////////////////////////////////////////////////////////
    // INCLUDED SCHEMA
    ////////////////////////////////////////////////////////////////////////

    Vector includedSchemaNames;

    /**
     * Get the value of includedSchemaNames.
     * @return value of includedSchemaNames.
     */
    public Vector getIncludedSchemaNames() {
        if (includedSchemaNames == null)
            includedSchemaNames = new Vector();
        return includedSchemaNames;
    }

    /**
     * Set the value of includedSchemaNames.
     * @param v  Value to assign to includedSchemaNames.
     */
    public void setIncludedSchemaNames(Vector  v) {
        this.includedSchemaNames = v;

    }

    Collection includedSchemas;

    public Collection getIncludedSchemas() {
        if (includedSchemas == null)
            includedSchemas = new Vector();
        return includedSchemas;
    }

    /**
     * Set the value of includedSchemaNames.
     * @param v  Value to assign to includedSchemaNames.
     */
    public void addIncludedSchema(Schema s) {
        this.getIncludedSchemas().add(s);
    }

    String createIncludeFileName(String schemaLocation){
        return (new java.io.File(fileName)).getParent()+"/"+schemaLocation;
    }


    public void realizeInclude()throws SchemaException{
        Schema ns;
        if (includedSchemaNames == null) return;
        Iterator i = includedSchemaNames.iterator();
        while (i.hasNext()){
            String uri = (String)i.next();
            try{
                if (affiche) System.out.println("on realize "+uri);
                ns = GeneralSchemaHandler.getSchemaWithoutRealize(createIncludeFileName(uri));
                ns.realizeInclude();
                // on ajoute tous les imports des includes
                ns.realizeImport();
                if (ns.schemaImport != null)
                    addSchemasImport(ns.schemaImport);
            } catch(Exception e){
                e.printStackTrace();
                throw new SchemaException(this,e.getMessage());
            }
            if (!ns.targetNamespace.equals(this.targetNamespace))
                this.errorHandler.schemaError(new SchemaException(this,
                                                                  SchemaMessages.getMessages("invalid targetNamespace"),
                                                                  new String[] {ns.targetNamespace,this.targetNamespace}));
        addIncludeElement(ns);
        }

    }



    public void addIncludeElement(Schema ns) throws SchemaException {
        addIncludedSchema(ns);
        // elements
        Iterator i = ns.getElements().values().iterator();
        while (i.hasNext()){
            Element e = (Element)i.next();
            addElement(e);
            e.setSchema(ns);
        }
        // groups
        i = ns.getGroups().values().iterator();
        while (i.hasNext()){
            GlobalGroup e = (GlobalGroup)i.next();
            addGroup(e);
            e.setSchema(ns);
        }
        // type
        i = ns.getTypes().values().iterator();
        while (i.hasNext()){
            Type e = (Type)i.next();
            addType(e);
            e.setSchema(ns);
        }
        // attributes
        i = ns.getAttributes().values().iterator();
        while (i.hasNext()){
            Attribute e = (Attribute)i.next();
            addAttribute(e);
            e.setSchema(ns);
        }
        // attributesList
        i = ns.getAttributeGroups().values().iterator();
        while (i.hasNext()){
            AttributeList e = (AttributeList)i.next();
            addAttributeGroup(e);
            e.setSchema(ns);
        }

    }

    


}// Schema

