package search;

import java.io.*;
import java.util.*;

/**
 * This class encapsulates an occurrence of a keyword in a document. It stores the
 * document name, and the frequency of occurrence in that document. Occurrences are
 * associated with keywords in an index hash table.
 * 
 * 
 */
class Occurrence {
	/**
	 * Document in which a keyword occurs.
	 */
	String document;
	
	/**
	 * The frequency (number of times) the keyword occurs in the above document.
	 */
	int frequency;
	
	/**
	 * Initializes this occurrence with the given document,frequency pair.
	 * 
	 * @param doc Document name
	 * @param freq Frequency
	 */
	public Occurrence(String doc, int freq) {
		document = doc;
		frequency = freq;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(" + document + "," + frequency + ")";
	}
}

/**
 * This class builds an index of keywords. Each keyword maps to a set of documents in
 * which it occurs, with frequency of occurrence in each document. Once the index is built,
 * the documents can searched on for keywords.
 *
 */
public class LittleSearchEngine {
	
	/**
	 * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
	 * an array list of all occurrences of the keyword in documents. The array list is maintained in descending
	 * order of occurrence frequencies.
	 */
	HashMap<String,ArrayList<Occurrence>> keywordsIndex;
	
	/**
	 * The hash table of all noise words - mapping is from word to itself.
	 */
	HashMap<String,String> noiseWords;
	
	/**
	 * Creates the keyWordsIndex and noiseWords hash tables.
	 */
	public LittleSearchEngine() {
		keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
		noiseWords = new HashMap<String,String>(100,2.0f);
	}
	
	/**
	 * This method indexes all keywords found in all the input documents. When this
	 * method is done, the keywordsIndex hash table will be filled with all keywords,
	 * each of which is associated with an array list of Occurrence objects, arranged
	 * in decreasing frequencies of occurrence.
	 * 
	 * @param docsFile Name of file that has a list of all the document file names, one name per line
	 * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
	 * @throws FileNotFoundException If there is a problem locating any of the input files on disk
	 */
	public void makeIndex(String docsFile, String noiseWordsFile) 
	throws FileNotFoundException {
		// load noise words to hash table
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) {
			String word = sc.next();
			noiseWords.put(word,word);
		}
		
		// index all keywords
		sc = new Scanner(new File(docsFile));
		while (sc.hasNext()) {
			String docFile = sc.next();
			HashMap<String,Occurrence> kws = loadKeyWords(docFile);
			mergeKeyWords(kws);
		}
		
		
	}
	
	public static void main(String[] args) throws FileNotFoundException
	{
		LittleSearchEngine l = new LittleSearchEngine();
		HashMap<String, String> noiseWords;
		noiseWords = l.noiseWords;

		Scanner scanner = new Scanner(new File("noisewords.txt"));
		while(scanner.hasNext()){//fills the noiseword hashmap
			String word = scanner.next();
			noiseWords.put(word, word);	
		}
		l.makeIndex("docs.txt", "noisewords.txt");
		scanner.close();
		System.out.println(l.getKeyWord("we're"));
		System.out.println(l.top5search("deep", "world"));
		
	}


	/**
	 * Scans a document, and loads all keywords found into a hash table of keyword occurrences
	 * in the document. Uses the getKeyWord method to separate keywords from other words.
	 * 
	 * @param docFile Name of the document file to be scanned and loaded
	 * @return Hash table of keywords in the given document, each associated with an Occurrence object
	 * @throws FileNotFoundException If the document file is not found on disk
	 */
	public HashMap<String,Occurrence> loadKeyWords(String docFile) 
	throws FileNotFoundException {
		
		//edit this
		HashMap<String,Occurrence> map = new HashMap<String,Occurrence>();
		Scanner sc = new Scanner(new File(docFile));
		while (sc.hasNext()) 
		{
			String s = sc.nextLine();
			if (!s.trim().isEmpty() && !(s == null))
			{	
				String[] t = s.split(" "); 
				for (int i = 0; i < t.length; i++)
				{
					String word = getKeyWord(t[i]);
					if (word != null) //
					{
						if (map.containsKey(word))
						{
							Occurrence temp = map.get(word);
							temp.frequency++; 
							map.put(word, temp); 
						}
						else
						{
							Occurrence occ = new Occurrence (docFile, 1); 
							map.put(word, occ); 
						}
					}		
				}
			}
		}
		return map; 	
	}
	
	/**
	 * Merges the keywords for a single document into the master keywordsIndex
	 * hash table. For each keyword, its Occurrence in the current document
	 * must be inserted in the correct place (according to descending order of
	 * frequency) in the same keyword's Occurrence list in the master hash table. 
	 * This is done by calling the insertLastOccurrence method.
	 * 
	 * @param kws Keywords hash table for a document
	 */
	public void mergeKeyWords(HashMap<String,Occurrence> kws) {
		
		ArrayList<Occurrence> l = new ArrayList<Occurrence>();
		String lKey;
		
		for(String key: kws.keySet())
		{
			Occurrence k = kws.get(key);
			
			if (keywordsIndex.containsKey(key) == false)
			{
				ArrayList<Occurrence> l2 = new ArrayList<Occurrence>();
				l2.add(k);
				keywordsIndex.put(key, l2);
			}
			else
			{
				keywordsIndex.get(key).add(k);
				insertLastOccurrence(keywordsIndex.get(key));	
			}				
		}
		
	}
	
	/**
	 * Given a word, returns it as a keyword if it passes the keyword test,
	 * otherwise returns null. A keyword is any word that, after being stripped of any
	 * TRAILING punctuation, consists only of alphabetic letters, and is not
	 * a noise word. All words are treated in a case-INsensitive manner.
	 * 
	 * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
	 * 
	 * @param word Candidate word
	 * @return Keyword (word without trailing punctuation, LOWER CASE)
	 */
	public String getKeyWord(String word) {
		
		String newWord = word.replaceAll("[^a-zA-Z]+$", "").toLowerCase();
		//System.out.println(newWord);
		char[] chars = newWord.toCharArray();
		
		for (char c : chars) 
		{
	        if(!Character.isLetter(c)) 
	        {
	            return null;
	        }
	    }
		
		for (String key : noiseWords.keySet())
		{
			if (key.equals(newWord))
				return null;
		}
		//System.out.println("The word is not a noise word and contains all alphabetical letters");
		//System.out.println(newWord);
		return newWord;
		
	}
	
	/**
	 * Inserts the last occurrence in the parameter list in the correct position in the
	 * same list, based on ordering occurrences on descending frequencies. The elements
	 * 0..n-2 in the list are already in the correct order. Insertion of the last element
	 * (the one at index n-1) is done by first finding the correct spot using binary search, 
	 * then inserting at that spot.
	 * 
	 * @param occs List of Occurrences
	 * @return Sequence of mid point indexes in the input list checked by the binary search process,
	 *         null if the size of the input list is 1. This returned array list is only used to test
	 *         your code - it is not used elsewhere in the program.
	 */
	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) {
	
		ArrayList<Integer> arr = new ArrayList<Integer>();
		
		if (occs.size() == 1)
		{
			return null;
		}
		int val = occs.get(occs.size() - 1).frequency;
		
		for (int i = 0; i < occs.size() - 1; i++)
		{
			arr.add(occs.get(i).frequency);
		}
		ArrayList<Integer> res = new ArrayList<Integer>();
		res = binary(arr, occs, val, 0, arr.size() - 1);
		if (res.size() > 0)
		{
			int loc = res.get(res.size() - 1);
			occs.add(loc, occs.remove(occs.size() - 1));
		}
		return res;
	}
	
	private ArrayList<Integer> binary(ArrayList<Integer> arr, ArrayList<Occurrence> occs, int key, int min, int max)
	{
	  ArrayList<Integer> mid = new ArrayList<Integer>(); 
	  while (max >= min)
	  {
	      int imid = (min + max) / 2;
	      Occurrence o = occs.get(imid);
	      int end = o.frequency;
	      mid.add(imid); 
	      if (end < key)
	        max = imid - 1;
	      else if (end > key)
	      {
	        min = imid + 1;
	        imid++;
	      }
	      else
	    	  break; 
	      
	  }
	  return mid; 
	}
	
	/**
	 * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
	 * document. Result set is arranged in descending order of occurrence frequencies. (Note that a
	 * matching document will only appear once in the result.) Ties in frequency values are broken
	 * in favor of the first keyword. (That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2
	 * also with the same frequency f1, then doc1 will appear before doc2 in the result. 
	 * The result set is limited to 5 entries. If there are no matching documents, the result is null.
	 * 
	 * @param kw1 First keyword
	 * @param kw1 Second keyword
	 * @return List of NAMES of documents in which either kw1 or kw2 occurs, arranged in descending order of
	 *         frequencies. The result size is limited to 5 documents. If there are no matching documents,
	 *         the result is null.
	 */
	public ArrayList<String> top5search(String kw1, String kw2) {
	
		
		kw1 = kw1.toLowerCase();
		kw2 = kw2.toLowerCase();
		
		int totalf1 = 0;
		int totalf2 = 0;
		
		ArrayList<String> result = new ArrayList<String>();
		ArrayList<Occurrence> l1 = new ArrayList<Occurrence>();
		ArrayList<Occurrence> l2 = new ArrayList<Occurrence>();
		
		if (keywordsIndex.get(kw1) != null)
		{
			l1 = keywordsIndex.get(kw1);
		}
		if (keywordsIndex.get(kw2) != null)
		{
			l2 = keywordsIndex.get(kw2);
		}
		if (l1 == null && l2 == null)
			return result;
		
		//System.out.println(l1.get(0) + " " + l1.get(1));
		//System.out.println(l2.get(0) + " " + l2.get(1));
		for(int i = 0; i < l1.size(); i++)
		{
			if(result.size() < 5)
			{
				int f1 = l1.get(i).frequency;
				String doc1 = l1.get(i).document;
				
				for(int j = 0; j < l2.size(); j++)
				{
					int f2 = l2.get(j).frequency;
					String doc2 = l2.get(j).document;
					
					if (f1 >= f2)
					{
						if (!result.contains(doc1) && result.size() < 5)
						{
							result.add(doc1);
						}
					}
					else if (f1 < f2)
					{
						if (!result.contains(doc2) && result.size() < 5)
						{
							result.add(doc2);
						}	
					}
				}
			}
		}
		
		/*for(int k = 0; k < result.size(); k++)
		{
			if(k + 1 == result.size())
			{
				System.out.print(result.get(k));
			}
			else
			{
				System.out.print(result.get(k) + ", ");
			}
		} */
		
		if(result.size() == 0)
		{
			return null;
		} 
		

		
		//System.out.println(result);

		return result;
	}
	
	
}
