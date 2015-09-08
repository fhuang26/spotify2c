package com.example.fhuang.myproj0;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CountryCodeDialogFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CountryCodeDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CountryCodeDialogFragment extends DialogFragment {
    public static final int N_COUNTRY_CODE = 6;
    // private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 title
     * @return A new instance of fragment CountryCodeDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CountryCodeDialogFragment newInstance(String title) {
        CountryCodeDialogFragment frag = new CountryCodeDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    public CountryCodeDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        int style = DialogFragment.STYLE_NORMAL, theme = 0;
        setStyle(style, theme);
    }

    class CustomOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            /*
            Toast.makeText(parent.getContext(),
                    "OnItemSelectedListener : " + parent.getItemAtPosition(pos).toString(),
                    Toast.LENGTH_LONG).show();
            */
            String s = parent.getItemAtPosition(pos).toString();
            ArtistPhotosC.country_code = s.substring(0, 2);
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }
    }

    private View vCountryCodeDialog;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        vCountryCodeDialog = inflater.inflate(R.layout.fragment_country_code_dialog, container, false);
        String title = "Country Code for Tracks";
        getDialog().setTitle(title);

        Spinner spinner = (Spinner) vCountryCodeDialog.findViewById(R.id.spCountryCode);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getActivity(),
                R.array.country_code_array, android.R.layout.simple_spinner_dropdown_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        spinner.setScrollBarSize(N_COUNTRY_CODE);

        for (int k = 0; k < N_COUNTRY_CODE; ++k) {
            spinner.setSelection(k);
            String s = spinner.getSelectedItem().toString();
            if (s.substring(0, 2).equals(ArtistPhotosC.country_code)) {
                break;
            }
        }

        spinner.setOnItemSelectedListener(new CustomOnItemSelectedListener());

        return vCountryCodeDialog;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
