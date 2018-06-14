package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_settings.*
import org.stepic.droid.R
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.ui.dialogs.AllowMobileDataDialogFragment
import org.stepic.droid.ui.dialogs.CoursesLangDialog
import org.stepic.droid.ui.dialogs.VideoQualityDialog

class SettingsFragment : FragmentBase(), AllowMobileDataDialogFragment.Callback {

    companion object {
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }

    val kb: String by lazy { getString(R.string.kb) }

    val mb: String by lazy { getString(R.string.mb) }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nullifyActivityBackground()

        notificationActionButton.setOnClickListener {
            screenManager.showNotificationSettings(activity)
        }

        fragmentSettingsWifiEnableSwitch.isChecked = !sharedPreferenceHelper.isMobileInternetAlsoAllowed//if first time it is true

        fragmentSettingsExternalPlayerSwitch.isChecked = userPreferences.isOpenInExternal

        fragmentSettingsExternalPlayerSwitch.setOnCheckedChangeListener { _, isChecked -> userPreferences.isOpenInExternal = isChecked }

        fragmentSettingsCalendarWidgetSwitch.isChecked = userPreferences.isNeedToShowCalendarWidget

        fragmentSettingsCalendarWidgetSwitch.setOnCheckedChangeListener { _, isChecked -> userPreferences.isNeedToShowCalendarWidget = isChecked }

        fragmentSettingsKeepScreenOnSwitch.isChecked = userPreferences.isKeepScreenOnSteps
        fragmentSettingsKeepScreenOnSwitch.setOnCheckedChangeListener { _, isChecked -> userPreferences.isKeepScreenOnSteps = isChecked }

        fragmentSettingsAdaptiveMode.isChecked = userPreferences.isAdaptiveModeEnabled
        fragmentSettingsAdaptiveMode.setOnCheckedChangeListener { _, isChecked -> userPreferences.isAdaptiveModeEnabled = isChecked }

        fragmentSettingsDiscountingPolicySwitch.isChecked = userPreferences.isShowDiscountingPolicyWarning

        fragmentSettingsDiscountingPolicySwitch.setOnCheckedChangeListener { _, isChecked -> userPreferences.isShowDiscountingPolicyWarning = isChecked }


        fragmentSettingsWifiEnableSwitch.setOnCheckedChangeListener { _, newCheckedState ->
            if (fragmentSettingsWifiEnableSwitch.isUserTriggered) {
                if (newCheckedState) {
                    //wifi only
                    onMobileDataStateChanged(false)
                } else {
                    //wifi and mobile internet
                    fragmentSettingsWifiEnableSwitch.isChecked = true
                    val dialogFragment = AllowMobileDataDialogFragment.newInstance()
                    dialogFragment.setTargetFragment(this@SettingsFragment, 0)
                    if (!dialogFragment.isAdded) {
                        dialogFragment.show(fragmentManager, null)
                    }
                }

            }
        }


        videoQualityView.setOnClickListener {
            val videoDialog = VideoQualityDialog.newInstance(forPlaying = false)
            if (!videoDialog.isAdded) {
                videoDialog.show(fragmentManager, null)
            }
        }

        videoPlayingQualityView.setOnClickListener {
            val videoDialog = VideoQualityDialog.newInstance(forPlaying = true)
            if (!videoDialog.isAdded) {
                videoDialog.show(fragmentManager, null)
            }
        }

        storageManagementButton.setOnClickListener { screenManager.showStorageManagement(activity) }

        langWidgetActionButton.setOnClickListener {
            CoursesLangDialog.newInstance().show(fragmentManager, null)
        }
    }

    override fun onDestroyView() {
        fragmentSettingsKeepScreenOnSwitch.setOnCheckedChangeListener(null)
        fragmentSettingsDiscountingPolicySwitch.setOnCheckedChangeListener(null)
        fragmentSettingsCalendarWidgetSwitch.setOnCheckedChangeListener(null)
        fragmentSettingsWifiEnableSwitch.setOnCheckedChangeListener(null)
        fragmentSettingsExternalPlayerSwitch.setOnCheckedChangeListener(null)
        storageManagementButton.setOnClickListener(null)
        notificationActionButton.setOnClickListener(null)
        super.onDestroyView()
    }

    private fun storeMobileState(isMobileAllowed: Boolean) {
        sharedPreferenceHelper.setMobileInternetAndWifiAllowed(isMobileAllowed)
    }

    override fun onMobileDataStateChanged(isMobileAllowed: Boolean) {
        fragmentSettingsWifiEnableSwitch.isChecked = !isMobileAllowed
        storeMobileState(isMobileAllowed)
    }
}