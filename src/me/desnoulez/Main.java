package me.desnoulez;

import net.htmlparser.jericho.*;

import java.net.URL;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        String sourceUrlString = "https://en.wikipedia.org/wiki/Test";
        if (args.length == 0)
            System.err.println("Using default argument of \"" + sourceUrlString + '"');
        else
            sourceUrlString = args[0];
        if (sourceUrlString.indexOf(':') == -1) sourceUrlString = "file:" + sourceUrlString;
        MicrosoftConditionalCommentTagTypes.register();
        PHPTagTypes.register();
        PHPTagTypes.PHP_SHORT.deregister(); // remove PHP short tags for this example otherwise they override processing instructions
        MasonTagTypes.register();
        Source source = new Source(new URL(sourceUrlString));

        // Call fullSequentialParse manually as most of the source will be parsed.
        source.fullSequentialParse();

        System.out.println("Document title:");
        String title = getTitle(source);
        System.out.println(title == null ? "(none)" : title);

        System.out.println("\nLinks to other documents:");
        List<net.htmlparser.jericho.Element> linkElements = source.getAllElements(net.htmlparser.jericho.HTMLElementName.A);
        for (Element linkElement : linkElements) {
            String href = linkElement.getAttributeValue("href");
            if (href == null) continue;
            // A element can contain other tags so need to extract the text from it:
            String label = linkElement.getContent().getTextExtractor().toString();
            if (!href.startsWith("#"))
                System.out.println(label + " <" + href + '>');
        }

        System.out.println("\nAll text from file (exluding content inside SCRIPT and STYLE elements):\n");
        System.out.println(source.getTextExtractor().setIncludeAttributes(true).toString());

//        System.out.println("\nSame again but this time extend the TextExtractor class to also exclude text from P elements and any elements with class=\"control\":\n");
//        TextExtractor textExtractor=new TextExtractor(source) {
//            public boolean excludeElement(StartTag startTag) {
//                return startTag.getName()==HTMLElementName.P || "control".equalsIgnoreCase(startTag.getAttributeValue("class"));
//            }
//        };
//        System.out.println(textExtractor.setIncludeAttributes(true).toString());
    }

    private static String getTitle(Source source) {
        Element titleElement = source.getFirstElement(HTMLElementName.TITLE);
        if (titleElement == null) return null;
        // TITLE element never contains other tags so just decode it collapsing whitespace:
        return CharacterReference.decodeCollapseWhiteSpace(titleElement.getContent());
    }

    private static String getMetaValue(Source source, String key) {
        for (int pos = 0; pos < source.length(); ) {
            StartTag startTag = source.getNextStartTag(pos, "name", key, false);
            if (startTag == null) return null;
            if (startTag.getName().equals(HTMLElementName.META))
                return startTag.getAttributeValue("content"); // Attribute values are automatically decoded
            pos = startTag.getEnd();
        }
        return null;
    }
}