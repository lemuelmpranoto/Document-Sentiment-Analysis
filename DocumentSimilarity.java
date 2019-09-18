package mp1;

import java.util.*;

public class DocumentSimilarity {
    /**
     * Determine the two documents from a given List that are most similar to
     * each other according to the Jensen-Shannon divergence score.
     *
     * @param docList is a List with at least two Document references
     * @return the DocumentPair that holds the two Documents most similar to
     * each other using the JS Divergence Score. If more than one
     * pair of Documents has the same similarity then returns any one.
     */
    public static DocumentPair closestMatch(List<Document> docList) {
        // TODO: Implement this method
        /**
         * Take the documents that have the lowest JSD score when compared
         * Return the document pair
         */
        ArrayList<DocumentPair> pairList = new ArrayList<>();
        DocumentPair minPair = null;


        for (int i = 0; i < docList.size() - 1; i++) {                                                                  // Iterating the docList and populating data for pairList
            for (int j = i + 1; j < docList.size(); j++) {
                DocumentPair tempPair = new DocumentPair(docList.get(i), docList.get(j));
                pairList.add(tempPair);
            }
        }

        for (int i = 0; i < pairList.size(); i++) {                                                                     // Iterating through pairList to find the DocumentPair with smallest JSD
            if (i == 0) {
                // Initialization of minPair
                minPair = pairList.get(0);
            } else {
                if (pairList.get(i).compareTo(minPair) < 0) {                                                           // Using func compareTo in class DocumentPair to compare JSD values
                    minPair = pairList.get(i);
                }
            }
        }
        return minPair;
    }


    /**
     * Return the two documents that have the greatest different in sentiment
     * scores as computed using Azure Computing Services.
     *
     * @param docList is not null
     * @return the DocumentPair with the two documents that have the greatest
     *          difference in sentiment scores. If two pairs have the same difference
     *          then the pair that has the lower JS Divergence is returned,
     *          and if there is a still a tie then any pair that is part of the tie is returned.
     */
    public static DocumentPair sentimentDiffMax(List<Document> docList) {
        // TODO: Implement this method
        // Use sort method

        Document docA = null;
        Document docB = null;

        double mainDiff = 0;
        double currentDiff = 0;

        DocumentPair currentPair;
        int num = 0;
        for (Document a: docList){
            int count = 0;
            for (Document b : docList){
                if (b != null && count > num){
                    currentPair = new DocumentPair(a, b);
                    currentDiff = currentPair.getSentimentDiff();
                    if (mainDiff < currentDiff){
                        docA = a;
                        docB = b;
                        mainDiff = currentDiff;
                    }
                }
                count++;
            }
            num++;
        }
        return new DocumentPair(docA, docB);
    }

    /**
     * Determine a set of document groups where a group of Documents are more
     * similar to each other than to Documents in a different group.
     *
     * @param docList   is a List with at least two Document references from which we
     *                  want to group Documents by similarity
     * @param numGroups n is the number of Document groups to create and 0 < numGroups <= n
     * @return a Map that represents how Documents are grouped.
     * Two Documents that are in the same group will map to the same Document,
     * and two Documents that are not in the same group will map to different
     * Documents. Further, the Document that represents a group will have the
     * lexicographically smallest id in that group.
     */
    public static Map<Document, Document> groupSimilarDocuments(List<Document> docList, int numGroups) {
        HashMap<Document, Document> partitionMap = new HashMap<>();                                                     // Initializing a partitionMap (output).
        DocumentCollection partitionCollect = new DocumentCollection();
        ArrayList<DocumentPair> pairList = new ArrayList<>();

        for (int i = 0; i < docList.size() - 1; i++) {                                                                  // Iterating the docList and populating data for pairList
            for (int j = i + 1; j < docList.size(); j++) {
                DocumentPair tempPair = new DocumentPair(docList.get(i), docList.get(j));
                pairList.add(tempPair);
            }
        }


        for (int i = 0; i < pairList.size(); i++) {                                                                     // Sorting pairList
            for (int j = 0; j < pairList.size(); j++) {
                if (pairList.get(j).compareTo(pairList.get(i)) > 0) {
                    DocumentPair tempPair = pairList.get(i);
                    pairList.set(i, pairList.get(j));
                    pairList.set(j, tempPair);
                }
            }
        }

        for(int i = 0; i < docList.size(); i++) {                                                                       // Placing each document in its own partition
            partitionCollect.add(docList.get(i));
        }

        int docGroupsCount = docList.size();
        while (docGroupsCount > numGroups) {                                                                            // Merging partitionCollect until partitionCollect's size = numGroups
            for (int i = 0; i < pairList.size(); i++) {
                Document doc1 = partitionCollect.find(pairList.get(i).getDoc1());
                Document doc2 = partitionCollect.find(pairList.get(i).getDoc2());
                if (doc1.compareTo(doc2) > 0) {                                                                         // Comparing lexicographically then merge
                    partitionCollect.merge(doc2, doc1);
                    docGroupsCount--;
                    break;
                }
            }
        }

        for(int i = 0; i < docList.size(); i++) {                                                                       // Populating data for partitionMap before return.
            partitionMap.put(docList.get(i),partitionCollect.find(docList.get(i)));
        }

        return partitionMap;
    }

}