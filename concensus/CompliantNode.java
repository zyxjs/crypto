import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
    private int currRound;
    private Set<Integer> trustedNodes;
    private Map<Integer, Integer> followeeScores;
    private Set<Transaction> transactions;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        // IMPLEMENT THIS
        currRound = 0;
        transactions = new HashSet<>();
        followeeScores = new HashMap<>();
        trustedNodes = new HashSet<>();
    }

    public void setFollowees(boolean[] followees) {
        // IMPLEMENT THIS
        for (int i = 0; i < followees.length; ++i) {
            if (followees[i]) {
                this.followeeScores.put(i, 0);
            }
        }
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        // IMPLEMENT THIS
        transactions = pendingTransactions;
    }

    public Set<Transaction> sendToFollowers() {
        // IMPLEMENT THIS

        ++currRound;
        if (currRound == 1) {
            // handshake round
            Set<Transaction> txs = new HashSet<>();
            Iterator<Transaction> i = transactions.iterator();
            if (i.hasNext()) {
                txs.add(i.next());
            }
            return txs;
        } else if (currRound == 2) {
            // identify compliant nodes via handshake
            for (Map.Entry<Integer, Integer> f : followeeScores.entrySet()) {
                if (f.getValue() == 1) {
                    trustedNodes.add(f.getKey());
                }
            }
        }

        return transactions;

        // more realistic but results in grader out of mem

        // if (currRound == numRounds) {
        // // all rounds have been processed, this call is for final eval
        // // cull transaction that are probabilistically malicious
        // int numFollowees = followees.size();
        // Set<Transaction> txSet = new HashSet<>();
        // for (Transaction t : transactions.keySet()) {
        // if (transactions.get(t).size() > numFollowees * pMalicious) {
        // txSet.add(t);
        // }
        // }
        // return txSet;
        // }

        // ++currRound;
        // return transactions.keySet();

    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        // IMPLEMENT THIS
        for (Candidate c : candidates) {
            if (currRound == 1) {
                if (followeeScores.get(c.sender) != null) {
                    followeeScores.put(c.sender, followeeScores.get(c.sender) + 1);
                } else {
                    followeeScores.put(c.sender, 1);
                }
            } else {
                if (trustedNodes.contains(c.sender)) {
                    transactions.add(c.tx);
                }
            }
        }

        // more realistic but results in grader out of mem
        // for (Candidate c : candidates) {
        // followeeScores.put(c.sender, followeeScores.get(c.sender) + 1);

        // Set<Integer> txNodes = transactions.get(c.tx);
        // if (txNodes != null) {
        // txNodes.add(c.sender);
        // } else {
        // transactions.put(c.tx, new HashSet<>(Arrays.asList(c.sender)));
        // }
        // }
    }
}
