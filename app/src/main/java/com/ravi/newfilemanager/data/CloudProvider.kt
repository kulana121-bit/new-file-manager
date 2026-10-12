package com.ravi.newfilemanager.data

/**
 * Cloud sync is NOT implemented in v1 (user decision 2026-10-12).
 * This stub keeps every cloud touch-point behind one interface so Drive/iCloud
 * can be switched on later without touching UI code.
 *
 * To enable later:
 *  1. Implement this interface (e.g. DriveCloudProvider).
 *  2. Return it from [CloudProviders.current].
 *  3. UI already branches on [isAvailable]: shows "Coming soon" when false.
 */
interface CloudProvider {
    val displayName: String
    val isAvailable: Boolean
    val comingSoonMessage: String
        get() = "$displayName sync is coming soon. The app is already wired to enable it later."
}

object CloudStub : CloudProvider {
    override val displayName: String = "Cloud Drive"
    override val isAvailable: Boolean = false
}

object CloudProviders {
    /** Swap this for a real implementation when cloud is enabled. */
    val current: CloudProvider = CloudStub
}
