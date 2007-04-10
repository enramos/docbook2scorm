package org.pyxx.dl.conv;


import java.util.Hashtable;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.File;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.xml.sax.EntityResolver;
import org.apache.xml.resolver.tools.CatalogResolver;

/**
 * Date: 21.07.2004
 * Time: 20:47:30
 *
 * Parameters:
 *  source xml
 *  tempdir
 *  depth
 *  outfile
 *  docbook xsl
 *  resources dir
 *
 *
 * Process:
 *  xsl transorm parameters - to ensure chunking depth
 *  walk through the tree - set chunk names (dbhtml), creating organizations
 *  add resources to manifest
 *  xsl transorm
 *  save manifest, copy resources, zip, clean
 *
 *
 */
public class docbook2scorm {

    public static String[] orgEN = {"chapter", "section", "part", "sect1", "sect2", "sect3", "appendix", "preface", "glossary"};
    public static String[] numEN = {"chapter", "section"};

    public static void main(String[] args) {
        int returncode = 0;
        //c:\bulka\simurg\temp\conv\out

        // Verify and assign parameters
        Hashtable params = ParamsHelper.parseSimplePairs(args);

        String command = "manifest";
        String sourceFileName = (String) params.get("-src");
        String packageFileName = null;
        String etcDir = (String) params.get("-etc");
        String manifestTemplateFileName = etcDir + "/imsmanifest-template.xml";
        String itemTemplateFileName = etcDir + "/item-template.xml";
        String tempDir = (String) params.get("-tmp");
        String resourcesDir = (String) params.get("-res");;
        int chunkDepth = 3;
        String chunkDepthStr = (String) params.get("-depth");;
        if(chunkDepthStr != null) {
            chunkDepth = Integer.parseInt(chunkDepthStr);
        }
        boolean numberBranches = false;
        String numberBranchesStr = (String) params.get("-number");;
        if(numberBranchesStr != null) {
            if(numberBranchesStr.equals("yes")) numberBranches = true;
        }

        // Read the source
        System.out.println("Parsing the source: " + sourceFileName);
        Document sourceDoc = null;
        try {

            EntityResolver resolver = new CatalogResolver();
            SAXReader reader = new SAXReader();
            reader.setEntityResolver(resolver);
            sourceDoc = reader.read(sourceFileName);
        }
        catch(Exception e) {
            System.out.println("Error reading the source - " + sourceFileName);
            e.printStackTrace();
            returncode = 1;
        }

        // Read templates

        Document manifestTemplateDoc = null;
        Document itemTemplateDoc = null;
        if(returncode == 0) {
            System.out.println("Parsing templates... ");
            try {
                SAXReader reader = new SAXReader(false);
                manifestTemplateDoc = reader.read(manifestTemplateFileName);
            }
            catch(Exception e) {
                System.out.println("Error reading the file - " + manifestTemplateFileName);
                e.printStackTrace();
                returncode = 1;
            }

            try {
                SAXReader reader = new SAXReader(false);
                itemTemplateDoc = reader.read(itemTemplateFileName);
            }
            catch(Exception e) {
                System.out.println("Error reading the file - " + itemTemplateFileName);
                e.printStackTrace();
                returncode = 1;
            }
        }


        // Generate manifest and add dbhtml chunking directives to the sourceDoc

        Document manifestDoc = null;
        String tempSourceFileName = tempDir + "/source.xml";
        if(returncode == 0) {
            System.out.println("Generating IMS Organizations...");
            try {
                manifestDoc = org.pyxx.dl.conv.docbook2scorm.makeImsManifest(sourceDoc, manifestTemplateDoc, itemTemplateDoc, chunkDepth, resourcesDir, numberBranches);

                OutputFormat format = OutputFormat.createCompactFormat();
                format.setTrimText(false);
                //format.setPadText(true);
                FileOutputStream tempos = new FileOutputStream(tempSourceFileName);
                XMLWriter writer = new XMLWriter(new OutputStreamWriter(tempos, "UTF-8"), format);
                writer.write(sourceDoc);
                writer.close();
                tempos.close();
            }
            catch(Exception e) {
                System.out.println("Error generating manifest and docbook xml.");
                returncode = 1;
            }

        }

        // Write manifest to the tempDir
        if(returncode == 0) {
            String manifestFileName = tempDir + "/imsmanifest.xml";
            System.out.println("Writing IMS Manifest: " + manifestFileName);
            try {
                OutputFormat format = OutputFormat.createPrettyPrint();
                XMLWriter writer = new XMLWriter(new OutputStreamWriter(new FileOutputStream(manifestFileName), "UTF-8"), format);
                writer.write(manifestDoc);
                writer.close();
            }
            catch(Exception e) {
                System.out.println("Error writing manifest - " + manifestFileName);
                returncode = 1;
            }
        }


        //return returncode;
    }

    public static boolean isStructureElement(Element element) {
        for(int i = 0; i < org.pyxx.dl.conv.docbook2scorm.orgEN.length; i++) {
            if(org.pyxx.dl.conv.docbook2scorm.orgEN[i].equals(element.getName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNumberedElement(Element element) {
        for(int i = 0; i < org.pyxx.dl.conv.docbook2scorm.numEN.length; i++) {
            if(org.pyxx.dl.conv.docbook2scorm.numEN[i].equals(element.getName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAppendixElement(Element element) {
        if("appendix".equals(element.getName())) {
            return true;
        }
        return false;
    }



    public static void walk(IMSManifestGenerator manGen, Element item, Element element, int depth, String resname, boolean numberBranches, String prefix) {
        int celnum = 0;
        int snumber = 0;
        char apnumber = 0;
        for ( int i = 0, size = element.nodeCount(); i < size; i++ ) {
            Node node = element.node(i);
            if ( node instanceof Element ) {
                Element celement = (Element) node;
                if(org.pyxx.dl.conv.docbook2scorm.isStructureElement(celement)) {
                    celnum++;
                    String cPrefix = "";
                    String tPrefix = "";
                    if(numberBranches) {
                        if(org.pyxx.dl.conv.docbook2scorm.isNumberedElement(celement)) {
                            snumber++;
                            cPrefix = prefix + snumber + ".";
                            tPrefix = cPrefix + " ";
                        }
                        else if(org.pyxx.dl.conv.docbook2scorm.isAppendixElement(celement)) {
                            apnumber ++;
                            char cnum = (char)(apnumber + 'A' - 1);
                            cPrefix = prefix + new Character(cnum) + ".";
                            tPrefix = cPrefix + " ";
                        }
                    }
                    Element titleEl = celement.element("title");
                    String title = "";
                    if(titleEl != null) title = titleEl.getText();
                    title = tPrefix + title;
                    //System.out.println(tPrefix + title);
                    Element descriptionEl = celement.element("abstract");
                    String description = "";
                    if(descriptionEl != null) description = descriptionEl.getText();

                    String cresname = resname + celement.getName().substring(0, 2) + celnum;
                    String reshref = cresname + ".html";
                    celement.addProcessingInstruction("dbhtml", "filename=\"" + reshref + "\"");

                    Element citem = manGen.addItem(item, title, description, reshref);
                    if(depth > 1) org.pyxx.dl.conv.docbook2scorm.walk(manGen, citem, celement, depth - 1, cresname, numberBranches, cPrefix);
                }
            }
        }
    }

    public static Document makeImsManifest(Document source, Document manifestTemplate, Document itemTemplate, int chunkDepth, String resourcesDir, boolean numberBranches) {
        IMSManifestGenerator manGen = new IMSManifestGenerator(manifestTemplate, itemTemplate);
        Element titleEl = (Element) source.selectSingleNode("/book/bookinfo/title");
        Element descriptionEl = (Element) source.selectSingleNode("/book/bookinfo/abstract");
        String title = "";
        if(titleEl != null) title = titleEl.getText();
        String description = "";
        if(descriptionEl != null) description = descriptionEl.getText();
        manGen.setGeneral(title, description, "index.html");
        org.pyxx.dl.conv.docbook2scorm.walk(manGen, manGen.getOrganization(), source.getRootElement(), chunkDepth, "", numberBranches, "");

        org.pyxx.dl.conv.docbook2scorm.resWalk(new File(resourcesDir), "", manGen);

        Document manifest = manGen.getManifest();
        return manifest;
    }

    public static void resWalk(File dir, String prefix, IMSManifestGenerator manGen) {
        File[] files = dir.listFiles();
        for(int i = 0; i < files.length; i++) {
            File file = files[i];
            if(file.isDirectory()) {
                org.pyxx.dl.conv.docbook2scorm.resWalk(file, prefix + file.getName() + "/", manGen);
            }
            else {
                manGen.requestResourceId(prefix + file.getName());
            }
        }
    }

}
