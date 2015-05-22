package com.example.jgreenb2.myselfie;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;

/**
 * Created by jgreenb2 on 5/22/15.
 */
public class ConfirmRenameDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        Bundle dialogParams = getArguments();
        String fromStr = dialogParams.getString("from");
        String toStr = dialogParams.getString("to");

        String confirmMsg = String.format(
                "Rename \"%s\" to \"%s\" ?",
                fromStr, toStr);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(inflater.inflate(R.layout.confirm_rename, null)).
                setMessage(confirmMsg).
                setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent("rename-selected-selfies-event");
                        intent.putExtra("ExecuteRename", true);
                        LocalBroadcastManager.getInstance(getDialog().getContext()).sendBroadcast(intent);
                    }
                }).
                setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent("rename-selected-selfies-event");
                        intent.putExtra("ExecuteRename", false);
                        LocalBroadcastManager.getInstance(getDialog().getContext()).sendBroadcast(intent);
                    }
                });

        return builder.create();
    }
}