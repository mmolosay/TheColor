package com.ordolabs.feature_home.ui.fragment.colordata.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.ColorDataDetailsObtainFragmentBinding
import com.ordolabs.feature_home.ui.fragment.colordata.IColorThemed
import com.ordolabs.feature_home.ui.fragment.colordata.base.BaseColorDataFragment
import com.ordolabs.feature_home.ui.fragment.colordata.base.IColorDataFragment
import com.ordolabs.feature_home.viewmodel.colordata.details.ColorDetailsObtainViewModel
import com.ordolabs.thecolor.model.ColorDetailsPresentation
import com.ordolabs.thecolor.util.ColorUtil
import com.ordolabs.thecolor.util.InflaterUtil.cloneInViewContext
import com.ordolabs.thecolor.util.ext.by
import com.ordolabs.thecolor.util.ext.mediumAnimDuration
import com.ordolabs.thecolor.util.ext.setFragment
import com.ordolabs.thecolor.util.ext.showToast
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.net.UnknownHostException

/**
 * Fragment that obtains [color] data in `ViewModel`
 * and passes it to child 'display-data-only' Fragment.
 * Displays loading, failure and success obtaining states.
 */
class ColorDataDetailsObtainFragment :
    BaseColorDataFragment<ColorDetailsPresentation>(),
    IColorThemed {

    override val color: ColorUtil.Color?
        get() = (parentFragment as? IColorThemed)?.color

    private val binding: ColorDataDetailsObtainFragmentBinding by viewBinding(CreateMethod.BIND)
    private val colorDetailsObtainVM: ColorDetailsObtainViewModel by viewModel() // independent, brand new ViewModel

    private var dataFragment: IColorDataFragment<ColorDetailsPresentation>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        obtainColorData()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.cloneInViewContext(container)
            .inflate(R.layout.color_data_details_obtain_fragment, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        this.dataFragment = null
    }

    override fun collectViewModelsData() {
        collectColorDetails()
        collectCoroutineException()
    }

    override fun setViews() {
        setColorDataDetailsFragment()
        setRetryBtn()
    }

    // region Settings views

    private fun setColorDataDetailsFragment() {
        val fragment = ColorDataDetailsFragment.newInstance(colorDetails = null)
        setFragment(fragment)
        this.dataFragment = fragment
    }

    private fun setRetryBtn() {
        binding.noContent.retryBtn.setOnClickListener {
            obtainColorData()
        }
    }

    // endregion

    // region Toggling views

    private fun showContentView() {
        binding.defaultFragmentContainer.isVisible = true
        binding.contentShimmer.root.isVisible = false
        binding.noContent.root.isVisible = false
    }

    private fun showLoadingView() {
        binding.defaultFragmentContainer.isVisible = false
        binding.contentShimmer.root.isVisible = true
        binding.noContent.root.isVisible = false
    }

    private fun showNoContentView() {
        binding.defaultFragmentContainer.isVisible = false
        binding.contentShimmer.root.isVisible = false
        binding.noContent.root.isVisible = true
    }

    private fun animContentVisibility(visible: Boolean, instant: Boolean = false) {
        val content = binding.defaultFragmentContainer
        val translation = resources.getDimension(com.ordolabs.thecolor.R.dimen.offset_8)
        if (visible) content.translationY = translation
        val translationY = 0f to translation by visible
        val alpha = 1f to 0f by visible
        val duration = 0L to mediumAnimDuration by instant
        ViewCompat.animate(content)
            .translationY(translationY)
            .alpha(alpha)
            .setDuration(duration)
            .setInterpolator(FastOutSlowInInterpolator())
            .withStartAction {
                showContentView()
            }
            .withEndAction {
                content.isVisible = visible
            }
            .start()
    }

    // endregion

    // region Other

    private fun obtainColorData() {
        val color = color ?: return
        colorDetailsObtainVM.getColorDetails(color)
    }

    // endregion

    private fun collectColorDetails() =
        colorDetailsObtainVM.details.collectOnLifecycle { resource ->
            resource.fold(
                onEmpty = ::onColorDetailsEmpty,
                onLoading = ::onColorDetailsLoading,
                onSuccess = ::onColorDetailsSuccess,
                onFailure = ::onColorDetailsFailure
            )
        }

    @Suppress("UNUSED_PARAMETER")
    private fun onColorDetailsEmpty(previous: ColorDetailsPresentation?) {
        animContentVisibility(visible = false)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onColorDetailsLoading(previous: ColorDetailsPresentation?) {
        showLoadingView()
    }

    private fun onColorDetailsSuccess(data: ColorDetailsPresentation) {
        populateViews(data)
        animContentVisibility(visible = true)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onColorDetailsFailure(
        previous: ColorDetailsPresentation?,
        payload: Any?,
        error: Throwable
    ) {
        when (error) {
            is UnknownHostException -> showNoContentView()
            else -> showToast(error.localizedMessage)
        }
    }

    private fun collectCoroutineException() =
        colorDetailsObtainVM.coroutineExceptionMessageRes.collectOnLifecycle { stringRes ->
            showToast(stringRes)
        }

    // region IColorDataFragment

    // delegates
    override fun populateViews(data: ColorDetailsPresentation) {
        dataFragment?.populateViews(data)
    }

    // endregion

    companion object {
        fun newInstance() =
            ColorDataDetailsObtainFragment()
    }
}