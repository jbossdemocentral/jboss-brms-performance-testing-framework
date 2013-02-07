package org.jboss.brms.test.util;

import java.io.ByteArrayInputStream;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public final class XPathUtil {
    private static final String BPMN2_NAMESPACE_URI = "http://www.omg.org/spec/BPMN/20100524/MODEL";
    private static final String BPMN_PROCESS_ID_XPATH_EXPR = "//bpmn2:process[@id]";
    private static final String BPMN_PROCESS_ID_ATTR = "id";

    /** Private constructor to prevent instantiation. */
    private XPathUtil() {
    }

    public static String getProcessIdFromBpmn(final String bpmn) {
        String processId = null;
        try {
            final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true);
            final Document doc = domFactory.newDocumentBuilder().parse(new ByteArrayInputStream(bpmn.getBytes("UTF-8")));
            final XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(new NamespaceContext() {
                @Override
                public Iterator<?> getPrefixes(final String namespaceURI) {
                    // Not used in this context.
                    return null;
                }

                @Override
                public String getPrefix(final String namespaceURI) {
                    // Not used in this context.
                    return null;
                }

                @Override
                public String getNamespaceURI(final String prefix) {
                    // Only require the URI for the bpmn2 NS.
                    return BPMN2_NAMESPACE_URI;
                }
            });
            final XPathExpression expr = xpath.compile(BPMN_PROCESS_ID_XPATH_EXPR);

            final Node node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            processId = node.getAttributes().getNamedItem(BPMN_PROCESS_ID_ATTR).getNodeValue();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return processId;
    }
}
