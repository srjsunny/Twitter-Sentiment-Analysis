package analyze;




public class specialSymbolRemover {

public static String remove(String s)
{
                       //removing urls
	String processed = s.replaceAll("(http[^ ]*)|(www\\.[^ ]*)", "").replaceAll("[^a-zA-Z\\s]", "").replaceAll("\\s+", " ").trim();
	return processed;
	
}	
}
