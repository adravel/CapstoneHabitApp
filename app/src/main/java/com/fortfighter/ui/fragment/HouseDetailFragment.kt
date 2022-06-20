package com.fortfighter.ui.fragment

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.fortfighter.R
import com.fortfighter.adapter.ToolAdapter
import com.fortfighter.databinding.FragmentHouseDetailBinding
import com.fortfighter.model.House
import com.fortfighter.model.Tool
import com.fortfighter.ui.dialog.HouseRescueConfirmationDialogFragmentArgs
import com.fortfighter.util.Response
import com.fortfighter.viewmodel.HouseDetailViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior

class HouseDetailFragment: Fragment() {

    private var _binding: FragmentHouseDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var toolAdapter: ToolAdapter

    private lateinit var houseId: String
    private lateinit var houseName: String

    private lateinit var storeBottomSheetBehavior: BottomSheetBehavior<*>

    private val viewModel: HouseDetailViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentHouseDetailBinding.inflate(inflater, container, false)

        // Initialize house data using Safe Args provided by navigation component
        val args: HouseRescueConfirmationDialogFragmentArgs by navArgs()
        houseId = args.houseId
        houseName = args.houseName

        // Set toolbar title
        binding.toolbarLayout.toolbar.title = houseName

        // Initialize bottom sheet
        storeBottomSheetBehavior = BottomSheetBehavior.from(binding.shopBottomSheetCard)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve child ID from shared preference
        val sharedPref = requireActivity().getSharedPreferences(getString(R.string.role_pref_key), Context.MODE_PRIVATE)
        val childId = sharedPref.getString(getString(R.string.role_pref_child_id_key), "")!!

        // Set child and house document IDs
        viewModel.setChildId(childId)
        viewModel.setHouseId(houseId)

        // Set the adapter and layoutManager for tool list RecyclerView
        toolAdapter = ToolAdapter(mutableListOf(), false, childId)
        binding.toolListRecyclerView.apply {
            adapter = toolAdapter
            layoutManager = GridLayoutManager(context, 2)
        }

        // Fetch house detail, tool list, and child cash data from Firestore
        viewModel.getHouseFromFirebase()
        viewModel.getToolsForSaleFromFirebase()
        viewModel.getChildCashFromFirestore()

        // Observe house LiveData in SharedViewModel
        viewModel.house.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {}
                is Response.Success -> {
                    val house = response.data
                    val houseStaticData = house.getHouseStaticData()!!

                    // Display asset images and progress card data
                    displayHouseData(this, house)

                    // Get tool list data
                    val toolResponse = viewModel.tools.value
                    if (toolResponse is Response.Success) {
                        val tools = toolResponse.data
                        val houseStatus = house.status.toInt()

                        // Display tool list depending on House status
                        toolAdapter.updateList(tools, houseStatus)
                    }

                    // Display house rescue intro dialog if house status is 1,
                    // HP is still full, and the dialog has not been displayed yet
                    if (house.status.toInt() == 1
                        && house.hp.toInt() == houseStaticData.maxHp
                        && viewModel.showHouseRescueIntroDialog
                    ) {
                        viewModel.showHouseRescueIntroDialog = false
                        findNavController().navigate(R.id.houseRescueIntroDialogFragment)
                    }

                    // Display house care intro dialog if house status 2,
                    // CP is still 0, and the dialog has not been displayed yet
                    if (house.status.toInt() == 2
                        && house.cp.toInt() == 0
                        && viewModel.showHouseCareIntroDialog
                    ) {
                        viewModel.showHouseCareIntroDialog = false
                        findNavController().navigate(R.id.houseCareIntroDialogFragment)
                    }
                }
                is Response.Failure -> {
                    Log.e("HouseDetail", response.message)
                    Toast.makeText(context, getString(R.string.data_fetch_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Observe tools LiveData in SharedViewModel
        viewModel.tools.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {}
                is Response.Success -> {
                    val tools = response.data

                    // Get house data
                    val houseResponse = viewModel.house.value
                    if (houseResponse is Response.Success) {
                        val houseStatus = houseResponse.data.status.toInt()

                        // Display tool list depending on House status
                        toolAdapter.updateList(tools, houseStatus)
                    }
                }
                is Response.Failure -> {
                    Log.e("HouseDetail", response.message)
                    Toast.makeText(context, getString(R.string.data_fetch_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Observe child cash LiveData in SharedViewModel
        viewModel.childCash.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {}
                is Response.Success -> {
                    val cash = response.data
                    binding.cashText.text = getString(R.string.child_cash_placeholder, cash)
                }
                is Response.Failure -> {
                    Log.e("HouseDetail", response.message)
                    Toast.makeText(context, getString(R.string.data_fetch_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Observe toolPurchaseResponse LiveData in SharedViewModel
        // to determine whether tool purchase transaction is successful or not
        viewModel.toolPurchaseResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {}
                is Response.Success -> {
                    // Clear the LiveData so the code below will be executed only once
                    viewModel.toolPurchaseResultResponseHandled()

                    // Collapse bottom sheet
                    storeBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

                    // Play GIF depending on the tool type
                    // and update house and child cash data when animation has finished playing
                    val toolType = response.data
                    playToolAnimation(this, toolType)
                }
                is Response.Failure -> {
                    // Show error message as a toast
                    val message = when (response.message) {
                        "NOT_ENOUGH_CASH_ERROR" -> R.string.not_enough_cash_failure
                        "MAX_CLEAN_COUNT_REACHED" -> R.string.max_clean_count_reached
                        "MAX_REPAIR_COUNT_REACHED" -> R.string.max_repair_count_reached
                        else -> {
                            Log.e("HouseDetail", response.message)
                            R.string.request_failed
                        }
                    }
                    Toast.makeText(context, getString(message), Toast.LENGTH_LONG).show()
                }
            }
        }

        // Set back button onClickListener
        binding.toolbarLayout.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // Set bottom sheet behavior to flip arrow icon when bottom sheet is expanded
        storeBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> binding.arrowIconImage.scaleY = -1F
                    BottomSheetBehavior.STATE_COLLAPSED -> binding.arrowIconImage.scaleY = 1F
                    else -> {}
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        // Set arrow icon onClickListener to expand/collapse the bottom sheet
        binding.arrowIconImage.setOnClickListener {
            if (storeBottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                storeBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                storeBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /// Display asset images and progress card data
    private fun displayHouseData(fragment: Fragment, house: House) {
        val houseStaticData = house.getHouseStaticData()!!

        binding.apply {
            // Display house name data in the card
            houseNameText.text = houseStaticData.name

            // Change house, fort, and dirt asset images
            // and display HP/CP data depending on House status
            when (house.status.toInt()) {
                // User is destroying the fort
                1 -> {
                    // Display HP data
                    val hp = house.hp.toInt()
                    val maxHp = houseStaticData.maxHp
                    houseProgressText.text = getString(R.string.house_hp_placeholder, hp, maxHp)
                    houseProgressBar.max = maxHp
                    houseProgressBar.progress = hp
                    houseProgressBar.setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.state_error))
                    houseProgressBar.trackColor = Color.parseColor("#F8E0D1")

                    // Display asset images
                    if (hp >= maxHp * 0.75) {
                        // HP is more than or equal to 75% of Max HP
                        // House is damaged, fort is still intact
                        Glide.with(fragment).load(houseStaticData.houseDamagedImageResId).into(houseImage)
                        Glide.with(fragment).load(R.drawable.img_game_fort_intact).into(fortImage)
                    } else {
                        // HP is less than 75% of Max HP
                        // House and fort is damaged
                        Glide.with(fragment).load(houseStaticData.houseDamagedImageResId).into(houseImage)
                        Glide.with(fragment).load(R.drawable.img_game_fort_damaged).into(fortImage)
                    }

                    // Clear dirt imageView
                    Glide.with(fragment).clear(dirtImage)
                }
                // User is taking care of the house
                2 -> {
                    // Display CP data
                    val cp = house.cp.toInt()
                    val maxCp = houseStaticData.maxCP
                    houseProgressText.text = getString(R.string.house_cp_placeholder, cp, maxCp)
                    houseProgressBar.max = maxCp
                    houseProgressBar.progress = cp
                    houseProgressBar.setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.green))
                    houseProgressBar.trackColor = Color.parseColor("#ECFFF6")

                    // Display asset images
                    // Display dirt image depending on how many times "Broom" tool has been used
                    when (house.cleanCount.toInt()) {
                        // 2 dirt
                        0 -> Glide.with(fragment).load(R.drawable.img_game_dirt_both).into(dirtImage)
                        // 1 dirt
                        1 -> Glide.with(fragment).load(R.drawable.img_game_dirt_grass).into(dirtImage)
                        // No dirt
                        2 -> Glide.with(fragment).clear(dirtImage)
                    }

                    // Display house image depending on how many times "Hammer" tool has been used
                    if (house.repairCount.toInt() < 8) {
                        // House is still damaged
                        Glide.with(fragment).load(houseStaticData.houseDamagedImageResId).into(houseImage)
                    } else {
                        // House if intact
                        Glide.with(fragment).load(houseStaticData.houseIntactImageResId).into(houseImage)
                    }

                    // Clear fort imageView
                    Glide.with(fragment).clear(fortImage)
                }
            }
        }
    }

    // Play tool animation GIF
    private fun playToolAnimation(fragment: Fragment, toolType: Int) {
        val toolStaticData = Tool(type = toolType.toLong()).getToolStaticData()!!

        // Initialize the animation drawable resource ID and its target imageView
        val animationResId = toolStaticData.animationResId
        val imageView = if (toolStaticData.isCrushingTool) {
            binding.frontGifImage
        } else {
            binding.centerGifImage
        }

        Glide.with(fragment)
            .asGif()
            .load(animationResId)
            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
            .listener(object : RequestListener<GifDrawable>{
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<GifDrawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: GifDrawable?,
                    model: Any?,
                    target: Target<GifDrawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    // Play GIF only once
                    resource?.setLoopCount(1)

                    // Set listener to know when the animation is complete
                    resource?.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
                        override fun onAnimationEnd(drawable: Drawable?) {
                            super.onAnimationEnd(drawable)

                            // Fetch the data to update the Views
                            viewModel.getHouseFromFirebase()
                            viewModel.getChildCashFromFirestore()

                            // Clear the ImageView
                            Glide.with(fragment).clear(imageView)
                        }
                    })
                    return false
                }
            })
            .into(imageView)
    }
}