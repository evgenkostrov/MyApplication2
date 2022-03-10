package com.example.myapplication

class ad {
}
package com.narcissus.marketplace.ui.catalog

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.narcissus.marketplace.R
import com.narcissus.marketplace.databinding.FragmentCatalogBinding
import com.narcissus.marketplace.databinding.FragmentFilterDialogBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class CatalogFragment : Fragment(R.layout.fragment_catalog) {
    private var _binding: FragmentCatalogBinding? = null
    private val binding get() = _binding!!

    private val catalogViewModel: CatalogViewModel by viewModels()
    private val catalogAdapter = CatalogAdapter()
    private val searchHistoryViewModel: SearchHistoryViewModel by viewModels()
    private val searchHistoryAdapterAdapter = SearchHistoryAdapter()

    lateinit var supportFragmentManager: FragmentManager

    override fun onAttach(context: Context) {
        super.onAttach(context)
        supportFragmentManager = requireActivity().supportFragmentManager
        val navController =
            (supportFragmentManager?.findFragmentById(R.id.nav_host_fragment) as NavHostFragment)
                .navController

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCatalogBinding.bind(view)

        setDepartmentList()
        setCatalogRecyclerView()
        setSearchListeners()
        setSearchHistoryList()
        setSearchHistoryRecyclerView()

    }

    private fun setSearchListeners() {
        binding.searchLayout.cvFilter.setOnClickListener {
            BottomSheetFragment().apply {
                show(supportFragmentManager, tag)
            }
        }
        binding.searchLayout.searchView.setOnFocusChangeListener { view, hasFocus ->
            if (view.hasFocus()) {
                binding.searchLayout.rvSearchHistory.visibility = View.VISIBLE
            } else {
                binding.searchLayout.rvSearchHistory.visibility = View.GONE
            }
        }
    }

    private fun setDepartmentList() {
        lifecycleScope.launch {
            flow {
                emit(catalogViewModel.getDepartmentList())
            }.flowOn(Dispatchers.IO)
                .collect {
                    catalogAdapter.departmentList = it
                }
        }
    }

    private fun setCatalogRecyclerView() {
        binding.rvDepartment.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(
                requireContext(),
                2
            )
            adapter = catalogAdapter
        }
    }

    private fun setSearchHistoryList() {
        lifecycleScope.launch {
            flow {
                emit(searchHistoryViewModel.getSearchHistoryList())
            }.flowOn(Dispatchers.IO)
                .collect {
                    searchHistoryAdapterAdapter.searchHistoryList = it
                }
        }
    }

    private fun setSearchHistoryRecyclerView() {
        binding.searchLayout.rvSearchHistory.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(
                requireContext()
            )
            adapter = searchHistoryAdapterAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


package com.narcissus.marketplace.ui.catalog


import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import coil.transform.CircleCropTransformation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.narcissus.marketplace.R
import com.narcissus.marketplace.databinding.ItemDepartmentCardBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CatalogAdapter: RecyclerView.Adapter<CatalogAdapter.DepartmentViewHolder>() {

    var departmentList: List<DepartmentModel> = emptyList()

    inner class DepartmentViewHolder(
        val binding: ItemDepartmentCardBinding,
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DepartmentViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemDepartmentCardBinding.inflate(inflater, parent, false)
        return DepartmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DepartmentViewHolder, position: Int) {
        val department = (departmentList[position])

        holder.binding.name.text = department.name

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val departmentStorageReference = Firebase.storage.reference
//                val firebaseAuth = FirebaseAuth.getInstance()
//                val user = firebaseAuth.currentUser
                val departmentImagePath = departmentStorageReference.child(
                    "departmentImage/${department.image}"
                )
                val maxDownloadSize = 1L * 1024 * 1024
                val bytes = departmentImagePath.getBytes(maxDownloadSize).await()
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                withContext(Dispatchers.Main) {
                    holder.binding.image.load(bitmap) {
//                        crossfade(true)
//                        crossfade(500)
//                        placeholder(R.drawable.ic_hint)
                        transformations(CoilGradientTransformation())
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {

                }
            }
        }
    }

    override fun getItemCount(): Int {
        return departmentList.size
    }
}

data class DepartmentModel(
    val name:String,
    val image:String
)





package com.narcissus.marketplace.ui.catalog

import androidx.lifecycle.ViewModel
import com.narcissus.marketplace.R

class CatalogViewModel:ViewModel() {

    fun getDepartmentList(): List<DepartmentModel>{
        return listOf(
            DepartmentModel(
                name = "Gadgets",
                image = "gadgets.jpg"
            ),
            DepartmentModel(
                name = "Appliance",
                image ="appliance.jpg"
            ),
            DepartmentModel(
                name = "Home & Living",
                image = "homeandliving.jpg"
            ),
            DepartmentModel(
                name = "School Supplies",
                image = "schoolsupplies.jpg"
            ),
            DepartmentModel(
                name = "Health & Beauty",
                image = "healthandbeauty.jpg"
            ),
            DepartmentModel(
                name = "For Babies",
                image = "babies.jpg"
            ),
            DepartmentModel(
                name = "Groceries",
                image = "groceries.jpg"
            ),
            DepartmentModel(
                name = "For Pets",
                image = "pets.jpg"
            ),
            DepartmentModel(
                name = "Fashion Women",
                image = "fashionwomen.jpg"
            ),
            DepartmentModel(
                name = "Fashion Men",
                image = "fashionmen.jpg"
            ),
            DepartmentModel(
                name = "Accessories",
                image = "accessories.jpg"
            ),
            DepartmentModel(
                name = "Sports & Lifestyle",
                image = "sport.png"
            ),
            DepartmentModel(
                name = "Toys & Collectibles",
                image = "toys.jpg"
            ),
            DepartmentModel(
                name = "Automotive",
                image = "automotive.jpg"
            )


        )
    }
}

package com.narcissus.marketplace.ui.catalog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.narcissus.marketplace.R
import com.narcissus.marketplace.databinding.FragmentBottomSheetBinding


class BottomSheetFragment : BottomSheetDialogFragment() {

    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    private var mBinding: FragmentBottomSheetBinding? = null

    private val binding get() = mBinding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheet = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        val view = View.inflate(context, R.layout.fragment_bottom_sheet, null) as View
        bottomSheet.setContentView(view);
        val bottomSheetLayout =
            bottomSheet.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout)
        }

        bottomSheetBehavior?.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO;
        bottomSheetBehavior?.isFitToContents = false
        bottomSheetBehavior?.addBottomSheetCallback(mBottomSheetBehaviorCallback)
        return bottomSheet
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivBack.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
    }


    private val mBottomSheetBehaviorCallback: BottomSheetCallback = object : BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
//            if (BottomSheetBehavior.STATE_EXPANDED == newState) {
//                showView(mBinding!!.appBarLayout, getActionBarSize());
//                hideAppBar(mBinding!!.ivServiceTitle);
//                hideAppBar(mBinding!!.viewLine);
//                dismiss();
//            }
            if (BottomSheetBehavior.STATE_HALF_EXPANDED == newState) {
                binding.animationView.visibility = View.VISIBLE

            }

            if (BottomSheetBehavior.STATE_COLLAPSED == newState) {

            }

            if (BottomSheetBehavior.STATE_HIDDEN == newState) {
                dismiss();
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }
}



<?xml version="1.0" encoding="utf-8"?>

<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
xmlns:app="http://schemas.android.com/apk/res-auto"
xmlns:tools="http://schemas.android.com/tools"
android:layout_width="match_parent"
android:layout_height="wrap_content"
>

<SearchView
android:id="@+id/searchView"
android:layout_width="0dp"
android:layout_height="56dp"
android:background="@drawable/searchview_background"
android:iconifiedByDefault="false"
android:paddingVertical="10dp"
android:queryBackground="@android:color/transparent"
android:queryHint="Search"
app:layout_constraintTop_toTopOf="parent"
app:layout_constraintStart_toStartOf="parent"
app:layout_constraintEnd_toStartOf="@id/cvFilter"
/>

<com.google.android.material.card.MaterialCardView

android:id="@+id/cvFilter"
android:layout_width="50dp"
android:layout_height="56dp"
android:layout_marginStart="16dp"
app:cardBackgroundColor="#EBEFFF"
app:cardCornerRadius="16dp"
app:cardElevation="1dp"
app:layout_constraintEnd_toEndOf="parent"
app:layout_constraintStart_toEndOf="@id/searchView"
app:layout_constraintTop_toTopOf="parent"
app:strokeColor="#EBEFFF"
app:strokeWidth="0.5dp">

<androidx.appcompat.widget.AppCompatImageView
android:id="@+id/ivFilter"
android:layout_width="wrap_content"
android:layout_height="wrap_content"
android:layout_gravity="center"
android:src="@drawable/ic_tune" />

</com.google.android.material.card.MaterialCardView>

<androidx.recyclerview.widget.RecyclerView
android:id="@+id/rvSearchHistory"
android:layout_width="match_parent"
android:layout_height="0dp"
android:layout_marginTop="8dp"
android:visibility="gone"
app:layout_constraintStart_toStartOf="parent"
app:layout_constraintTop_toBottomOf="@id/searchView" />

</androidx.constraintlayout.widget.ConstraintLayout>




<com.google.android.material.card.MaterialCardView
xmlns:android="http://schemas.android.com/apk/res/android"
xmlns:app="http://schemas.android.com/apk/res-auto"
xmlns:tools="http://schemas.android.com/tools"
android:id="@+id/cardView"
android:layout_width="match_parent"
android:layout_height="wrap_content"
android:layout_marginHorizontal="10dp"
android:layout_marginBottom="20dp"

app:cardBackgroundColor="@android:color/white"
app:cardCornerRadius="10dp"
app:cardElevation="10dp"
app:strokeWidth="0.5dp">

<androidx.constraintlayout.widget.ConstraintLayout
android:layout_width="match_parent"
android:layout_height="wrap_content"
tools:ignore="UnusedAttribute">

<androidx.appcompat.widget.AppCompatImageView
android:id="@+id/image"
android:layout_width="0dp"
android:layout_height="150dp"
android:scaleType="centerCrop"
app:layout_constraintBottom_toBottomOf="parent"
app:layout_constraintEnd_toEndOf="parent"
app:layout_constraintStart_toStartOf="parent"
app:layout_constraintTop_toTopOf="parent"
tools:src="@tools:sample/avatars"/>

<com.google.android.material.textview.MaterialTextView
android:id="@+id/name"
android:layout_width="0dp"
android:layout_height="wrap_content"
android:shadowColor="@color/black"
android:shadowDx="2"
android:shadowDy="2"
android:shadowRadius="2"
android:gravity="center"
android:maxLines="1"
android:layout_marginStart="10dp"
android:layout_marginBottom="5dp"
android:fontFamily="@font/montserrat_semibold"
android:textColor="@color/white"
android:textSize="18sp"
android:textStyle="normal"
app:layout_constraintStart_toStartOf="parent"
app:layout_constraintBottom_toBottomOf="parent"
tools:text="Card"/>

</androidx.constraintlayout.widget.ConstraintLayout>

</com.google.android.material.card.MaterialCardView>


holder.binding.image.load(it) {
    memoryCachePolicy(CachePolicy.READ_ONLY)
//                        crossfade(true)
//                        crossfade(500)
//                        placeholder(R.drawable.ic_hint)
    size(300,300)
    transformations(CoilGradientTransformation())
//                        okhttp3.OkHttpClient.Builder()
//                            .cache()
}
