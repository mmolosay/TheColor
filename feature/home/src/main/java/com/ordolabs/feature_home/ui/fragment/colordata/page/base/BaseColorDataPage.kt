package com.ordolabs.feature_home.ui.fragment.colordata.page.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.michaelbull.result.get
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.ColorDataPageBinding
import com.ordolabs.feature_home.ui.fragment.BaseFragment
import com.ordolabs.feature_home.ui.fragment.colordata.IColorDataFragment
import com.ordolabs.feature_home.ui.fragment.colordata.IColorThemed
import com.ordolabs.feature_home.viewmodel.ColorDataViewModel
import com.ordolabs.thecolor.util.ColorUtil
import com.ordolabs.thecolor.util.ColorUtil.isDark
import com.ordolabs.thecolor.util.ext.by
import com.ordolabs.thecolor.util.ext.findFragmentById
import com.ordolabs.thecolor.util.ext.getNextFor
import com.ordolabs.thecolor.util.ext.mediumAnimDuration
import com.ordolabs.thecolor.util.ext.setFragment
import com.ordolabs.thecolor.util.ext.showToast
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.net.UnknownHostException
import com.ordolabs.thecolor.R as RApp

abstract class BaseColorDataPage<D> :
    BaseFragment(),
    IColorDataPage<D>,
    IColorDataFragment<D> {

    private val binding: ColorDataPageBinding by viewBinding(CreateMethod.BIND)
    protected val colorDataVM: ColorDataViewModel by sharedViewModel()

    protected val color: ColorUtil.Color?
        get() = (parentFragment as? IColorThemed)?.color

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /* ViewPager2 inflates its fragments without container,
        * thus parent view group theme can not be used :< */
        val themeOverlay = if (color?.isDark() == true) {
            RApp.style.ThemeOverlay_TheColor_Dark
        } else {
            RApp.style.ThemeOverlay_TheColor_Light
        }
        val themedContext = ContextThemeWrapper(context, themeOverlay)
        return inflater
            .cloneInContext(themedContext)
            .inflate(R.layout.color_data_page, container, false)
    }

    override fun collectViewModelsData() {
        collectPageData()
    }

    override fun setViews() {
        setContentView()
        setNoContentView()
    }

    private fun setContentView() {
        setColorDataFragment()
        setChangePageBtn()
    }

    private fun setChangePageBtn() =
        binding.changePageBtn.let { button ->
            button.setOnClickListener {
                val dest = getNextFor(page)
                colorDataVM.changeDataPage(dest)
            }
            button.text = getChangePageBtnText()
        }

    private fun setColorDataFragment() {
        val fragment = makeColorDataFragmentNewInstance()
        setFragment(fragment)
    }

    private fun setNoContentView() {
        setRetryBtn()
    }

    private fun setRetryBtn() {
        binding.noContent.retryBtn.setOnClickListener l@{
            colorDataVM.fetchColorDetails(color ?: return@l)
        }
    }

    // delegates
    @Suppress("UNCHECKED_CAST")
    override fun populateViews(data: D) {
        val fragment = findFragmentById().get() as? IColorDataFragment<D> ?: return
        fragment.populateViews(data)
    }

    private fun showContentView() {
        binding.content.isVisible = true
        binding.contentShimmer.root.isVisible = false
        binding.noContent.root.isVisible = false
    }

    private fun showLoadingView() {
        binding.content.isVisible = false
        binding.contentShimmer.root.isVisible = true
        binding.noContent.root.isVisible = false
    }

    private fun showNoContentView() {
        binding.content.isVisible = false
        binding.contentShimmer.root.isVisible = false
        binding.noContent.root.isVisible = true
    }

    private fun animContentVisibility(visible: Boolean, instant: Boolean = false) {
        val content = binding.content
        val translation = resources.getDimension(RApp.dimen.offset_8)
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

    private fun collectPageData() =
        data.collectOnLifecycle { resource ->
            resource.fold(
                onEmpty = ::onPageDataEmpty,
                onLoading = ::onPageDataLoading,
                onSuccess = ::onPageDataSuccess,
                onFailure = ::onPageDataFailure
            )
        }

    @Suppress("UNUSED_PARAMETER")
    private fun onPageDataEmpty(previous: D?) {
        animContentVisibility(visible = false)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onPageDataLoading(previous: D?) {
        showLoadingView()
    }

    private fun onPageDataSuccess(data: D) {
        populateViews(data)
        animContentVisibility(visible = true)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onPageDataFailure(
        previous: D?,
        payload: Any?,
        error: Throwable
    ) =
        when (error) {
            is UnknownHostException -> showNoContentView()
            else -> showToast(error.localizedMessage)
        }
}