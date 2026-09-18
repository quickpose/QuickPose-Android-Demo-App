package ai.quickpose.demo

object QuickPoseConfig {
    /** Register for your free SDK key at https://dev.quickpose.ai and paste it here. */
    const val SDK_KEY = "YOUR SDK KEY HERE"

    val hasSdkKey: Boolean
        get() = SDK_KEY.isNotBlank() && SDK_KEY != "YOUR SDK KEY HERE"
}
