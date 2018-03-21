import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.*;

import Utils.Utils;

public class SimCalculator {
	boolean DEBUG = true;
	summary_query SQ;
	FileOutputStream fosidf;
	
	int FILEIDXSIZE = 5537; //last file name + 1

	DocumentParser QnA;
	Utils ut;

	int[] queryCat;
	int[][] docCat;
	double[] numOfMat;
	double[] tgL_incl_Sm;
	
    HashMap<String, Integer[]> tfVectors;
    HashMap<String, Integer[]> tfVectorsQ;
    HashMap<String, Integer[]> tfVectorsA;
    
    HashMap<String, Integer[]> tfVectors_biQ;
    HashMap<String, Integer[]> tfVectors_biA;
    
    HashMap<String, Integer[]> tfVectors_triQ;
    HashMap<String, Integer[]> tfVectors_triA;
    
    HashMap<String, ArrayList<Integer>> tfVectors_tgBi;
    HashMap<String, ArrayList<Integer>> tfVectors_tgbi_A;
    HashMap<String, ArrayList<Integer>> tfVectors_tgbi_Q;
    
    HashMap<String, Integer[]> tfVectors_tgUni;
    
    HashMap<String, Integer[]> tfVectors_smry;
    
    HashMap<String, Integer[]> tfVectors_tg;
    
    int[][] Answers = {{5126},{3325,3330},{728,726},{1001,1196,2025},{3422},{2128,2129,2130},
    		{407},{4587,4588},{3911},{3891},{4564},{3885},{3950,1570,71}
    		,{5029},{1910,1911,1883,1884,1885},{4304,4305},{836,3952},{4688},{4127},{2838, 4743, 5051, 2802}};
    	/*{
    		{	1555,	1554,	4531}
    		,{3216,	3217	}
    		,{284,	285,	286,	366,	446}
    		,{744,	745,	746,	4219	}
    		,{4261	}
    		,{1543,	1544,	2101	}
    		,{3601}
    		,{1352	,1370	}
    		,{4050}
    		,{2634	}
    		,{2682		}
    		,{4302}
    		,{4301	}
    		,{4308}
    		,{4330}
    		,{4328}
    		,{4344}
    		,{4351}
    		,{4390	}
    		,{3422	}
    		,{1032	}
    		,{96}
    		,{356}
    		,{452}
    		,{815}
    		,{840}
    		,{895}
    		,{1224}
    		,{2210}
    		,{2434}
    		,{3294}
    		,{3401,	3412,	3413,	3414,	3453}
    		,{	3420,	3421}
    		,{493	,494	}
    		,{497}
    		,{725}
    		,{1711,	1712	,1728	}
    		,{1128,	1125	}
    		,{3869}
    		,{3878}
    		};*/
    //int QUERYSIZE = 40;
    double[] summaryPoint = new double[FILEIDXSIZE];
    double[] summaryFPoint = new double[FILEIDXSIZE];
    int[][] searchResults, searchResults_CM_Q ,searchResults_CM_A,searchResults_CS_Q, searchResults_CS_A,
    		searchResults_CT_Q, searchResults_CT_A,searchResults_BM_Q, searchResults_BM_A,searchResults_BS_Q, 
    		searchResults_BS_A, searchResults_BT_Q, searchResults_BT_A,searchResults_Tg,searchResults_Tu,
    		serachResults_SM, searchResults_TgC, searchResults_SMT, searchResults_TIS
    		; //10개씩 20개

	public SimCalculator(DocumentParser dpQnA) throws Exception {
		// TODO Auto-generated constructor stub
		ut = new Utils();
		
		fosidf = new FileOutputStream("idf.txt");
		this.QnA = dpQnA;
		searchResults = new int[Answers.length][];
    	searchResults_BM_Q = new int[Answers.length][];
    	searchResults_BS_Q = new int[Answers.length][];
    	searchResults_BT_Q = new int[Answers.length][];
    	searchResults_CM_Q = new int[Answers.length][];
    	searchResults_CS_Q = new int[Answers.length][];
    	searchResults_CT_Q = new int[Answers.length][];
    	searchResults_BM_A = new int[Answers.length][];
    	searchResults_BS_A = new int[Answers.length][];
    	searchResults_BT_A = new int[Answers.length][];
    	searchResults_CM_A = new int[Answers.length][];
    	searchResults_CS_A = new int[Answers.length][];
    	searchResults_CT_A = new int[Answers.length][];
    	searchResults_Tg = new int[Answers.length][];
    	searchResults_Tu = new int[Answers.length][];
    	searchResults_TgC = new int[Answers.length][];
    	serachResults_SM = new int[Answers.length][];
    	searchResults_SMT = new int[Answers.length][];
    	searchResults_TIS = new int[Answers.length][];
    	
    	docCat = getObj_int("./svm/table.te");
	}
	
	private int[][] getObj_int(String path) throws Exception{
		// TODO Auto-generated method stub
    	ObjectInputStream ois= new ObjectInputStream(new FileInputStream(path));
    	int[][] vectorCountsTgMph_Q2 = (int[][]) ois.readObject();
    	ois.close();
    	
    	return vectorCountsTgMph_Q2;
	}

	 private HashMap<String, ArrayList<Integer>> getObj_hash(String path) throws Exception{
			// TODO Auto-generated method stub
	    	ObjectInputStream ois= new ObjectInputStream(new FileInputStream(path));
	    	@SuppressWarnings("unchecked")
			HashMap<String, ArrayList<Integer>> vectorCountsTgMph_Q2 = (HashMap<String, ArrayList<Integer>>) ois.readObject();
	    	ois.close();
	    	
	    	return vectorCountsTgMph_Q2;
	    	
		}
		private void mergetHash(HashMap<String, ArrayList<Integer>> tfV_Q,
				HashMap<String, ArrayList<Integer>> tfV_A,
				HashMap<String, ArrayList<Integer>> tfV) {
			// TODO Auto-generated method stub
			tfV.putAll(tfV_A);
			
			for(String vec : tfV_Q.keySet())
			{
				if(tfV.get(vec)==null)
				{
					tfV.put(vec, tfV_Q.get(vec));
				}
				else
				{
					ArrayList<Integer> oldval = tfV.get(vec);
					ArrayList<Integer> newval = tfV.get(vec);
					
					for(Integer docN : tfV_Q.get(vec))
					{
						if(newval.contains(docN)) continue;
						else
						{
							newval.add(docN);
						}
					}
					
					tfV.replace(vec, oldval, newval);
				}
			}
		}

	public void tfCalculator() throws Exception {
		// TODO Auto-generated method stub

    		tfVectors_tgbi_Q = getObj_hash("./crf/syll_Q_bi.hash");
    
    		tfVectors_tgbi_A = getObj_hash("./crf/syll_A_bi.hash");

    		tfVectors_tgBi = new HashMap<String, ArrayList<Integer>>();
    		
    		tfVectors_tgUni = new HashMap<String, Integer[]>();
 
    		mergetHash(tfVectors_tgbi_Q,tfVectors_tgbi_A,tfVectors_tgBi);

    	tfVectorsQ = new HashMap<String, Integer[]>();
        tfVectorsA = new HashMap<String, Integer[]>();
        tfVectors_biQ = new HashMap<String,Integer[]>();
        tfVectors_biA = new HashMap<String,Integer[]>();
        tfVectors_triQ = new HashMap<String,Integer[]>();
        tfVectors_triA = new HashMap<String,Integer[]>();
        tfVectors_smry = new HashMap<String,Integer[]>();
        tfVectors_tg = new HashMap<String,Integer[]>();
        
   
        getTf(tfVectorsQ, QnA.docVectorsQ);
        getTf(tfVectorsA, QnA.docVectorsA);
        getTf(tfVectors_biQ, QnA.docVectors_bigramQ);
        getTf(tfVectors_biA, QnA.docVectors_bigramA);
        getTf(tfVectors_triQ, QnA.docVectors_trigramQ);
        getTf(tfVectors_triA, QnA.docVectors_trigramA);
        getTf(tfVectors_tgUni, QnA.docVectorsTgUni);
        getTf(tfVectors_smry, QnA.docVectorsSmBi);
        getTf(tfVectors_tg, QnA.docVectorsTgWord);
	}

	

	private void getTf(HashMap<String, Integer[]> tfV, String[][] docV)
    {
    	
    	 for(int currentIdx = 1; currentIdx < FILEIDXSIZE ; currentIdx++){
    		 String[] doc = docV[currentIdx];
    		 if(doc == null){ continue;}//docVector[i] (i is not the one of the 'real' document names) is null
    		////////이건 왜했을까...////// 
         	HashSet<String> hashVector = new HashSet<String>();
         	for(int j =0;j<doc.length;j++) 
         	{
         		if(doc[j] != null)
         			hashVector.add(doc[j]);
         		else
         			break;
         	}
         	//////////////////////////
         	
         	//////현재 document에 등장한 형태소/음절ngram 가지고[ key:형태소 또는 음절ngrm value:해당 형태소 또는 음절ngram이 출현한 문서 번호 array ] 이런 hash를 만들어요
 	        for(String vec : hashVector)
 	        {
 	        	if(vec == null)
 	        		break;
 	        	
 	        	Integer[] olddocIdx = tfV.get(vec);
 	        	if(olddocIdx != null )
 	        	{
 	        		Integer[] newdocIdx = Arrays.copyOf(olddocIdx, olddocIdx.length+1);//매번 array copy하네요... 죄송...
 	 	        	newdocIdx[olddocIdx.length] = currentIdx;
 	 	        	
 	        		//tfV.replace(vec, olddocIdx, newdocIdx);
 	        		tfV.put(vec, newdocIdx);
 	        	}
 	        	else // current vector is not found in hashVector
 	        	{	
 	        		if(ut.isSignificant(vec))//issignificant는 불용어 처리를 위해 있는 메서드에요! 조사나 어미에 해당하는 형태소들은 유사도 계산할때 사용하지 않아요.(음절단위로는 사용돼요.)
 	        		{
 	        			Integer[] docIdx = new Integer[1];
 	        			docIdx[0] = currentIdx;
 	        			tfV.put(vec,  docIdx);	
 	        		}
 	        	}
 	        	
 	        }

         }
    }
	private int getValidsDocs(int validx,int[] validDocs,  String[] queryV, HashMap<String, ArrayList<Integer>> tfV) {
		// TODO Auto-generated method stub

		for(String vec : queryV)
    	{
			
			 ArrayList<Integer> DocIdxs = tfV.get(vec);
    		if(DocIdxs!=null)
	    		for(int idx : DocIdxs)
	    		{
	    			if(ut.array_search(validDocs, idx)== -1) 
	    			{
	    				validDocs[validx++] = idx;
	    				
	    			}
	    		}
    		else
    			continue;
    	}
		
		return validx;
	}
	//////////getSimilarity///////////////
	//쿼리랑 문서들이랑 유사도계산
	//유사도 계산 알고리즘은 cosine similarity, BM25 두가지 사용(타겟은 exact matching도 썼음)
	//유사도 계산 대상은 
	//- Q의 형태소,음절bigram, 음절 trigram
	//- A의 형태소,음절bigram, 음절 trigram
	//- 카테고리 일치 여부
	//유사도 계산해서 상위 10개
	//////////////////////////////////////
    public void getSimilarity(int queryIdx, int[][] idxArr, double[][] scoreArr ) throws Exception {
    	
		int fileIdxSize = QnA.FILEIDXSIZE;
		
		//쿼리랑 Q 문서랑 유사도 계산
		double[] cosSim_Q = new double[fileIdxSize];//형태소 단위로 cosine similarity 계산
    	double[] cosSim_bi_Q = new double[fileIdxSize];//음절 bigram단위로 cosine similarity 계산
    	double[] cosSim_tri_Q = new double[fileIdxSize];//음절 tiigram단위로 cosine similarity 계산
    	double[] BMscore_Q = new double[fileIdxSize];//형태소 단위로 BM25 계산
    	double[] BMscore_bi_Q = new double[fileIdxSize];//음절 bigram단위로 BM25 계산
    	double[] BMscore_tri_Q = new double[fileIdxSize];//음절 tiigram단위로 BM25 계산
    	//쿼리랑 A 문서랑 유사도 계산
    	double[] cosSim_A = new double[fileIdxSize];
    	double[] cosSim_bi_A = new double[fileIdxSize];
    	double[] cosSim_tri_A = new double[fileIdxSize];
    	double[] BMscore_A = new double[fileIdxSize];
    	double[] BMscore_bi_A = new double[fileIdxSize];
    	double[] BMscore_tri_A = new double[fileIdxSize];
    	//타겟
    	double[] target_bi = new double[fileIdxSize];
    	double[] target_uni = new double[fileIdxSize];
    	//서머리
    	double[] summery_bi = new double[fileIdxSize];
    	//카테고리 일치여부
    	double[] catPoint = new double[fileIdxSize];
    	//총점
    	double[] TotalScore = new double[fileIdxSize];

    	/////타겟////
    	numOfMat = new double[fileIdxSize];
    	for (int i = 0; i < fileIdxSize; i++) {
    		if(QnA.docVectorsTgWord[i].length == 0) continue; //.len 으로 바꿔
    		numOfMat[i] = (double)checkContains(QnA.queryTg, QnA.docVectorsTgWord[i]);
    		
    	}
    	/////서머리////
    	tgL_incl_Sm = new double[fileIdxSize];
    	for (int i = 0; i < fileIdxSize; i++) {
    		
    		if(QnA.summary_trimed[i] == null) continue; //.len 으로 바꿔 
    		tgL_incl_Sm[i] = (double)checkContains( QnA.summary_trimed[i],QnA.queryTg_L);
    	}
    	///타겟이랑 서머리 끝////
    	
    	
    	int[] validDocs = new int[fileIdxSize];//쿼리에 나온 형태소나 음절 ngram을 포함한 문서들의 인덱스 리스트, 이 문서들에 대해서만 점수를 계산해요. 등장도 안하면 어차피 0점 이니깐

    	int validx = 0;//'쿼리에 나온 형태소나 음절 ngram을 포함한 문서들의 인덱스 리스트' 의 크기
    	validx = getValidsDocs(validDocs,validx, QnA.queryVector,tfVectorsQ);
    	validx = getValidsDocs(validDocs,validx, QnA.queryVector,tfVectorsA);
    	validx = getValidsDocs(validDocs,validx, QnA.queryVector_bigram,tfVectors_biQ);
    	validx = getValidsDocs(validDocs,validx, QnA.queryVector_bigram,tfVectors_biA);
    	validx = getValidsDocs(validDocs,validx, QnA.queryVector_trigram,tfVectors_triQ);
    	validx = getValidsDocs(validDocs,validx, QnA.queryVector_trigram,tfVectors_triA);
    	validx = getValidsDocs(validx, validDocs, QnA.queryVectorTgBi,tfVectors_tgBi);
	    validx = getValidsDocs(validDocs,validx, QnA.queryVectorTgUni,tfVectors_tgUni);	
	    validx = getValidsDocs(validDocs,validx, QnA.queryVector_bigram, tfVectors_smry);
	    //validx = getValidsDocs(validDocs,validx, QnA.queryTg, tfVectors_tg);
    //	validx = getValidsDocs(validx,validDocs, queryVectorTgSyl,tfVectors_tgsyl);//해빈에 hash에서는 int 가 파일 번호임 요요
    	
    	//타겟
	    getSummaryPoint(QnA.query, QnA.queryTg);
	    /////
	   
    	for (int i = 0; i < validDocs.length; i++) {
    		////i번째 문서에 대해 유사도 계산/////
    		
    		if(validDocs[i] == 0) break;
    		
    	/*	double[] point_doc_Q = new double[QnA.docVectorsQ[validDocs[i]].length + QnA.queryVector.length];
    		double[] point_doc_A = new double[QnA.docVectorsA[validDocs[i]].length + QnA.queryVector.length];
    		double[] point_query_Q = new double[QnA.docVectorsQ[validDocs[i]].length + QnA.queryVector.length];
            double[] point_query_A = new double[QnA.docVectorsA[validDocs[i]].length + QnA.queryVector.length];
         
            double[] point_doc_bi_Q = new double[QnA.docVectors_bigramQ[validDocs[i]].length + QnA.queryVector_bigram.length];
            double[] point_query_bi_Q = new double[QnA.docVectors_bigramQ[validDocs[i]].length + QnA.queryVector_bigram.length];

            double[] point_doc_Tri_Q = new double[QnA.docVectors_trigramQ[validDocs[i]].length + QnA.queryVector_trigram.length];
            double[] point_doc_Tri_A = new double[QnA.docVectors_trigramA[validDocs[i]].length + QnA.queryVector_trigram.length];
            double[] point_query_Tri_Q = new double[QnA.docVectors_trigramQ[validDocs[i]].length + QnA.queryVector_trigram.length];
            double[] point_query_Tri_A = new double[QnA.docVectors_trigramA[validDocs[i]].length + QnA.queryVector_trigram.length];
          
            double[] point_doc_smry = null;
            double[] point_query_smry = null;
            */
 
    		//point_doc, point_query에 각 형태소 또는 음절ngram 들의 tf-idf 값이 들어가요
    		//(count가 tf로 쓰이는데 찾아보니까 이래도되긴한대요... 길이로 정규화한 일반적인 tf가 성능이 더 좋을 것 같아요 ㅠㅠ)
    		//point_doc, point_query 차원이 계속 바뀌어서 cosine similarity가 정상적으로 계산되는게 맞나 의아했는데
    		//어차피 query랑 문서에 안나온 형태소(또는 음절)의 tfidf 값 0일 테니까 내적할때 상관 없는게 맞네요.. 꼴에 계산량 줄이려고 이렇게 짰던거 같아요
    		
    		double[] point_doc_Q = new double[QnA.docVectorsQ[validDocs[i]].length + QnA.queryVector.length];
    		double[] point_doc_A = new double[QnA.docVectorsA[validDocs[i]].length + QnA.queryVector.length];
    		double[] point_query_Q = new double[QnA.docVectorsQ[validDocs[i]].length + QnA.queryVector.length];
            double[] point_query_A = new double[QnA.docVectorsA[validDocs[i]].length + QnA.queryVector.length];
            
            double[] point_doc_bi_Q = new double[QnA.docVectors_bigramQ[validDocs[i]].length + QnA.queryVector_bigram.length];
            double[] point_doc_bi_A = new double[QnA.docVectors_bigramA[validDocs[i]].length + QnA.queryVector_bigram.length];
            double[] point_query_bi_Q = new double[QnA.docVectors_bigramQ[validDocs[i]].length + QnA.queryVector_bigram.length];
            double[] point_query_bi_A = new double[QnA.docVectors_bigramA[validDocs[i]].length + QnA.queryVector_bigram.length];

            double[] point_doc_Tri_Q = new double[QnA.docVectors_trigramQ[validDocs[i]].length + QnA.queryVector_trigram.length];
            double[] point_doc_Tri_A = new double[QnA.docVectors_trigramA[validDocs[i]].length + QnA.queryVector_trigram.length];
            double[] point_query_Tri_Q = new double[QnA.docVectors_trigramQ[validDocs[i]].length + QnA.queryVector_trigram.length];
            double[] point_query_Tri_A = new double[QnA.docVectors_trigramA[validDocs[i]].length + QnA.queryVector_trigram.length];
          
            double[] point_doc_smry = null;
            double[] point_query_smry = null;
            
            
            double[] point_doc_tgBi;
            double[] point_query_tgBi;
            
            
            double[] point_doc_tgUni;
            double[] point_query_tgUni;
            
            ////////////////////i번째 문서에 대해 BM25 점수 계산, tfidf도 여기서 계산해서 point_doc, point_query에 드가요////////////////////
            BMscore_Q[validDocs[i]] = getPoint(validDocs[i],QnA.mAVGDL_Q,point_doc_Q,point_query_Q
        			,QnA.docVectorsQ,QnA.queryVector,QnA.vectorCountsQ,QnA.queryVecCount,tfVectorsQ);
	        BMscore_A[validDocs[i]] = getPoint(validDocs[i],QnA.mAVGDL_A,point_doc_A,point_query_A
	    			,QnA.docVectorsA,QnA.queryVector,QnA.vectorCountsA,QnA.queryVecCount,tfVectorsA);
	        BMscore_bi_Q[validDocs[i]] = getPoint(validDocs[i],QnA.bAVGDL_Q,point_doc_bi_Q, point_query_bi_Q
	        		, QnA.docVectors_bigramQ, QnA.queryVector_bigram, QnA.vectorCounts_bigramQ
	        		, QnA.queryVecCount_bigram,tfVectors_biQ);
	        BMscore_bi_A[validDocs[i]] = getPoint(validDocs[i],QnA.bAVGDL_A,point_doc_bi_A, point_query_bi_A
	        		, QnA.docVectors_bigramA, QnA.queryVector_bigram, QnA.vectorCounts_bigramA
	        		, QnA.queryVecCount_bigram,tfVectors_biA);
	        BMscore_tri_Q[validDocs[i]] = getPoint(validDocs[i],QnA.tAVGDL_Q,point_doc_Tri_Q, point_query_Tri_Q
	        		, QnA.docVectors_trigramQ, QnA.queryVector_trigram,QnA.vectorCounts_trigramQ
	        		, QnA.queryVecCount_trigram,tfVectors_triQ);
	        BMscore_tri_A[validDocs[i]] = getPoint(validDocs[i],QnA.tAVGDL_A,point_doc_Tri_A, point_query_Tri_A
	        		, QnA.docVectors_trigramA, QnA.queryVector_trigram,QnA.vectorCounts_trigramA
	        		, QnA.queryVecCount_trigram,tfVectors_triA);
	    
	        ////타겟, 서머리/////
	        point_doc_tgBi = new double[QnA.docVectorsTgBi[validDocs[i]].length + QnA.queryVectorTgBi.length];
        	point_query_tgBi = new double[QnA.docVectorsTgBi[validDocs[i]].length + QnA.queryVectorTgBi.length];
          
	   	getPoint(validDocs[i],point_doc_tgBi, point_query_tgBi, QnA.docVectorsTgBi, QnA.queryVectorTgBi, 
            		QnA.vectorCountsTgBi,QnA.queryVecCountTgBi,tfVectors_tgBi);
        	
        	double temp = 0.0;
        	point_doc_tgUni = new double[QnA.docVectorsTgUni[validDocs[i]].length + QnA.queryVectorTgUni.length];
        	point_query_tgUni = new double[QnA.docVectorsTgUni[validDocs[i]].length + QnA.queryVectorTgUni.length];
        	getPoint(validDocs[i],temp,point_doc_tgUni, point_query_tgUni, QnA.docVectorsTgUni, QnA.queryVectorTgUni, 
            		QnA.vectorCountsTgUni,QnA.queryVecCountTgUni,tfVectors_tgUni);
        	
        	if(QnA.docVectorsSmBi[validDocs[i]]!=null)
            {
             	point_doc_smry = new double[QnA.docVectorsSmBi[validDocs[i]].length + QnA.queryVector_bigram.length];
         	    point_query_smry = new double[QnA.docVectorsSmBi[validDocs[i]].length + QnA.queryVector_bigram.length];
         	    getPoint(validDocs[i],temp,point_doc_smry, point_query_smry, QnA.docVectorsSmBi, QnA.queryVector_bigram, 
               		QnA.vectorCountsSmBi,QnA.queryVecCount_bigram,tfVectors_smry);
            } 
        	
        	////////////////
        	
        	catPoint[validDocs[i]] = catSimilarity(validDocs[i],queryIdx,docCat,queryCat);
        	
        	
        	////////////////////tfidf를 자질로 cosine similarity 계산////////////////////
        	cosSim_Q[validDocs[i]] = cosineSimilarity(point_doc_Q, point_query_Q);
            cosSim_A[validDocs[i]] = cosineSimilarity(point_doc_A, point_query_A);
            cosSim_bi_Q[validDocs[i]] = cosineSimilarity(point_doc_bi_Q, point_query_bi_Q);
            cosSim_bi_A[validDocs[i]] = cosineSimilarity(point_doc_bi_A, point_query_bi_A);
            cosSim_tri_Q[validDocs[i]] = cosineSimilarity(point_doc_Tri_Q, point_query_Tri_Q);
            cosSim_tri_A[validDocs[i]] = cosineSimilarity(point_doc_Tri_A, point_query_Tri_A);
            target_bi[validDocs[i]] = cosineSimilarity(point_doc_tgBi, point_query_tgBi); 
            							//(double)countSimilarity(validDocs[i],  QnA.docVectorsTgBi,
            								//	QnA.queryVectorTgBi,QnA.vectorCountsTgBi, QnA.queryVecCountTgBi);
            target_uni[validDocs[i]]=cosineSimilarity(point_doc_tgUni, point_query_tgUni);
            
            if(QnA.docVectorsSmBi[validDocs[i]]!=null)
            	summery_bi[validDocs[i]]=cosineSimilarity(point_doc_smry, point_query_smry);
            
            TotalScore[validDocs[i]] = sumScore(//그냥 weighted sum이에요
            				BMscore_A[validDocs[i]],BMscore_Q[validDocs[i]],BMscore_bi_A[validDocs[i]]
            				,BMscore_bi_Q[validDocs[i]],BMscore_tri_A[validDocs[i]],BMscore_tri_Q[validDocs[i]]
            				,cosSim_A[validDocs[i]],cosSim_Q[validDocs[i]],cosSim_bi_A[validDocs[i]],cosSim_bi_Q[validDocs[i]]
            				,cosSim_tri_A[validDocs[i]],cosSim_tri_Q[validDocs[i]],target_bi[validDocs[i]],target_uni[validDocs[i]]
            				,summery_bi[validDocs[i]], summaryPoint[validDocs[i]] , summaryFPoint[validDocs[i]]
            				, numOfMat[queryIdx]/8 , tgL_incl_Sm[queryIdx]/8)
            		*(catPoint[validDocs[i]]*0.1+1.0)
            		;
            
            
        }
    	
 
    	
    	int[] idxArrCM_Q = new int[10], idxArrCS_Q = new int[10],idxArrCT_Q = new int[10],idxArrBM_Q = new int[10]
    			,idxArrBS_Q = new int[10],idxArrBT_Q = new int[10],idxArrCM_A = new int[10], idxArrCS_A = new int[10]
    			,idxArrCT_A = new int[10],idxArrBM_A = new int[10],idxArrBS_A = new int[10],idxArrBT_A = new int[10]
    			,idxArrTg = new int[10], idxArrTT = new int[10], idxArrTu = new int[10], idxArrSmry = new int[10]
    			,idxArrTgC = new int[10], idxArrSmTr = new int[10],idxArrTIS = new int[10], idxArrSmTrF = new int[10];
    	
    /*	
      	getTop10(cosSim_tri_Q,idxArrCT_Q);
    	getTop10(BMscore_A,idxArrBM_A);
     	getTop10(BMscore_tri_A,idxArrBT_A);
    	getTop10(BMscore_tri_Q,idxArrBT_Q);
     	getTop10(target_uni,idxArrTu);
     	getTop10(target_bi,idxArrTg);
    	getTop10(numOfMat,idxArrTgC);
    	getTop10(summaryPoint,idxArrSmTr);
    	getTop10(summaryFPoint, idxArrSmTrF);
    	getTop10(tgL_incl_Sm,idxArrTIS );
    	
    	getTop10(TotalScore,idxArrTT);
    */

    	///각 유사도 계산 방법 별 상위 10개의 문서의 idx를 idxArr에 넣어줍니당///
    	getTop10(cosSim_A,idxArrCM_A);
    	getTop10(cosSim_Q,idxArrCM_Q);
    	getTop10(cosSim_bi_A,idxArrCS_A);
    	getTop10(cosSim_bi_Q,idxArrCS_Q);
    	getTop10(cosSim_tri_A,idxArrCT_A);
    	getTop10(cosSim_tri_Q,idxArrCT_Q);
    	getTop10(BMscore_A,idxArrBM_A);
    	getTop10(BMscore_Q,idxArrBM_Q);
    	getTop10(BMscore_bi_A,idxArrBS_A);
    	getTop10(BMscore_bi_Q,idxArrBS_Q);
    	getTop10(BMscore_tri_A,idxArrBT_A);
    	getTop10(BMscore_tri_Q,idxArrBT_Q);
    	getTop10(target_bi,idxArrTg);
    	getTop10(target_uni,idxArrTu);
    	getTop10(summery_bi, idxArrSmry);
    	getTop10(numOfMat,idxArrTgC);
    	getTop10(summaryPoint,idxArrSmTr);
    	getTop10(summaryFPoint, idxArrSmTrF);
    	getTop10(tgL_incl_Sm,idxArrTIS );
    	
    	getTop10(TotalScore,idxArrTT);
    	
    	///상위 열개 문서의 idx들을 searchResult에 모아줘요 나중에 xml 쓰는 용///
    	searchResults_BM_A[queryIdx] = idxArrBM_A;
    	searchResults_BM_Q[queryIdx] = idxArrBM_Q;
    	searchResults_BS_A[queryIdx] = idxArrBS_A;
    	searchResults_BS_Q[queryIdx] = idxArrBS_Q;
       	searchResults_BT_A[queryIdx] = idxArrBT_A;
    	searchResults_BT_Q[queryIdx] = idxArrBT_Q;
    	searchResults_CM_A[queryIdx] = idxArrCM_A;
    	searchResults_CM_Q[queryIdx] = idxArrCM_Q;
    	searchResults_CS_A[queryIdx] = idxArrCS_A;
    	searchResults_CS_Q[queryIdx] = idxArrCS_Q;
    	searchResults_CT_A[queryIdx] = idxArrCT_A;
    	searchResults_CT_Q[queryIdx] = idxArrCT_Q;
    	searchResults_Tg[queryIdx] = idxArrTg;
    	searchResults_Tu[queryIdx] = idxArrTu;
    	serachResults_SM[queryIdx] = idxArrSmry;
    	searchResults_TgC[queryIdx] = idxArrTgC;
    	searchResults[queryIdx] = idxArrTT;
    	searchResults_SMT[queryIdx] = idxArrSmTr;
    	searchResults_TIS[queryIdx] = idxArrTIS;
    	
    	if(DEBUG)
    	{
    		printRank("CoSim Morph",idxArrCM_Q,idxArrCM_A,cosSim_Q,cosSim_A);
	    	printRank("CoSim Bi",idxArrCS_Q,idxArrCS_A,cosSim_bi_Q,cosSim_bi_A);
	    	printRank("CoSim Tri",idxArrCT_Q,idxArrCT_A,cosSim_tri_Q,cosSim_tri_A);
	    	printRank("BM Morph",idxArrBM_Q,idxArrBM_A,BMscore_Q,BMscore_A);
	    	printRank("BM Bi",idxArrBS_Q,idxArrBS_A,BMscore_bi_Q,BMscore_bi_A);
	    	printRank("Target bi", idxArrTg, target_bi);

	    	printRank("Summery", idxArrSmry, summery_bi);

	       	printRank("BM Tri",idxArrBT_Q,idxArrBT_A,BMscore_tri_Q,BMscore_tri_A);
	      	printRank("Target uni", idxArrTu, target_uni);
	    	printRank("Target contains", idxArrTgC, numOfMat);
	      	printRank("Summery Trimed", idxArrSmTr, summaryPoint);
	    	printRank("Summery Trimed Focus", idxArrSmTrF, summaryFPoint);
	    	printRank("target included summery", idxArrTIS, tgL_incl_Sm);
	    	printRank("Total", idxArrTT, TotalScore);
    	}
    	
    	
    	{
    		idxArr[queryIdx] = idxArrTT;
    		for(int k = 0; k< scoreArr[queryIdx].length; k++)
    		{
    			scoreArr[queryIdx][k] = TotalScore[idxArrTT[k]];
    		}
    	}
    	
    	if(DEBUG)
    	{
    		System.out.println("answers' score");
    		int[] answers = Answers[queryIdx];
    		
    		for(int ans : answers)
    		{
    			System.out.println("***********" + ans + "*******************");
    			System.out.println( "target unigram : " + target_uni[ans]);
    			System.out.println( "target contains : " + numOfMat[ans]);
    			System.out.println( "target contains : " + numOfMat[ans]);
    			System.out.println( "Summery Trimed : " + summaryPoint[ans]);
    			System.out.println( "Summery Trimed Focus : " + summaryFPoint[ans]);
    			System.out.println( "target included summery : " + tgL_incl_Sm[ans]);
    			System.out.println( "target bi : " + target_bi[ans]);
    			System.out.println( "total : " + TotalScore[ans]);
    		}
    	}

    }
    private void getSummaryPoint(String query, String[] queryTg)
    {
    	//summaryPoint = 
    	String query_str = new String(query+"\t");
    	for(String tg : queryTg)
    	{
    		if(tg == null) break;
    		query_str += tg+" ";
    	}
    	SQ.calcSummary(query_str,summaryPoint, summaryFPoint);
    }
	private double catSimilarity(int fileidx,int queryidx, int[][] docCat2, int[] queryCat2) {
		// TODO Auto-generated method stub
		int numOfMat = 0;
		for(int i = 1; i<docCat2[fileidx].length;i++)
		{
			if(docCat2[fileidx][i] == 1)
			{
				
					if(queryCat2[i]==1) numOfMat++;
			}
		}
		return numOfMat;
	}

	private int checkContains(String[] queryTg/*included*/, String[] queryDoc/*including*/) {
		// TODO Auto-generated method stub
		int numOfMat = 0;
		for(int i = 0;i<queryTg.length;i++)
		{
			if(queryTg[i] == null) break;
			for(int j = 0; j<queryDoc.length;j++)
			{
				if(queryDoc[j] == null) break;
				
				//if(queryTg[i].replaceAll(" ", "").contains(queryDoc[j].replaceAll(" ", ""))) numOfMat++; // 0.10
				if(queryDoc[j].isEmpty() || queryTg[i].isEmpty() ) continue;
				if(queryTg[i].length() != 1)
				{
					if(queryDoc[j].contains(queryTg[i])) numOfMat++; // 0.186
				}
				else //length of [included] is 1
				{
					if(queryDoc[j].equals(queryTg[i])) numOfMat++;
					else if(queryDoc[j].indexOf(queryTg[i])== 0) numOfMat++;
					else
					{
						String[] splited = queryDoc[j].split(" ");
						for(String sp : splited)
						{
							if(sp.equals(queryTg[i])) numOfMat++;
						}
							
					}
				}
					
					
			}
			
		}
		return numOfMat;
	}

	

	private double sumScore(double BMscore_A, double BMscore_Q, double BMscore_bi_A, double BMscore_bi_Q
    		, double BMscore_tri_A, double BMscore_tri_Q, double cosSim_A, double cosSim_Q, double cosSim_bi_A,
			double cosSim_bi_Q, double cosSim_tri_A, double cosSim_tri_Q, double target_bi, double target_uni
			,double summery_bi, double summary_trimed,double summary_trimed_F, double qrTg_in_docTg, double smr_in_tg) {
		// TODO Auto-generated method stub
    /*	BMscore_A[validDocs[i]],BMscore_Q[validDocs[i]],BMscore_bi_A[validDocs[i]]
				,BMscore_bi_Q[validDocs[i]],BMscore_tri_A[validDocs[i]],BMscore_tri_Q[validDocs[i]]
				,cosSim_A[validDocs[i]],cosSim_Q[validDocs[i]],cosSim_bi_A[validDocs[i]],cosSim_bi_Q[validDocs[i]]
				,cosSim_tri_A[validDocs[i]],cosSim_tri_Q[validDocs[i]],target_bi[validDocs[i]]*/
				
    	double sum;
    	
    	/*sum =   0.0*cosSim_A
    			+ 0.0*cosSim_bi_A 
    			+ 0.0*cosSim_tri_A  
    			+ 5.31481336e+01*BMscore_A 
    			+ 0.0*BMscore_bi_A 
    			+ 2.16722183e+01*BMscore_tri_A 
    			+ 0.0*cosSim_Q 
    			+ 0.0*cosSim_bi_Q 
    			+ 4.80685881e+02*cosSim_tri_Q 
    			+ 0.0*BMscore_Q 
    			+ 0.0*BMscore_bi_Q 
    			+ 2.04515828e+02*BMscore_tri_Q 
    			+ 3.81467451e+01*target_bi 
    			+ 1.19733934e+01*target_uni 
    			+ 0.0*summery_bi 
    			+ 1.11146943e+02*summary_trimed 
    			+ 1.74457346e+02*summary_trimed_F
    			+ 3.40613990e-01*qrTg_in_docTg 
    			+ 5.96916230e-01*smr_in_tg;
   */
    	/*sum =   0.0*cosSim_A
    			+ 0.0*cosSim_bi_A 
    			+ 0.0*cosSim_tri_A  
    			+ 72.46693591*BMscore_A 
    			+ 0.0*BMscore_bi_A 
    			+ 70.51532455*BMscore_tri_A 
    			+ 0.0*cosSim_Q 
    			+ 0.0*cosSim_bi_Q 
    			+ 398.49870448*cosSim_tri_Q 
    			+ 0.0*BMscore_Q 
    			+ 0.0*BMscore_bi_Q 
    			+ 68.04903189*BMscore_tri_Q 
    			+ 0.0*target_bi 
    			+ 29.9404722*target_uni 
    			+ 0.0*summery_bi 
    			+ 105.86232265*summary_trimed 
    			+ 105.86232265*summary_trimed_F
    			+ 0.46130023*qrTg_in_docTg 
    			+ 0.42719733*smr_in_tg;
		return sum;*/
    	/*
    	sum =   0.0*cosSim_A
    			+ 0.0*cosSim_bi_A 
    			+ 0.0*cosSim_tri_A  
    			+ 5.29321333e+01*BMscore_A 
    			+ 0.0*BMscore_bi_A 
    			+ 2.18739436e+01*BMscore_tri_A 
    			+ 0.0*cosSim_Q 
    			+ 0.0*cosSim_bi_Q 
    			+ 4.81119209e+02*cosSim_tri_Q 
    			+ 0.0*BMscore_Q 
    			+ 0.0*BMscore_bi_Q 
    			+ 2.04601772e+02*BMscore_tri_Q 
    			+ 3.80467334e+01*target_bi 
    			+ 1.20815782e+01*target_uni 
    			+ 0.0*summery_bi 
    			+ 1.11167453e+02*summary_trimed 
    			+ 1.74579428e+02*summary_trimed_F
    			+ 3.45089020e-01*qrTg_in_docTg 
    			+  3.72695866e-01*smr_in_tg;*/
    /*	sum =   0.0*cosSim_A
    			+ 0.0*cosSim_bi_A 
    			+ 0.0*cosSim_tri_A  
    			+ 72.46693591*BMscore_A 
    			+ 0.0*BMscore_bi_A 
    			+ 70.51532455*BMscore_tri_A 
    			+ 0.0*cosSim_Q 
    			+ 0.0*cosSim_bi_Q 
    			+ 398.49870448*cosSim_tri_Q 
    			+ 0.0*BMscore_Q 
    			+ 0.0*BMscore_bi_Q 
    			+ 68.04903189*BMscore_tri_Q 
    			+ 0.0*target_bi 
    			+ 29.9404722*target_uni 
    			+ 0.0*summery_bi 
    			+ 105.86232265*summary_trimed 
    			+ 105.86232265*summary_trimed_F
    			+ 0.46130023*qrTg_in_docTg 
    			+ 0.42719733*smr_in_tg;
   */
    	sum =   cosSim_A
    			+ cosSim_bi_A 
    			+ cosSim_tri_A  
    			+ BMscore_A 
    			+ BMscore_bi_A 
    			+ BMscore_tri_A 
    			+ cosSim_Q 
    			+ cosSim_bi_Q 
    			+ cosSim_tri_Q 
    			+ BMscore_Q 
    			+ BMscore_bi_Q 
    			+ BMscore_tri_Q 
    			+ target_bi 
    			+ target_uni 
    			+ summery_bi 
    			+ summary_trimed 
    			+ summary_trimed_F
    			+ qrTg_in_docTg 
    			+ smr_in_tg;
		return sum;
	}

	private void printRank(String model, int[] arrQ, int[] arrA, double[] scoreQ, double[] scoreA) {
		// TODO Auto-generated method stub
    	int[] arr;
    	double[] score;
    	String[] documents;
    	String[][] mph;
		System.out.println("<<"+ model+">>");
		for(int i = 0; i<2; i++)
		{
			if(i == 0 ) 
			{
				System.out.println("::Q");
				arr = arrQ;
				score = scoreQ;
				documents = QnA.documentsQ;
				mph = QnA.docVectorsQ;
			}
			else 
			{
				System.out.println("::A");
				arr = arrA;
				score = scoreA;
				documents = QnA.documentsA;
				mph = QnA.docVectorsA;
			}
			for(int j = 0;j<10;j++)
			{
				System.out.println((j+1) + " : " + arr[j] + " : " + score[arr[j]]);
				System.out.println(documents[arr[j]]);
				String mp = new String();
				for(int k = 0;k<mph[arr[j]].length;k++)
				{
					if(mph[arr[j]][k] == null) break;
					mp+=mph[arr[j]][k];
				}
				System.out.println(mp);
			}
			
		}
		
	}
    private void printRank(String model, int[] arr, double[] score) {
		// TODO Auto-generated method stub
    	
		System.out.println("<<"+ model+">>");
		
			for(int j = 0;j<10;j++)
			{
				System.out.println((j+1) + " : " + arr[j]+ " : " + score[arr[j]]);
				System.out.println(QnA.documentsQ[arr[j]]);
				System.out.println(QnA.documentsA[arr[j]]);
				String target = new String();
				for(int i = 0;i<QnA.docVectorsTgBi[arr[j]].length;i++)
				{
					if(QnA.docVectorsTgBi[arr[j]][i] == null) break;
					target += QnA.docVectorsTgBi[arr[j]][i] + "/";
				}
				System.out.println(target);
				
				if(QnA.docVectorsTgUni[arr[j]]!=null)
				{
					target = new String();
					for(int i = 0;i<QnA.docVectorsTgUni[arr[j]].length;i++)
					{
						if(QnA.docVectorsTgUni[arr[j]][i] == null) break;
						target += QnA.docVectorsTgUni[arr[j]][i] + "/";
					}
					System.out.println(target);
				}
				if(QnA.docVectorsTgWord[arr[j]]!=null)
				{
					target = new String();
					for(int i = 0;i<QnA.docVectorsTgWord[arr[j]].length;i++)
					{
						if(QnA.docVectorsTgWord[arr[j]][i] == null) break;
						target += QnA.docVectorsTgWord[arr[j]][i] + "/";
					}
					System.out.println(target);
				}
				if(QnA.summary_trimed[arr[j]]!=null)
				{
					target = new String();
					for(int i = 0;i<QnA.summary_trimed[arr[j]].length;i++)
					{
						if(QnA.summary_trimed[arr[j]][i] == null) break;
						target += QnA.summary_trimed[arr[j]][i] + "/";
					}
					System.out.println(target);
				}
			}
	}


    private void getTop10(double[] arr, int idx10[])
    {
    	double[] top10 = {-100, -100,-100,-100,-100,-100,-100,-100,-100,-100};

    	double[] temp;
    	int[] itemp;
    	
    	for(int i = 0;i<10;i++)
    	{
    		idx10[i] = 0;
    	}
    	for(int i=0;i<arr.length;i++)
    	{
    		for(int j=0;j<10;j++)
    		{
    			if(arr[i]>top10[j])
    			{
    				temp = Arrays.copyOfRange(top10, j, 9);
    				top10[j] = arr[i];
    				for(int k = j+1;k<10;k++)
    				{
    					top10[k] = temp[k-j-1];
    				}
    				
    				itemp = Arrays.copyOfRange(idx10, j, 9);
    				idx10[j] = i;
    				for(int k = j+1;k<10;k++)
    				{
    					idx10[k] = itemp[k-j-1];
    				}
    				
    				break;
    			}
    			
    		}
    	}
    	
    }
    ///////getValidsDocs///////
    //쿼리에 포함된 형태소 또는 음절 ngram을 포함한 문서들의 index들을 validDocs에 추가, 추가한 만큼 validx 키워줌 validx 리턴 안해도 바뀔텐데 굳이 리턴함
    ///////////////////////////
    private int getValidsDocs(int[] validDocs, int validx, String[] queryV, HashMap<String, Integer[]> tfV) {
		// TODO Auto-generated method stub

		for(String vec : queryV)
    	{
    		Integer[] DocIdxs = tfV.get(vec);//vec(쿼리 내의 형태소 혹은 ngram)을 포함한 문서들의 idx들의 array
    		if(DocIdxs!=null)
	    		for(int idx : DocIdxs)
	    		{
	    			if(ut.array_search(validDocs, idx)== -1) 
	    			{
	    				validDocs[validx++] = idx;//validDocs에 해당 idx 들의 array를 넣어줘요, 넣은 만큼 validx 키움
	    				
	    			}
	    		}
    		else
    			continue;
    	}
		
		return validx;
	}

	   private double cosineSimilarity(double[] docVector1, double[] docVector2) {
	        double dotProduct = 0.0;
	        double magnitude1 = 0.0;
	        double magnitude2 = 0.0;
	        double cosineSimilarity = 0.0;

	        for (int i = 0; i < docVector1.length; i++) //docVector1 and docVector2 must be of same length
	        {
	            dotProduct += docVector1[i] * docVector2[i];  //a.b
	            magnitude1 += Math.pow(docVector1[i], 2);  //(a^2)
	            magnitude2 += Math.pow(docVector2[i], 2); //(b^2)
	        }

	        magnitude1 = Math.sqrt(magnitude1);//sqrt(a^2)
	        magnitude2 = Math.sqrt(magnitude2);//sqrt(b^2)

	        if (magnitude1 != 0.0 && magnitude2 != 0.0) {
	            cosineSimilarity = dotProduct / (magnitude1 * magnitude2);
	        } else {
	            return 0.0;
	        }
	        return cosineSimilarity;
	    }



	   private void getPoint(int i/*name of valid document*/,double[] point_doc, double[] point_query, String[][] docVectors2,
				String[] queryVector2, int[][] vectorCounts2, int[] queryVecCount2, HashMap<String, ArrayList<Integer>> tfidfVectors2) {
			// TODO Auto-generated method stub
	     	HashSet<String> totalVector = new HashSet<String>();	    	
	    	for(int j=0;j<docVectors2[i].length;j++)
	    	{
	    		if(docVectors2[i][j] == null) break;
	    		if(!totalVector.contains(docVectors2[i][j])) totalVector.add(docVectors2[i][j]);
	    	
	    	}
	    	for(int j=0;j<queryVector2.length;j++)
	    	{
	    		if(queryVector2[j] == null) break;
	    		if(!totalVector.contains(queryVector2[j])) totalVector.add(queryVector2[j]);
	    	}
	    	
	    	int j = 0;
	    	for(String vec : totalVector)
	    	{
	    		
	    		int idx = ut.array_search(docVectors2[i], vec);
	       		if(idx!=-1) point_doc[j] = (double)vectorCounts2[i][idx];
	    		else point_doc[j] = 0.0;
	    		
	    		idx = ut.array_search(queryVector2, vec);
	    		if(idx != -1) point_query[j]= (double)queryVecCount2[idx];
	    		else  point_query[j] = 0.0;
	    		
	    		double idf;
	    	
	    		if(tfidfVectors2.get(vec)!=null)
	    		{
	    			double N = docVectors2.length, /* 요요 이거 근사값임 ㅡㅡ vector count 를 다 더해야 하는거 아닌가?*/
	    					n = (double)(tfidfVectors2.get(vec).size());//vec 가지는 문서의 갯! 수!
	    			idf = Math.log(N/n);
	    			
	    		}
	    		else
	    		{
	    			idf = 0.0;
	    		}
	    		
	    		

	    		point_doc[j] *=idf/*idf*/;
	    		point_query[j] *=idf/*idf*/;


	    		j++;
	    	}
	    
	    	return;
		}
	   //////getPoint//////
	   //한문서, 쿼리 간 BM25 점수 계산하고, 0-1사이의 값으로 변환
	   //0-1사이 값으로 만들으려고 시그모이드 함수를 썼는데 이제와서 생각해 보니 이래서 bm25 성능이 안좋았던 것 같아요... 다른 방법을 사용해서 정규화 해보는게 어떨까요
	   //raw count를 term frequency로 썻네요... count로 한다고 안되는 건 아닐 것 같은데, 문장길이로 정규화 하는게 더 좋을 것 같아요ㅠㅠ 어떻게 상탄거지
	   //point_doc, point_query에 각 형태소(또는 음절 ngram)의 tf-idf값들이 들어가요.
	   ////////////////////
	private double getPoint(int i/*name of valid document*/,double AVGDL, double[] point_doc, double[] point_query, String[][] docVectors2,
			String[] queryVector2, int[][] vectorCounts2, int[] queryVecCount2, HashMap<String, Integer[]> tfidfVectors2) throws Exception {
		// TODO Auto-generated method stub
    	double BMscore = 0.0;
    	double K = 1.6; //하이퍼 파라미터
    	double B = 0.75; //하이퍼 파라미터
    	
    	double a = 0.06; //하이퍼 파라미터
    	double b = 0.6;  //하이퍼 파라미터
    	double BMsigmoid;
    	
    
    	HashSet<String> totalVector = new HashSet<String>();
    	
    	
    	int docLen = 0;
    	for(int j=0;j<docVectors2[i].length;j++)
    	{
    		if(docVectors2[i][j] == null) break;
    		if(!totalVector.contains(docVectors2[i][j])) totalVector.add(docVectors2[i][j]);
    		docLen += vectorCounts2[i][j];
    	}
    	for(int j=0;j<queryVector2.length;j++)
    	{
    		if(queryVector2[j] == null) break;
    		if(!totalVector.contains(queryVector2[j])) totalVector.add(queryVector2[j]);
    	}
    	
    	int j = 0;
    	for(String vec : totalVector)
    	{
    		
    		int idx = ut.array_search(docVectors2[i], vec);
    		
    		if(idx!=-1) point_doc[j] = (double)vectorCounts2[i][idx];//???
    		else point_doc[j] = 0.0;
    		
    		idx = ut.array_search(queryVector2, vec);
    		if(idx != -1) point_query[j]= (double)queryVecCount2[idx];//????
    		else  point_query[j] = 0.0;
    		
    		double idf, BMidf;
    	
    		if(tfidfVectors2.get(vec)!=null)
    		{
    			double N = docVectors2.length, 
    					n = (double)(tfidfVectors2.get(vec).length);
    			idf = Math.log(N/n);
    			BMidf = Math.log((N-n+0.5)/(n+0.5));
    		}
    		else
    		{
    			idf = BMidf = 0.0;
    		}
    		
    		if(!ut.isSignificantV(idf,vec,fosidf)) BMidf = idf = 0;
    		
    		if(ut.array_search(queryVector2, vec)!=-1)
    			BMscore += BMidf/*idf*/*point_doc[j]*(1+K)/(point_doc[j]+K*(1-B+B*docLen/AVGDL));
    		
    		point_doc[j] *=idf;
    		point_query[j] *=idf;
    		
    		
    		j++;
    	}
    	
    	BMsigmoid = 1/(1+Math.exp(-a*BMscore));
    	
    	
    	return BMsigmoid*b;
	}

	public void initSummery() {
		// TODO Auto-generated method stub
		try {
			SQ = new summary_query(".\\summary\\summary.txt");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//int [][] arrIdx = new int[QUERYSIZE][10];
		/*for(int i = 0; i<QUERYSIZE;i++)
		{
			getTop10(summaryPoint[i], arrIdx[i]);
		}
		
		MAP map = new MAP(arrIdx, Answers, 10);
		System.out.println("MAP summaryQuery:::::::::::::: " + map.Get_Map() );
		*/
	}

	public void setCat(int[][] answer) {
		// TODO Auto-generated method stub
		queryCat = answer[1];
		if(QnA.isWhiteQuery)
		{
			queryCat[1] = 1;
		}
	}

}
