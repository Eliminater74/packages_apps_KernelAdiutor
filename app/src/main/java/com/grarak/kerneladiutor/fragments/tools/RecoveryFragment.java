/*
 * Copyright (C) 2015 Willi Ye
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.grarak.kerneladiutor.fragments.tools;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.grarak.kerneladiutor.R;
import com.grarak.kerneladiutor.elements.CardViewItem;
import com.grarak.kerneladiutor.elements.DividerCardView;
import com.grarak.kerneladiutor.fragments.RecyclerViewFragment;
import com.grarak.kerneladiutor.utils.Utils;
import com.grarak.kerneladiutor.utils.root.RootFile;
import com.grarak.kerneladiutor.utils.root.RootUtils;
import com.grarak.kerneladiutor.utils.tools.Recovery;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by willi on 11.04.15.
 */
public class RecoveryFragment extends RecyclerViewFragment {

    private AppCompatSpinner mRecoverySpinner;
    private AppCompatButton mFlashNowButton;

    private List<Recovery> mCommands;

    @Override
    public boolean showApplyOnBoot() {
        return false;
    }

    @Override
    public RecyclerView getRecyclerView() {
        View view = getParentView(R.layout.recovery_recyclerview);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.recovery_variants));
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mRecoverySpinner = (AppCompatSpinner) view.findViewById(R.id.recovery_spinner);
        mRecoverySpinner.setAdapter(dataAdapter);
        mRecoverySpinner.setSelection(Utils.getBoolean("twrp", false, getActivity()) ? 1 : 0);
        mRecoverySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Utils.saveBoolean("twrp", position == 1, getActivity());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        view.findViewById(R.id.wipe_data_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAction(Recovery.RECOVERY_COMMAND.WIPE_DATA, null);
            }
        });

        view.findViewById(R.id.wipe_cache_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAction(Recovery.RECOVERY_COMMAND.WIPE_CACHE, null);
            }
        });

        view.findViewById(R.id.flash_zip_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/zip");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 0);
            }
        });

        mFlashNowButton = (AppCompatButton) view.findViewById(R.id.flash_now_button);
        mFlashNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCommands.size() < 1) {
                    Utils.toast(getString(R.string.add_action_first), getActivity());
                    return;
                }

                Utils.confirmDialog(null, getString(R.string.flash_now_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String file = "/cache/recovery/" + mCommands.get(0).getFile(mRecoverySpinner
                                .getSelectedItemPosition() == 1 ? Recovery.RECOVERY.TWRP : Recovery.RECOVERY.CWM);
                        RootFile recoveryFile = new RootFile(file);
                        recoveryFile.delete();
                        for (Recovery commands : mCommands) {
                            for (String command : commands.getCommands(mRecoverySpinner.getSelectedItemPosition() == 1 ?
                                    Recovery.RECOVERY.TWRP :
                                    Recovery.RECOVERY.CWM))
                                recoveryFile.write(command, true);
                        }
                        RootUtils.runCommand("reboot recovery");
                    }
                }, getActivity());
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mFlashNowButton.setVisibility(View.INVISIBLE);

        return (RecyclerView) view.findViewById(R.id.recycler_view);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null)
            addAction(Recovery.RECOVERY_COMMAND.FLASH_ZIP, new File(data.getDataString()));
    }

    @Override
    public void preInitCardView() {
    }

    @Override
    public void initCardView(Bundle savedInstanceState) {
        mCommands = new ArrayList<>();

        DividerCardView.DDividerCard mActionsDividerCard = new DividerCardView.DDividerCard();
        mActionsDividerCard.setText(getString(R.string.actions));

        addView(mActionsDividerCard);
    }

    @Override
    public void postInitCardView(Bundle savedInstanceState) {
        Utils.circleAnimate(mFlashNowButton, 0, mFlashNowButton.getHeight());
    }

    private void addAction(Recovery.RECOVERY_COMMAND recovery_command, File file) {
        String description = null;
        switch (recovery_command) {
            case WIPE_DATA:
                description = getString(R.string.wipe_data);
                break;
            case WIPE_CACHE:
                description = getString(R.string.wipe_cache);
                break;
            case FLASH_ZIP:
                description = file.getAbsolutePath().replace("/file:", "").replace("/content:/com.estrongs.files", "")
                        .replace("/content:/com.android.externalstorage.documents", "");
                if (!description.endsWith(".zip") || description.startsWith("file:") || description.startsWith("/content:")
                        || description.startsWith("content:")) {
                    Utils.toast(getString(R.string.went_wrong), getActivity());
                    return;
                }
                break;
        }

        final Recovery recovery = new Recovery(recovery_command, new File(description));
        mCommands.add(recovery);

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.recovery_actionview, null, false);

        final CardViewItem.DCardView mActionCard = new CardViewItem.DCardView();

        ((TextView) view.findViewById(R.id.action_text)).setText(description);
        view.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeView(mActionCard);
                mCommands.remove(recovery);
            }
        });

        mActionCard.setView(view);
        addView(mActionCard);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.recovery_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String command = null;
        int message = 0;
        switch (item.getItemId()) {
            case R.id.menu_reboot:
                command = "reboot";
                message = R.string.reboot;
                break;
            case R.id.menu_reboot_recovery:
                command = "reboot recovery";
                message = R.string.reboot_recovery;
                break;
            case R.id.menu_reboot_bootloader:
                command = "reboot bootloader";
                message = R.string.reboot_bootloader;
                break;
            case R.id.menu_reboot_download:
                command = "reboot download";
                message = R.string.reboot_download;
                break;
        }

        final String c = command;
        Utils.confirmDialog(null, getString(message), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                RootUtils.runCommand(c);
            }
        }, getActivity());
        return true;
    }

}
