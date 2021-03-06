import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Kj Nam
 * @since 2017-04-04
 */
public class CustomSplitter implements Splitter {

    @Override
    public String[] split(String formula) {
        String [] textNumbers = null;

        Matcher matcher = Pattern.compile("//(.)\n(.*)").matcher(formula);
        if (matcher.find()) {
            String customDelimiter = matcher.group(1);
            textNumbers =  matcher.group(2).split(customDelimiter);
        }

        return textNumbers;
    }

    @Override
    public boolean support(String formula) {
        return formula.matches("^\\/\\/(.+)\n((?:\\d+)(?:\\1\\d+)*)$");
    }
}
