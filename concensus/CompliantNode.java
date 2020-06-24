import java.util.HashSet;
import java.util.Set;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
    private double pFollow;
    private double pMalicious;
    private double pDistribution;
    private double numRounds;
    private Set<Transaction> transactions;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        // IMPLEMENT THIS
        pFollow = p_graph;
        pMalicious = p_malicious;
        pDistribution = p_txDistribution;
        this.numRounds = numRounds;
        transactions = new HashSet<>();
    }

    public void setFollowees(boolean[] followees) {
        // IMPLEMENT THIS
        for (int i = 0; i < followees.length; ++i) {
            followees[i] = Math.random() < pFollow;
        }
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        // IMPLEMENT THIS
        transactions = pendingTransactions;
    }

    public Set<Transaction> sendToFollowers() {
        // IMPLEMENT THIS
        return transactions;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        // IMPLEMENT THIS
        for (Candidate c : candidates) {
            transactions.add(c.tx);
        }
    }
}
