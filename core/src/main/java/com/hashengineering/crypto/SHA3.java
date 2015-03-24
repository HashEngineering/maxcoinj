package com.hashengineering.crypto;

import fr.cryptohash.Keccak256;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Hash Engineering on 4/24/14 for the X11 algorithm
 */
public class SHA3 {

    private static final Logger log = LoggerFactory.getLogger(SHA3.class);
    private static boolean native_library_loaded = false;

    static {

        try {
            System.loadLibrary("keccak");
            native_library_loaded = true;
        }
        catch(UnsatisfiedLinkError x)
        {

        }
        catch(Exception e)
        {
            native_library_loaded = false;
        }
    }

    public static byte[] digest(byte[] input, int offset, int length)
    {
        //long start = System.currentTimeMillis();
        try {
            return native_library_loaded ? keccak256_native(input, offset, length) : keccak256(input, offset, length);
        } catch (Exception e) {
            return null;
        }
        finally {
            //long time = System.currentTimeMillis()-start;
            //log.info("X11 Hash time: {} ms per block", time);
        }
    }

    public static byte[] digest(byte[] input) {
        //long start = System.currentTimeMillis();
        try {
            return native_library_loaded ? keccak256_native(input, 0, input.length) : keccak256(input);
        } catch (Exception e) {
            return null;
        }
        finally {
            //long time = System.currentTimeMillis()-start;
            //log.info("X11 Hash time: {} ms per block", time);
        }
    }

    static native byte [] keccak256_native(byte [] input, int offset, int length);


    static byte [] keccak256(byte input[])
    {

        Keccak256 keccak = new Keccak256();
        return keccak.digest(input);
    }

    static byte [] keccak256(byte [] input, int offset, int length)
    {
        Keccak256 keccak256 = new Keccak256();
        keccak256.update(input, offset, length);
        return keccak256.digest();
    }
}
