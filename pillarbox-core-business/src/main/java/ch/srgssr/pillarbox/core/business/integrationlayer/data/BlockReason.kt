/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.data

/**
 * Block reason
 */
@Suppress("UndocumentedPublicProperty")
enum class BlockReason {
    GEOBLOCK,
    LEGAL,
    COMMERCIAL,
    AGERATING18,
    AGERATING12,
    STARTDATE,
    ENDDATE,
    JOURNALISTIC,
    UNKNOWN,
}
