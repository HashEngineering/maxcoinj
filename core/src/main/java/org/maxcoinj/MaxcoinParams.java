package org.maxcoinj;

import com.hashengineering.crypto.difficulty.kgw;
import org.bitcoinj.core.*;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptOpCodes;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

import static com.google.common.base.Preconditions.checkState;

/**
 * Created by HashEngineering on 1/11/15.
 */
public class MaxcoinParams extends NetworkParameters {
    private static final Logger log = LoggerFactory.getLogger(AbstractBlockChain.class);

    /** Block heights for the hard forks */
    static final int BLOCK_HEIGHT_FORK1 = 140000;
    static final int BLOCK_HEIGHT_FORK2 = 177500;
    static final int BLOCK_HEIGHT_FORK3 = 600000;
    static final int BLOCK_HEIGHT_FORK4 = 635000;

    public MaxcoinParams() {
        super();
        interval = INTERVAL;
        targetTimespan = TARGET_TIMESPAN;
        maxTarget = CoinDefinition.proofOfWorkLimit;
        dumpedPrivateKeyHeader = 128 + CoinDefinition.AddressHeader;
        addressHeader = CoinDefinition.AddressHeader;
        p2shHeader = CoinDefinition.p2shHeader;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader};

        port = CoinDefinition.Port;
        packetMagic = CoinDefinition.PacketMagic;
        genesisBlock.setDifficultyTarget(CoinDefinition.genesisBlockDifficultyTarget);
        genesisBlock.setTime(CoinDefinition.genesisBlockTime);
        genesisBlock.setNonce(CoinDefinition.genesisBlockNonce);
        id = ID_MAINNET;
        subsidyDecreaseBlockCount = CoinDefinition.subsidyDecreaseBlockCount;
        spendableCoinbaseDepth = CoinDefinition.spendableCoinbaseDepth;

        createGenesis();

        String genesisHash = genesisBlock.getHashAsString();
        checkState(genesisHash.equals(CoinDefinition.genesisHash),
                genesisHash);

        CoinDefinition.initCheckpoints(checkpoints);

        dnsSeeds = CoinDefinition.dnsSeeds;

    }
    private static MaxcoinParams instance;
    public static synchronized MaxcoinParams get() {
        if (instance == null) {
            instance = new MaxcoinParams();
        }
        return instance;
    }

    public String getPaymentProtocolId() {
        return PAYMENT_PROTOCOL_ID_MAINNET;
    }

    //TODO:  put these bytes into the CoinDefinition
    private void createGenesis() {
        //Block genesisBlock = new Block(n);
        genesisBlock.removeTransaction(0);
        Transaction t = new Transaction(this);
        try {
            // A script containing the difficulty bits and the following message:
            //

            byte[] bytes = Utils.HEX.decode(CoinDefinition.genesisTxInBytes);
            t.addInput(new TransactionInput(this, t, bytes));
            ByteArrayOutputStream scriptPubKeyBytes = new ByteArrayOutputStream();
            Script.writeBytes(scriptPubKeyBytes, Utils.HEX.decode(CoinDefinition.genesisTxOutBytes));
            scriptPubKeyBytes.write(ScriptOpCodes.OP_CHECKSIG);
            t.addOutput(new TransactionOutput(this, t, Coin.valueOf(CoinDefinition.genesisBlockValue, 0), scriptPubKeyBytes.toByteArray()));
        } catch (Exception e) {
            // Cannot happen.
            throw new RuntimeException(e);
        }
        genesisBlock.addTransaction(t);
    }

    @Override
    public boolean checkDifficultyTransitions(StoredBlock storedPrev, Block nextBlock, BlockStore blockStore) throws BlockStoreException, VerificationException {
        if (this.getId().equals(NetworkParameters.ID_TESTNET))
        {
            Block prev = storedPrev.getHeader();
            checkTestnetDifficulty(storedPrev, prev, nextBlock, blockStore);
            return true;
        }
        else if(storedPrev.getHeight()+1 < 200)
        {
            super.checkDifficultyTransitions(storedPrev,nextBlock, blockStore);
            return true;
        }
        else
        {
            checkDifficultyTransitionsV2(storedPrev, nextBlock, blockStore);
            return true;
        }


    }

    private void checkDifficultyTransitionsV2( StoredBlock storedPrev, Block nextBlock, BlockStore blockStore ) throws BlockStoreException, VerificationException {
        final long      	BlocksTargetSpacing			= (storedPrev.getHeight()+1 > BLOCK_HEIGHT_FORK4) ? 60 : 30; // 2.5 minutes
        int         		TimeDaySeconds				= 60 * 60 * 24;
        long				PastSecondsMin				= TimeDaySeconds / 100;  //6 hours
        long				PastSecondsMax				= TimeDaySeconds *14 / 100;  // 7 days
        long				PastBlocksMin				= PastSecondsMin / BlocksTargetSpacing;  //144 blocks
        long				PastBlocksMax				= PastSecondsMax / BlocksTargetSpacing;  //4032 blocks

        if(!kgw.isNativeLibraryLoaded())
            //    long start = System.currentTimeMillis();
            KimotoGravityWell(blockStore, storedPrev, nextBlock, BlocksTargetSpacing, PastBlocksMin, PastBlocksMax);
            //long end1 = System.currentTimeMillis();
            //if(kgw.java.isNativeLibraryLoaded())
        else
            //      KimotoGravityWell_N(storedPrev, nextBlock, BlocksTargetSpacing, PastBlocksMin, PastBlocksMax);
            //  long end2 = System.currentTimeMillis();
            //if(kgw.java.isNativeLibraryLoaded())
            KimotoGravityWell_N2(blockStore, storedPrev, nextBlock, BlocksTargetSpacing, PastBlocksMin, PastBlocksMax);
        /*long end3 = System.currentTimeMillis();

        long java = end1 - start;
        long n1 = end2 - end1;
        long n2 = end3 - end2;
        if(i > 20)
        {
            j += java;
            N += n1;
            N2 += n2;
            if(i != 0 && ((i % 10) == 0))
             //log.info("KGW 10 blocks: J={}; N={} -%.0f%; N2={} -%.0f%", java, n1, ((double)(java-n1))/java*100, n2, ((double)(java-n2))/java*100);
                 log.info("KGW {} blocks: J={}; N={} -{}%; N2={} -{}%", i-20, j, N, ((double)(j-N))/j*100, N2, ((double)(j-N2))/j*100);
        }
        ++i;*/
    }



    private void KimotoGravityWell(BlockStore blockStore, StoredBlock storedPrev, Block nextBlock, long TargetBlocksSpacingSeconds, long PastBlocksMin, long PastBlocksMax)  throws BlockStoreException, VerificationException {
	/* current difficulty formula, megacoin - kimoto gravity well */
        //const CBlockIndex  *BlockLastSolved				= pindexLast;
        //const CBlockIndex  *BlockReading				= pindexLast;
        //const CBlockHeader *BlockCreating				= pblock;
        StoredBlock         BlockLastSolved             = storedPrev;
        StoredBlock         BlockReading                = storedPrev;
        Block               BlockCreating               = nextBlock;

        BlockCreating				= BlockCreating;
        long				PastBlocksMass				= 0;
        long				PastRateActualSeconds		= 0;
        long				PastRateTargetSeconds		= 0;
        double				PastRateAdjustmentRatio		= 1f;
        BigInteger			PastDifficultyAverage = BigInteger.valueOf(0);
        BigInteger			PastDifficultyAveragePrev = BigInteger.valueOf(0);;
        double				EventHorizonDeviation;
        double				EventHorizonDeviationFast;
        double				EventHorizonDeviationSlow;

        long start = System.currentTimeMillis();
        long endLoop = 0;

        if (BlockLastSolved == null || BlockLastSolved.getHeight() == 0 || (long)BlockLastSolved.getHeight() < PastBlocksMin)
        { verifyDifficulty(this.getMaxTarget(), nextBlock); }

        int i = 0;
        //log.info("KGW: i = {}; height = {}; hash {} ", i, BlockReading.getHeight(), BlockReading.getHeader().getHashAsString());

        long totalCalcTime = 0;
        long totalReadtime = 0;
        long totalBigIntTime = 0;


        for (i = 1; BlockReading != null && BlockReading.getHeight() > 0; i++) {
            long startLoop = System.currentTimeMillis();
            if (PastBlocksMax > 0 && i > PastBlocksMax)
            {
                break;
            }
            PastBlocksMass++;
            BigInteger PastDifficultyAverageN = new BigInteger("0");
            if (i == 1)	{ PastDifficultyAverage = BlockReading.getHeader().getDifficultyTargetAsInteger(); }
            else
            {
                PastDifficultyAverage = ((BlockReading.getHeader().getDifficultyTargetAsInteger().subtract(PastDifficultyAveragePrev)).divide(BigInteger.valueOf(i)).add(PastDifficultyAveragePrev));
            }
            PastDifficultyAveragePrev = PastDifficultyAverage;

            long diff = System.currentTimeMillis();
            totalBigIntTime += diff - startLoop;

            PastRateActualSeconds			= BlockLastSolved.getHeader().getTimeSeconds() - BlockReading.getHeader().getTimeSeconds();
            PastRateTargetSeconds			= TargetBlocksSpacingSeconds * PastBlocksMass;
            PastRateAdjustmentRatio			= 1.0f;
            if (PastRateActualSeconds < 0) { PastRateActualSeconds = 0; }
            if (PastRateActualSeconds != 0 && PastRateTargetSeconds != 0) {
                PastRateAdjustmentRatio			= (double)PastRateTargetSeconds / PastRateActualSeconds;
            }
            EventHorizonDeviation			= 1 + (0.7084 * java.lang.Math.pow((Double.valueOf(PastBlocksMass)/Double.valueOf(28.2)), -1.228));
            EventHorizonDeviationFast		= EventHorizonDeviation;
            EventHorizonDeviationSlow		= 1 / EventHorizonDeviation;

            if (PastBlocksMass >= PastBlocksMin) {
                if ((PastRateAdjustmentRatio <= EventHorizonDeviationSlow) || (PastRateAdjustmentRatio >= EventHorizonDeviationFast))
                {
                    /*assert(BlockReading)*/;
                    break;
                }
            }
            long calcTime = System.currentTimeMillis();
            StoredBlock BlockReadingPrev = blockStore.get(BlockReading.getHeader().getPrevBlockHash());
            if (BlockReadingPrev == null)
            {
                //If this is triggered, then we are using checkpoints and haven't downloaded enough blocks to verify the difficulty.
                //assert(BlockReading);     //from C++ code
                //break;                    //from C++ code
                return;
            }
            //log.info("KGW: i = {}; height = {}; hash {} ", i, BlockReadingPrev.getHeight(), BlockReadingPrev.getHeader().getHashAsString());
            BlockReading = BlockReadingPrev;
            endLoop = System.currentTimeMillis();
            //log.info("KGW: i = {}; height = {}; total time {}=calc {}+read {}", i, BlockReadingPrev.getHeight(), endLoop - startLoop, calcTime - startLoop, endLoop-calcTime);
            totalCalcTime += calcTime - startLoop;
            totalReadtime += endLoop-calcTime;
        }

        /*CBigNum bnNew(PastDifficultyAverage);
        if (PastRateActualSeconds != 0 && PastRateTargetSeconds != 0) {
            bnNew *= PastRateActualSeconds;
            bnNew /= PastRateTargetSeconds;
        } */
        //log.info("KGW iterations: {}, rewinding from {} to {}; time {}", i, BlockReading.getHeight(), storedPrev.getHeight()+1, endLoop - start);
        //log.info("KGW-J: i = {}; height = {}; total time {}=calc {}+read {} / bigint {} + other {}", i, storedPrev.getHeight(), System.currentTimeMillis() - start, totalCalcTime, totalReadtime, totalBigIntTime, totalCalcTime-totalBigIntTime);
        //log.info("KGW-J, {}, {}, {}", storedPrev.getHeight(), i, System.currentTimeMillis() - start);
        BigInteger newDifficulty = PastDifficultyAverage;
        if (PastRateActualSeconds != 0 && PastRateTargetSeconds != 0) {
            newDifficulty = newDifficulty.multiply(BigInteger.valueOf(PastRateActualSeconds));
            newDifficulty = newDifficulty.divide(BigInteger.valueOf(PastRateTargetSeconds));
        }

        if (newDifficulty.compareTo(this.getMaxTarget()) > 0) {
            log.info("Difficulty hit proof of work limit: {}", newDifficulty.toString(16));
            newDifficulty = this.getMaxTarget();
        }


        //log.info("KGW-J Difficulty Calculated: {}", newDifficulty.toString(16));
        verifyDifficulty(newDifficulty, nextBlock);

    }

    private void KimotoGravityWell_N(BlockStore blockStore, StoredBlock storedPrev, Block nextBlock, long TargetBlocksSpacingSeconds, long PastBlocksMin, long PastBlocksMax)  throws BlockStoreException, VerificationException {
	/* current difficulty formula, megacoin - kimoto gravity well */
        //const CBlockIndex  *BlockLastSolved				= pindexLast;
        //const CBlockIndex  *BlockReading				= pindexLast;
        //const CBlockHeader *BlockCreating				= pblock;
        StoredBlock         BlockLastSolved             = storedPrev;
        StoredBlock         BlockReading                = storedPrev;
        Block               BlockCreating               = nextBlock;

        /*
        BlockCreating				= BlockCreating;

        long				PastBlocksMass				= 0;
        long				PastRateActualSeconds		= 0;
        long				PastRateTargetSeconds		= 0;
        double				PastRateAdjustmentRatio		= 1f;
        BigInteger			PastDifficultyAverage = BigInteger.valueOf(0);
        BigInteger			PastDifficultyAveragePrev = BigInteger.valueOf(0);;
        double				EventHorizonDeviation;
        double				EventHorizonDeviationFast;
        double				EventHorizonDeviationSlow;
        */
        long start = System.currentTimeMillis();
        long endLoop = 0;

        if (BlockLastSolved == null || BlockLastSolved.getHeight() == 0 || (long)BlockLastSolved.getHeight() < PastBlocksMin)
        { verifyDifficulty(this.getMaxTarget(), nextBlock); return; }

        int i = 0;
        //log.info("KGW: i = {}; height = {}; hash {} ", i, BlockReading.getHeight(), BlockReading.getHeader().getHashAsString());

        long totalCalcTime = 0;
        long totalReadtime = 0;
        long totalBigIntTime = 0;

        int init_result = kgw.KimotoGravityWell_init(TargetBlocksSpacingSeconds, PastBlocksMin, PastBlocksMax, 28.2);


        for (i = 1; BlockReading != null && BlockReading.getHeight() > 0; i++) {
            long startLoop = System.currentTimeMillis();
            int result = kgw.KimotoGravityWell_loop(i, BlockReading.getHeader().getDifficultyTargetAsInteger().toByteArray(),BlockReading.getHeight(), BlockReading.getHeader().getTimeSeconds(), BlockLastSolved.getHeader().getTimeSeconds());

            //if(i == 1)
            //log.info("KGW-N: difficulty of i=1:%x->{}",  BlockReading.getHeader().getDifficultyTarget(), BlockReading.getHeader().getDifficultyTargetAsInteger().toString(16));
            //    log.info("KGW-N: difficulty of i=1: " + BlockReading.getHeader().getDifficultyTarget() + "->" + BlockReading.getHeader().getDifficultyTargetAsInteger().toString(16));
            if(result == 1)
                break;
            if(result == 2)
                return;
            /*
            if (PastBlocksMax > 0 && i > PastBlocksMax)
            {
                break;
            }
            PastBlocksMass++;
            BigInteger PastDifficultyAverageN = new BigInteger("0");
            if (i == 1)	{ PastDifficultyAverage = BlockReading.getHeader().getDifficultyTargetAsInteger(); }
            else
            {
                //PastDifficultyAverage = ((BlockReading.getHeader().getDifficultyTargetAsInteger().subtract(PastDifficultyAveragePrev)).divide(BigInteger.valueOf(i)).add(PastDifficultyAveragePrev));
            }
            PastDifficultyAveragePrev = PastDifficultyAverage;

            long diff = System.currentTimeMillis();
            totalBigIntTime += diff - startLoop;

            PastRateActualSeconds			= BlockLastSolved.getHeader().getTimeSeconds() - BlockReading.getHeader().getTimeSeconds();
            PastRateTargetSeconds			= TargetBlocksSpacingSeconds * PastBlocksMass;
            PastRateAdjustmentRatio			= 1.0f;
            if (PastRateActualSeconds < 0) { PastRateActualSeconds = 0; }
            if (PastRateActualSeconds != 0 && PastRateTargetSeconds != 0) {
                PastRateAdjustmentRatio			= (double)PastRateTargetSeconds / PastRateActualSeconds;
            }
            EventHorizonDeviation			= 1 + (0.7084 * java.lang.Math.pow((Double.valueOf(PastBlocksMass)/Double.valueOf(144)), -1.228));
            EventHorizonDeviationFast		= EventHorizonDeviation;
            EventHorizonDeviationSlow		= 1 / EventHorizonDeviation;

            if (PastBlocksMass >= PastBlocksMin) {
                if ((PastRateAdjustmentRatio <= EventHorizonDeviationSlow) || (PastRateAdjustmentRatio >= EventHorizonDeviationFast))
                {
                    //assert(BlockReading);
                    break;
                }
            }*/
            long calcTime = System.currentTimeMillis();
            StoredBlock BlockReadingPrev = blockStore.get(BlockReading.getHeader().getPrevBlockHash());
            if (BlockReadingPrev == null)
            {
                //If this is triggered, then we are using checkpoints and haven't downloaded enough blocks to verify the difficulty.
                //assert(BlockReading);     //from C++ code
                //break;                    //from C++ code
                return;
            }
            //log.info("KGW: i = {}; height = {}; hash {} ", i, BlockReadingPrev.getHeight(), BlockReadingPrev.getHeader().getHashAsString());
            BlockReading = BlockReadingPrev;
            endLoop = System.currentTimeMillis();
            //log.info("KGW-N: i = {}; height = {}; total time {}=calc {}+read {}", i, BlockReadingPrev.getHeight(), endLoop - startLoop, calcTime - startLoop, endLoop-calcTime);
            totalCalcTime += calcTime - startLoop;
            totalReadtime += endLoop-calcTime;
        }

        /*CBigNum bnNew(PastDifficultyAverage);
        if (PastRateActualSeconds != 0 && PastRateTargetSeconds != 0) {
            bnNew *= PastRateActualSeconds;
            bnNew /= PastRateTargetSeconds;
        } */
        //log.info("KGW iterations: {}, rewinding from {} to {}; time {}", i, BlockReading.getHeight(), storedPrev.getHeight()+1, endLoop - start);
        //log.info("KGW-N: i = {}; height = {}; total time {}=calc {}+read {}", i, storedPrev.getHeight(), System.currentTimeMillis() - start, totalCalcTime, totalReadtime /*, totalBigIntTime, totalCalcTime-totalBigIntTime*/);
        //log.info("KGW-N, {}, {}, {}", storedPrev.getHeight(), i, System.currentTimeMillis() - start);
        /*BigInteger newDifficulty = PastDifficultyAverage;
        if (PastRateActualSeconds != 0 && PastRateTargetSeconds != 0) {
            newDifficulty = newDifficulty.multiply(BigInteger.valueOf(PastRateActualSeconds));
            newDifficulty = newDifficulty.divide(BigInteger.valueOf(PastRateTargetSeconds));
        }*/

        BigInteger newDifficulty = new BigInteger(kgw.KimotoGravityWell_close());

        if (newDifficulty.compareTo(this.getMaxTarget()) > 0) {
            log.info("Difficulty hit proof of work limit: {}", newDifficulty.toString(16));
            newDifficulty = this.getMaxTarget();
        }


        //log.info("KGW-N Difficulty Calculated: {}", newDifficulty.toString(16));
        verifyDifficulty(newDifficulty, nextBlock);

    }
    private void KimotoGravityWell_N2(BlockStore blockStore, StoredBlock storedPrev, Block nextBlock, long TargetBlocksSpacingSeconds, long PastBlocksMin, long PastBlocksMax)  throws BlockStoreException, VerificationException {
	/* current difficulty formula, megacoin - kimoto gravity well */
        //const CBlockIndex  *BlockLastSolved				= pindexLast;
        //const CBlockIndex  *BlockReading				= pindexLast;
        //const CBlockHeader *BlockCreating				= pblock;
        StoredBlock         BlockLastSolved             = storedPrev;
        StoredBlock         BlockReading                = storedPrev;
        Block               BlockCreating               = nextBlock;

        BlockCreating				= BlockCreating;
        long				PastBlocksMass				= 0;
        long				PastRateActualSeconds		= 0;
        long				PastRateTargetSeconds		= 0;
        double				PastRateAdjustmentRatio		= 1f;
        BigInteger			PastDifficultyAverage = BigInteger.valueOf(0);
        BigInteger			PastDifficultyAveragePrev = BigInteger.valueOf(0);;
        double				EventHorizonDeviation;
        double				EventHorizonDeviationFast;
        double				EventHorizonDeviationSlow;

        long start = System.currentTimeMillis();
        long endLoop = 0;

        if (BlockLastSolved == null || BlockLastSolved.getHeight() == 0 || (long)BlockLastSolved.getHeight() < PastBlocksMin)
        { verifyDifficulty(this.getMaxTarget(), nextBlock); return; }

        int i = 0;
        //log.info("KGW: i = {}; height = {}; hash {} ", i, BlockReading.getHeight(), BlockReading.getHeader().getHashAsString());

        long totalCalcTime = 0;
        long totalReadtime = 0;
        long totalBigIntTime = 0;

        int init_result = kgw.KimotoGravityWell_init(TargetBlocksSpacingSeconds, PastBlocksMin, PastBlocksMax,28.2d);


        for (i = 1; BlockReading != null && BlockReading.getHeight() > 0; i++) {
            long startLoop = System.currentTimeMillis();
            int result = kgw.KimotoGravityWell_loop2(i, BlockReading.getHeader().getDifficultyTarget(),BlockReading.getHeight(), BlockReading.getHeader().getTimeSeconds(), BlockLastSolved.getHeader().getTimeSeconds());
            BigInteger diff = BlockReading.getHeader().getDifficultyTargetAsInteger();
            //if(i == 1)
            //    log.info("KGW-N2: difficulty of i=1: " + BlockReading.getHeader().getDifficultyTarget() +"->"+ diff.toString(16));
            if(result == 1)
                break;
            if(result == 2)
                return;
            /*
            if (PastBlocksMax > 0 && i > PastBlocksMax)
            {
                break;
            }
            PastBlocksMass++;
            BigInteger PastDifficultyAverageN = new BigInteger("0");
            if (i == 1)	{ PastDifficultyAverage = BlockReading.getHeader().getDifficultyTargetAsInteger(); }
            else
            {
                //PastDifficultyAverage = ((BlockReading.getHeader().getDifficultyTargetAsInteger().subtract(PastDifficultyAveragePrev)).divide(BigInteger.valueOf(i)).add(PastDifficultyAveragePrev));
            }
            PastDifficultyAveragePrev = PastDifficultyAverage;

            long diff = System.currentTimeMillis();
            totalBigIntTime += diff - startLoop;

            PastRateActualSeconds			= BlockLastSolved.getHeader().getTimeSeconds() - BlockReading.getHeader().getTimeSeconds();
            PastRateTargetSeconds			= TargetBlocksSpacingSeconds * PastBlocksMass;
            PastRateAdjustmentRatio			= 1.0f;
            if (PastRateActualSeconds < 0) { PastRateActualSeconds = 0; }
            if (PastRateActualSeconds != 0 && PastRateTargetSeconds != 0) {
                PastRateAdjustmentRatio			= (double)PastRateTargetSeconds / PastRateActualSeconds;
            }
            EventHorizonDeviation			= 1 + (0.7084 * java.lang.Math.pow((Double.valueOf(PastBlocksMass)/Double.valueOf(144)), -1.228));
            EventHorizonDeviationFast		= EventHorizonDeviation;
            EventHorizonDeviationSlow		= 1 / EventHorizonDeviation;

            if (PastBlocksMass >= PastBlocksMin) {
                if ((PastRateAdjustmentRatio <= EventHorizonDeviationSlow) || (PastRateAdjustmentRatio >= EventHorizonDeviationFast))
                {
                    //assert(BlockReading);
                    break;
                }
            }*/
            long calcTime = System.currentTimeMillis();
            StoredBlock BlockReadingPrev = blockStore.get(BlockReading.getHeader().getPrevBlockHash());
            if (BlockReadingPrev == null)
            {
                //If this is triggered, then we are using checkpoints and haven't downloaded enough blocks to verify the difficulty.
                //assert(BlockReading);     //from C++ code
                //break;                    //from C++ code
                return;
            }
            //log.info("KGW: i = {}; height = {}; hash {} ", i, BlockReadingPrev.getHeight(), BlockReadingPrev.getHeader().getHashAsString());
            BlockReading = BlockReadingPrev;
            endLoop = System.currentTimeMillis();
            //log.info("KGW-N: i = {}; height = {}; total time {}=calc {}+read {}", i, BlockReadingPrev.getHeight(), endLoop - startLoop, calcTime - startLoop, endLoop-calcTime);
            totalCalcTime += calcTime - startLoop;
            totalReadtime += endLoop-calcTime;
        }

        /*CBigNum bnNew(PastDifficultyAverage);
        if (PastRateActualSeconds != 0 && PastRateTargetSeconds != 0) {
            bnNew *= PastRateActualSeconds;
            bnNew /= PastRateTargetSeconds;
        } */
        //log.info("KGW iterations: {}, rewinding from {} to {}; time {}", i, BlockReading.getHeight(), storedPrev.getHeight()+1, endLoop - start);
        //log.info("KGW-N: i = {}; height = {}; total time {}=calc {}+read {}", i, storedPrev.getHeight(), System.currentTimeMillis() - start, totalCalcTime, totalReadtime /*, totalBigIntTime, totalCalcTime-totalBigIntTime*/);
        //log.info("KGW-N2, {}, {}, {}", storedPrev.getHeight(), i, System.currentTimeMillis() - start);
        /*BigInteger newDifficulty = PastDifficultyAverage;
        if (PastRateActualSeconds != 0 && PastRateTargetSeconds != 0) {
            newDifficulty = newDifficulty.multiply(BigInteger.valueOf(PastRateActualSeconds));
            newDifficulty = newDifficulty.divide(BigInteger.valueOf(PastRateTargetSeconds));
        }*/

        BigInteger newDifficulty = new BigInteger(kgw.KimotoGravityWell_close());

        if (newDifficulty.compareTo(this.getMaxTarget()) > 0) {
            log.info("Difficulty hit proof of work limit: {}", newDifficulty.toString(16));
            newDifficulty = this.getMaxTarget();
        }



        verifyDifficulty(newDifficulty, nextBlock);

    }
    private void verifyDifficulty(BigInteger calcDiff, Block nextBlock)
    {
        if (calcDiff.compareTo(this.getMaxTarget()) > 0) {
            log.info("Difficulty hit proof of work limit: {}", calcDiff.toString(16));
            calcDiff = this.getMaxTarget();
        }
        int accuracyBytes = (int) (nextBlock.getDifficultyTarget() >>> 24) - 3;
        BigInteger receivedDifficulty = nextBlock.getDifficultyTargetAsInteger();

        // The calculated difficulty is to a higher precision than received, so reduce here.
        BigInteger mask = BigInteger.valueOf(0xFFFFFFL).shiftLeft(accuracyBytes * 8);
        calcDiff = calcDiff.and(mask);

        if (calcDiff.compareTo(receivedDifficulty) != 0)
            throw new VerificationException("Network provided difficulty bits do not match what was calculated: " +
                    receivedDifficulty.toString(16) + " vs " + calcDiff.toString(16));
    }
    @Override
    public boolean checkTestnetDifficulty(StoredBlock storedPrev, Block prev, Block next, BlockStore blockStore) throws VerificationException, BlockStoreException {
         //verifyDifficulty(Utils.decodeCompactBits(0x1d13ffec), storedPrev, next, getAlgo(next));
        return true;
    }

    //main.cpp GetBlockValue(height, fee)

    static final int nGenesisBlockRewardCoin = 5;
    static final int nBlockRewardStartCoin = 96;
    static final int nBlockRewardFork1Coin = 48;
    static final int nBlockRewardFork3Coin = 16;
    public Coin getBlockInflation(int height)
    {

        if (height == 0)
        {
            return Coin.valueOf(nGenesisBlockRewardCoin, 0);
        }

        Coin subsidy = Coin.valueOf(nBlockRewardStartCoin, 0);

        if (height > BLOCK_HEIGHT_FORK1)
        {
            subsidy = Coin.valueOf(nBlockRewardFork1Coin, 0);
        }

        if (height > BLOCK_HEIGHT_FORK3)
        {
            subsidy = Coin.valueOf(nBlockRewardFork3Coin, 0);
        }


        return subsidy.shiftRight(height / subsidyDecreaseBlockCount);


    }

}
