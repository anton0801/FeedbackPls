package com.feedback.android.app.presentation.ui.fragments.lk

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.feedback.android.app.R
import com.feedback.android.app.common.*
import com.feedback.android.app.common.extensions.downloadImageAsBitmap
import com.feedback.android.app.databinding.FragmentProfileBinding
import com.feedback.android.app.domain.model.TariffModel
import com.feedback.android.app.presentation.MainActivity
import com.feedback.android.app.presentation.ui.views.LabeledInput
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private val binding: FragmentProfileBinding by viewBinding(FragmentProfileBinding::inflate)
    private val vm: ProfileViewModel by viewModels()

    private val tabContainers by lazy {
        listOf(
            binding.editProfileContainer,
            binding.sendLinkContainer,
            binding.changePinContainer,
            binding.tariffsContainer
        )
    }

    private lateinit var rvAdapter: RVAdapter<TariffModel>
    private val onItemBtnPayExtendClickListener: (TariffModel, Boolean) -> Unit =
        { item, isExtand ->
            vm.onEvent(ProfileViewModel.UiEvent.PayTariff(Utils.userData!!.id, item.id))
        }

    private lateinit var getPhotoResultLauncher: ActivityResultLauncher<String>

    override fun onAttach(context: Context) {
        super.onAttach(context)

        getPhotoResultLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                val originalBitmap =
                    MediaStore.Images.Media.getBitmap(activity?.contentResolver, uri)
                if (originalBitmap != null) {
                    val rotatedImg = modifyOrientation(originalBitmap, uri)
                    if (rotatedImg != null) {
                        val resizedBitmap = resizeImg(
                            rotatedImg,
                            rotatedImg.width,
                            rotatedImg.height
                        )
                        val dir = File(context.cacheDir, "user_avatar")
                        if (!dir.exists())
                            dir.mkdirs()
                        val file = File(dir, "tmp_avatar.png")
                        file.createNewFile()
                        file.outputStream().use { out ->
                            resizedBitmap?.compress(Bitmap.CompressFormat.PNG, 100, out)
                            out.flush()
                            vm.currentUserAvatar = file
                            binding.noHaveUserAvatar.isVisible = false
                            binding.avatar.post {
                                binding.avatar.setImageBitmap(resizedBitmap)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun modifyOrientation(bitmap: Bitmap, uri: Uri): Bitmap? {
        return try {
            val inputStream = context?.contentResolver?.openInputStream(uri)
            if (inputStream != null) {
                val ei = ExifInterface(inputStream)
                val orientation: Int =
                    ei.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotate(bitmap, 90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotate(bitmap, 180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotate(bitmap, 270f)
                    ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flip(
                        bitmap,
                        horizontal = true,
                        vertical = false
                    )
                    ExifInterface.ORIENTATION_FLIP_VERTICAL -> flip(
                        bitmap,
                        horizontal = false,
                        vertical = true
                    )
                    else -> bitmap
                }
            } else {
                bitmap
            }
        } catch (e: java.lang.Exception) {
            bitmap
        }
    }

    private fun rotate(bitmap: Bitmap, degrees: Float): Bitmap? {
        val matrix = Matrix()
        matrix.setRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun flip(bitmap: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap? {
        val matrix = Matrix()
        matrix.setScale(if (horizontal) -1f else 1f, if (vertical) -1f else 1f)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeToVmFieldsUpdates()

        binding.createProfile.isVisible =
            Utils.userData?.name == null && Utils.userData?.email == null &&
                    Utils.userData?.about == null && Utils.userData?.birthday == null
        binding.profileContainer.isVisible =
            Utils.userData?.name != null && Utils.userData?.email != null &&
                    Utils.userData?.about != null && Utils.userData?.birthday != null

        rvAdapter = RVAdapter { parent, _ ->
            TariffViewHolder(
                layoutInflater.inflate(R.layout.tariff_list_item, parent, false),
                Utils.userData?.tariffId ?: -1,
                Utils.userData?.payedToDate?.split(" ")?.get(0)?.replace("-", "/"),
                onItemBtnPayExtendClickListener
            )
        }
        binding.tariffsRv.apply {
            adapter = rvAdapter
            layoutManager = LinearLayoutManager(context)
        }

        binding.createProfile.setOnClickListener { vm.onEvent(ProfileViewModel.UiEvent.CreateProfile) }

        applyTabs()

        binding.sendBtn.setOnClickListener {
            vm.onEvent(
                ProfileViewModel.UiEvent.SendData(
                    firstName = binding.firstName.getInputValue(),
                    surname = binding.surname.getInputValue(),
                    lastName = binding.lastName.getInputValue(),
                    birthday = binding.birthday.getInputValue(),
                    aboutTitle = binding.titleInput.text.toString(),
                    aboutText = binding.aboutInput.text.toString(),
                    whatsappAvailable = binding.haveWhatsappCheckbox.isChecked,
                    profession = binding.profession.getInputValue(),
                    phone = Utils.userData?.phone ?: "",
                    website = binding.myLinkWebsite.getInputValue(),
                    email = binding.email.getInputValue()
                )
            )
        }
        binding.myUrl.setOnClickListener {
            vm.onEvent(ProfileViewModel.UiEvent.OpenLink(binding.myUrl.text.toString()))
        }
        binding.myLinkWebsite.setOnClickListener {
            vm.onEvent(ProfileViewModel.UiEvent.OpenLink(binding.myUrl.text.toString()))
        }
        binding.saveNewPinBtn.setOnClickListener {
            vm.onEvent(
                ProfileViewModel.UiEvent.ChangePinCode(
                    phone = Utils.userData?.phone ?: "",
                    newPinCode = binding.newPinInput.text.toString()
                )
            )
        }
        binding.avatar.setOnClickListener {
            vm.onEvent(ProfileViewModel.UiEvent.AttachPhoto)
        }
    }

    override fun onStart() {
        super.onStart()
        requestNeededPermissions()
        if (!vm.isUserDataSet) {
            setUserData()
        }
        vm.getTariffs()
        if (!vm.hasOpenedCertainTabInOnStart) {
            activity?.let {
                (it as MainActivity).needSelectAnyCertainTab()?.let { tabId ->
                    vm.onEvent(ProfileViewModel.UiEvent.SelectTab(tabId))
                    vm.hasOpenedCertainTabInOnStart = true
                }
            }
        }
    }

    private fun requestNeededPermissions() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                124
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 124) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                requestNeededPermissions()
                showSnackbar("Вы должны дать разрешение для использования приложения")
            }
        }
    }

    private fun attachPhoto() {
        getPhotoResultLauncher.launch("image/*")
    }

    private fun resizeImg(bitmap: Bitmap, width: Int, height: Int): Bitmap? {
        val widthToSave = 500.0
        val dimension = width.toDouble() / widthToSave
        val heightToSave = height.toDouble() / dimension
        return Bitmap.createScaledBitmap(bitmap, widthToSave.toInt(), heightToSave.toInt(), true)
    }

    private fun subscribeToVmFieldsUpdates() {
        lifecycleScope.launchWhenStarted {
            vm.eventFlow.collectLatest { event ->
                event?.getContentIfNotHandled()?.let { uiEvent ->
                    when (uiEvent) {
                        is ProfileViewModel.UiEvent.ShowErrorLabel -> {
                            showErrorLabel(uiEvent.fieldsWithError)
                        }
                        is ProfileViewModel.UiEvent.ShowSnackbar -> {
                            showSnackbar(uiEvent.message)
                        }
                        is ProfileViewModel.UiEvent.SelectTab -> {
                            setSelectedTab(
                                when (uiEvent.tabId) {
                                    binding.editProfileTab.id -> binding.editProfileTab
                                    binding.sendLink.id -> binding.sendLink
                                    binding.changePin.id -> binding.changePin
                                    binding.tariffs.id -> binding.tariffs
                                    else -> null
                                }
                            )
                        }
                        is ProfileViewModel.UiEvent.CreateProfile -> {
                            binding.createProfile.isVisible = false
                            binding.profileContainer.isVisible = true
                            vm.onEvent(ProfileViewModel.UiEvent.SelectTab(binding.editProfileTab.id))
                        }
                        is ProfileViewModel.UiEvent.OpenLink -> {
                            val intent = Intent(uiEvent.action, Uri.parse(uiEvent.link)).apply {
                                if (uiEvent.action != Intent.ACTION_VIEW) {
                                    val userData = Utils.userData
                                    val userDataToShare =
                                        "${userData?.name} ${userData?.profession}"
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "${uiEvent.link}\n\n$userDataToShare"
                                    )

                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    vm.isUserDataSet = false
                                }
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            try {
                                startActivity(
                                    if (uiEvent.action == Intent.ACTION_VIEW)
                                        intent
                                    else
                                        Intent.createChooser(intent, "Выберите приложение")
                                )
                            } catch (e: ActivityNotFoundException) {
                                showSnackbar("Не удалось найти приложение для открытия")
                            }
                        }
                        is ProfileViewModel.UiEvent.AttachPhoto -> attachPhoto()
                        is ProfileViewModel.UiEvent.OpenWebView -> {
                            val action =
                                ProfileFragmentDirections.actionProfileFragmentToWebViewFragment(
                                    uiEvent.url, Utils.userData!!.id, uiEvent.tariffId
                                )
                            findNavController().navigate(action)
                        }
                    }
                }
            }
        }

        vm.sendUserDataLiveData.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { state ->
                binding.progressBar.isVisible = state.isLoading
                if (!state.isLoading) {
                    state.error?.let { errors ->
                        val errorsList = errors.split(", ")
                        if (errorsList.isNotEmpty())
                            vm.onEvent(ProfileViewModel.UiEvent.ShowErrorLabel(
                                try {
                                    errorsList.map { it.toInt() }
                                } catch (e: Exception) {
                                    listOf()
                                }
                            ))
                        else
                            vm.onEvent(ProfileViewModel.UiEvent.ShowSnackbar("Что-то пошло не так"))
                    }

                    state.data?.let { data ->
                        vm.onEvent(ProfileViewModel.UiEvent.SelectTab(null))
                        vm.onEvent(ProfileViewModel.UiEvent.ShowErrorLabel(listOf()))
                        binding.myUrl.isVisible = true
                        binding.myUrl.text =
                            "https://оставьте-мне-отзыв.рф/id${Utils.userData?.id}"
                        binding.myLinkWebsite.setInputValueActive(true)
                        vm.onEvent(ProfileViewModel.UiEvent.ShowSnackbar(data.data.toString()))
                        if (binding.errorLabel.isVisible)
                            binding.errorLabel.isVisible = false
                    }
                }
            }
        }

        vm.changePinCodeState.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { state ->
                binding.progressBar.isVisible = state.isLoading
                if (!state.isLoading) {
                    state.error?.let { error ->
                        vm.onEvent(ProfileViewModel.UiEvent.ShowSnackbar(error))
                    }
                    state.data?.let { data ->
                        binding.currentPin.text = binding.newPinInput.text.toString()
                        binding.newPinInput.setText("")
                        vm.onEvent(ProfileViewModel.UiEvent.ShowSnackbar(data.data.toString()))
                    }
                }
            }
        }

        vm.tariffs.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { state ->
                binding.progressBar.isVisible = state.isLoading
                if (!state.isLoading) {
                    state.error?.let { error ->
                        vm.onEvent(ProfileViewModel.UiEvent.ShowSnackbar(error))
                    }
                    state.data?.let { data ->
                        rvAdapter.setItems(data)
                    }
                }
            }
        }

        vm.payTariff.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { state ->
                binding.progressBar.isVisible = state.isLoading
                if (!state.isLoading) {
                    state.error?.let { error ->
                        vm.onEvent(ProfileViewModel.UiEvent.ShowSnackbar(error))
                    }
                    state.data?.let { data ->
                        vm.onEvent(
                            ProfileViewModel.UiEvent.OpenWebView(
                                data.confirmationUrl,
                                vm.selectedTariffIdForPay!!
                            )
                        )
                    }
                }
            }
        }
    }

    private var prevSelectedTab: AppCompatTextView? = null

    private fun applyTabs() {
        vm.onEvent(ProfileViewModel.UiEvent.SelectTab(null))
        setClickListenerToTab(binding.editProfileTab)
        setClickListenerToTab(binding.sendLink) {
            vm.onEvent(
                ProfileViewModel.UiEvent.OpenLink(
                    binding.myUrl.text.toString(),
                    Intent.ACTION_SEND
                )
            )
        }
        setClickListenerToTab(binding.changePin) {
            binding.newPinInput.requestFocus()
        }
        setClickListenerToTab(binding.tariffs) {
            Log.d("LOG_D", "applyTabs: tariffs_click")
        }
    }

    private fun setSelectedTab(tab: AppCompatTextView?) {
        val activeTypeface = ResourcesCompat.getFont(requireContext(), R.font.sansation_regular)
        val disabledTypeface = ResourcesCompat.getFont(requireContext(), R.font.sansation_light)
        Log.d("LOG_D_D_D", "setSelectedTab: ${tab?.text}, ${tab?.tag.toString()}")
        if (tab == null) {
            prevSelectedTab?.let {
                tabContainers[it.tag.toString().toInt()].isVisible = false
                it.typeface = disabledTypeface
            }
            return
        }

        prevSelectedTab?.let {
            tabContainers[it.tag.toString().toInt()].isVisible = false
            it.typeface = disabledTypeface
        }
        tabContainers[tab.tag.toString().toInt()].isVisible = true
        tab.typeface = activeTypeface
        prevSelectedTab = tab
    }

    private fun setClickListenerToTab(
        tab: AppCompatTextView,
        onClickCallback: (() -> Unit)? = null
    ) {
        tab.setOnClickListener {
            vm.onEvent(ProfileViewModel.UiEvent.SelectTab(tab.id))
            onClickCallback?.invoke()
        }
    }

    private var prevErrorLabels = listOf<Int>()

    private fun showErrorLabel(errorLabels: List<Int>) {
        binding.errorLabel.isVisible = true
        binding.scrollView.post {
            binding.scrollView.fullScroll(View.FOCUS_DOWN)
        }

        if (prevErrorLabels.isNotEmpty()) {
            prevErrorLabels.forEach { id ->
                val view = binding.root.findViewById<View>(id)
                when (view) {
                    is LabeledInput -> view.enableErrorVisibility(false)
                    is AppCompatImageView -> {
                        binding.avatar.setBackgroundResource(R.drawable.profile_avatar_bg)
                        binding.noHaveUserAvatar.setTextColor(resources.getColor(R.color.black))
                    }
                    is AppCompatEditText -> {
                        if (id == R.id.about_input) {
                            binding.aboutInput.setBackgroundResource(R.drawable.error_border_bg)
                        } else {
                            binding.titleInput.setBackgroundResource(R.drawable.error_bottom_input_underline)
                        }
                    }
                }
            }
            binding.sendBtn.setBackgroundResource(R.drawable.active_btn_state)
            binding.sendBtn.setTextColor(resources.getColor(R.color.black))
        }

        if (errorLabels.isNotEmpty()) {
            errorLabels.forEach { id ->
                val view = binding.root.findViewById<View>(id)
                when (view) {
                    is LabeledInput -> view.enableErrorVisibility(true)
                    is AppCompatImageView -> {
                        binding.avatar.setBackgroundResource(R.drawable.error_profile_avatar_bg)
                        binding.noHaveUserAvatar.setTextColor(resources.getColor(R.color.colorError))
                    }
                    is AppCompatEditText -> {
                        if (id == R.id.about_input) {
                            binding.aboutInput.setBackgroundResource(R.drawable.error_border_bg)
                        } else {
                            binding.titleInput.setBackgroundResource(R.drawable.error_bottom_input_underline)
                        }
                    }
                }
            }
            binding.sendBtn.setBackgroundResource(R.drawable.error_btn_state)
            binding.sendBtn.setTextColor(resources.getColor(R.color.colorError))
        }

        prevErrorLabels = errorLabels
    }

    private fun setUserData() {
        Log.d("LOG_D_D_D", "setUserData: ${Utils.userData}")
        Utils.userData?.let { userModel ->
            binding.myUrl.isVisible = userModel.isPublished != 0
            binding.myLinkWebsite.setInputValueActive(userModel.isPublished != 0)

            if (userModel.name != null) {
                val nameParts = userModel.name.split(" ")
                val firstName = nameParts[1]
                val surname = nameParts[0]
                val lastName = nameParts[2]
                binding.firstName.setInputValue(firstName)
                binding.lastName.setInputValue(lastName)
                binding.surname.setInputValue(surname)
            }

            val lastTagBStartIndex = userModel.about?.indexOf("</b>") ?: -1
            val aboutTitle = userModel.about?.substring(3, lastTagBStartIndex)
            val aboutText =
                userModel.about?.substring(
                    lastTagBStartIndex + 10,
                    userModel.about.length ?: -1
                )
            val currentPin = Base64.decode(userModel.pinCode, Base64.DEFAULT)
            binding.currentPin.text = String(currentPin, Charsets.UTF_8)

            binding.titleInput.setText(aboutTitle)
            binding.aboutInput.setText(
                HtmlCompat.fromHtml(
                    aboutText ?: "",
                    HtmlCompat.FROM_HTML_MODE_COMPACT
                )
            )

            binding.myLinkWebsite.setInputValue(
                userModel.website ?: "https://оставьте-мне-отзыв.рф/id${Utils.userData?.id}"
            )
            binding.myUrl.text = "https://оставьте-мне-отзыв.рф/id${userModel.id}"

            binding.profession.setInputValue(userModel.profession ?: "")
            binding.phone.setInputValue(userModel.phone.toString())
            binding.haveWhatsappCheckbox.isChecked = userModel.whatsappAvailable == 1
            binding.email.setInputValue(userModel.email ?: "")
            if (userModel.birthday != null) {
                binding.birthday.setInputValue(userModel.birthday)
            }

            binding.notActivatedAccountContainer.isVisible = userModel.isAccountPayed == 0
            if (userModel.isAccountPayed != 0) {
                when {
                    userModel.tariffName.equals("бессрочный", true) -> {
                        binding.selectTariffLabel.isVisible = false
                        binding.tariffExpirationDate.isVisible = false
                        binding.tariffsRv.isVisible = false
                        binding.tariffName.text = getString(R.string.endless_tariff_active_text)
                    }
                    userModel.tariffName.equals("Акционный", true) -> {
                        binding.tariffDescription.isVisible = true
                        binding.selectTariffLabel.isVisible = false
                        binding.tariffExpirationDate.isVisible = false

                        binding.tariffName.text =
                            getString(R.string.selected_tariff_text, userModel.tariffName)
                        binding.tariffDescription.text = HtmlCompat.fromHtml(
                            userModel.tariffDescription.toString(),
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                    }
                    else -> {
                        binding.tariffName.text =
                            getString(R.string.selected_tariff_text, userModel.tariffName)
                        binding.tariffExpirationDate.text =
                            getString(
                                R.string.selected_tariff_date_expiration,
                                userModel.payedToDate?.split(" ")?.get(0)?.replace("-", "/")
                            )
                    }
                }
                binding.activatedAccountContainer.isVisible = true
            }

            // find cache avatar if not found download it in cache directory
            if (vm.currentUserAvatar != null) {
                binding.avatar.post {
                    binding.avatar.setImageURI(vm.currentUserAvatar!!.absolutePath.toUri())
                }
                binding.noHaveUserAvatar.isVisible = false
            } else if (userModel.avatar != null) {
                binding.avatar.post {
                    downloadImageAsBitmap(requireContext(), userModel.avatar) { bitmap ->
                        binding.avatar.setImageBitmap(bitmap)
                    }
                }
                binding.noHaveUserAvatar.isVisible = false

                findCacheAvatar { avatar ->
                    vm.currentUserAvatar = avatar
                    if (avatar == null) {
                        val dir = File(context?.cacheDir, "user_avatar")
                        if (!dir.exists())
                            dir.mkdirs()
                        // clear directory
                        if (dir.listFiles() != null)
                            dir.listFiles().forEach { it.delete() }

                        val avatarParts = userModel.avatar.split("/")
                        val fileNameWithExt = avatarParts[avatarParts.size - 1]

                        val file = File(dir, fileNameWithExt)
                        file.createNewFile()

                        lifecycleScope.launch(Dispatchers.IO) {
                            downloadFileFromServer(
                                file, userModel.avatar.toString()
                            ).collect { file ->
                                vm.currentUserAvatar = file
                            }
                        }
                    }
                }
            }
            vm.isUserDataSet = true
        }
    }

    private fun findCacheAvatar(found: (File?) -> Unit) {
        val dir = File(context?.cacheDir, "user_avatar")
        if (dir.exists()) {
            dir.listFiles().forEach { file ->
                found(file)
            }
        }
        found(null)
    }

}