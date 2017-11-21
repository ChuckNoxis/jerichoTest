package me.desnoulez;

import net.htmlparser.jericho.*;

import java.net.URL;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception
    {
        init();
        Source source = new Source(new URL(parseArgs(args)));

        // Call fullSequentialParse manually as most of the source will be parsed.
        source.fullSequentialParse();

        printTitle(source);
        printLinks(source);
        //printTextExcluding(source);
        printText(source);
        //printTextSpecific(source);
    }

    private static void init()
    {
        MicrosoftConditionalCommentTagTypes.register();
        PHPTagTypes.register();
        PHPTagTypes.PHP_SHORT.deregister(); // remove PHP short tags for this example otherwise they override processing instructions
        MasonTagTypes.register();
    }

    private static String parseArgs(String[] args)
    {
        String sourceUrlString = "https://en.wikipedia.org/wiki/Test";

        if (args.length == 0)
            System.err.println("Using default argument of \"" + sourceUrlString + '"' + '\n');
        else
            sourceUrlString = args[0];
        if (sourceUrlString.indexOf(':') == -1)
            sourceUrlString = "file:" + sourceUrlString;
        return sourceUrlString;
    }

    private static void printText(Source source)
    {
        System.out.println("\nAll text:");
        System.out.println(source.getTextExtractor().setExcludeNonHTMLElements(false).toString());
    }

    private static void printTextSpecific(Source source)
    {
        System.out.println("\nText extend the TextExtractor class to also exclude text from P elements and any elements with class=\"control\":\n");
        TextExtractor textExtractor=new TextExtractor(source) {
            public boolean excludeElement(StartTag startTag) {
                return startTag.getName()==HTMLElementName.P || "control".equalsIgnoreCase(startTag.getAttributeValue("class"));
            }
        };
        System.out.println(textExtractor.setIncludeAttributes(true).toString());
    }

    private static void printTextExcluding(Source source)
    {
        System.out.println("\nAll text from file (exluding content inside SCRIPT and STYLE elements):\n");
        System.out.println(source.getTextExtractor().setIncludeAttributes(true).toString());
    }

    private static void printLinks(Source source)
    {
        System.out.println("\nLinks to other documents:");
        List<net.htmlparser.jericho.Element> links = getLinks(source);
        for (Element linkElement : links)
        {
            String href = linkElement.getAttributeValue("href");
            if (href == null)
                continue;
            // A element can contain other tags so need to extract the text from it:
            String label = linkElement.getContent().getTextExtractor().toString();
            if (!href.startsWith("#") && !href.endsWith("#")) {
                if (label.isEmpty())
                    System.out.println("(none)" + " <" + href + '>');
                else
                    System.out.println(label + " <" + href + '>');
            }
        }
    }

    private static void printTitle(Source source)
    {
        System.out.println("Document title:");
        String title = getTitle(source);
        System.out.println(title == null ? "(none)" : title);
    }

    private static List<net.htmlparser.jericho.Element> getLinks(Source source)
    {
        List<net.htmlparser.jericho.Element> links = source.getAllElements(net.htmlparser.jericho.HTMLElementName.A);
        return (links);
    }

    private static String getTitle(Source source)
    {
        Element titleElement = source.getFirstElement(HTMLElementName.TITLE);
        if (titleElement == null)
            return (null);
        // TITLE element never contains other tags so just decode it collapsing whitespace:
        return (CharacterReference.decodeCollapseWhiteSpace(titleElement.getContent()));
    }
}