package com.app.chefbook.UI.SettingsFragment


import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import cn.pedant.SweetAlert.SweetAlertDialog
import com.app.chefbook.DI.DataManager.componentFragment
import com.app.chefbook.Data.DataManager
import com.app.chefbook.Model.ServiceModel.RequestModel.ChangePassword
import com.app.chefbook.Model.ServiceModel.RequestModel.ChangeProfile
import com.app.chefbook.R
import com.app.chefbook.Utilities.getInflateLayout
import com.app.chefbook.Utilities.isValidEmail
import kotlinx.android.synthetic.main.dialog_password_change.*
import kotlinx.android.synthetic.main.dialog_password_change.layout_Password
import kotlinx.android.synthetic.main.fragment_settings.*
import javax.inject.Inject

class SettingsFragment : Fragment() {

    @Inject
    lateinit var dataManager: DataManager
    private lateinit var viewModel: SettingsViewModel
    var userName: String = ""
    //var toolbar_gallery: Toolbar? = null
    private var repUserName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        componentFragment.inject(this)
        viewModel = ViewModelProviders.of(this, SettingsViewModelFactory(dataManager))
            .get(SettingsViewModel::class.java)

        arguments?.let {
            val safeArgs = SettingsFragmentArgs.fromBundle(it)
            userName = safeArgs.userName
        }

        activity?.actionBar?.title = userName
        //requireActivity().title = userName
        //toolbar_gallery = requireActivity().findViewById(R.id.toolbar_profile_content) as Toolbar
        //toolbar_gallery!!.removeAllViewsInLayout()
        //toolbar_gallery!!.title = userName
        viewModel.getProfileDetails()
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        var passwordChangeDialog: Dialog?
        val loadingDialog = SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE).setTitleText("Yükleniyor...")
        var btnSavePassword: Button? = null

        btnPasswordChange.setOnClickListener {
            passwordChangeDialog = Dialog(context!!)
            passwordChangeDialog!!.setContentView(R.layout.dialog_password_change)
            passwordChangeDialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            passwordChangeDialog!!.setCanceledOnTouchOutside(false)
            passwordChangeDialog!!.setTitle("Şifre Değiştir")
            passwordChangeDialog!!.show()

            btnSavePassword = passwordChangeDialog?.findViewById(R.id.btnSavePassword)
        }

        btnSaveSettings.setOnClickListener {

            layout_UserName.error = ""
            layout_FullName.error = ""
            layout_Biography.error = ""
            layout_Email.error = ""

            var changeState = true

            if (changeUserName.length() < 6 || changeUserName.length() > 16) {
                layout_UserName.error = "En az 6, en fazla 16 karakter olmalı"
                changeState = false
            }
            if (changeFullName.length() > 50) {
                layout_FullName.error = "En fazla 50 karakter olmalı"
                changeState = false
            }
            if (changeBiography.length() > 150) {
                layout_Biography.error = "En fazla 50 karakter olmalı"
            }
            if (changeEmail.length() > 50 || !isValidEmail(changeEmail.text.toString())) {
                layout_Email.error = "En fazla 50 karakterden oluşan geçerli bir mail adresi girin"
            }

            if (changeState) {

                loadingDialog.show()

                viewModel.changeProfileDetails(
                    ChangeProfile(
                        userName = changeUserName.text.toString(),
                        nameSurName = changeFullName.text.toString(),
                        biography = changeBiography.text.toString(),
                        mail = changeEmail.text.toString()
                    )
                )
                repUserName = changeUserName.text.toString()
            }
        }

        btnSavePassword?.setOnClickListener {
            Log.d("Button", "CLICK SAVE BUTTON")
            layout_Password.error = ""
            layout_PasswordAgain.error = ""

            var changeState = true

            if (oldPassword.length() < 8 || oldPassword.length() > 16) {
                layout_Password.error = "En az 8, en fazla 16 karakter olmalı"
                changeState = false
            }
            if (changePassword.length() < 8 || changePasswordAgain.length() > 16) {
                layout_Password.error = "En az 8, en fazla 16 karakter olmalı"
                changeState = false
            }
            if (changePasswordAgain.text.toString() != changePasswordAgain.text.toString()) {
                layout_PasswordAgain.error = "Şifre tekrarı uyuşmalıdır"
                changeState = false
            }

            if (changeState) {

                loadingDialog.show()
                viewModel.changePassword(
                    ChangePassword(
                        oldPassword = oldPassword.text.toString(),
                        password = changePassword.text.toString(),
                        verifiedPassword = changePasswordAgain.text.toString()
                    )
                )
            }
        }

        viewModel.changeProfileDetailsState.observe(this, Observer {

            loadingDialog.cancel()

            when (it) {
                "200" -> {
                    SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("Profil bilgileri güncellendi!")
                        .show()

                    requireActivity().title = repUserName
                }
                "301" -> {
                    SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Kullanıcı adı zaten mevcut!")
                        .show()
                }

                "302" -> {
                    SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Email zaten mevcut!")
                        .show()
                }
            }
        })

        viewModel.changePasswordState.observe(this, Observer {

            loadingDialog.cancel()

            when (it) {
                "200" -> {
                    SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("Şifre Değiştirildi!")
                        .show()
                }

                "301" -> {
                    SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Eski şifre yanlış!")
                        .show()
                }

                "302" -> {
                    SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Şifre tekrarı, yeni şifre ile uyuşmuyor!")
                        .show()
                }
            }
        })

        viewModel.profileDetails.observe(this, Observer {
            changeUserName.setText(it.userName)
            changeFullName.setText(it.nameSurName)
            changeBiography.setText(it.biography)
            changeEmail.setText(it.mail)
        })
    }
}
