import java.util.*;

public class MaxFeeTxHandler {
    private UTXOPool utPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public MaxFeeTxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        utPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * DONE (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     * DONE (2) the signatures on each input of {@code tx} are valid, 
     * DONE (3) no UTXO is claimed multiple times by {@code tx},
     * DONE (4) all of {@code tx}s output values are non-negative, and
     * DONE (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
        Set<UTXO> claimedUTXO = new HashSet<UTXO>();
        double totalInputValue = 0.0;
        for (int i=0; i<tx.numInputs(); ++i) {
            Transaction.Input input = tx.getInput(i);
            UTXO u = new UTXO(input.prevTxHash, input.outputIndex);

            if (!utPool.contains(u)) 
                return false;

            if (claimedUTXO.contains(u))
                return false;

            Transaction.Output o = utPool.getTxOutput(u);
            if (!Crypto.verifySignature(o.address, tx.getRawDataToSign(i), input.signature))
                return false;

            claimedUTXO.add(u);
            totalInputValue += o.value;
        }

        double totalOutputValue = 0.0;
        for (Transaction.Output o : tx.getOutputs()) {
            if (o.value < 0.0)
                return false;

            totalOutputValue += o.value;
        }

        // exclude 0 fee transactions
        if (totalInputValue <= totalOutputValue)
            return false;

        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        ArrayList<Transaction> validTxs = new ArrayList<>();
        for (Transaction t : possibleTxs) {
            if (isValidTx(t)) {
                for (Transaction.Input i : t.getInputs()) {
                    UTXO u = new UTXO(i.prevTxHash, i.outputIndex);
                    utPool.removeUTXO(u);
                }

                for (int i=0; i<t.numOutputs(); ++i) {
                    UTXO u = new UTXO(t.getHash(), i);
                    utPool.addUTXO(u, t.getOutput(i));
                }
                validTxs.add(t);
            }
        }
        return validTxs.toArray(new Transaction[0]);
    }

}
