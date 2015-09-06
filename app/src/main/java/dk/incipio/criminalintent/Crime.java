package dk.incipio.criminalintent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.UUID;


public class Crime {
    private UUID mId;
    private String mTitle;
    private Date mDate;
    private boolean mSolved;
    private static final String JSON_ID="id";
    private static final String JSON_TITLE="title";
    private static final String JSON_SOLVED="solved";
    private static final String JSON_DATE="date";


    public Crime() {
        // Generate a unique Identifier
        mId=UUID.randomUUID();
        mDate = new Date();
        mSolved = false;
    }


    // Constructor that triggers on receiving a JSON object
    public Crime(JSONObject json) throws JSONException {
        mId=UUID.fromString(json.getString(JSON_ID));
        if (json.has(JSON_TITLE)) mTitle=json.getString(JSON_TITLE);
        mSolved = json.getBoolean(JSON_SOLVED);
        mDate=new Date(json.getLong(JSON_DATE));
    }



    @Override
    public String toString() {
        return mTitle;
    }


    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_ID, mId.toString());
        json.put(JSON_TITLE, mTitle);
        json.put(JSON_DATE, mDate.getTime());
        json.put(JSON_SOLVED, mSolved);

        return json;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date mDate) {
        this.mDate = mDate;
    }

    public boolean isSolved() {
        return mSolved;
    }

    public void setSolved(boolean mSolved) {
        this.mSolved = mSolved;
    }


    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }
}
