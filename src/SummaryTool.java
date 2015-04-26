import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Object;
import java.lang.reflect.Array;
import java.util.regex.*;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;

public class SummaryTool 
{
	
	
//Text into sentences
	public static String[] splitToSentences(String content)
	{
		
		String[] sent = sentenceDetect.sentDetect(content);
		return sent;
	}
	
//Text into paragraphs
	public static String[] splitToParagraphs(String content)
	{
    	String[] mystring = content.split("\n\r\n");
		
		return mystring;
	}
	
	public static <T> Collection <T> intersect (Collection <? extends T> a, Collection <? extends T> b)
	{
	    Collection <T> result = new ArrayList <T> ();

	    for (T t: a)
	    {
	        if (b.remove (t)) result.add (t);
	    }

	    return result;
	}
	
//Computing the intersection(common words) between two sentences
	public static float sentenceIntersection (String sentence1, String sentence2)
	{
		String[] sent1 = tokenizer.tokenize(sentence1);
		String[] sent2 = tokenizer.tokenize(sentence2);
		
		if (sent1.length + sent2.length == 0)
			return 0;
		
		
		List<String> intersectArray = (List<String>) intersect(new ArrayList<String>(Arrays.asList(sent1)),new ArrayList<String>(Arrays.asList(sent2)));
		
		float result = ((float)(float)intersectArray.size() / ((float)sent1.length + ((float)sent2.length) / 2));
		
		return result;
	}
	
	public static String[] intersection(String[] sent1, String[] sent2)
	{
		if(sent1 == null || sent1.length == 0 || sent2 == null || sent2.length == 0)
			return new String[0];
		
		List<String> sent1List = new ArrayList<String>(Arrays.asList(sent1));
		List<String> sent2List = new ArrayList<String>(Arrays.asList(sent2));
			
		sent1List.retainAll(sent2List);
		
		String[] intersect = sent1List.toArray(new String[0]);
		
		return intersect;
	}
	
	public static String formatSentence(String sentence)
	{
		return sentence;
	}

	public static String getBestsentenceFromParagraph(String paragraph)
	{
		String[] sentences = splitToSentences(formatSentence(paragraph));
		if(sentences == null || sentences.length <= 2)
			return "";
		
		float[][] intersectionMatrix = getSentenceIntersectionMatrix(sentences);
		
		float[] sentenceScores = getSentenceScores(sentences, intersectionMatrix);
		
		return getBestSentence(sentences,  sentenceScores);
	}
	public static float[][] getSentenceIntersectionMatrix(String[] sentences)
	{
		//Split the content in to sentences
		
		
		int n = sentences.length;
		
		float[][] intersectionMatrix= new float[n][n];
		
		for(int i = 0; i< n; i++)
		{
			for(int j = 0; j< n; j++)
			{
				try
				{
					if(i == j)
						continue;
					
				intersectionMatrix[i][j] = sentenceIntersection(sentences[i], sentences[j]);	
				}
				catch(Exception e)
				{
					System.out.println(e.getMessage());
				}
			}
		}
		
		//Build the sentence dictionary
		//The score of a sentence is the sum of all its intersections
		
		return intersectionMatrix;
	}
	
	public static float[] getSentenceScores(String[] sentences, float[][] scores)
	{
		float[] scoresReturn = new float[sentences.length];
		
		for(int i=0; i<sentences.length; i++)
		{
			int sentenceScore = 0;
			for(int j=0; j<scores[i].length; j++)
			{
				sentenceScore += scores[i][j];				
			}
			scoresReturn[i] = sentenceScore;
		}
		
		return scoresReturn;
	}
	
	public static String getBestSentence(String[] sentences, float[] scores)
	{	
		
		return sentences[getMaxIndex(scores)];
		
	}
	
	public static int getMaxIndex(float[] array)
	{
		int maxIndex = 0;
		float max = -1;
		for(int i=0; i<array.length; i++)
		{
			if(array[i]>max)
			{
				max = array[i];
				maxIndex = i;
			}
			
		}
		return maxIndex;
	}
	
	public static TokenizerME tokenizer;
	public static SentenceDetectorME sentenceDetect;
	public static SummaryTool Instance;
	
	public SummaryTool()
	{
		initialize();
	}
	
	public void initialize()
	{
		InputStream sentenceModelIS = this.getClass().getResourceAsStream("Data/en-sent.bin"); //new FileInputStream("src/Data/en-sent.bin");
		SentenceModel model;
		try 
		{	
			model = new SentenceModel(sentenceModelIS);
			sentenceDetect = new SentenceDetectorME(model);
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		InputStream tokenizerModelIS = this.getClass().getResourceAsStream("Data/en-token.bin"); //new FileInputStream("src/Data/en-token.bin");
		TokenizerModel tokenModel;
		try 
		{	
			tokenModel = new TokenizerModel(tokenizerModelIS);
		    tokenizer = new TokenizerME(tokenModel);
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public static void main(String[] args) 
	{
		String title = "this is a title";
		String content = "";
		
		Instance = new SummaryTool();
		
		 try {
			 
			 content = new Scanner(new File(args[0])).useDelimiter("\\Z").next();
			
		 }
		 catch (FileNotFoundException e) {
		        e.printStackTrace();
		    }		 
		
		String[] paragraphs = splitToParagraphs(content);
		StringBuilder summary = new StringBuilder();
		
		for(String p : paragraphs)
		{
			String bestSent = getBestsentenceFromParagraph(p);
			if(bestSent != null && bestSent.length() > 0)
				summary.append(bestSent);
		}
		
		System.out.println(summary);
	}
}


