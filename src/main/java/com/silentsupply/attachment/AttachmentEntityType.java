package com.silentsupply.attachment;

/**
 * Types of entities that can have file attachments.
 */
public enum AttachmentEntityType {

    /** Product listing attachment (e.g., spec sheets, images). */
    PRODUCT,

    /** Order attachment (e.g., purchase orders, invoices). */
    ORDER,

    /** RFQ attachment (e.g., requirements docs). */
    RFQ
}
