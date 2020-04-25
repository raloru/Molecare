package com.rlopez.molecare.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.rlopez.molecare.R;

public class NewMoleDialog extends AppCompatDialogFragment {

    // Input for the new mole name
    private EditText inputMoleName;

    private NewMoleDialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Create a builder for an alert dialog using the default dialog theme
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Link view with corresponding layout and get input from view
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.new_mole_dialog, null);
        inputMoleName = view.findViewById(R.id.inputMoleName);

        // Set the view, title, and buttons
        builder.setView(view)
                .setTitle(R.string.title_new_mole)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User cancelled dialog, close it
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Get text from input and pass it to activity
                        String moleName = inputMoleName.getText().toString();
                        listener.getMoleName(moleName);
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }

    // Called when this dialog is attached to its context
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        // Initialize listener
        try {
            listener = (NewMoleDialogListener) context;
        } catch (ClassCastException e) {
            // Activity must implement a listener
            throw new ClassCastException(context.toString() + "listener not implemented");
        }
    }

    // Dialog listener interface. Implemented in activities
    public interface NewMoleDialogListener {
        void getMoleName(String moleName);
    }
}
