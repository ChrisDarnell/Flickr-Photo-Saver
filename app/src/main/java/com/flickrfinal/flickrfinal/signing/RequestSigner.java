

package com.flickrfinal.flickrfinal.signing;

import android.util.Log;
import android.util.Pair;

import com.flickrfinal.flickrfinal.FlickrDemoConstants;
import com.flickrfinal.flickrfinal.StaticUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class RequestSigner {

    protected static final String SIGNATURE_ARGUMENT   = "&api_sig=";
    protected static final String ARGUMENT_SEPARATOR   = "&";
    protected static final String ARGUMENT_EQUALS      = "=";
    protected static final String ARGUMENT_METHOD      = "\\?";

    protected static final String FORMAT_ARGUMENT      = "&format=json&nojsoncallback=1";
    protected static final String API_KEY_ARGUMENT     = "&api_key=";


    public static String signRequest(final String unsorted, final String tokenSecret) {
        ArrayList<Pair<String,String>> argList = new ArrayList<>();
        StringBuilder signatureStringBuilder = new StringBuilder();

        String[] kvPairs = unsorted.split(ARGUMENT_SEPARATOR);
        for(int i = 1; i < kvPairs.length; i++) {
            String[] pair = kvPairs[i].split(ARGUMENT_EQUALS);
            argList.add(new Pair<>(pair[0], pair[1]));

            if(FlickrDemoConstants.DEBUG_ENABLE_VERBOSE) {
                Log.i("signRequest", "Adding " + pair[0] + ", " + pair[1]);
            }
        }

        // Sort alphabetically
        Collections.sort(argList, new PairComparator());


        String[] methodPair = kvPairs[0].split(ARGUMENT_METHOD)[1].split(ARGUMENT_EQUALS);
        argList.add(new Pair<>(methodPair[0], methodPair[1]));
        for(int i = 0; i < argList.size(); i++) {
            signatureStringBuilder.append(argList.get(i).first);
            signatureStringBuilder.append(argList.get(i).second);
        }

        final String signature = generateSignature(signatureStringBuilder.toString(), tokenSecret);

        if(FlickrDemoConstants.DEBUG_ENABLE_VERBOSE) {
            Log.i("signRequest", "signature: " + signature);
        }

        final String signed = unsorted + SIGNATURE_ARGUMENT + signature;

        if(FlickrDemoConstants.DEBUG_ENABLE_VERBOSE) {
            Log.i("signRequest", "final url: " + signed);
        }

        return signed;
    }


    protected static String generateSignature(final String argList, final String tokenSecret) {
        final String signingString =  tokenSecret + argList;

        final String signature = StaticUtil.getMD5Hash(signingString);

        if(FlickrDemoConstants.DEBUG_ENABLE_VERBOSE) {
            Log.i("generateSignature", "signing string: " + signingString);
            Log.i("generateSignature", "signature: " + signature);
        }

        return signature;
    }

    protected static class PairComparator implements Comparator<Pair<String,String>> {
        public int compare(Pair<String,String> pair1, Pair<String,String> pair2){
            return pair1.first.compareTo(pair2.first) ;
        }
    }



    public static String addStaticParams(final String url) {
        return url + FORMAT_ARGUMENT + API_KEY_ARGUMENT + FlickrDemoConstants.FLICKR_API_KEY;
    }
}
