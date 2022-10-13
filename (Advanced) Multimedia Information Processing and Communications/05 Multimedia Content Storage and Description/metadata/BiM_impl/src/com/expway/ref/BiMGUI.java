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

package com.expway.ref;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import java.awt.*;
import java.awt.event.*;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.IOException;

import com.expway.binarisation.GeneralBinaryHandler;
import com.expway.binarisation.GeneralDecompressor;
import com.expway.binarisation.CodingParameters;

import java.net.URL;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import com.expway.util.TextAreaOutputStream;
import com.expway.util.Path;

public class BiMGUI extends GeneralBinaryHandler {
    static JFrame jFrame;

    static String sXMLDefaultFile="";
    static String sOutputDefaultFile="";

    FileTextField fTFXMLInputFile;
    FileTextField fTFOutputFile;

    TextAreaOutputStream taos;

    JProgressBar jEncodingProgress;
    JButton jBStartEncoding;
    JButton jBStartDecoding;
    static EncoderThread encoderThread=null;
    static DecoderThread decoderThread=null;

    public Component createComponents() {
        JPanel mainPane= new JPanel();
        mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));

        JPanel firstLine=new JPanel();
        firstLine.setLayout(new FlowLayout());
        mainPane.add(firstLine);

        try {
            URL urlIcon=(ClassLoader.getSystemClassLoader()).getResource("data/logompeg7.jpg");
            ImageIcon image=new ImageIcon(urlIcon);
            JLabel jLogo=new JLabel(image);        
            firstLine.add(jLogo);
        } catch(Exception e) {}

        JPanel buttonRow = new JPanel();
        buttonRow.setLayout(new GridLayout(0,1));
        firstLine.add(buttonRow);

        fTFOutputFile=new FileTextField("Output file :","bin",sOutputDefaultFile,this);
        fTFXMLInputFile=new FileTextField("XML Input file :","xml",sXMLDefaultFile,this);

        buttonRow.add(fTFXMLInputFile,BorderLayout.EAST);
        buttonRow.add(fTFOutputFile,BorderLayout.EAST);

        JPanel middleLine=new JPanel();
        middleLine.setLayout(new FlowLayout());
        mainPane.add(middleLine);

        JTextArea jOutput=new JTextArea(20,80);
        JScrollPane jScrollPane=new JScrollPane(jOutput,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jOutput.setFont(new Font("Monospaced",Font.PLAIN,12));
        jOutput.setEditable(false);
        middleLine.add(jScrollPane);   

        taos=new TextAreaOutputStream(jOutput,jScrollPane);

        System.setOut(new PrintStream(taos));
        System.setErr(new PrintStream(taos));

        JPanel secondLine=new JPanel();
        secondLine.setLayout(new FlowLayout());
        mainPane.add(secondLine);
        jBStartEncoding = new JButton("Start encoding");
        jBStartEncoding.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    startEncoding();
                }
            });
        secondLine.add(jBStartEncoding);

        jBStartDecoding = new JButton("Start decoding");
        jBStartDecoding.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    startDecoding();
                }
            });
        secondLine.add(jBStartDecoding);

        jEncodingProgress=new JProgressBar();
        jEncodingProgress.setVisible(false);
        jEncodingProgress.setStringPainted(true);
        secondLine.add(jEncodingProgress);

        JPanel buttonsPanel=new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        middleLine.add(buttonsPanel);

        /*
        JPanel buttonsPanel1=new JPanel();
        buttonsPanel1.setLayout(new BoxLayout(buttonsPanel1, BoxLayout.Y_AXIS));
        buttonsPanel1.setBorder(BorderFactory.createTitledBorder(""));
        buttonsPanel.add(buttonsPanel1);
        buttonsPanel.add(Box.createVerticalStrut(30));
        */

        JPanel buttonsPanel2=new JPanel();
        buttonsPanel2.setLayout(new BoxLayout(buttonsPanel2, BoxLayout.Y_AXIS));
        buttonsPanel2.setBorder(BorderFactory.createTitledBorder("Warning - For development purposes"));
        buttonsPanel.add(buttonsPanel2);

        JCheckBox jc;


        jc=new JCheckBox("ZLib strings instead of UTF-8 strings",CodingParameters.bSpecificCodecs);
        buttonsPanel2.add(jc);
        jc.addItemListener(new ItemListener(){public void itemStateChanged(ItemEvent e) {
            int i=e.getStateChange();
            if (i==ItemEvent.SELECTED) CodingParameters.bSpecificCodecs=true;
            if (i==ItemEvent.DESELECTED) CodingParameters.bSpecificCodecs=false;
        } });

        

        

        

        jc=new JCheckBox("Altkom - SpecificVideoCodecs",CodingParameters.bSpecificVideoCodecs);
        buttonsPanel2.add(jc);
        jc.addItemListener(new ItemListener(){public void itemStateChanged(ItemEvent e) {
            int i=e.getStateChange();
            if (i==ItemEvent.SELECTED) CodingParameters.bSpecificVideoCodecs=true;
            if (i==ItemEvent.DESELECTED) CodingParameters.bSpecificVideoCodecs=false;
        } });

        jc=new JCheckBox("Siemens - SpecificTimeDatatypes",CodingParameters.bSpecificTimeDatatypes);
        buttonsPanel2.add(jc);
        jc.addItemListener(new ItemListener(){public void itemStateChanged(ItemEvent e) {
            int i=e.getStateChange();
            if (i==ItemEvent.SELECTED) CodingParameters.bSpecificTimeDatatypes=true;
            if (i==ItemEvent.DESELECTED) CodingParameters.bSpecificTimeDatatypes=false;
        } });

        boolean bBinaryNavigationMode;
        if (BiMCommandLine.iNavigationPathMode==Path.SIEMENS_NAVIGATION_PATH) bBinaryNavigationMode=true;
        else bBinaryNavigationMode=false;

        
        
        return mainPane;
    }

    public void startEncoding() {
        if (encoderThread==null) {
            encoderThread=new EncoderThread(this);
            encoderThread.start();
        }
    }

    public void startDecoding() {
        if (decoderThread==null) {
            decoderThread=new DecoderThread(this);
            decoderThread.start();
        }
    }

    public void refreshProgressBar(int iLine) {
        jEncodingProgress.setValue(iLine);
        jEncodingProgress.setString("Encoding ... line "+iLine+"/"+jEncodingProgress.getMaximum());
    }

    public void refreshProgressBar() {
        jEncodingProgress.setValue(0);
        jEncodingProgress.setString("Reading schemas...");
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) { }

        if (args.length == 1){
            sXMLDefaultFile=args[0];
            sOutputDefaultFile=args[0]+".bin";
        }
            
        jFrame = new JFrame("BiMGUI");

        BiMGUI bimGUI=new BiMGUI();

        Component contents = bimGUI.createComponents();
        jFrame.getContentPane().add(contents, BorderLayout.CENTER);

        jFrame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    BiMGUI.encoderThread=null;
                    System.exit(0);
                }
            });
        
        jFrame.pack();
        jFrame.setVisible(true);
    }

    public void startElement(String uri, String local, String raw, Attributes attrs) throws SAXException {
        super.startElement(uri,local,raw,attrs);
        refreshProgressBar(locator.getLineNumber());
    }
}

class EncoderThread extends Thread implements Runnable {
    BiMGUI bimGUI;

    EncoderThread(BiMGUI b) {
        bimGUI=b;
    }

    public void run() {

        bimGUI.jBStartEncoding.setEnabled(false);
        bimGUI.jBStartDecoding.setEnabled(false);
        String xml=bimGUI.fTFXMLInputFile.getText();
        String out=bimGUI.fTFOutputFile.getText();

        // Compte le nombre de lignes du fichier XML
        int iXMLFileNbLignes=0;
        LineNumberReader fXML=null;
        try {
            fXML=new LineNumberReader(new FileReader(bimGUI.fTFXMLInputFile.getText()));
            while (fXML.readLine()!=null) {
                iXMLFileNbLignes++;
            }
            fXML.close();
        } catch (Exception e) {
        }

        bimGUI.taos.clear();
        bimGUI.jEncodingProgress.setMinimum(0);
        bimGUI.jEncodingProgress.setMaximum(iXMLFileNbLignes);    
        bimGUI.refreshProgressBar();
        bimGUI.jEncodingProgress.setVisible(true);    
    
        try {
            // Vide les schéma
            bimGUI.clearSetOfDefinitions();
            bimGUI.initialise();           
            bimGUI.setInput(xml);
            bimGUI.setOutput(out);
            bimGUI.binarise();
        } catch(Exception e) {
            System.out.println(e);
            e.printStackTrace(System.err);
        }

        bimGUI.taos.moveToEnd();

        bimGUI.jBStartEncoding.setEnabled(true);
        bimGUI.jBStartDecoding.setEnabled(true);
        bimGUI.encoderThread=null;
        bimGUI.jEncodingProgress.setVisible(false);
    }
}

class DecoderThread extends Thread implements Runnable {
    BiMGUI bimGUI;
    GeneralDecompressor gd;

    DecoderThread(BiMGUI b) {
        bimGUI=b;
        gd=new GeneralDecompressor();
    }

    public void run() {
        bimGUI.jBStartDecoding.setEnabled(false);
        bimGUI.jBStartEncoding.setEnabled(false);

        String xml=bimGUI.fTFXMLInputFile.getText()+"_D.xml";
        String bim=bimGUI.fTFOutputFile.getText();

        bimGUI.taos.clear();
        bimGUI.jEncodingProgress.setMinimum(0);
        bimGUI.jEncodingProgress.setMaximum(100);    
        bimGUI.jEncodingProgress.setVisible(false);    
    
        try {
            // Vide les schéma
            bimGUI.clearSetOfDefinitions();
            bimGUI.initialise(); 
            gd.setInput(bim);          
            gd.setOutput(xml);
            System.out.println("Decompressing "+bim+" ..."); 
            gd.decompress();
            System.out.println("Decompression OK : output filename="+xml);
        } catch(Exception e) {
            System.out.println(e);
            e.printStackTrace(System.err);
        }

        bimGUI.taos.moveToEnd();

        bimGUI.jBStartEncoding.setEnabled(true);
        bimGUI.jBStartDecoding.setEnabled(true);
        bimGUI.decoderThread=null;
        bimGUI.jEncodingProgress.setVisible(false);
    }


}

class FileTextFieldActionListener implements ActionListener {
    String sExtension;
    JTextField jTextField;
    FileTextField fTextField,fOutput;

    FileTextFieldActionListener(String s, JTextField j, FileTextField f,FileTextField fo) {
        sExtension=s;
        jTextField=j;
        fTextField=f;
        fOutput=fo;
    }

    public void actionPerformed(ActionEvent e) {
        // @@ path à extraire du défaut
        FileDialog fileDialog = new FileDialog(BiMGUI.jFrame);
        fileDialog.setMode(FileDialog.LOAD);
        
            fileDialog.setFile("*."+sExtension);
        fileDialog.show();
        String sNomComplet=fileDialog.getDirectory()+fileDialog.getFile();
        if (fileDialog.getFile()!=null) {
            jTextField.setText(sNomComplet);

            // Update du nom du fichier de sortie si c'est le field input
            if (fTextField!=fOutput) {
                fOutput.jTextField.setText(sNomComplet+".bin");
            }
        }
    }
}

class FileTextField extends JPanel {
    String sExtension;
    JTextField jTextField;

    public FileTextField(String label,String ext,String def,BiMGUI bimGUI) {
        sExtension=ext;

        final JLabel jLabel=new JLabel(label);
        jTextField=new JTextField(def,30);        
        jLabel.setLabelFor(jTextField);
        
        JButton jButton = new JButton("Choose");
        jButton.addActionListener(new FileTextFieldActionListener(sExtension,jTextField,this,bimGUI.fTFOutputFile));

        setLayout(new FlowLayout());
        add(jLabel);
        add(jTextField);
        add(jButton);
    }

    public String getText() {
        return jTextField.getText(); 
    }
}
