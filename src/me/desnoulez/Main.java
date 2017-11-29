package me.desnoulez;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import net.htmlparser.jericho.*;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Objects;

public class Main {
    public static void main(String[] args) throws Exception {
        init();
        Source source = new Source(new URL(parseArgs(args)));
        InputStream input = new URL(parseArgs(args)).openStream();

        // Call fullSequentialParse manually as most of the source will be parsed.
        source.fullSequentialParse();

        printTitle(source);
/*      printLinks(source);
        System.out.println();
        printImgs(source);*/
        //printText(source);
        printTextExcluding(source);
        //printTextSpecific(source);
        /*for (Segment s : source)
        {
            for (CharacterReference c : s.getAllCharacterReferences())
            {
                System.out.println(c.getChar());
            }
        }*/
        System.out.println("Detected Jericho Encoding : " + source.getEncoding());
        System.out.println("Detected ICU4J Encoding : " + detectEncoding(input));
    }

    private static String detectEncoding(InputStream in) throws
            Exception {
        CharsetDetector detector = new CharsetDetector();
        detector.setText(in);
        CharsetMatch charsetMatch = detector.detect();
        if (charsetMatch == null) {
            throw new Exception("Cannot detect source charset.");
        }
        return charsetMatch.getName();
    }

    private static void init() {
        MicrosoftConditionalCommentTagTypes.register();
        PHPTagTypes.register();
        PHPTagTypes.PHP_SHORT.deregister(); // remove PHP short tags for this example otherwise they override processing instructions
        MasonTagTypes.register();
    }

    private static String parseArgs(String[] args) {
        String sourceUrlString = "https://blogs.nasa.gov/spacestation/2017/09/02/touchdown-expedition-52-back-on-earth/";

        if (args.length == 0)
            System.err.println("Using default argument of \"" + sourceUrlString + '"' + '\n');
        else
            sourceUrlString = args[0];
        if (sourceUrlString.indexOf(':') == -1)
            sourceUrlString = "file:" + sourceUrlString;
        return sourceUrlString;
    }

    private static void printText(Source source) {
        System.out.println("\nAll text:\n");
        System.out.println(source.getTextExtractor().setExcludeNonHTMLElements(false).toString());
    }

    private static void printLinks(Source source) {
        System.out.println("\nLinks to other documents:\n");
        List<net.htmlparser.jericho.Element> links = getLinks(source);
        for (Element linkElement : links) {
            String href = linkElement.getAttributeValue("href");
            String name = linkElement.getAttributeValue("name");
            String title = linkElement.getAttributeValue("title");
            if (href == null)
                continue;
            // A element can contain other tags so need to extract the text from it:
            String label = linkElement.getContent().getTextExtractor().toString();
            if (!href.startsWith("#") && !href.endsWith("#")) {
                System.out.println("Href link : " + href);
                if (label.isEmpty())
                    System.out.println("Inside <a> : (none)");
                else
                    System.out.println("Inside <a> : " + label);
                if (name != null)
                    System.out.println("Name : " + name);
                if (title != null)
                    System.out.println("Title : " + title);
                System.out.println();
            }
        }
    }

    private static void printImgs(Source source) {
        System.out.println("\nImgs found:\n");
        List<Element> images = getImages(source);
        for (Element imgElement : images) {
            String alt = imgElement.getAttributeValue("alt");
            String longdesc = imgElement.getAttributeValue("longdesc");
            String title = imgElement.getAttributeValue("title");
            if ((alt == null) && (longdesc == null) && (title == null))
                continue;
            // A element can contain other tags so need to extract the text from it:
            String label = imgElement.getContent().getTextExtractor().toString();
            if (label.isEmpty())
                System.out.println("Inside <img> : (none)");
            else
                System.out.println("Inside <img> : " + label);
            if (alt != null)
                System.out.println("Alt : " + alt);
            if (longdesc != null)
                System.out.println("Longdesc : " + longdesc);
            if (title != null)
                System.out.println("Title : " + title);
            System.out.println();
        }
    }


    private static void printTitle(Source source) {
        System.out.println("Document title:");
        String title = getTitle(source);
        System.out.println(title == null ? "(none)" : title);
    }

    private static List<net.htmlparser.jericho.Element> getLinks(Source source) {
        return (source.getAllElements(net.htmlparser.jericho.HTMLElementName.A));
    }

    private static List<net.htmlparser.jericho.Element> getImages(Source source) {
        return (source.getAllElements(HTMLElementName.IMG));
    }

    private static String getTitle(Source source) {
        Element titleElement = source.getFirstElement(HTMLElementName.TITLE);
        if (titleElement == null)
            return (null);
        // TITLE element never contains other tags so just decode it collapsing whitespace:
        return (CharacterReference.decodeCollapseWhiteSpace(titleElement.getContent()));
    }

    private static void printTextSpecific(Source source) {
        System.out.println("\nText extend the TextExtractor class to also exclude text from P elements and any elements with class=\"control\":\n");
        TextExtractor textExtractor = new TextExtractor(source) {
            public boolean excludeElement(StartTag startTag) {
                return Objects.equals(startTag.getName(), HTMLElementName.P) || "control".equalsIgnoreCase(startTag.getAttributeValue("class"));
            }
        };
        System.out.println(textExtractor.setIncludeAttributes(true).toString());
    }

    private static void printTextExcluding(Source source) {
        System.out.println("\nAll text from file (exluding content inside SCRIPT and STYLE elements):\n");
        System.out.println(source.getTextExtractor().setIncludeAttributes(true).toString());
    }
}