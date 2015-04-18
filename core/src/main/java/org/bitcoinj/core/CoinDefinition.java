package org.bitcoinj.core;

import java.math.BigInteger;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: HashEngineering
 * Date: 8/13/13
 * Time: 7:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class CoinDefinition {


    public static final String coinName = "MaxCoin";
    public static final String coinTicker = "MAX";
    public static final String coinURIScheme = "maxcoin";
    public static final String cryptsyMarketId = "152";
    public static final String cryptsyMarketCurrency = "BTC";
    public static final String PATTERN_PRIVATE_KEY_START = "5";
    public static final String PATTERN_PRIVATE_KEY_START_COMPRESSED = "[Q]";
    public static final String PATTERN_PRIVATE_KEY_START_TESTNET = "9";
    public static final String PATTERN_PRIVATE_KEY_START_COMPRESSED_TESTNET = "c";

    public static String lowerCaseCoinName() { return coinName.toLowerCase(); }

    public enum CoinPrecision {
        Coins,
        Millicoins,
    }
    public static final CoinPrecision coinPrecision = CoinPrecision.Coins;


    public static final String BLOCKEXPLORER_BASE_URL_PROD = "http://max.cryptoexplore.com/";    //blockr.io
    public static final String BLOCKEXPLORER_ADDRESS_PATH = "address/";
    public static final String BLOCKEXPLORER_TRANSACTION_PATH = "tx/";
    public static final String BLOCKEXPLORER_BLOCK_PATH = "block/";
    public static final String BLOCKEXPLORER_BASE_URL_TEST = BLOCKEXPLORER_BASE_URL_PROD;

    public static final String DONATION_ADDRESS = "mLhpkXGWyDy2YBFtTzF9c6jYRZ1HXzUjcr";  //http://foundation.maxcoin.co.uk/donate/

    public static final String UNSPENT_API_URL = "http://explorer.maxcoin.co.uk/chain/Maxcoin/unspent/";
    public enum UnspentAPIType {
        BitEasy,
        Blockr,
        Abe
    };
    public static final UnspentAPIType UnspentAPI = UnspentAPIType.Abe;

    public static boolean checkpointFileSupport = true;
    public static int checkpointDaysBack = 21;

    public static final int TARGET_TIMESPAN = (int)(180);  // 3 minutes per difficulty cycle, on average.
    public static final int TARGET_SPACING = (int)(30);  // 30 seconds per block.
    public static final int INTERVAL = TARGET_TIMESPAN / TARGET_SPACING;  //108 blocks

    public static final int getInterval(int height, boolean testNet) {
            return INTERVAL;      //108
    }
    public static final int getIntervalCheckpoints() {
            return INTERVAL * 500;    //1080
    }

    public static int spendableCoinbaseDepth = 120; //main.h: static const int COINBASE_MATURITY
    public static final int MAX_MONEY = 106058400;//).multiply(Utils.COIN);                 //main.h:  MAX_MONEY
    //public static final String MAX_MONEY_STRING = "200000000";     //main.h:  MAX_MONEY

    public static final Coin DEFAULT_MIN_TX_FEE = Coin.valueOf(10000);   // MIN_TX_FEE
    public static final Coin DUST_LIMIT = Coin.valueOf(10000); //main.h CTransaction::GetMinFee        0.01 coins

    public static final int PROTOCOL_VERSION = 70005;          //version.h PROTOCOL_VERSION
    public static final int MIN_PROTOCOL_VERSION = 70000;        //version.h MIN_PROTO_VERSION - eliminate 60001 which are on the wrong fork
    public static final int INIT_PROTO_VERSION = 70000;            //version.h

    public static final int BLOCK_CURRENTVERSION = 1;   //CBlock::CURRENT_VERSION
    public static final int MAX_BLOCK_SIZE = 1 * 1000 * 1000;


    public static final boolean supportsBloomFiltering = false; //Requires PROTOCOL_VERSION 70000 in the client
    public static boolean supportsIrcDiscovery() {
        return PROTOCOL_VERSION <= 70000;
    }

    public static final int Port    = 8668;       //protocol.h GetDefaultPort(testnet=false)
    public static final int TestPort = 18668;     //protocol.h GetDefaultPort(testnet=true)

    //
    //  Production
    //
    public static final int AddressHeader = 110;             //base58.h CBitcoinAddress::PUBKEY_ADDRESS
    public static final int p2shHeader = 112;             //base58.h CBitcoinAddress::SCRIPT_ADDRESS
    public static final boolean allowBitcoinPrivateKey = true; //for backward compatibility with previous version of digitalcoin
    public static final long PacketMagic = 0xf9bebbd2;      //0xf9, 0xbe, 0xbb, 0xd2

    //Genesis Block Information from main.cpp: LoadBlockIndex
    static public long genesisBlockDifficultyTarget = (0x1e00ffff);         //main.cpp: LoadBlockIndex
    static public long genesisBlockTime = 1390822264L;                       //main.cpp: LoadBlockIndex
    static public long genesisBlockNonce = (11548217);                         //main.cpp: LoadBlockIndex
    static public String genesisHash = "0000002d0f86558a6e737a3a351043ee73906fe077692dfaa3c9328aaca21964"; //main.cpp: hashGenesisBlock
    static public int genesisBlockValue = 5;                                                              //main.cpp: LoadBlockIndex
    static public final int genesisBlockVersion = 112;
    //taken from the raw data of the block explorer
    static public String genesisTxInBytes = "04ffff001d01043453686170652d7368696674696e6720736f66747761726520646566656e647320616761696e737420626f746e6574206861636b73";   //"Digitalcoin, A Currency for a Digital Age"
    static public String genesisTxOutBytes = "04678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5f";

    //net.cpp strDNSSeed
    static public String[] dnsSeeds = new String[] {
            "dnsseed.maxcoin.co.uk"
     };

    public static int minBroadcastConnections = 1;   //0 for default; we need more peers.

    //
    // TestNet - digitalcoin - not tested
    //
    public static final boolean supportsTestNet = false;
    public static final int testnetAddressHeader = 127;             //base58.h CBitcoinAddress::PUBKEY_ADDRESS_TEST
    public static final int testnetp2shHeader = 130;             //base58.h CBitcoinAddress::SCRIPT_ADDRESS_TEST
    public static final long testnetPacketMagic = 0x1e00ffff;      //0xfc, 0xc1, 0xb7, 0xdc
    public static final String testnetGenesisHash = "0000000a23e3eb42dc87b61d4015c80ffd85471d34e2c7210c7ca63b78a58612";
    static public long testnetGenesisBlockDifficultyTarget = (0x1e0ffff0L);         //main.cpp: LoadBlockIndex
    static public long testnetGenesisBlockTime = 1390434081L;                       //main.cpp: LoadBlockIndex
    static public long testnetGenesisBlockNonce = (1582968);                         //main.cpp: LoadBlockIndex






    public static int subsidyDecreaseBlockCount = 60*24*365*4;     //main.cpp GetBlockValue(height, fee)

    public static BigInteger proofOfWorkLimit = Utils.decodeCompactBits(0x1e00ffff);  //main.cpp bnProofOfWorkLimit (~uint256(0) >> 20); // digitalcoin: starting difficulty is 1 / 2^12

    static public String[] testnetDnsSeeds = new String[] {
          "not supported"
    };
    //from main.h: CAlert::CheckSignature
    public static final String SATOSHI_KEY = "04fc9702847840aaf195de8442ebecedf5b095cdbb9bc716bda9110971b28a49e0ead8564ff0db22209e0374782c093bb899692d524e9d6a6956e7c5ecbcd68284";
    public static final String TESTNET_SATOSHI_KEY = "04302390343f91cc401d56d68b123028bf52e5fca1939df127f63c6467cdf9c8e2c14b61104cf817d0b780da337893ecc4aaff1309e536162dabbdb45200ca2b0a";

    /** The string returned by getId() for the main, production network where people trade things. */
    public static final String ID_MAINNET = "org.maxcoin.production";
    /** The string returned by getId() for the testnet. */
    public static final String ID_TESTNET = "org.maxcoin.test";
    /** Unit test network. */
    public static final String ID_UNITTESTNET = "com.google.maxcoin.unittest";

    //checkpoints.cpp Checkpoints::mapCheckpoints
    public static void initCheckpoints(Map<Integer, Sha256Hash> checkpoints)
    {

    }

    //Unit Test Information
    public static final String UNITTEST_ADDRESS = "DPHYTSm3f96dHRY3VG1vZAFC1QrEPkEQnt";
    public static final String UNITTEST_ADDRESS_PRIVATE_KEY = "QU1rjHbrdJonVUgjT7Mncw7PEyPv3fMPvaGXp9EHDs1uzdJ98hUZ";

}
