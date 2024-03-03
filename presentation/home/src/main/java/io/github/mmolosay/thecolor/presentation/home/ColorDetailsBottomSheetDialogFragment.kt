package io.github.mmolosay.thecolor.presentation.home

// TODO:  show Color Details of swatch in Color Scheme screen when the swatch is clicked
//class ColorDetailsBottomSheetDialogFragment :
//    BaseBottomSheetDialogFragment(),
//    ColorThemedView,
//    ColorDetailsParent {
//
//    private var details: ColorDetails? = null
//    private var obtainView: ColorDetailsObtainView? = null
//
//    override val color: Color?
//        get() = details?.color
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?,
//    ): View? {
//        return inflater.inflate(R.layout.color_data_details_dialog, container, false)
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        this.obtainView = null
//    }
//
//    // region Parse arguments
//
//    override fun parseArguments(args: Bundle) {
//        super.parseArguments(args)
//        parseColorDetails(args)
//    }
//
//    private fun parseColorDetails(bundle: Bundle) {
//        val key = ARGUMENT_KEY_COLOR_DETAILS
//        if (!bundle.containsKey(key)) return
//        this.details = bundle.getParcelable(key)
//    }
//
//    // endregion
//
//    // region Set fragments
//
//    override fun setFragments() {
//        super.setFragments()
//        setColorDetailsObtainFragment()
//    }
//
//    private fun setColorDetailsObtainFragment() {
//        this.obtainView = ContextUtil.setFragmentOrGet(
//            childFragmentManager,
//            R.id.defaultFragmentContainer,
//            transactionTag = null
//        ) {
//            ColorDetailsObtainFragment.newInstance(details)
//        }
//    }
//
//    // endregion
//
//    // region Set views
//
//    override fun setViews() {
//        // nothing is here
//    }
//
//    // endregion
//
//    // region ColorDetailsParent
//
//    override fun onExactColorClick(exact: Color) {
//        obtainView?.obtainColorDetails(exact)
//    }
//
//    // endregion
//
//    companion object {
//
//        private const val ARGUMENT_KEY_COLOR_DETAILS = "ARGUMENT_KEY_COLOR_DETAILS"
//
//        fun newInstance(details: ColorDetails) =
//            ColorDetailsBottomSheetDialogFragment().apply {
//                arguments = bundleOf(
//                    ARGUMENT_KEY_COLOR_DETAILS to details
//                )
//            }
//    }
//}