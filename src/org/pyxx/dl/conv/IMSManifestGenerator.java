package org.pyxx.dl.conv;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.DocumentHelper;
import org.dom4j.XPath;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;


/**
 * Date: 24.07.2004
 * Time: 15:16:26
 */


/*
For manifest:
	@identifier = "MANIFEST_X"
	metadata/imsmd:lom/imsmd:general/ibmls:identifier/text() = "X"
	organizations/organization/@identifier = "ORG_*"
	organizations/organization/title/text() = book title
	organizations/organization/metadata/imsmd:lom/imsmd:general/imsmd:title/imsmd:langstring/text()
    organizations/organization/metadata/imsmd:lom/imsmd:general/imsmd:description/imsmd:langstring/text()
*/
public class IMSManifestGenerator {

    private static final int ID_SIZE = 32;
    private static final String RESOURCE_DIR = "resources";

    private Document manifest;
    private Document manifestTemplate;
    private Document itemTemplate;
    private Element resElement;
    private HashMap resources;

    public IMSManifestGenerator(Document manifestTemplate, Document itemTemplate) {
        this.manifestTemplate = manifestTemplate;
        this.itemTemplate = itemTemplate;
        resources = new HashMap();
        manifest = DocumentHelper.createDocument();
        manifest.add(manifestTemplate.getRootElement().createCopy());
        resElement = manifest.getRootElement().element("resources");
    }

    public static Map getImsNamespacesMap(String defns, String defnsuri) {
        Map nsmap = new HashMap();
        nsmap.put(defns, defnsuri);
        nsmap.put("adlcp", "http://www.adlnet.org/xsd/adlcp_rootv1p2");
        nsmap.put("ibmls", "http://www.ibm.com/learningspace");
        nsmap.put("imsmd", "http://www.imsglobal.org/xsd/imsmd_v1p2");
        nsmap.put("imsss", "http://www.imsglobal.org/xsd/imsss");
        nsmap.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        return nsmap;
    }

    public static String generateID(int size) {
        StringBuffer sb = new StringBuffer(size);
        for(int i = 0; i < size; i++) {
            int code = (int) Math.round((Math.random() * 15));
            sb.append(Integer.toHexString(code));
        }
        return sb.toString().toUpperCase();
    }

    public void setGeneral(String title, String description, String reshref) {
        XPath xpath = manifest.createXPath("/imscp:manifest/imscp:organizations/imscp:organization/imscp:title");
        xpath.setNamespaceURIs(Collections.singletonMap("imscp", "http://www.imsglobal.org/xsd/imscp_v1p1"));
        Element xpres = (Element) xpath.selectSingleNode(manifest);
        if(xpres != null) xpres.setText(title);

        Map nsmap = org.pyxx.dl.conv.IMSManifestGenerator.getImsNamespacesMap("imscp", "http://www.imsglobal.org/xsd/imscp_v1p1");

/*
		xpath = manifest.createXPath("/imscp:manifest/imscp:organizations/imscp:organization/imscp:metadata/imsmd:lom/imsmd:general/imsmd:title/imsmd:langstring");
		xpath.setNamespaceURIs(nsmap);
		xpres = (Element) xpath.selectSingleNode(manifest);
		if(xpres != null) xpres.setText(title);
*/

/*
		xpath = manifest.createXPath("/imscp:manifest/imscp:organizations/imscp:organization/imscp:metadata/imsmd:lom/imsmd:general/imsmd:description/imsmd:langstring");
		xpath.setNamespaceURIs(nsmap);
		xpres = (Element) xpath.selectSingleNode(manifest);
		if(xpres != null) xpres.setText(description);
*/

        String bookid = org.pyxx.dl.conv.IMSManifestGenerator.generateID(org.pyxx.dl.conv.IMSManifestGenerator.ID_SIZE);
        String orgid = org.pyxx.dl.conv.IMSManifestGenerator.generateID(org.pyxx.dl.conv.IMSManifestGenerator.ID_SIZE);

        manifest.getRootElement().addAttribute("identifier", "MANIFEST_" + bookid);

        xpath = manifest.createXPath("/imscp:manifest/imscp:organizations");
        xpath.setNamespaceURIs(nsmap);
        xpres = (Element) xpath.selectSingleNode(manifest);
        if(xpres != null) xpres.addAttribute("default", "ORG_" + orgid);

        xpath = manifest.createXPath("/imscp:manifest/imscp:organizations/imscp:organization");
        xpath.setNamespaceURIs(nsmap);
        xpres = (Element) xpath.selectSingleNode(manifest);
        if(xpres != null) xpres.addAttribute("identifier", "ORG_" + orgid);

        //getOrganization().addAttribute("identifierref", requestResourceId(reshref));
        addItem(getOrganization(), title, description, reshref);
    }

    public Element getOrganization() {
        XPath xpath = manifest.createXPath("/imscp:manifest/imscp:organizations/imscp:organization");
        xpath.setNamespaceURIs(Collections.singletonMap("imscp", "http://www.imsglobal.org/xsd/imscp_v1p1"));
        Element xpres = (Element) xpath.selectSingleNode(manifest);
        return xpres;
    }

/*
Set for item:
	@identifier = "ITEM_[an]{32}"
	@identifierref
	title/text()
	metadata/imsmd:lom/imsmd:general/imsmd:title/imsmd:langstring/text()
	metadata/imsmd:lom/imsmd:general/imsmd:description/imsmd:langstring/text()
*/

    public Element addItem(Element parent, String title, String descr, String reshref) {
        Element tempItem = itemTemplate.getRootElement().createCopy();
        Document temp = DocumentHelper.createDocument();
        temp.add(tempItem);
        //make changes on temp
        tempItem.addAttribute("identifier", "ITEM_" + org.pyxx.dl.conv.IMSManifestGenerator.generateID(org.pyxx.dl.conv.IMSManifestGenerator.ID_SIZE));
        tempItem.addAttribute("identifierref", requestResourceId(reshref));
        Map nsmap = org.pyxx.dl.conv.IMSManifestGenerator.getImsNamespacesMap("imscp", "http://www.imsglobal.org/xsd/imscp_v1p1");

        XPath xpath = temp.createXPath("/imscp:item/imscp:title");
        xpath.setNamespaceURIs(nsmap);
        Element xpres = (Element) xpath.selectSingleNode(temp);
        if(xpres != null) xpres.setText(title);

/*
		xpath = temp.createXPath("/imscp:item/imscp:metadata/imsmd:lom/imsmd:general/imsmd:title/imsmd:langstring");
		xpath.setNamespaceURIs(nsmap);
		xpres = (Element) xpath.selectSingleNode(temp);
		if(xpres != null) xpres.setText(title);

		xpath = temp.createXPath("/imscp:item/imscp:metadata/imsmd:lom/imsmd:general/imsmd:description/imsmd:langstring");
		xpath.setNamespaceURIs(nsmap);
		xpres = (Element) xpath.selectSingleNode(temp);
		if(xpres != null) xpres.setText(descr);

*/

        Element item = tempItem.createCopy();
        parent.add(item);

        return item;
    }
/*
	<resource
	identifier="resource_1"
	type="webcontent"
	adlcp:scormtype="asset"
	href="resources/resource_1/yaga.html">
	  <file href="resources/resource_1/yaga.html"/>
	</resource>
*/

    public void addResource(String id, String href) {
        resources.put(href, id);
        String chref = org.pyxx.dl.conv.IMSManifestGenerator.RESOURCE_DIR + "/" + href;
        resElement.addElement("resource")
                .addAttribute("identifier", id)
                .addAttribute("type", "webcontent")
                .addAttribute("adlcp:scormtype", "asset")
                .addAttribute("href", chref)
                .addElement("file")
                .addAttribute("href", chref);
    }

    public String getResourceId(String href) {
        return (String) resources.get(href);
    }

    public String requestResourceId(String href) {
        String ret = getResourceId(href);
        if(ret == null) {
            ret = "resource_" + (resources.size() + 1);
            addResource(ret, href);
        }
        return ret;
    }

    public Document getManifest() {
        return manifest;
    }

}
