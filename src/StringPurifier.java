
import java.util.ArrayList;
import java.util.HashSet;

import kacteil.kma.MorphemeAnalysis;

public class StringPurifier {
	

	
		public StringPurifier(){}
		
		
		public String getPurifiedString(String sen){
			return	replaceMeaninglessWords(replaceSpecialWords( replaceBracket( sen) ));
		}
		
		public String getPurifiedBfMorph(String set){
			return replaceBracket(replaceMphMarks(set));
		}
		private String replaceMphMarks(String sen)
		{
			return sen
					.replaceAll("\\+"," ")
					.replaceAll("\\/"," / ")
					;
		}
		public String replaceBracket(String sen){
			return  sen
					//.replaceAll("\\[", "(").replaceAll("\\]", ")")
					//.replaceAll("\\{", "(").replaceAll("\\}", ")")
					.replace("\\("," (" ).replaceAll("\\)",") ")
					.replaceAll("\\[", " [").replaceAll("\\]", "] ")
					;
		}
		
		public String replaceSpecialWords(String sen){
			return	sen
					.replaceAll("¶", " ")
					.replaceAll("\t", " ").replaceAll("\n", " ")
					.replaceAll("=>", "→")
					//.replaceAll("ː", "").replaceAll(":", " ")
					.replaceAll("？", "?").replaceAll("！", "!")
					.replaceAll("→", "→ ").replaceAll("←", "← ")
					.replaceAll("¶", " ").replaceAll("＝", "=")
					.replaceAll("×", "× ")
					
					.replaceAll("\\^", "")
					.replaceAll("~", "-")
					;
		}

		public String replaceMeaninglessWords(String sen){
			return	sen
					.replaceAll("어떤어떤", "-")
					.replaceAll("무슨무슨", "-")
					.replaceAll("언제언제", "-")
					.replaceAll("어디어디", "-")
					.replaceAll("누구누구", "-")
					.replaceAll("뭐라뭐라", "-")
					.replaceAll("블라블라", "-")
					;
		}

		// Get all words in separator character, such as quotation('), in the sentence
		// s = "abc 'def' and 'fgh' ijk";	return => {def, fgh}
		public ArrayList<String> getSeparatedWordList(String s, char sep){
			ArrayList<String> list = new ArrayList<String>();
			int a, b = -1;

			while( (a = s.indexOf(sep, b+1) ) >= 0){
				b = s.indexOf(sep, a+1);
				list.add(s.substring(a+1, b));
			}
			
			return list;
		}
		
		public HashSet<String> getBrackedWordList(String s){
			HashSet<String> list = new HashSet<String>();
			int a, b = -1;

			while( (a = s.indexOf('(', b+1) ) >= 0){
				b = s.indexOf(')', a+1);
				list.add(s.substring(a+1, b));
			}
			
			return list;
		}

		// return String sen in <Tag>sen</Tag>
		public String getAttribute(String body, String Tag){
			int idx1, idx2;
			idx1 = 0 + Tag.length() + 2;
			idx2 = body.indexOf("</" + Tag + ">");
			
			if(idx1 >= idx2 || idx2 < 0)
				return "";
			return body.substring(idx1, idx2);
		}
		
		public String getReplacedStringForMFWord(String sen){
			return sen
					.replaceAll
					("'", "").replaceAll("`", "")
					.replaceAll("“", "").replaceAll("”", "").replaceAll("\"", "")
					.replaceAll("／","/")
					.replaceAll("/", " / ")
					.replaceAll("[+]", " + ")
					.replaceAll("[.]", "")
					.replaceAll("-\\)", " / ")
					.replaceAll(",", ", ")
					.replaceAll("~", "-")
				;
		}
		
		public String getSeparatorForMFWord(){
			return " \t?!";
		}


}
