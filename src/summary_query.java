


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class summary_query {
	public static final String fileEncoding = "UTF-8";

	private String smryFilePath = "";
	double[] score;
	double[] fscore;
	////////////////////////////////////////////////////////////////////////////////
	
	
	static double cosine_similarity(int[] q, int[]d){
		
		double inn_prod = 0;
		double norm1=0, norm2 = 0;
		for(int i=0; i < q.length ; i++){
			inn_prod += q[i] * d[i];
			norm1 += q[i]*q[i];
			norm2 += d[i]*d[i];
		}
	    norm1 = Math.sqrt(norm1);
	    
	    if(((Math.sqrt(norm1))*Math.sqrt(norm2)) == 0 ){
	    	return 0;
	    }
	    else {
	    	return inn_prod / ((Math.sqrt(norm1))*Math.sqrt(norm2));
	    }
		
		
	}
	
	
	private void summary_queryfunc(String query, BufferedReader summary_file, String focus, boolean focus_TF) throws IOException{
		String line = null;
		String[] line_split = null;

		
		HashMap<String, Integer > word_hasging = new HashMap<String,Integer>();
		

		int hash_num = 0;
		while(true){
			line = summary_file.readLine();
			
			if(line == null) break;
			
			line_split = line.split("\t");
			
		//	int num = new Integer(line_split[0]).intValue();
		
			line_split[1] = " "+line_split[1]+" ";
			
			for(int i=0; i<line_split[1].length() ; i++){
				if(i+2 < line_split[1].length()){ 
					String temp = line_split[1].substring(i, i+2);
					if(!word_hasging.containsKey(temp)){
						word_hasging.put(temp, hash_num);
						hash_num++;
					}
				}
			}

		}
			//������� �ؽø���
		
		
		//query 
		query =" "+query+" ";
		
		int[] q_vec = new int[word_hasging.size()];
		int q_idx = 0;
		
		for(int i=0; i < query.length() ; i++){
			if(i+2 < query.length() && word_hasging.get(query.substring(i,i+2)) != null){
				q_idx = word_hasging.get(query.substring(i,i+2));
				q_vec[q_idx] =1 ;
			}
		}
		
		//focus
		int[] fq_vec = new int[word_hasging.size()];
		
		
		if(focus_TF == true){
			focus =" "+focus+" ";

			int fq_idx = 0;
		
			for(int i=0; i < focus.length() ; i++){
				if(i+2 < focus.length() && word_hasging.get(focus.substring(i,i+2)) != null){
					fq_idx = word_hasging.get(focus.substring(i,i+2));
					fq_vec[fq_idx] =1 ;
				}
			}
		}
	
		//5336 ����
		BufferedReader summary = new BufferedReader(new FileReader(smryFilePath));
		int bigger_num = 0;
		while(true){
			line = summary.readLine();
			
			if(line == null) break;
			
			line_split = line.split("\t");
			int d_idx = 0;
			
		//	int num = new Integer(line_split[0]).intValue();
		
			line_split[1] = " "+line_split[1]+" ";
			int[] d_vec = new int[word_hasging.size()];
			
			for(int i=0; i<line_split[1].length() ; i++){
				
				if(i+2 < line_split[1].length() ){
					d_idx = word_hasging.get(line_split[1].substring(i, i+2));
					d_vec[d_idx] = 1;
				}
				
			}
			//int num= Integer.parseInt(line_split[0].trim()); // error occus : string.value[0] == "" !!!!!!!
			int num = new Integer(line_split[0].replaceAll("[\uFEFF-\uFFFF]", "")).intValue();
			score[num] = cosine_similarity(q_vec, d_vec);
			fscore[num] = cosine_similarity(fq_vec, d_vec);
			
	
		}
		
		//System.out.println(bigger_num);
		
		/*double[] total_score = new double[5537];
		
		for(int i=0; i<5537 ; i++){
			
			total_score[i] = fscore[i]+score[i];
		}
		return total_score;
	*/
	}

	public  summary_query(String summaryFilePath/*, String query_split*/) throws Exception{
		this.smryFilePath = summaryFilePath;
	}
	public void calcSummary(String query_str/*query + \t + target*/, double[] scoreQS, double[] scoreTS)
	{
		
		try {
			//	BufferedReader input = new BufferedReader(new FileReader("summary.txt"));
				//String query_str = null;
			score = scoreQS;
			fscore = scoreTS;
				String[] query_split=null;
				
				//double[][] result = new double[20][5537];
				//BufferedReader query = new BufferedReader(new FileReader(".\\summary\\query_focus.txt"));
				
				
					//query_str = query.readLine();
					//if(query_str == null) return null;
					
					query_split = query_str.split("\t");
					
					BufferedReader input = new BufferedReader(new FileReader(smryFilePath));

					if(query_split.length > 1){ 
						summary_queryfunc(query_split[0], input,query_split[1],true);
					}
					else summary_queryfunc(query_split[0], input,null,false);
				
				
			
				
				
				//return result;
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		
	}
	
}
