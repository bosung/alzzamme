import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import Utils.Utils;
import kacteil.kma.MorphemeAnalysis;
//import kr.co.shineware.util.common.model.Pair;


public class DocumentParser {
	boolean DEBUG = false;
	boolean QUERYCHANGE = true;
	
	//객체지향적으로 Q/A쌍 문서 각각 분석한것들 묶어놨어야 했는데 자바를 C처럼 써버렸네용
	//그래서 index 깨지면 안돼요 ㅠㅠㅠㅠ 중간중간 null이 들어가 있기도 한데 그것도 idx 맞추려고 그렇게 했던 거에요... 
	//네이*에서 긁어온 summary들이랑 국립국어웬에서 제공한 문서들이랑 idx 맞춰야됐어서 null이 중간중간있어요
	
	int FILEIDXSIZE = 5537; //last file name + 1
	int actFILESIZE = 0; //실제 문서 갯수, null인 문서 빼구
	boolean isWhiteQuery;//쿼리가 빈 문장 들어왔을 때 용 예외처리
	StringPurifier strPuri;

	double mAVGDL_Q = 0.0;//Average document length (morpheme)
	double bAVGDL_Q = 0.0;//Average document length (syllable) // bigarm
	double tAVGDL_Q = 0.0;//Average document length (trigram)
	double mAVGDL_A = 0.0;//Average document length (morpheme)
	double bAVGDL_A = 0.0;//Average document length (syllable) // bigarm
	double tAVGDL_A = 0.0;//Average document length (trigram)
	
	TargetSelector TS;//CRF 타겟 셀렉터, 과제에선 쓰이지 않는다고 들었어요
	
	MorphemeAnalysis morpheme;//형태소분석기
	Utils ut;
	String filepathQ, filepathA;
	
	String query = null;
	String[] queryTg = null;//쿼리의 '타겟'
	
	String[] queryTg_L = null;
	
	String[][] summary_trimed;
	
	summary_query SQ;
	
	/////Q/A쌍을 나눠서 Q따로 유사도 계산하고 A따로 유사도 계산하고 나중에 총점 구할때 weighted sum 해요
	//vector : 벡터아님(환장) 해당 문서에서 나온 형태소(혹은 음절 ngram)를 string으로 중복없이 가지고 있는 array
	//count : vector[idx] 가 해당 문서에서 몇번 등장했는지 count[idx]에 저장 (term frequency 계산하려면 필요)
	
	/////형태소(형태소 unigram)
	String[] documentsQ = null;
	String[] documentsA = null;
    String[][] docVectorsQ = null;
    String[][] docVectorsA = null;
    int[][] vectorCountsQ = null;
    int[][] vectorCountsA = null;
    
    String[] queryVector = null;
    int[] queryVecCount = null;
    HashMap<String, Integer[]> tfVectorsQ;
    HashMap<String, Integer[]> tfVectorsA;
    /////음절 bigram
    String[][] docVectors_bigramQ = null;
    int[][] vectorCounts_bigramQ= null;
    String[] queryVector_bigram = null;
    int[] queryVecCount_bigram = null;
    HashMap<String, Integer[]> tfVectors_biQ;
    
    String[][] docVectors_bigramA = null;
    int[][] vectorCounts_bigramA = null;
    HashMap<String, Integer[]> tfVectors_biA;
    /////음절 trigram
    String[] queryVector_trigram = null;
    int[] queryVecCount_trigram = null;
    HashMap<String, Integer[]> tfVectors_tri;
    String[][] docVectors_trigramQ = null;
    int[][] vectorCounts_trigramQ = null;
    HashMap<String, Integer[]> tfVectors_triQ;
    String[][] docVectors_trigramA = null;
    int[][] vectorCounts_trigramA = null;
    HashMap<String, Integer[]> tfVectors_triA;
    
    /////타겟///////////////////////////////////////
    String[][] docVectorsTgBi = null;
    int[][] vectorCountsTgBi = null;
    String[] queryVectorTgBi = null;
    int[] queryVecCountTgBi = null;
    HashMap<String, ArrayList<Integer>> tfVectors_tgBi;

    String[][] docVectorsTgBi_A = null;
    int[][] vectorCountsTgBi_A = null;
    String[] queryVectorTgBi_A = null;
    int[] queryVecCountTgBi_A = null;
    HashMap<String, ArrayList<Integer>> tfVectors_tgbi_A;
    
    String[][] docVectorsTgBi_Q = null;
    int[][] vectorCountsTgBi_Q = null;
    String[] queryVectorTgBi_Q = null;
    int[] queryVecCountTgBi_Q = null;
    HashMap<String, ArrayList<Integer>> tfVectors_tgbi_Q;
    
    String[][] docVectorsTgUni = null;
    int[][] vectorCountsTgUni = null;
    String[] queryVectorTgUni = null;
    int[] queryVecCountTgUni = null;
    HashMap<String, Integer[]> tfVectors_tgUni;
    
    String[][] docVectorsTgWord = null;
    int[][] vectorCountsTgWord = null;
    String[] queryVectorTgWord = null;
    int[] queryVecCountTgWord = null;
    HashMap<String, ArrayList<Integer>> tfVectors_tgWord;
 
    String[][] docVectorsTgWord_Q = null;
    int[][] vectorCountsTgWord_Q = null;
    String[] queryVectorTgWord_Q = null;
    int[] queryVecCountTgWord_Q = null;
    HashMap<String, ArrayList<Integer>> tfVectors_tgWord_Q;
    
    String[][] docVectorsTgWord_A = null;
    int[][] vectorCountsTgWord_A = null;
    String[] queryVectorTgWord_A = null;
    int[] queryVecCountTgWord_A = null;
    HashMap<String, ArrayList<Integer>> tfVectors_tgWord_A;
    //////////////여기까지 다 타겟때문에 들어간 부분, 과제에선 안쓴다구 들었습니다용. 타겟을 어떻게 써먹어야 할지 고민 많이 했어서 여러가지 방식이 잔뜩 들어가 있네요(대환장)
    
    ///서머리 관련//////
    String[][] docVectorsSmBi = null;
    int[][] vectorCountsSmBi = null;
    HashMap<String, ArrayList<Integer>> tfVectors_smBi;
    /////서머리 끝///
    
   ///Document Parser///
   //그냥 Q, A, summary(사람손으로 QA문서 요약한거) 읽어서 형태소분석, 음절 bigram, 음절 trigram
   /////////////////////
    public DocumentParser(String filePath) throws FileNotFoundException, IOException, Exception {
		ut = new Utils();
		
		strPuri = new StringPurifier();
		
    	filepathQ = filePath + "\\Questions.txt";
    	filepathA = filePath + "\\Answers.txt";
    	
    	SQ = new summary_query(".\\summary\\summary.txt");//사람이 직접 만든 Q/A쌍에 대한 summary. (만에하나 이런 수작업을 하자고 하면 격렬하게 반대하세요.)
    	
    	morpheme = new MorphemeAnalysis(true, false, true);//강원대 칵테일 형태소 분석기
        
       
        morpheme.loadFile("./data/");
        System.out.println("mph load complete");
        documentsQ = new String[FILEIDXSIZE];
        documentsA = new String[FILEIDXSIZE];
        
        readDocs(filepathQ, documentsQ);
        readDocs(filepathA, documentsA);
        
        docVectorsQ=new String[FILEIDXSIZE][];
        vectorCountsQ = new int[FILEIDXSIZE][];
        docVectorsA=new String[FILEIDXSIZE][];
        vectorCountsA = new int[FILEIDXSIZE][];
        
        docVectors_bigramQ=new String[FILEIDXSIZE][];
        vectorCounts_bigramQ = new int[FILEIDXSIZE][];
        docVectors_bigramA=new String[FILEIDXSIZE][];
        vectorCounts_bigramA = new int[FILEIDXSIZE][];
        
        docVectors_trigramQ=new String[FILEIDXSIZE][];
        vectorCounts_trigramQ = new int[FILEIDXSIZE][];
        docVectors_trigramA=new String[FILEIDXSIZE][];
        vectorCounts_trigramA = new int[FILEIDXSIZE][];
        
        docVectorsSmBi = new String[FILEIDXSIZE][];
        vectorCountsSmBi = new int[FILEIDXSIZE][];
        
   
        
       mAVGDL_Q = mAVGDL_A = 0.0;//average document length (morpheme)
       bAVGDL_Q = bAVGDL_A = 0.0;//average document length (syllable_bigram)
       tAVGDL_Q = tAVGDL_A = 0.0;//average document length (syllable_trigram)  = (sSUM-1)/total
      /////타겟, 서머리////  
      docVectorsTgBi=new String[FILEIDXSIZE][];
      vectorCountsTgBi = new int[FILEIDXSIZE][];
      
      docVectorsTgWord=new String[FILEIDXSIZE][];
      vectorCountsTgWord = new int[FILEIDXSIZE][];
          
      docVectorsTgUni=new String[FILEIDXSIZE][];
      vectorCountsTgUni = new int[FILEIDXSIZE][];
      
      docVectorsTgBi_Q = getObj_vector("./crf/syll_Q_bi.vec");
      vectorCountsTgBi_Q = getObj_count("./crf/syll_Q_bi.counts");

      docVectorsTgBi_A = getObj_vector("./crf/syll_A_bi.vec");
      vectorCountsTgBi_A = getObj_count("./crf/syll_A_bi.counts");
      
      mergetObj(docVectorsTgBi_Q,vectorCountsTgBi_Q, docVectorsTgBi_A,vectorCountsTgBi_A, docVectorsTgBi,vectorCountsTgBi);
     
      docVectorsTgWord_Q = getObj_vector("./crf/syll_Q_word.vec");
      vectorCountsTgWord_Q = getObj_count("./crf/syll_Q_word.counts");

      docVectorsTgWord_A = getObj_vector("./crf/syll_A_word.vec");
      vectorCountsTgWord_A = getObj_count("./crf/syll_A_word.counts");
      
      mergetObj(docVectorsTgWord_Q,vectorCountsTgWord_Q, docVectorsTgWord_A,vectorCountsTgWord_A, docVectorsTgWord,vectorCountsTgWord/*이건 쓰지마*/);
      
      String[] summery = new String[FILEIDXSIZE];
      readSummery(".\\summary\\summary.txt", summery);
  	 
      summary_trimed = new String[FILEIDXSIZE][];
      readSummeryTrimed(".\\summary\\summaryTab.txt",summary_trimed);
      ////////////////
      
      TS = new TargetSelector();
        
        int fileidx;
        
        for (fileidx = 1; fileidx < FILEIDXSIZE ; fileidx ++) {
  
        	String analyzedstr;
        	if(documentsQ[fileidx] == null) continue;

        	
        	analyzedstr = morpheme.Start(strPuri.getPurifiedBfMorph(documentsQ[fileidx]));//형태소분석함

        	
        		docVectorsQ[fileidx] = new String[analyzedstr.length()];
		        vectorCountsQ[fileidx] = new int[analyzedstr.length()];
		        docVectors_bigramQ[fileidx] = new String[documentsQ[fileidx].length()+1];
		        vectorCounts_bigramQ[fileidx] = new int[documentsQ[fileidx].length()+1];
		        docVectors_trigramQ[fileidx] = new String[documentsQ[fileidx].length()];
		        vectorCounts_trigramQ[fileidx] = new int[documentsQ[fileidx].length()];
		        
	            ////문서를 형태소/음절bigram/음절trigram 단위로 자르고, vectord와 count를 만듦, 각 단위별 문서의 길이를 AVGDL에 다 더함(나중에 문서갯수로 나눠서 평균냄)
	            mAVGDL_Q += tokenize_string (analyzedstr.replaceAll(" ", "+"), docVectorsQ[fileidx], vectorCountsQ[fileidx]);
	            bAVGDL_Q += tokenize_string_syllable("$",documentsQ[fileidx], docVectors_bigramQ[fileidx], vectorCounts_bigramQ[fileidx],2);
	            tAVGDL_Q += tokenize_string_syllable("$",documentsQ[fileidx], docVectors_trigramQ[fileidx], vectorCounts_trigramQ[fileidx],3);// 요요 넌 안나누니?
	        	
	            analyzedstr = morpheme.Start(strPuri.getPurifiedBfMorph(documentsA[fileidx]));
	        
	        	docVectorsA[fileidx] = new String[analyzedstr.length()];
		        vectorCountsA[fileidx] = new int[analyzedstr.length()];
		        docVectors_bigramA[fileidx] = new String[documentsA[fileidx].length()+1];
		        vectorCounts_bigramA[fileidx] = new int[documentsA[fileidx].length()+1];
		        docVectors_trigramA[fileidx] = new String[documentsA[fileidx].length()];
		        vectorCounts_trigramA[fileidx] = new int[documentsA[fileidx].length()];
		        
		        ////문서를 형태소/음절bigram/음절trigram 단위로 자르고, vectord와 count를 만듦, 각 단위별 문서의 길이를 AVGDL에 다 더함(나중에 문서갯수로 나눠서 평균냄)
	            mAVGDL_A += tokenize_string (analyzedstr.replaceAll(" ", "+"), docVectorsA[fileidx], vectorCountsA[fileidx]);
	            bAVGDL_A += tokenize_string_syllable("$",documentsA[fileidx], docVectors_bigramA[fileidx], vectorCounts_bigramA[fileidx],2);
	            tAVGDL_A += tokenize_string_syllable("$",documentsA[fileidx], docVectors_trigramA[fileidx], vectorCounts_trigramA[fileidx],3);// 요요 넌 안나누니?
               
	            ///서머리
	            if(summery[fileidx]!=null)
	            {
	            	docVectorsSmBi[fileidx] = new String[summery[fileidx].length()];
	            	vectorCountsSmBi[fileidx] = new int[summery[fileidx].length()];
	            	tokenize_string_syllable(" ",summery[fileidx], docVectorsSmBi[fileidx], vectorCountsSmBi[fileidx],2);
	            }
	            ///////
	            
             
        }
        mAVGDL_Q /= (double)actFILESIZE;//그냥 filesize 아니고  actFILESIZE, null이 아닌 실제 문서 갯수
        bAVGDL_Q /= (double)actFILESIZE;
        mAVGDL_A /= (double)actFILESIZE;
        bAVGDL_A /= (double)actFILESIZE;
        
        //타겟때메 들어간 부분
        wordToUni(docVectorsTgWord, docVectorsTgUni, vectorCountsTgUni);
        ////
    }
    


	@SuppressWarnings("resource")
	private void readDocs(String filepath, String[] documents) throws Exception {
	// TODO Auto-generated method stub
		BufferedReader in = null;
    	{
        	
            in = new BufferedReader(new FileReader(filepath));
            
            String doc = null;
            String fNumStr = "";
            int fNum;
            actFILESIZE = 0;
            while ((fNumStr = in.readLine()) != null) {
                //input.append(strPuri.getPurifiedString(s)); 
                fNum = new Integer(fNumStr);
                if((doc = in.readLine())== null) break;
                documents[fNum] = strPuri.getPurifiedString(doc);
                actFILESIZE++;
            }
            
            
          
       }
}



	@SuppressWarnings("resource")
	private void readSummeryTrimed(String filepath, String[][] smryT) throws Exception {
		// TODO Auto-generated method stub

		File fs = new File(filepath);
		
		BufferedReader in = new BufferedReader(new FileReader(fs));
		            
		String line = null;
		StringBuilder strIdx = new StringBuilder();
		int fileidx;

		while ((line = in.readLine()) != null) 
		{
			int numEnd;
			line = line.replaceAll("[\uFEFF-\uFFFF]", "");
			for(numEnd = 0; numEnd < line.length();numEnd++)
			{
				if(line.charAt(numEnd)<='9' && line.charAt(numEnd)>='0')
				{
					strIdx.append(line.charAt(numEnd));
				}
				else
					break;
			}
			fileidx = Integer.parseInt(strIdx.toString());
			strIdx.delete(0,numEnd);
			String smrTline =  line.substring(numEnd + 1);
			
			smryT[fileidx] = smrTline.split("\t");
			
			
		}
		
	}



	@SuppressWarnings("resource")
	private void readSummery(String canName, String[] summery) throws Exception {
		// TODO Auto-generated method stub
		
		File fs = new File(canName);
		
		BufferedReader in = new BufferedReader(new FileReader(fs));
		            
		String line = null;
		StringBuilder strIdx = new StringBuilder();
		int fileidx;

		while ((line = in.readLine()) != null) 
		{
			line = line.replaceAll("[\uFEFF-\uFFFF]", "")/*.replaceAll("'", "")*/;
			int numEnd;
			for(numEnd = 0; numEnd < line.length();numEnd++)
			{
				if(line.charAt(numEnd)<='9' && line.charAt(numEnd)>='0')
				{
					strIdx.append(line.charAt(numEnd));
				}
				else
					break;
			}
			fileidx = Integer.parseInt(strIdx.toString());
			strIdx.delete(0,numEnd);
			summery[fileidx] =  line.substring(numEnd + 1);
		}
		
	}



	private void wordToUni(String[][] TgWord, String[][] TgUni, int[][] cntTgUni) {
		// TODO Auto-generated method stub
		for(int docI = 0 ; docI<FILEIDXSIZE;docI++)
		{
			
			if(documentsA[docI] == null) continue;
			else
			{
				TgUni[docI] = new String[documentsA[docI].length() + documentsQ[docI].length()];
				cntTgUni[docI] = new int[documentsA[docI].length() + documentsQ[docI].length()];
			}
			int curUni = 0;
			for(int wordI = 0;wordI < TgWord[docI].length; wordI++)
			{
				curUni = insertVetor_noDup(TgWord[docI][wordI],curUni,TgUni[docI],cntTgUni[docI]);
				
			}
		}
	}
	private int insertVetor_noDup(String word, int curUni,String[] TgUni, int[] cntTgUni) {
		// TODO Auto-generated method stub
		int uniI;
		for(int ltI = 0; ltI<word.length();ltI++)
		{
			uniI = ut.array_search(TgUni, word.substring(ltI, ltI+1));
			
			if(uniI!=-1)
			{
				// 야호 cntTgUni[uniI]++;
			}
			else
			{
				if(word.charAt(ltI) != ' ') 
				{
					TgUni[curUni] = word.substring(ltI, ltI+1);
					cntTgUni[curUni++] = 1;
				}
			}
		}
		
		return curUni;
	}

	
	private void mergetObj(String[][] docV_Q, int[][] vecC_Q, String[][] docV_A,int[][] vecC_A , String[][] docV, int[][] vecC) throws Exception {
		// TODO Auto-generated method stub
		HashMap<String, Integer> temp;
		for(int i= 0;i<FILEIDXSIZE;i++)/*i is the given document name*/
		{
			temp = new HashMap<String,Integer>();
			//if(docV_Q[i]!=null)
				for(int j = 0;j<docV_Q[i].length;j++){
					if(docV_Q[i][j]==null) break;
					temp.put(docV_Q[i][j], vecC_Q[i][j]);
				}
			//if(docV_A[i]!=null)
				for(int j = 0;j<docV_A[i].length;j++)
				{
					if(docV_A[i][j]==null) break;
					if(temp.get(docV_A[i][j])==null)
					{
						temp.put(docV_A[i][j], vecC_A[i][j]);
					}
					else
					{
						int old = temp.get(docV_A[i][j]);
						//temp.replace(docV_A[i][j], old, old+vecC_A[i][j]);
						temp.put(docV_A[i][j],old+vecC_A[i][j]);
					}
				}
			
			int j = 0;
			docV[i] = new String[temp.size()];
			vecC[i] = new int[temp.size()];
			for(String vec: temp.keySet()) // 여기여기
			{
				if(temp.get(vec) == null) break;
				
				docV[i][j] = vec;
				vecC[i][j] = temp.get(vec);
				j++;
			}
			
		}
		/*FileOutputStream output = new FileOutputStream("jooA.txt");
		String wrtLine;
		for(int i = 0; i<FILEIDXSIZE;i++)
		{
			 
				wrtLine = new String(""+i + "\n");
				output.write(wrtLine.getBytes());
		
				wrtLine = new String();
				
				for(int j=0;j<docV[i].length;j++)
				{
					if(docV[i][j] == null) break;
					else
						wrtLine += docV[i][j] + "#";
					
				}
					wrtLine += '\n';
					output.write(wrtLine.getBytes());

				wrtLine = new String();
				for(int j=0;j<docV[i].length;j++)
				{
					if(docV[i][j] == null) break;
					else
						wrtLine += vecC[i][j] + "#";
						
				}
					wrtLine += '\n';
					output.write(wrtLine.getBytes());
		}
		output.close();*/
	}
	private int[][] getObj_count(String path) throws Exception{
		// TODO Auto-generated method stub
    	ObjectInputStream ois= new ObjectInputStream(new FileInputStream(path));
    	int[][] vectorCountsTgMph_Q2 = (int[][]) ois.readObject();
    	ois.close();
    	
    	return vectorCountsTgMph_Q2;
	}

	private String[][] getObj_vector( String path) throws Exception{
		// TODO Auto-generated method stub
    	ObjectInputStream ois= new ObjectInputStream(new FileInputStream(path));
    	String[][] docVectorsTgMph_Q2 = (String[][]) ois.readObject();
    	ois.close();
    	
    	return docVectorsTgMph_Q2;
	}

/*	private void countToBin(int[][] vectorCountsTgMph2) {
		// TODO Auto-generated method stub
		for(int i = 0; i<vectorCountsTgMph2.length;i++)
			for(int j = 0; j<vectorCountsTgMph2[i].length;j++)
			{
				if(vectorCountsTgMph2[i][j] != 0) vectorCountsTgMph2[i][j] =1;
			}
	}
*/
	private double tokenize_string_syllable(String mark, String input, String[] vector_syl, int[] count_syl, int unitLen) {
		// TODO Auto-generated method stub
    	String strMarkAdded = new String(mark+input+mark+mark);
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
	//////tokenize_string//////
	//입력 string을 "+"로 토크나이즈 해줍니다
	//vector 라고 돼있긴 한데 진짜 실수로된 벡터는 아니에요 ㅠㅠ 왜 이렇게 이름을 지었을까요...
	//vector는 해당 문서에 등장한 형태소들을 중복없이 가지고 있는 array에요
	//count[idx]는 vector[idx] 형태소가 해당 문서에 몇번 등장했는지 count를 센 거에요 term frequency 때문에 만든 거에요
	///////////////////////////
	private double tokenize_string (String target, String[] vector, int[] count) {

		StringTokenizer token = null;
		String temp;
		int tokenLen = 0;
		if (target == null) return tokenLen;

		token = new StringTokenizer(target, "+");
		while (token.hasMoreTokens()) {
			tokenLen++;
			temp = token.nextToken();
			int idx = ut.array_search(vector,temp);

			
			if (idx == -1) {
				int count_idx = ut.insertVector(vector,temp);//insert and get index
				if (count_idx != -1) count[count_idx] = 1;
			} else {
				count[idx]++;
			}
		}

		return tokenLen;
	}


	public String parseQuery(String Query) throws IOException, Exception {
		// TODO Auto-generated method stub
		
		{	
			isWhiteQuery = false;
			query = strPuri.getPurifiedString(Query);
			String analyzedstr = morpheme.Start(strPuri.getPurifiedBfMorph(query));//쿼리 형태소 분석함
			
	        queryVector = new String[analyzedstr.length()];//bi, tri... 이런거 없이 걍 있으면 보통 형태소에요
	        queryVector_bigram = new String[query.length()+1];
	        queryVector_trigram = new String[query.length()];

	        queryVecCount = new int[analyzedstr.length()];
	        queryVecCount_bigram = new int[query.length()+1];
	        queryVecCount_trigram = new int[query.length()];
	
	        queryVectorTgBi = new String[query.length()+1];
	        queryVecCountTgBi = new int[query.length()+1];
	        
	        queryVectorTgUni = new String[query.length()];
	        queryVecCountTgUni = new int[query.length()];
	  
	        tokenize_string (analyzedstr.replaceAll(" ", "+"),queryVector, queryVecCount);
	        tokenize_string_syllable("$",query, queryVector_bigram, queryVecCount_bigram,2);
	        tokenize_string_syllable("$",query, queryVector_trigram, queryVecCount_trigram,3);
	      
	        queryTg = new String[query.length()];
	        queryTg_L = new String[query.length()];
		    isWhiteQuery = TS.getTarget(morpheme,query,queryTg,queryTg_L,queryVectorTgUni,queryVecCountTgUni, queryVectorTgBi, queryVecCountTgBi);
		       /* 여기바뀜 */
		   
		        if(DEBUG)
		        {
			        System.out.println("tar` of query:: ");
			        for(int i = 0; i< queryVecCountTgBi.length;i++)
			        {
			        	if(queryVectorTgBi[i] == null ) break;
			        	System.out.println(": " + queryVectorTgBi[i] + " : " + queryVecCountTgBi[i]);
			        }
			        System.out.println("mph of query:: ");
			        for(int i = 0; i< queryVector.length;i++)
			        {
			        	if(queryVector[i] == null ) break;
			        	System.out.println(": " + queryVector[i]);
			        }
			        System.out.println("target long:: ");
			        for(int i = 0; i< queryTg_L.length;i++)
			        {
			        	if(queryTg_L[i] == null ) break;
			        	System.out.println(": " + queryTg_L[i]);
			        }
			        System.out.println("target short:: ");
			        for(int i = 0; i< queryTg.length;i++)
			        {
			        	if(queryTg[i] == null ) break;
			        	System.out.println(": " + queryTg[i]);
			        }
			        
		        }
	       
		   return analyzedstr;
		}
	}





}
