import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class AlzameeMain {
public static final String fileEncoding = "UTF-8";

static int NUMOFDOCUMENTS = 5537;
static int NUMOFQUERIES = 20;
static int NUMOFMODELS = 19;
static int NUMOFANSWERS = 10;
static String[] query_list;
static int[][] idx_array = new int[NUMOFQUERIES][NUMOFANSWERS];
static double[][] score_array = new double[NUMOFQUERIES][NUMOFANSWERS];
public static void main(String[] args) throws Exception {

	// TODO Auto-generated method stub
	System.out.println("Hello!");
	String[] queryMph = new String[2];
	Compact cp = new Compact();
	DocumentParser dpQnA = new DocumentParser(".\\AllDocuments\\");
	System.out.println("parsing complete");
	SimCalculator sim = new SimCalculator(dpQnA);
	sim.tfCalculator();
	System.out.println("TF calculating complete");
	 
	read_query();
	String Query;
  	for(int i = 0;i<NUMOFQUERIES;i++)
  	{
		Query = query_list[i];
		System.out.println("Now...." + Query+"...");
		queryMph[1]= dpQnA.parseQuery(Query);
		sim.initSummery();
		cp.getter(1, queryMph);
		sim.setCat(cp.answer);
		sim.getSimilarity(i,idx_array,score_array); //calculates cosine similarity  
		
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
