package io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import by.kirich1409.viewbindingdelegate.viewBinding
import io.github.mmolosay.thecolor.presentation.color.Color
import io.github.mmolosay.thecolor.presentation.home.R
import io.github.mmolosay.thecolor.presentation.home.databinding.ColorDataObtainFragmentBinding
import io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.base.BaseColorDataFragment
import io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.base.ColorDataView
import io.github.mmolosay.thecolor.presentation.util.InflaterUtil.cloneInViewContext
import io.github.mmolosay.thecolor.presentation.util.ext.by
import io.github.mmolosay.thecolor.presentation.util.ext.mediumAnimDuration
import io.github.mmolosay.thecolor.presentation.util.ext.setFragmentOrGet
import io.github.mmolosay.thecolor.presentation.util.ext.showToast
import io.github.mmolosay.thecolor.utils.Resource
import kotlinx.coroutines.flow.Flow
import java.net.UnknownHostException
import io.github.mmolosay.thecolor.presentation.design.R as DesignR

/**
 * Obtains [color] data of type [D] and passes it to child 'display-data-only' Fragment.
 * Displays loading, failure and success obtaining states.
 *
 * Requires [getParentFragment] to be [ColorThemedView] as well.
 */
abstract class ColorDataObtainFragment<D> :
    BaseColorDataFragment<D>(),
    ColorThemedView {

    // region Abstract

    abstract fun getColorDataFlow(): Flow<Resource<D>>
    abstract fun obtainColorData()
    abstract fun makeColorDataFragment(): BaseColorDataFragment<D>
    abstract fun makeContentShimmerFragment(): Fragment

    // endregion

    override val color: Color?
        get() = (parentFragment as? ColorThemedView)?.color

    private val binding by viewBinding(ColorDataObtainFragmentBinding::bind)

    protected var dataView: ColorDataView<D>? = null
        private set

    private var wasOnResumeCalled: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
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
        this.dataView = null
    }

    // region Set up

    @CallSuper
    override fun collectViewModelsData() {
        collectColorData()
    }

    private fun collectColorData() =
        getColorDataFlow().collectOnLifecycle { resource ->
            resource.fold(
                onEmpty = ::onColorDataEmpty,
//                onLoading = ::onColorDataLoading,
                onSuccess = ::onColorDataSuccess,
//                onFailure = ::onColorDataFailure,
            )
        }

    private fun onColorDataEmpty() {
//        animContentVisibility(visible = false)
    }

    private fun onColorDataLoading() {
        showLoadingView()
    }

    private fun onColorDataSuccess(data: D) {
        populateViews(data)
//        animContentVisibility(visible = true)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onColorDataFailure(
        payload: Any?,
        error: Throwable,
    ) {
        when (error) {
            is UnknownHostException -> showNoContentView()
            else -> showToast(error.localizedMessage)
        }
    }

    // endregion

    // region Set fragments

    override fun setFragments() {
//        setColorDataFragment()
//        setContentShimmerFragment()
    }

    private fun setColorDataFragment() {
        this.dataView = setFragmentOrGet { makeColorDataFragment() }
    }

    private fun setContentShimmerFragment() {
        val container = binding.contentShimmerFragmentContainer
        setFragmentOrGet(container.id) {
            makeContentShimmerFragment()
        }
    }

    // endregion

    // region Set views

    @CallSuper
    override fun setViews() {
//        setRetryBtn()
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
        val translation = resources.getDimension(DesignR.dimen.offset_8)
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

    // region ColorDataView

    // delegates
    override fun populateViews(data: D) {
        dataView?.populateViews(data)
    }

    // endregion
}