package dk.incipio.criminalintent;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;


public class CrimeFragment extends Fragment {
    public static final String EXTRA_CRIME_ID="dk.incipio.criminalintent.crime_id";
    private static final String TAG = "CrimeFragment";
    public static final String DIALOG_DATE="date";
    private static final int REQUEST_DATE=0;
    private static final int REQUEST_PHOTO=1;
    private static final int REQUEST_CONTACT=2;
    private static final String DIALOG_IMAGE = "image";

    private Crime mCrime;
    private EditText mTitleField;
    private CheckBox mSolvedCheckBox;
    private Button mDateButton;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private Button mSuspectButton;


    public static CrimeFragment newInstance(UUID id) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_CRIME_ID, id);


        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get id from extra and retreive crime details
        UUID id = (UUID) getArguments().getSerializable(EXTRA_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(id);

        setHasOptionsMenu(true);  // will implement option menu callback on behalf of the activity
    }

    @TargetApi(11)
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime,parent,false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (NavUtils.getParentActivityName(getActivity())!=null) {
                getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        mTitleField = (EditText)v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mPhotoButton = (ImageButton) v.findViewById(R.id.crime_imageButton);
        mPhotoView = (ImageView)v.findViewById(R.id.crime_imageView);
        Button reportButton = (Button)v.findViewById(R.id.crime_report_button);

        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                i.putExtra(Intent.EXTRA_SUBJECT, R.string.crime_report_subject);
                i.createChooser(i, getString(R.string.send_report));
                startActivity(i);
            }
        });
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Photo p = mCrime.getPhoto();
                if (p == null) {
                    return;
                }
                FragmentManager fm = getActivity().getSupportFragmentManager();
                String path = getActivity().getFileStreamPath(p.getFilename()).getAbsolutePath();
                ImageFragment.newInstance(path).show(fm, DIALOG_IMAGE);
            }
        });

        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), CrimeCameraActivity.class);
                startActivityForResult(i, REQUEST_PHOTO);

            }
        });

        // Disable ImageButton if the phone has no camera
        PackageManager pm = getActivity().getPackageManager();
        boolean hasCamera = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT) ||
                (Build.VERSION.SDK_INT>=Build.VERSION_CODES.GINGERBREAD && Camera.getNumberOfCameras()>0);
        if (!hasCamera) mPhotoButton.setEnabled(false);

        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mDateButton = (Button)v.findViewById(R.id.crime_date);
        // mDateButton.setText(mCrime.getDate().toString());
        updateDate();
        // mDateButton.setEnabled(false);

        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                // DatePickerFragment dialog = new DatePickerFragment();

                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);

                dialog.show(fm, DIALOG_DATE);

            }
        });


        mSolvedCheckBox = (CheckBox)v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());

        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
            }
        });



        // Chapter 20 challenge
        if (Build.VERSION.SDK_INT <  Build.VERSION_CODES.HONEYCOMB) {
            registerForContextMenu(mPhotoView);
        } else {

            final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {

                // Called when the action mode is created; startActionMode() was called
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    // Inflate a menu resource providing context menu items
                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.crime_context_fragment, menu);
                    return true;
                }

                // Called each time the action mode is shown. Always called after onCreateActionMode, but
                // may be called multiple times if the mode is invalidated.
                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false; // Return false if nothing is done
                }

                // Called when the user selects a contextual menu item
                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.delete_photo:
                            deletePhoto();
                            mode.finish(); // Action picked, so close the CAB
                            return true;
                        default:
                            return false;
                    }
                }

                // Called when the user exits the action mode
                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    //mActionMode = null;
                }
            };

            mPhotoView.setOnLongClickListener( new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    Log.d(TAG, "On Long Click");
                    getActivity().startActionMode(actionModeCallback);
                    return true;
                }
            });
        }

        mSuspectButton = (Button) v.findViewById(R.id.crime_suspect_button);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(i, REQUEST_CONTACT);
            }
        });

        if (mCrime.getSuspect()!=null) {
            mSuspectButton.setText(mCrime.getSuspect());
        }

        return v;
    }

    private void deletePhoto() {
        if (mCrime.getPhoto() != null) {
            String path = getActivity().getFileStreamPath(mCrime.getPhoto().getFilename()).getAbsolutePath();
            File f = new File(path);
            f.delete();

            PictureUtils.cleanImageView(mPhotoView);
            mCrime.setPhoto(null);

            Log.w("Delete Check", "File deleted: " + path);
        }

    }


    private void showPhoto() {
        // reset the image button's image based on our photo
        Photo p = mCrime.getPhoto();
        BitmapDrawable b = null;

        if (p != null) {
            String path = getActivity().getFileStreamPath(p.getFilename()).getAbsolutePath();
            b=PictureUtils.getScaledDownDrawable(getActivity(), path);
        }

        mPhotoView.setImageDrawable(b);

    }


    @Override
    public void onStart() {
        super.onStart();
        showPhoto();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode!= Activity.RESULT_OK)
            return;

        if (requestCode==REQUEST_DATE)
        {
            Date date = (Date)data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);

            // mDateButton.setText(mCrime.getDate().toString());
            updateDate();
        } else if (requestCode == REQUEST_PHOTO)
        {
            // Delete it, if mCrime already has a photo associated
            deletePhoto();

            // Create a new Photo object and associate it with the Crime Object... and display
            String fileName = data.getStringExtra(CrimeCameraFragment.EXTRA_PHOTO_FILENAME);
            if (fileName != null) {
                Photo p = new Photo(fileName);
                mCrime.setPhoto(p);
                showPhoto();
            }
         }
        else if(requestCode == REQUEST_CONTACT)
        {
            Uri ContactUri = data.getData();
            // specify which field you are interested in
            String[] queryFields = new String[] {ContactsContract.Contacts.DISPLAY_NAME};
            Cursor c = getActivity().getContentResolver().query(ContactUri, queryFields, null, null, null);

            // Double check there is results here
            if (c.getCount() == 0) {
                c.close();
                return;
            }

            // Pull out the first column of the first row of data
            c.moveToFirst();
            String suspect = c.getString(0);
            mCrime.setSuspect(suspect);
            mSuspectButton.setText(suspect);
            c.close();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        CrimeLab.get(getActivity()).saveCrimes();
    }

    @Override
    public void onStop() {
        super.onStop();
        PictureUtils.cleanImageView(mPhotoView);
    }

    private void updateDate() {
        DateFormat mDateFormat = android.text.format.DateFormat.getLongDateFormat(getActivity());
        DateFormat mTimeFormat = android.text.format.DateFormat.getTimeFormat(getActivity());
        String date = (mDateFormat.format(mCrime.getDate()));
        String time = (mTimeFormat.format(mCrime.getDate()));

        mDateButton.setText(date + " " + time);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(getActivity())!=null) {
                    NavUtils.navigateUpFromSameTask(getActivity());
                }

                return true;

            default:

                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.crime_context_fragment, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_photo:
                deletePhoto();
                return true;
        }
        return false;
    }

    private String getCrimeReport() {
        String solvedString = null;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EEE, MMM DD";
        String dataString = android.text.format.DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        String report = getString(R.string.crime_report, mCrime.getTitle(), dataString, solvedString, suspect);

        return report;
    }


}
