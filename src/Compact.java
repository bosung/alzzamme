
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.StringTokenizer;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;

public class Compact {
	

	//추가
	private static double atof(String s)
	{
		return Double.valueOf(s).doubleValue();
	}
	//추가
	private static int atoi(String s)
	{
		return Integer.parseInt(s);
	}
	//추가
	private static void predict(BufferedReader input, DataOutputStream output, svm_model model, int predict_probability) throws IOException
	{
		int svm_type=svm.svm_get_svm_type(model);
		int nr_class=svm.svm_get_nr_class(model);
		double[] prob_estimates=null;

		if(predict_probability == 1)
		{
			if(svm_type == svm_parameter.EPSILON_SVR || svm_type == svm_parameter.NU_SVR)
			{
				svm_predict.info("Prob. model for test data: target value = predicted value + z,\nz: Laplace distribution e^(-|z|/sigma)/(2sigma),sigma="+svm.svm_get_svr_probability(model)+"\n");
			}
			else
			{
				int[] labels=new int[nr_class];
				svm.svm_get_labels(model,labels);
				prob_estimates = new double[nr_class];
				output.writeBytes("labels");
				for(int j=0;j<nr_class;j++)
					output.writeBytes(" "+labels[j]);
				output.writeBytes("\n");
			}
		}
		while(true)
		{
			String line = input.readLine();
			if(line == null) break;

			StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");

			double target = atof(st.nextToken());
			int m = st.countTokens()/2;
			svm_node[] x = new svm_node[m];
			for(int j=0;j<m;j++)
			{
				x[j] = new svm_node();
				x[j].index = atoi(st.nextToken());
				x[j].value = atof(st.nextToken());
			}

			double v;
			if (predict_probability==1 && (svm_type==svm_parameter.C_SVC || svm_type==svm_parameter.NU_SVC))
			{
				v = svm.svm_predict_probability(model,x,prob_estimates);
				output.writeBytes(v+" ");
				for(int j=0;j<nr_class;j++)
					output.writeBytes(prob_estimates[j]+" ");
				output.writeBytes("\n");
			}
			else
			{
				v = svm.svm_predict(model,x);
				output.writeBytes(v+"\n");
			}
		}
	}

	//위에 모두 추가된 것
	
	
	final static String result_dir="./svm/result/result";
	//final static String libsvm_dir = ".\\svm\\libsvm-3.20\\windows";
	final static String query_svm_dir = "./svm/test/test_data";
	final static String model_svm_dir = "./svm/model/model";
	//final static String query_dir="./before_data/before_KMA_query_2015.txt";
	final static String khiword_crf_dir = "./svm/word_chi/khi_word";
	
	
	
	public static int answer[][];
	static int query_cnt = 0;
	static HashMap<String,Integer>word_hash = new HashMap<String,Integer>();
	final static String query[] = new String[1000]; 
	/*static void getter(int cnt, String[] t)
	{
		query_cnt = cnt;
		for(int i=1;i<=query_cnt;i++)query[i] = t[i];
	}*/
	
	//추가
		static svm_model model[] = new svm_model[8];

		
		
	static void make_svm_input(int cate) throws IOException
	{
		int i, j, v, size = word_hash.size(), have[] = new int[word_hash.size()+1]; 
		String str;
		FileWriter out = new FileWriter(query_svm_dir+cate+".txt");
		BufferedWriter outer = new BufferedWriter(out);
	
		for(i=1; i<= query_cnt; i++)
		{
			StringTokenizer token = new StringTokenizer(query[i], "+ ");
			while(token.hasMoreTokens())
			{
				str = token.nextToken();
				if(word_hash.containsKey(str))have[word_hash.get(str)] = i;
			}
			out.write("0 ");
			for(j=1; j<=size; j++)
			{
				v = have[j]==i ? 1 : 0;
				out.write(j+":"+v);
				if(j!=size)out.write(" ");
			}
			if(i!=query_cnt)out.write("\n");
		}
		outer.close();
		out.close();
	}
	
	static void make_hash() throws ClassNotFoundException, FileNotFoundException, IOException
	{
		for(int i=1;i<=7;i++)
		{
			ObjectInputStream ois;
			ois = new ObjectInputStream(new FileInputStream(khiword_crf_dir+i+".khi"));
			word_hash =  (HashMap<String, Integer>) ois.readObject();
			ois.close();
			make_svm_input(i);
			word_hash.clear();
		}
	}

	static void Section5() throws IOException, InterruptedException
	{

		Process process = null; int i, j, t;
		Runtime runTime; answer = new int[query_cnt+1][8];
		for(i = 1; i<= 7; i++)
		{
			
			//추가
			BufferedReader input = new BufferedReader(new FileReader(query_svm_dir+i+".txt"));
			DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(result_dir+i+".txt")));
			predict(input,output,model[i],0);
			input.close();
			output.close();
			
			/*runTime = Runtime.getRuntime();	
			process = runTime.exec(libsvm_dir+"\\svm-predict.exe svm\\test\\test_data"+i+".txt svm\\model\\model"+i+".txt svm\\result\\result"+i+".txt");
			process.getErrorStream().close();
			process.getInputStream().close();
			process.getOutputStream().close();
			process.waitFor();*/
			BufferedReader bw = new BufferedReader(new FileReader(result_dir+i+".txt"));
		
			for(j=1;j<=query_cnt;j++)
			{
				//System.out.println("**********************" + i + "*"+ j);
				t = bw.readLine().charAt(0)-48;
				if(t==-3)t=-1;
				if(t==1)answer[j][i] = 1;
				else answer[j][i] = 0;
			}
			bw.close();
		}
	}
	static void outputer()
	{
		/*for(int i=1;i<=query_cnt;i++)
		{
			for(int j=1;j<=7;j++)System.out.print(answer[i][j]+" ");
			System.out.print("\n");
		}*/
	}

	public void getter(int cnt, String[] t) throws Exception, Exception, Exception 
	{
		query_cnt = cnt;
		for(int i=1;i<=query_cnt;i++)query[i] = t[i];
		
		//try{
			for(int i=1;i<=7;i++)//추가 - 생성자에서 하기
			{model[i] = svm.svm_load_model(model_svm_dir+i+".txt");}
			make_hash();
			Section5();
			outputer();
		//}catch(Exception e){ System.out.println("error 1"+" : "+e.getMessage()); }	
		
		
	}
	
	
}


