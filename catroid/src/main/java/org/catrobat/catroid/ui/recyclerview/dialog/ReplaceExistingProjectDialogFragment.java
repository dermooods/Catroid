/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2018 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * An additional term exception under section 7 of the GNU Affero
 * General Public License, version 3, is available at
 * http://developer.catrobat.org/license_additional_term
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.catroid.ui.recyclerview.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.R;
import org.catrobat.catroid.utils.DownloadUtil;
import org.catrobat.catroid.utils.Utils;

public class ReplaceExistingProjectDialogFragment extends DialogFragment {

	public static final String TAG = ReplaceExistingProjectDialogFragment.class.getSimpleName();

	public static final String BUNDLE_KEY_PROGRAM_NAME = "programName";
	public static final String BUNDLE_KEY_URL = "url";

	public static ReplaceExistingProjectDialogFragment newInstance(String programName, String url) {
		ReplaceExistingProjectDialogFragment dialog = new ReplaceExistingProjectDialogFragment();

		Bundle bundle = new Bundle();
		bundle.putString(BUNDLE_KEY_PROGRAM_NAME, programName);
		bundle.putString(BUNDLE_KEY_URL, url);
		dialog.setArguments(bundle);

		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final String programName = getArguments().getString(BUNDLE_KEY_PROGRAM_NAME);
		final String url = getArguments().getString(BUNDLE_KEY_URL);

		View view = View.inflate(getActivity(), R.layout.dialog_overwrite_project, null);

		final TextInputLayout inputLayout = view.findViewById(R.id.input);
		final RadioGroup radioGroup = view.findViewById(R.id.radio_group);

		final TextInputDialog.TextWatcher textWatcher = new TextInputDialog.TextWatcher() {

			@Nullable
			@Override
			public String validateInput(String input, Context context) {
				String error = null;

				if (input.isEmpty()) {
					return context.getString(R.string.name_empty);
				}

				input = input.trim();

				if (input.isEmpty()) {
					error = context.getString(R.string.name_consists_of_spaces_only);
				} else if (Utils.checkIfProjectExistsOrIsDownloadingIgnoreCase(input)) {
					error = context.getString(R.string.name_already_exists);
				}

				return error;
			}
		};

		TextInputDialog.Builder builder = new TextInputDialog.Builder(getContext())
				.setText(programName)
				.setTextWatcher(textWatcher)
				.setPositiveButton(getString(R.string.ok), new TextInputDialog.OnClickListener() {
					@Override
					public void onPositiveButtonClick(DialogInterface dialog, String textInput) {
						switch (radioGroup.getCheckedRadioButtonId()) {
							case R.id.rename:
								DownloadUtil.getInstance().startDownload(getActivity(), url, textInput, true);
								break;
							case R.id.replace:
								ProjectManager.getInstance().setCurrentProject(null);
								DownloadUtil.getInstance().startDownload(getActivity(), url, textInput, false);
								break;
							default:
								throw new IllegalStateException(TAG + ": Cannot find RadioButton.");
						}
					}
				});

		final AlertDialog alertDialog = builder
				.setTitle(R.string.overwrite_title)
				.setView(view)
				.setNegativeButton(R.string.notification_download_project_cancel, null)
				.create();

		radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
				switch (checkedId) {
					case R.id.replace:
						inputLayout.setVisibility(TextView.GONE);
						alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
						break;
					case R.id.rename:
						inputLayout.setVisibility(TextView.VISIBLE);
						alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
								.setEnabled(textWatcher.validateInput(inputLayout.getEditText().toString(), getContext()) == null);
						break;
				}
			}
		});

		return alertDialog;
	}
}
