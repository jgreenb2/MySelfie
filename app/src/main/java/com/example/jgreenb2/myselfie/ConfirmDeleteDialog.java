package com.example.jgreenb2.myselfie;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

/**
 * Created by jgreenb2 on 5/15/15.
 */
public class ConfirmDeleteDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        Bundle dialogParams = getArguments();
        int nSelected = dialogParams.getInt("nSelected");
        String confirmMsg = String.format(
                "About to delete %d item(s). This action cannot be undone. Ok to proceed?",
                nSelected);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(inflater.inflate(R.layout.confirm_delete, null)).
                setMessage(confirmMsg).
                setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent("delete-selected-selfies-event");
                        intent.putExtra("ExecuteDelete",true);
                        LocalBroadcastManager.getInstance(getDialog().getContext()).sendBroadcast(intent);
                    }
                }).
                setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent("delete-selected-selfies-event");
                        intent.putExtra("CancelDelete",true);
                        LocalBroadcastManager.getInstance(getDialog().getContext()).sendBroadcast(intent);
                    }
                });


        return builder.create();
    }
}

