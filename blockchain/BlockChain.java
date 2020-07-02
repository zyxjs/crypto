import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;

    private ArrayList<Set<Block>> chain;
    private Map<byte[], UTXOPool> utxoPools;
    private TransactionPool txPool;

    /**
     * create an empty block chain with just a genesis block. Assume
     * {@code genesisBlock} is a valid block
     */
    public BlockChain(Block genesisBlock) {
        // IMPLEMENT THIS

        HashSet genesisBlockSet = new HashSet<>();
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
        Block maxHeightBlock = null;

        // get max height block(s)
        for (Block b : chain.get(chain.size() - 1)) {
            // get oldest block in max height set (smallest hash)
            if (maxHeightBlock == null) {
                maxHeightBlock = b;
            } else {
                byte[] blockHash = b.getHash();
                byte[] maxHeightBlockHash = maxHeightBlock.getHash();
                for (int i = 0; i < blockHash.length; ++i) {
                    if (i >= maxHeightBlockHash.length) {
                        break;
                    }
                    if (blockHash[i] < maxHeightBlockHash[i]) {
                        maxHeightBlock = b;
                        break;
                    }
                }
            }
        }
        return maxHeightBlock;
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

        // DONE 1 - iterate through chain to find prev block desc height order
        // DONE 2 - add block to chain
        // DONE 3 - process txs using txhandler and set utxo pool for new block
        // DONE 4 - process coinbase tx
        // DONE 5 - if height>limit remove blocks in oldest height
        // DONE 6 - remove block transactions from pool
        for (int i = chain.size() - 1; i >= 0; --i) {
            for (Block b : chain.get(i)) {
                if (block.getPrevBlockHash() == b.getHash()) {

                    // validate block transactions
                    TxHandler handler = new TxHandler(utxoPools.get(b.getHash()));
                    if (!handleBlockTxs(block, handler)) {
                        return false;
                    }

                    // set UTXO pool for new block
                    utxoPools.put(block.getHash(), handler.getUTXOPool());

                    // process coinbase tx
                    handleCoinbase(block);

                    if (i + 1 >= chain.size()) {
                        // new longest chain
                        Set<Block> maxHeightBlockSet = new HashSet<>();
                        maxHeightBlockSet.add(block);
                        chain.add(maxHeightBlockSet);

                    } else {
                        // add to set for blocks in existing height
                        chain.get(i + 1).add(block);
                    }

                    // remove block txs from pool
                    for (Transaction t : block.getTransactions()) {
                        txPool.removeTransaction(t.getHash());
                    }

                    // remove old blocks
                    if (chain.size() > CUT_OFF_AGE + 1) {
                        Set<Block> removedBlocks = chain.remove(0);
                        for (Block removedBlock : removedBlocks) {
                            utxoPools.remove(removedBlock.getHash());
                        }
                    }
                    return true;
                }
            }
        }

        return false;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        // IMPLEMENT THIS
        txPool.addTransaction(tx);
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