import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;

    private ArrayList<ArrayList<Block>> chain;
    private Map<byte[], UTXOPool> utxoPools;
    private TransactionPool txPool;

    /**
     * create an empty block chain with just a genesis block. Assume
     * {@code genesisBlock} is a valid block
     */
    public BlockChain(Block genesisBlock) {
        // IMPLEMENT THIS

        ArrayList<Block> genesisBlockSet = new ArrayList<>();
        genesisBlockSet.add(genesisBlock);
        chain = new ArrayList<>();
        chain.add(genesisBlockSet);

        utxoPools = new HashMap<>();
        utxoPools.put(genesisBlock.getHash(), new UTXOPool());
        handleBlockTxs(genesisBlock, new TxHandler(utxoPools.get(genesisBlock.getHash())));
        handleCoinbase(genesisBlock);

        txPool = new TransactionPool();
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        // IMPLEMENT THIS
        // genesis block guaranteed
        return chain.get(chain.size() - 1).get(0);
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        // IMPLEMENT THIS
        return utxoPools.get(getMaxHeightBlock().getHash());
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        // IMPLEMENT THIS
        return txPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all
     * transactions should be valid and block should be at
     * {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block
     * height 2) if the block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot
     * create a new block at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        // IMPLEMENT THIS

        // genesis block won't be mined
        if (block.getPrevBlockHash() == null) {
            return false;
        }

        // parent not found (culled by CUT_OFF_AGE)
        int parentHeight = getParentBlockHeight(block);
        if (parentHeight < 0) {
            return false;
        }

        // validate block transactions
        TxHandler handler = new TxHandler(utxoPools.get(block.getPrevBlockHash()));
        if (!handleBlockTxs(block, handler)) {
            return false;
        }

        // set UTXO pool for new block
        utxoPools.put(block.getHash(), handler.getUTXOPool());

        // process coinbase tx
        handleCoinbase(block);

        // add block
        if (parentHeight + 1 >= chain.size()) {
            // new longest chain
            ArrayList<Block> maxHeightBlockList = new ArrayList<>();
            maxHeightBlockList.add(block);
            chain.add(maxHeightBlockList);

        } else {
            // add to set for blocks in existing height
            chain.get(parentHeight + 1).add(block);
        }

        // remove block txs from pool
        for (Transaction t : block.getTransactions()) {
            txPool.removeTransaction(t.getHash());
        }

        // remove old blocks
        if (chain.size() > CUT_OFF_AGE + 1) {
            ArrayList<Block> removedBlocks = chain.remove(0);
            for (Block removedBlock : removedBlocks) {
                utxoPools.remove(removedBlock.getHash());
            }
        }

        return true;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        // IMPLEMENT THIS
        txPool.addTransaction(tx);
    }

    private int getParentBlockHeight(Block block) {
        for (int i = chain.size() - 1; i >= 0; --i) {
            for (Block b : chain.get(i)) {
                if (block.getPrevBlockHash() == b.getHash()) {
                    return i;
                }
            }
        }
        return -1;
    }

    // pre: utxopool is set for block in utxoPools
    private void handleCoinbase(Block block) {
        UTXOPool uPool = utxoPools.get(block.getHash());
        Transaction coinbase = block.getCoinbase();
        for (int c = 0; c < coinbase.numOutputs(); ++c) {
            uPool.addUTXO(new UTXO(coinbase.getHash(), c), coinbase.getOutput(c));
        }
    }

    private boolean handleBlockTxs(Block block, TxHandler handler) {
        Transaction[] validTxs = handler.handleTxs(block.getTransactions().toArray(new Transaction[0]));
        return validTxs.length == block.getTransactions().size();
    }
}