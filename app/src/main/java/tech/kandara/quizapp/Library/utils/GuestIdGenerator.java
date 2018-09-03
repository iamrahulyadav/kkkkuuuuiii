package tech.kandara.quizapp.Library.utils;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Created by Abinash on 6/23/2017.
 */

public final class GuestIdGenerator {
    private SecureRandom random=new SecureRandom();

    public String nextGuestId(){
        return new BigInteger(130, random).toString(32);
    }
}
