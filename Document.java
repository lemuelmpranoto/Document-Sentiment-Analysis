package mp1;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Document implements Comparable<Document> {
    private final String docURL;                                                                                        // the URL for the document
    private final String id;                                                                                            // an identifier for the document, e.g., book title
    private HashMap<String, Integer> wordMap = new HashMap<String, Integer>();                                                        // HashMap for words (keys) and frequency of each word (values)
    private Scanner urlScanner;                                                                                         // Scanner for URL
    private Scanner urlScanner1;
    private HashMap<String, Double> prbltyMap = new HashMap<String, Double>();                                                       // HashMap for words (keys) and probability of each word (values)
    private HashSet<String> similarWords = new HashSet<String>();                                                            // HashSet for similar words between two documents
    private double wordCount = 0;
    private int overallSentiment;

    // You may need more fields for a document.

    /**
     * Create a document given a URL to the document's text as well as an id
     *
     * @param id is not null and is not the empty
     * @param url is not null and is not the empty string
     * @throws IOException
     */
    public Document(String id, String url) throws IOException {
        assert (id != null);
        assert (!id.equals(""));
        assert (url != null);
        assert (!url.equals(""));

        this.docURL = url;
        this.id = id;

        //Scanner to scan and put words into document
        urlScanner1 = new Scanner (new URL(url).openStream());
        // TODO: Your constructor will have more work to do.

        //Scanning url and putting data in to wordMap
        urlScanner = new Scanner(new URL(url).openStream());

        while (urlScanner.hasNext()) {


            String nextWord = urlScanner.next();
            wordCount++;
            int flag = 0;
            for (String key : wordMap.keySet()) {
                if (key.equals(nextWord)) {
                    flag = 1;
                    break;
                }
            }
            if (flag == 1) {
                wordMap.put(nextWord, wordMap.get(nextWord) + 1);
            } else {
                wordMap.put(nextWord, 1);
            }
        }

        //Iterating wordMap and putting data into prbltyMap
        for (Map.Entry<String, Integer> entry : wordMap.entrySet()) {                                                   // Populating probabiliy map for further calculations (computeJSDiv)
            double d1 = wordCount;
            double d2 = (double) entry.getValue();
            double d3 = d2 / d1;
            prbltyMap.put(entry.getKey(), d3);
        }
        calculateSentiment();
    }

    public Document(String url) throws IOException {
        // a simpler constructor that sets the id to be the url
        this(url, url);
        //calculateSentiment();
    }

    /**
     * Compute the Jensen-Shannon Divergence between the two textList
     * on the basis of the words used.
     *
     * @param otherDoc is not null
     * @return the Jensen-Shannon divergence multiplied by 100 and rounded to the nearest integer
     */
    public long computeJSDiv(Document otherDoc) {
        // TODO: Implement this method
        similarWords = new HashSet<String>();
        double jsd = 0;
        //Iterating each prbltyMap for calculating similar words' JSD
        for (Map.Entry<String, Double> p_i : prbltyMap.entrySet()) {
            for (Map.Entry<String, Double> q_i : otherDoc.prbltyMap.entrySet()) {
                if (p_i.getKey().equals(q_i.getKey())) {
                    double m_i = 0.5 * (p_i.getValue() + q_i.getValue());
                    jsd += 0.5*((p_i.getValue() * ((Math.log10(p_i.getValue() / m_i)) / Math.log10((double)2))) + (q_i.getValue() * ((Math.log10(q_i.getValue()/m_i)) / Math.log10((double)2))));
                    similarWords.add(p_i.getKey());
                    break;
                }
            }
        }

        //Iterating each prbltyMap again for calculating different words' JSD
        for (Map.Entry<String, Double> p_i_2 : prbltyMap.entrySet()){
            if(!(similarWords.contains(p_i_2.getKey()))) {
                jsd += 0.5 * p_i_2.getValue();
            }
        }

        //Iterating each prbltyMap again for calculating different words' JSD
        for (Map.Entry<String, Double> q_i_2 : otherDoc.prbltyMap.entrySet()){
            if(!(similarWords.contains(q_i_2.getKey()))) {
                jsd += 0.5 * q_i_2.getValue();
            }
        }

        return Math.round(jsd * 100);
    }

    /**
     * Compute the overall sentiment of the Document.
     * The overall sentiment is the median sentiment obtained by computing
     * the median of segments of length of ~5000 characters.
     * @return the overall sentiment (multiplied by 100 and rounded to the nearest integer)
     */
    public int getOverallSentiment() {
        return this.overallSentiment;                                                                                   // Return calculateSentiment method

    }

        // TODO: Implement this method
    private void calculateSentiment() {

        StringBuilder sb = new StringBuilder(5000);
        TextCollection textCollection = new TextCollection();

        try {                                                                                                           // Try-catch block
            AzureSentimentAnalysis.init();
        } catch (Exception e) {
            System.out.println("Failed to find sentiment value" + e);
        }
        String nextWord = "";
        int id = 1;
        while (urlScanner1.hasNext()) {
            // Should this be < or <= ? Check the docs
            nextWord = urlScanner1.next();
            if (sb.length() + nextWord.length() + 1 > 5000) {
                // Need a new string builder
                //stringIteration = sb.toString();
                textCollection.add((Integer.toString(id)), "en", sb.toString());
                sb.delete(0, sb.length());
                id++;
                sb.append(nextWord);
            } else {
                // Append word to existing string builder
                sb.append(nextWord).append(" ");
            }
        }
        //Increment id
        id++;
        //sb.delete(sb.length() - 1, sb.length());
        textCollection.add(Integer.toString(id), "en", sb.toString());


        //Sort arraylist of sentiment scores
        int medianSentiment;
        ArrayList<SentimentResponse> scoresSentiment = new ArrayList<>(AzureSentimentAnalysis.getSentiments(textCollection));
        System.out.println("ScoresSentimentSize: " + scoresSentiment.size());
        Collections.sort(scoresSentiment);
        //If statement to check if document is empty
        if (scoresSentiment.isEmpty()) {
            System.out.println("Score sentiment is empty");
        }

        for (int i = 0; i < scoresSentiment.size(); i++) {
            System.out.println(i + "  " + scoresSentiment.get(i).getScore());
        }
        //Find median sentiment
        if (scoresSentiment.size() % 2 == 1) {
            medianSentiment = scoresSentiment.get((scoresSentiment.size() - 1) / 2).getScore();
        } else {
            double meanSentiment;
            meanSentiment = (scoresSentiment.get((scoresSentiment.size() / 2)).getScore() + scoresSentiment.get((scoresSentiment.size() / 2) - 1).getScore()) / 2.0;

            medianSentiment = (int) Math.round(meanSentiment);
        }
        //Return a sentiment score for the document based on the median score
        this.overallSentiment = medianSentiment;
    }

    /**
     * Produce a string representation of a Document
     */
    @Override
    public String toString() {
        // return the id associated with this document
        return this.id;
    }


    // There should be no need to change any of the code below.
    // Read, but do not change.

    /**
     * Compare two Document objects for equality
     *
     * @param other
     * @return true if this Document and the other Document represent the same
     * document.
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof Document) {
            Document otherDoc = (Document) other;
            return (this.id.equals(otherDoc.id));
        } else {
            return false;
        }
    }

    /**
     * Compute the hashCode for this Document object
     *
     * @return the hashCode for this Document object
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public int compareTo(Document other) {
        if (this.equals(other)) {
            return 0;
        } else {
            return id.compareTo(other.id);
        }
    }
}
