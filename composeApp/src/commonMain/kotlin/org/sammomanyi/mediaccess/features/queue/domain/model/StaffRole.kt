package org.sammomanyi.mediaccess.features.queue.domain.model

enum class StaffRole {
    ADMIN,          // cover approvals + staff management + all access
    RECEPTIONIST,   // cover approvals + check-in verification + queue assignment
    DOCTOR,         // patient queue management
    PHARMACIST      // billing + prescription (Phase 2)
}