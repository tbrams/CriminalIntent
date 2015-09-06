package dk.incipio.criminalintent;


import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.UUID;

public class CrimeLab {
    private static final String TAG = "CrimeLab";
    private static final String FILENAME = "crimes.json";

    private ArrayList<Crime> mCrimes;
    private CriminalIntendJSONSerializer mSerializer;
    private static CrimeLab sCrimeLab;
    private Context  mAppContext;

    private CrimeLab(Context appContext) {
        mAppContext = appContext;
        mSerializer = new CriminalIntendJSONSerializer(mAppContext, FILENAME);

        try {
            mCrimes = mSerializer.loadCrimes();
        } catch (Exception e) {
            mCrimes = new ArrayList<Crime>();
            Log.d(TAG, "Error loading crimes");
        }

    }

    public boolean saveCrimes(){
        try {
            mSerializer.saveCrimes(mCrimes);
            Log.d(TAG, "Crimes saved to file");
            return true;
        } catch (Exception e) {
            Log.d(TAG, "Failed to save the crimes to file", e);
            return false;
        }
    }


    public static CrimeLab get(Context c) {
        if (sCrimeLab==null) {
            sCrimeLab=new CrimeLab(c.getApplicationContext());
        }
        return sCrimeLab;
    }


    public void addCrime(Crime c) {
        mCrimes.add(c);
    }


    public ArrayList<Crime> getCrimes() {
        return mCrimes;
    }

    public Crime getCrime(UUID id) {
        for (Crime c: mCrimes) {
            if (c.getId().equals(id))
                    return c;
        }
        return null;
    }
}


