import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
//자바를 씨처럼의 좋은 예
//시간복잡도 1도 생각안한 좋은 예

//'tg, target, 타겟'
//쿼리의 '핵심부'를 뜻합니다.
//저희가 사용했던 도메인이 맞춤법이나 국어사용관련 질의 도메인이였기 때문에, 쿼리에서 인용구나 용례가 나오는지, 어디서 나오는지를 CRF로 추출해요.
//'summary'
//Q/A쌍 문서 각각에 대해 사람이 손으로 만든 요약 네이*에서 긁어왔어요
//타겟이랑 서머리 둘다 과제에선 안쓴다구 들었어요

public class AlzameeMain {
public static final String fileEncoding = "UTF-8";

//하이퍼 파라미터들.. 죄송합니다 이런코드라서
static int NUMOFDOCUMENTS = 5537;
static int NUMOFQUERIES = 20;
static int NUMOFMODELS = 19;
static int NUMOFANSWERS = 10;

static String[] query_list;

//20개 쿼리 각각에 대해서 유사도 점수가 제일 높은 상위 10개 문서의 인덱스, 스코어를 idx_array score_array에 넣어뒀다가 마지막에 xml 파일에 출력해요
static int[][] idx_array = new int[NUMOFQUERIES][NUMOFANSWERS];
static double[][] score_array = new double[NUMOFQUERIES][NUMOFANSWERS];

public static void main(String[] args) throws Exception {

	// TODO Auto-generated method stub
	System.out.println("Hello!");
	String[] queryMph = new String[2];//계속 Mph이런 약자가 나오는건 morphem(맞나요??) 무튼 형태소란 뜻이에요. 쿼리 형태소 분석결과에요. 
									  //굳이 쓰지도 않으면서 2차원으로 잡은건 svm 코드랑 붙이는데 svm 짠 친구가 그렇게 해달라그래서...queryMph[0]는 안써요...(환장)
	Compact cp = new Compact(); //SVM으로 쿼리의 카테고리를 결정에요. 나중에 유사도 점수 계산할때 같은 카테고리의 문서들에 가중치를 줍니다.
	DocumentParser dpQnA = new DocumentParser(".\\AllDocuments\\");//구문구조 파싱하는거 아니에요 그냥 QA문서 읽어서 형태소분석하고, ngram 만들어주고 그래요
	System.out.println("parsing complete");
	
	SimCalculator sim = new SimCalculator(dpQnA);//유사도 점수 계산하는 모듈
	//tf (term frequency) 계산을... 하는건 아니구요..ㅠㅠ [key:형태소나 음절ngram value:해당 형태소나 음절 ngram이 등장한 문서번호 array] 이런 해쉬를 만들어요 코드에서 이 해쉬를 계속 tf라고 하네요..죄송해요
	sim.tfCalculator();
	////////////////////////
	System.out.println("TF calculating complete");
	 
	read_query();
	
	String Query;
  	for(int i = 0;i<NUMOFQUERIES;i++)
  	{
		Query = query_list[i];
		System.out.println("Now...." + Query+"...");
		queryMph[1]= dpQnA.parseQuery(Query);//쿼리 형태소 분석
		sim.initSummery();
		cp.getter(1, queryMph);//SVM 모델 가져옴
		sim.setCat(cp.answer);//cp.answer : query의 category  이때 SVM 코딩은 제가 했던게 아니라..ㅠㅠ 잘 모르겠어용...
		////드디어 쿼리랑 문서들이랑 유사도 계산////
		sim.getSimilarity(i,idx_array,score_array); //calculates cosine similarity  
		/////////////////////////////////
  	}
  	     
  	write_xml();
  	System.out.println("Bye!");
	}

private static void read_query(){
    query_list = new String[NUMOFQUERIES];

       int query_num = 0;
       String queryFileName = "query.xml";
       Scanner scan;
       try {
          scan = new Scanner(new File(queryFileName),"UTF-8");
       
          ArrayList<String> qList = new ArrayList<String>();
          String buf;

          while(scan.hasNext()){
             buf = scan.nextLine();

             if(buf.equals("<text>")){
                query_list[query_num] = scan.nextLine();
                query_num++;
             }
          }

          scan.close();
       
       } catch (FileNotFoundException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
       }
}
public static void write_xml() throws IOException{
    BufferedWriter out = new BufferedWriter(new FileWriter("result.xml"));
    for(int query_num = 0; query_num< NUMOFQUERIES; query_num++)
    {
	    out.write("<query><qnum>"+(query_num+1)+"</qnum>");
	    out.newLine();
	    out.write("<rank>");
	    out.newLine();
	    for(int i=0 ; i<idx_array[query_num].length ; i++){
	       out.write(idx_array[query_num][i]+"\t"+score_array[query_num][i]);
	       out.newLine();
	    }
	    out.write("</rank>");
	    out.newLine();
	    out.write("</query>");
	    out.newLine();
    }
    out.close();
 }
}
