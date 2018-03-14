

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import Utils.Utils;
import cc.mallet.fst.CRF;
import cc.mallet.types.Instance;
import cc.mallet.types.Sequence;
import kacteil.kma.MorphemeAnalysis;

public class TargetSelector {
	Utils ut;
	final static String inputdir="./traindata_txt/test_input2.txt";
	CRF crfMorph, crfSyll,crfMorph_new, crfSyll_new;
	public TargetSelector() throws Exception{
	
		// get morph trained data
		
		ut = new Utils();
		
		ObjectInputStream oisM= new ObjectInputStream(new FileInputStream("./crf/model_morph.crf"));
		crfMorph =  (CRF) oisM.readObject();
		oisM.close();
			    
			    // get syll trained data
		ObjectInputStream oisS= new ObjectInputStream(new FileInputStream("./crf/model_syll.crf"));
		crfSyll =  (CRF) oisS.readObject();
		oisS.close();
		
		ObjectInputStream oisMn= new ObjectInputStream(new FileInputStream("./crf/newModel/model_morph.crf"));
		crfMorph_new =  (CRF) oisMn.readObject();
		oisMn.close();
		
		ObjectInputStream oisSn= new ObjectInputStream(new FileInputStream("./crf/newModel/model_syll.crf"));
		crfSyll_new =  (CRF) oisSn.readObject();
		oisSn.close();
				
	}
	private static String change(String st){
		String rep = st.replace("+", "'");
		String rep2 = rep.replace("/", "\"");
		return rep2;
	}
	
	private static String backchange(String st){
		String rep = st.replace("'", "+");
		String rep2 = rep.replace("\"", "/");
		return rep2;
	}
	
	
	public static ArrayList<String> targetSetMorph(String inputLine, CRF crfMorph, MorphemeAnalysis morpheme) throws FileNotFoundException, IOException, ClassNotFoundException{
		
ArrayList<String> result = new ArrayList<String>();
		
		// parse morpheme to arraylist
		ArrayList<String> morph = new ArrayList<String>();
		ArrayList<Integer> wordposition = new ArrayList<Integer>();
		String[] wordoriginal = inputLine.split(" ");
		inputLine=change(inputLine);
		String morphResult = morpheme.Start(inputLine);
		if(morphResult!=null){
		
			
			String[] morphword = morphResult.split(" ");
			for(int i=0; i<morphword.length; i++){
				String[] morphplus = morphword[i].split("\\+");
				for(int j=0; j<morphplus.length; j++){
					morph.add( backchange(morphplus[j].trim().split("/")[0]) + "\t" + backchange(morphplus[j].trim().split("/")[1]) );
					wordposition.add(i);
				}
			}
			
			
		}
		
		// make tag input
	    String tagInput = "";
	    for(int i=0; i<morph.size(); i++){
	    	tagInput+=(morph.get(i).split("\t")[0]+"\t"+morph.get(i).split("\t")[1]+"\tO\n");
	    }
	   
	   
	    // get morph bio tags
	    Instance inst = crfMorph.getInputPipe().instanceFrom(new Instance(tagInput, null, null, null));
		@SuppressWarnings("rawtypes")
		Sequence output = crfMorph.transduce((Sequence) inst.getData());
		String[] BIOs = output.toString().trim().split(" ");
		
		
		/*/////////////////////////////////////////////////////////////////
		
			get morph target and change to syllable words
		
		/////////////////////////////////////////////////////////////////*/
		boolean[] isPushed = new boolean[wordoriginal.length];
		Arrays.fill(isPushed, false);
		for(int i=0; i<BIOs.length; i++){
			if(BIOs[i].equals("B") || BIOs[i].equals("I")){
				if(isPushed[wordposition.get(i)]==false){
					result.add(wordoriginal[wordposition.get(i)].trim());
					isPushed[wordposition.get(i)]=true;
				}
			}
		}
	
		return result;
	    

	
		    
	}
	
	public static ArrayList<String> targetSetSyll(String inputLine, CRF crfSyll){
		
		ArrayList<String> result = new ArrayList<String>();
		
		// make tag input
	    String tagInput = "";
		for(int i=0; i<inputLine.length(); i++){
			String now=Character.toString(inputLine.charAt(i));
			tagInput+=(now+"\tnull\tO\n");
		}
		
		// get morph bio tags
	    Instance inst = crfSyll.getInputPipe().instanceFrom(new Instance(tagInput, null, null, null));
		@SuppressWarnings("rawtypes")
		Sequence output = crfSyll.transduce((Sequence) inst.getData());
		String[] BIOs = output.toString().trim().split(" ");
		
		// get target set
		String word="";
		for(int i=0; i<inputLine.length(); i++){
			String now=Character.toString(inputLine.charAt(i));
			if(BIOs[i].equals("B") || BIOs[i].equals("I")){
				word+=now;
			}
			else{
				if(word!=""){
					result.add(word);
				}
				word="";
			}
		}
		
		return result;
		
	}
	
	public boolean getTarget(MorphemeAnalysis morpheme,String inputLine,String[] queryTg,String[] queryTg_L, String[] queryTgUni, int[] queryUniCount
			,String[] queryVector, int[] queryVecCount) throws IOException, ClassNotFoundException{
		
	
		
		// read input
		//FileInputStream fis = new FileInputStream(inputdir);
		//BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		//String inputLine=null;
		
			boolean isWhiteSpace = false;
			
			ArrayList<String> setMorph = targetSetMorph(inputLine, crfMorph, morpheme);
			ArrayList<String> setSyll = targetSetSyll(inputLine, crfSyll);
			String[] tempMph = new String[setMorph.size()];
			int[] tempMphCount = new int[setMorph.size()];
			String[] tempSyl = new String[setSyll.size()+setMorph.size()];
			int[] tempSylCount = new int[setSyll.size()+setMorph.size()];//곱하기다 얘...
			
			setToArr(setMorph,tempMph,tempMphCount);
			setToArr(setSyll,tempSyl,tempSylCount);
			
			ArrayList<String> setMorph_new = targetSetMorph(inputLine, crfMorph_new, morpheme);
			ArrayList<String> setSyll_new = targetSetSyll(inputLine, crfSyll_new);
			String[] tempMph_new = new String[setMorph_new.size()];
			int[] tempMphCount_new = new int[setMorph_new.size()];
			String[] tempSyl_new = new String[setSyll_new.size()+setMorph_new.size()];
			int[] tempSylCount_new = new int[setSyll_new.size()+setMorph_new.size()];//곱하기다 얘...
			
			setToArr(setMorph,tempMph,tempMphCount);
			setToArr(setSyll,tempSyl,tempSylCount);
			
			setToArr(setMorph_new,tempMph_new,tempMphCount_new);
			setToArr(setSyll_new,tempSyl_new,tempSylCount_new);
			if(tempSyl_new!=null)
				isWhiteSpace = white_space(tempSyl_new);
			
			int i;
			for(i = 0;i<tempSyl.length;i++)
			{
				if(tempSyl[i]==null) 
					break;
				else
					queryTg_L[i] = tempSyl[i];
			}
		
			for(int j = 0;j<tempMph.length;j++)
			{
				if(tempMph[j]==null) break;
				tempSyl[i] = tempMph[j];
				tempSylCount[i] = tempMphCount[j];
				queryTg_L[i] = tempMph[j];
				i++;
			}
			///////////////////
			for(i = 0;i<tempSyl_new.length;i++)
			{
				if(tempSyl_new[i]==null) 
					break;
				else
					queryTg[i] = tempSyl_new[i];
			}
		
			for(int j = 0;j<tempMph_new.length;j++)
			{
				if(tempMph_new[j]==null) break;
				tempSyl_new[i] = tempMph_new[j];
				tempSylCount_new[i] = tempMphCount_new[j];
				queryTg[i] = tempMph_new[j];
				i++;
			}
			
			sentenceTobigram(tempSyl,queryVector,queryVecCount);
			sentenceTounigram(tempSyl, queryTgUni, queryUniCount);
			
			return isWhiteSpace;
	}
	private static boolean white_space(String [] arr){
	         for(int i=0; i < arr.length ; i++){
	        	 if(arr[i] == null) break;
	            for(int j= i+1 ; j < arr.length ;j++){
	            	if(arr[j] == null) break;
	               if(arr[i].replaceAll(" ", "").equals(arr[j].replace(" ", ""))){
	                  return true;
	               }
	            }
	         }
	         return false;
	}
	private void sentenceTounigram(String[] tgStr, String[] queryVectorSyl, int[] queryVecCountSyl) {//곱하기 까먹지 말고 고치기
		// TODO Auto-generated method stub
	//	String[] queryTgBi = new String[queryVectorSyl.length];
		
		for(int tgIdx = 0;tgIdx<tgStr.length;tgIdx++)
		{
			if(tgStr[tgIdx] == null) break;
			
			String[] vector_syl = new String[tgStr[tgIdx].length()];
			int[] count_syl = new int[tgStr[tgIdx].length()];
			
			int unitLen = 1;
			tokenize_string_syllable(tgStr[tgIdx], vector_syl, count_syl, unitLen);//$ -> ' '로 바꿔
			insertVector(queryVectorSyl, queryVecCountSyl,vector_syl, count_syl,false);//중복 없이 insert -> 1로
			
		}
	}
	private void sentenceTobigram(String[] tgStr, String[] queryVectorSyl, int[] queryVecCountSyl) {//곱하기 까먹지 말고 고치기
		// TODO Auto-generated method stub
	//	String[] queryTgBi = new String[queryVectorSyl.length];
		
		for(int tgIdx = 0;tgIdx<tgStr.length;tgIdx++)
		{
			if(tgStr[tgIdx] == null) break;
			
			String[] vector_syl = new String[tgStr[tgIdx].length()+1];
			int[] count_syl = new int[tgStr[tgIdx].length()+1];
			
			int unitLen = 2;
			tokenize_string_syllable(tgStr[tgIdx], vector_syl, count_syl, unitLen);//$ -> ' '로 바꿔
			
			insertVector(queryVectorSyl, queryVecCountSyl,vector_syl, count_syl, true);//중복 없이 insert -> 1로
			
		}
		
		/*int i ;
		for(i = 0;i<queryVectorSyl.length; i++)
		{
			if(queryTgBi [i] == null) break;
			
			queryVectorSyl[i] = queryTgBi [i];
			
		}*/
		/*for(;i<queryVectorSyl.length; i++)
		{
			queryVecCountSyl[i] = 0;
		}
		*/
	}
	/*vector 를 bi 에 중복없이 넣고 중복 갯수를 헤아림*/
	private void insertVector(String[] queryTgBi,int[] queryTgCnt, String[] vector_syl, int[] count, boolean cnt) {
		// TODO Auto-generated method stub
		for(int i = 0;i<vector_syl.length;i++)
		{
			for(int j = 0;j<queryTgBi.length;j++)
			{
				if(queryTgBi[j] == null)
				{
					queryTgBi[j]  = vector_syl[i];
					queryTgCnt[j] = /*count[i]*/1;
					break;
				}
				if(queryTgBi[j].equals(vector_syl[i]))
				{
					if(cnt)
						queryTgCnt[j]+=count[i];
					break;
				}
			}
		}
			
		
	}
	private double tokenize_string_syllable(String input, String[] vector_syl, int[] count_syl, int unitLen) {
		// TODO Auto-generated method stub
		String strMarkAdded;
		if(unitLen!=1)
			strMarkAdded = new String(" "+input+"  ");
		else
			strMarkAdded = new String(input.replace(" ", "") + "  ");
		
    	String temp;
    	int tokenLen = 0;
    	for(int i=0;i<strMarkAdded.length()-unitLen;i++)
    	{
    		tokenLen++;
    		temp = strMarkAdded.substring(i, i+unitLen);
			int idx = ut.array_search(vector_syl,temp);

			if (idx == -1) {
				int count_idx = ut.insertVector(vector_syl,temp);//insert and get index
				if (count_idx != -1) count_syl[count_idx] = 1;
			} else {
				count_syl[idx]++;
			}
    	}
		return tokenLen;
	}

	private static void setToArr(ArrayList<String> setMorph, String[] queryVectorMph, int[] queryVecCountMph) {
		// TODO Auto-generated method stub
		HashMap<String, Integer> tempHash = new HashMap<String, Integer>();
		for(String vec : setMorph)
		{
			Integer curValue = tempHash.get(vec);
			if(curValue != null) tempHash.put(vec, curValue+1);
				// tempHash.replace(vec, curValue, curValue+1);
			else tempHash.put(vec, 1);
		}
		//queryVectorMph = (String[]) tempHash.keySet().toArray();
		int i = 0;
		for(String key : tempHash.keySet()){
			queryVectorMph[i] = key;
			i++;
		}
		i = 0;
		for(String vec : queryVectorMph)
		{
			//System.out.println("sdfdjlskdlfdsfjsdklsdjklfjsldkf : " + vec);
			if(vec == null) break;
			queryVecCountMph[i] = tempHash.get(vec);   ////// 여기 왜그러지?
			i++;
		}
	}
	
}
