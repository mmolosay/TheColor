package com.ordolabs.feature_home.ui.fragment.colordata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.ColorDataObtainFragmentBinding
import com.ordolabs.feature_home.ui.fragment.colordata.base.BaseColorDataFragment
import com.ordolabs.feature_home.ui.fragment.colordata.base.IColorDataFragment
import com.ordolabs.thecolor.model.color.Color
import com.ordolabs.thecolor.util.InflaterUtil.cloneInViewContext
import com.ordolabs.thecolor.util.ext.by
import com.ordolabs.thecolor.util.ext.mediumAnimDuration
import com.ordolabs.thecolor.util.ext.setFragment
import com.ordolabs.thecolor.util.ext.showToast
import com.ordolabs.thecolor.util.struct.Resource
import kotlinx.coroutines.flow.Flow
import java.net.UnknownHostException

/**
 * Obtains [color] data of type [D] and passes it to child 'display-data-only' Fragment.
 * Displays loading, failure and success obtaining states.
 */
abstract class ColorDataObtainFragment<D> :
    BaseColorDataFragment<D>(),
    IColorThemed {

    override val color: Color?
        get() = (parentFragment as? IColorThemed)?.color

    private val binding: ColorDataObtainFragmentBinding by viewBinding(CreateMethod.BIND)

    private var dataFragment: IColorDataFragment<D>? = null
    private var wasOnResumeCalled: Boolean = false

    // region Abstract

    abstract fun getColorDataFlow(): Flow<Resource<D>>
    abstract fun obtainColorData()
    abstract fun makeColorDataFragment(): BaseColorDataFragment<D>
    abstract fun makeContentShimmerFragment(): Fragment

    // endregion

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.cloneInViewContext(container)
            .inflate(R.layout.color_data_obtain_fragment, container, false)
    }

    override fun onResume() {
        super.onResume()
        if (!wasOnResumeCalled) {
            obtainColorData()
            this.wasOnResumeCalled = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        this.dataFragment = null
    }

    @CallSuper
    override fun collectViewModelsData() {
        collectColorData()
    }

    override fun setViews() {
        setColorDataFragment()
        setContentShimmerFragment()
        setRetryBtn()
    }

    // region Settings views

    private fun setColorDataFragment() {
        val fragment = makeColorDataFragment()
        setFragment(fragment)
        this.dataFragment = fragment
    }

    private fun setContentShimmerFragment() {
        val fragment = makeContentShimmerFragment()
        setFragment(fragment, binding.contentShimmerFragmentContainer.id)
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
        binding.contentShimmerFragmentContainer.isVisible = false
        binding.noContent.root.isVisible = false
    }

    private fun showLoadingView() {
        binding.defaultFragmentContainer.isVisible = false
        binding.contentShimmerFragmentContainer.isVisible = true
        binding.noContent.root.isVisible = false
    }

    private fun showNoContentView() {
        binding.defaultFragmentContainer.isVisible = false
        binding.contentShimmerFragmentContainer.isVisible = false
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

    // region IColorDataFragment

    // delegates
    override fun populateViews(data: D) {
        dataFragment?.populateViews(data)
    }

    // endregion

    private fun collectColorData() =
        getColorDataFlow().collectOnLifecycle { resource ->
            resource.fold(
                onEmpty = ::onColorDataEmpty,
                onLoading = ::onColorDataLoading,
                onSuccess = ::onColorDataSuccess,
                onFailure = ::onColorDataFailure
            )
        }

    @Suppress("UNUSED_PARAMETER")
    private fun onColorDataEmpty(previous: D?) {
        animContentVisibility(visible = false)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onColorDataLoading(previous: D?) {
        showLoadingView()
    }

    private fun onColorDataSuccess(data: D) {
        populateViews(data)
        animContentVisibility(visible = true)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onColorDataFailure(
        previous: D?,
        payload: Any?,
        error: Throwable
    ) {
        when (error) {
            is UnknownHostException -> showNoContentView()
            else -> showToast(error.localizedMessage)
        }
    }
}